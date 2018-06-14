package ch.bfh.lightmapper.displayservice.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import ch.bfh.lightmapper.displayservice.model.Luminaire;
import ch.bfh.lightmapper.displayservice.view.DisplayStartView;
import ch.bfh.lightmapper.displayservice.view.SceneryView;

/**
 * Die Klasse CommunicationController ist für die Kommunikationskontrolle
 * zwischen dem Display-Service und dem Agent zuständig. Sie bestimmt, was bei
 * welcher einkommenden Nachricht passiert und leitet die entsprechenden Befehle
 * ein.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 27.04.2018
 * @version 1.0
 */
public class CommunicationController {
	// Konstanten
	public static final int DELAY_TIME = 500;
	
	// Membervariabeln
	private DisplayStartView displayStartView;
	private MqttCallback mqttCallback;
	private IMqttActionListener brokerConnectListener;
	private IMqttActionListener topicSubscribeListener;
	private MqttController mqttController;
	private ArrayList<String> subscribedTopics = new ArrayList<String>();
	private SceneryView sceneryView;
	private static CommunicationController instance;

	/**
	 * Konstruktor für die Klasse CommunicationController.
	 */
	public CommunicationController() {
		this.mqttController = new MqttController();
	}
	
	public static synchronized CommunicationController getInstance(){
		if(instance == null){
			instance = new CommunicationController();
		}
		
		return instance;
	}
	
	public void setDisplayStartView(DisplayStartView displayStartView){
		this.displayStartView = displayStartView;
	}
	
	public void setSceneryView(SceneryView sceneryView){
		this.sceneryView = sceneryView;
	}

	/**
	 * Funktion instanziiert den MqttController und stellt eine Verbindung zum
	 * Broker her.
	 * 
	 * @param -
	 *            IP-Adresse des Brokers
	 */
	public void startConnection(String brokerIp) {
		// Initialisierung
		this.subscribedTopics.clear();

		this.mqttCallback = new MqttCallback() {
			@Override
			public void connectionLost(Throwable cause) {
				cause.printStackTrace();
				CommunicationController.this.displayStartView.updateStatus(DisplayStartView.STATUS_MQTT_CONNECTION_LOST,
						true);
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) {
				try {
					handleIncomingMessage(topic, message);
				} catch (MqttException | UnsupportedEncodingException e) {
					CommunicationController.this.displayStartView.updateStatus(DisplayStartView.STATUS_MQTT_FAILURE,
							true);
					e.printStackTrace();
				}
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {

			}
		};

		this.topicSubscribeListener = new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				for (String tpc : asyncActionToken.getTopics()) {
					CommunicationController.this.subscribedTopics.add(tpc);
				}

				if (MqttController.COUNT_TPC_IN == CommunicationController.this.subscribedTopics.size()) {
					CommunicationController.this.displayStartView
							.updateStatus(DisplayStartView.STATUS_MQTT_SUBCSCRIBE_FINISHED, false);
					try {
						CommunicationController.this.subscribeFinished();
					} catch (UnsupportedEncodingException | MqttException e) {
						CommunicationController.this.displayStartView.updateStatus(DisplayStartView.STATUS_MQTT_FAILURE,
								true);
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				CommunicationController.this.displayStartView
						.updateStatus(DisplayStartView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
				exception.printStackTrace();
			}
		};

		this.brokerConnectListener = new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				displayStartView.updateStatus(DisplayStartView.STATUS_MQTT_CONNECTED, false);
				try {
					CommunicationController.this.mqttController
							.subscribeToAllTopics(CommunicationController.this.topicSubscribeListener);
				} catch (MqttException e) {
					CommunicationController.this.displayStartView
							.updateStatus(DisplayStartView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				displayStartView.updateStatus(DisplayStartView.STATUS_MQTT_CONNECTION_FAILURE, true);
			}
		};
		try {
			this.mqttController.connect(brokerIp, this.brokerConnectListener, this.mqttCallback);
		} catch (MqttException e) {
			displayStartView.updateStatus(DisplayStartView.STATUS_MQTT_CONNECTION_FAILURE, true);
			e.printStackTrace();
		}
	}

	/**
	 * Entscheidet, was bei welcher ankommenden Nachricht getan werden muss.
	 * 
	 * @param topic
	 *            - Topic der ankommenden Nachricht.
	 * @param message
	 *            - Ankommende Nachricht.
	 * @throws MqttException
	 * @throws MqttPersistenceException
	 * @throws UnsupportedEncodingException
	 */
	private void handleIncomingMessage(String topic, MqttMessage message)
			throws MqttPersistenceException, MqttException, UnsupportedEncodingException {
		if (topic.equals(MqttController.TPC_LUMINAIRE_IN_MAPPING)) {
			if (message.toString().contains(MqttController.MSG_LUMINAIRE_NEXT_LED)) {
				int luminaireNr = Integer
						.parseInt(message.toString().replaceAll(MqttController.MSG_LUMINAIRE_NEXT_LED, ""));
				Luminaire luminaire = new Luminaire(luminaireNr);
				luminaire.setColor(SceneryView.COLOR_ON);
				luminaire.setOn(true);
				this.sceneryView.changeLuminaireSettings(luminaire);
				try {
					Thread.sleep(DELAY_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block e.printStackTrace();
				}
				this.mqttController.publish(MqttController.TPC_LUMINAIRE_OUT_MAPPING,
						MqttController.MSG_LUMINAIRE_DONE);
				
			} else if (message.toString().equals(MqttController.MSG_LUMINAIRE_INITIAL_STATE)) {
				Luminaire luminaire = new Luminaire(0);
				luminaire.setColor(SceneryView.COLOR_OFF);
				luminaire.setOn(false);

				for (int i = 0; i <= this.sceneryView.getNumberOfLuminaires(); i++) {
					luminaire.setUid(i);
					this.sceneryView.changeLuminaireSettings(luminaire);
				}

				try {
					Thread.sleep(DELAY_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block e.printStackTrace();
				}

				this.mqttController.publish(MqttController.TPC_LUMINAIRE_OUT_MAPPING,
						MqttController.MSG_LUMINAIRE_DONE);
			}
		} else if (topic.equals(MqttController.TPC_LUMINAIRE_IN_LUMINAIRE_CHANGED)) {
			ArrayList<Luminaire> luminairesChanged = messageToArrayList(message.toString());

			for (Luminaire l : luminairesChanged) {
				this.sceneryView.changeLuminaireSettings(l);
			}
		}
	}

	/**
	 * Wandelt den String der Message in eine ArrayList von Luminaires um.
	 * @param message - String einer MqttMessage
	 * @return
	 */
	private ArrayList<Luminaire> messageToArrayList(String message) {
		
		message = message.substring(1, message.length()-1);
		message = message.replaceAll("\\s+", "");
        String[] changedLuminaires = message.split(",");
        ArrayList<Luminaire> luminaires = new ArrayList();
        
        
        for(int i = 0; i < changedLuminaires.length; i++){
            String[] attr = changedLuminaires[i].split(MqttController.MSG_PART_SPLIT_CHARACTER);
            Luminaire l = new Luminaire(Integer.parseInt(attr[0]));
            l.setColor(attr[1]);
            l.setBrightness(Integer.parseInt(attr[2]));
            if(Integer.parseInt(attr[3]) == Luminaire.LUMINAIRE_ON){
                l.setOn(true);
            }else{
                l.setOn(false);
            }
            
            luminaires.add(l);
        }
		
		return luminaires;
	}

	/**
	 * Funktion wird aufgerufen, wenn alle benötigten Topics subscribed wurden.
	 */
	private void subscribeFinished() throws MqttPersistenceException, UnsupportedEncodingException, MqttException {
		this.mqttController.publish(MqttController.TPC_LUMINAIRE_OUT_SERVICE_AVAILABLE,
				MqttController.MSG_LUMINAIRE_IS_HERE + MqttController.MSG_PART_SPLIT_CHARACTER
						+ MqttController.CLIENT_USERNAME + MqttController.MSG_PART_SPLIT_CHARACTER
						+ this.mqttController.getClientId(),
				MqttController.QOS_2, true);
		this.displayStartView.showConnectionSucceed();
	}

	/**
	 * Funktion wird ausgeführt, wenn das Programm geschlossen wird.
	 */
	public void handleExit() {
		if (this.mqttController != null) {
			this.subscribedTopics.clear();
			if (this.mqttController.isConnected()) {
				try {
					this.mqttController.disconnect();
					CommunicationController.this.displayStartView
							.updateStatus(DisplayStartView.STATUS_MQTT_DISCONNECTED, false);
				} catch (UnsupportedEncodingException | MqttException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
