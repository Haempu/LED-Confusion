package ch.bfh.lightmapper.ledstripservice.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import ch.bfh.lightmapper.ledstripservice.model.Luminaire;
import ch.bfh.lightmapper.ledstripservice.view.LedStripView;

public class LuminaireController {
	
	//Konstanten
	public static final int WAIT_FOR_LUMINAIRE_CHANGED_MIN = 250;
	public static final int WAIT_FOR_LUMINAIRE_CHANGED_INCREASE = 50;
	public static final int INCREASE_WAIT_TIME_AFTER_LUMINAIRES = 100; // Erhöht den Wert für das Warten bis der Wert auf die LED geschrieben wurde nach dieser Anzahl LEDs auf dem Bus.
	public static final int STANDARD_MAPPING_BRIGHTNESS = 20;
	
	// Membervariablen
	private LedStripView ledStripView;
	private MqttCallback mqttCallback;
	private IMqttActionListener brokerConnectListener;
	private IMqttActionListener topicSubscribeListener;
	private TinkerforgeController tinkerforgeController;
    private ArrayList<String> subscribedTopics = new ArrayList<String>();
	private MqttController mqttController;
	private int numberOfLed;
	private int mappingBrightness;

	 /**
     * Konstruktor für die Klasse Luminairecontroller.
     * @param luminaireView
     */
    public LuminaireController(LedStripView ledStripView) {
    	this.ledStripView = ledStripView;
    	this.mqttController = new MqttController();
    	this.tinkerforgeController = new TinkerforgeController();
    }
    
    /**
     * Funktion instanziiert den MqttController und den LuminaireController. Stellt eine Verbindung zum Mqtt-Broker sowie zum Tinkerforge Masterbrick her.
     */
	public void startConnection(String brokerIp, String masterBrickIp, String masterBrickPort, String masterBrickUid, String ledStripUid, String numberOfLed, String mappingBrightness){
		// Initialisierung
		this.subscribedTopics.clear();
		this.numberOfLed = Integer.parseInt(numberOfLed);
		if (!mappingBrightness.isEmpty() && ((Integer.parseInt(mappingBrightness) > 0 && Integer.parseInt(mappingBrightness) <= 100))) {
			this.mappingBrightness = Integer.parseInt(mappingBrightness);
		}
		else {
			this.mappingBrightness = STANDARD_MAPPING_BRIGHTNESS;
		}
		
		this.mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            	LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_MQTT_CONNECTION_LOST, true);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
            	try {
					handleIncomingMessage(topic, message);
				} catch (MqttException | UnsupportedEncodingException e) {
					LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_MQTT_FAILURE, true);
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
					LuminaireController.this.subscribedTopics.add(tpc);
				}

				if (MqttController.COUNT_TPC_IN == LuminaireController.this.subscribedTopics.size()) {
					LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_MQTT_SUBCSCRIBE_FINISHED, false);
					try {
						LuminaireController.this.subscribeFinished();
					} catch (UnsupportedEncodingException | MqttException e) {
						LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_MQTT_FAILURE, true);
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
				exception.printStackTrace();
			}
		};
        
        this.brokerConnectListener = new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				ledStripView.updateStatus(LedStripView.STATUS_MQTT_CONNECTED, false);
				try {
					LuminaireController.this.mqttController.subscribeToAllTopics(LuminaireController.this.topicSubscribeListener);
				} catch (MqttException e) {
					LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				ledStripView.updateStatus(LedStripView.STATUS_MQTT_CONNECTION_FAILURE, true);
			}
        };
        // Stellt Verbindung zu Tinkerforge her
        try {
			this.tinkerforgeController.connectToTinkerforge(masterBrickIp, masterBrickPort, masterBrickUid, ledStripUid);
			LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_TF_CONNECTED, false);
		} catch (NumberFormatException | AlreadyConnectedException | TimeoutException | NotConnectedException
				| IOException e) {
			LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_TF_CONNECTION_FAILURE, true);
			e.printStackTrace();
		}
        // Stellt Verbindung zu Mqtt her
        try {
			this.mqttController.connect(brokerIp, this.brokerConnectListener, this.mqttCallback);
		} catch (MqttException e) {
			ledStripView.updateStatus(LedStripView.STATUS_MQTT_CONNECTION_FAILURE, true);
			e.printStackTrace();
		}
        
	}

	/**
	 * Entscheidet, was bei welcher ankommenden Nachricht getan werden muss.
	 * @param topic - Topic der ankommenden Nachricht.
	 * @param message - Ankommende Nachricht.
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 * @throws UnsupportedEncodingException 
	 */
	private void handleIncomingMessage(String topic, MqttMessage message) throws MqttPersistenceException, MqttException, UnsupportedEncodingException {
		if (topic.equals(MqttController.TPC_LUMINAIRE_IN_MAPPING)) {
			if (message.toString().contains(MqttController.MSG_LUMINAIRE_NEXT_LED)) {
				int luminaireNr = Integer.parseInt(message.toString().replaceAll(MqttController.MSG_LUMINAIRE_NEXT_LED, ""));
				Luminaire luminaire = new Luminaire(luminaireNr);
				luminaire.setColor(TinkerforgeController.COLOR_ON);
				luminaire.setBrightness(this.mappingBrightness);
				luminaire.setOn(true);
				try {
					this.tinkerforgeController.changeLuminaireSettings(luminaire);
					Thread.sleep(calculateWaitTime(luminaireNr));
				} catch (InterruptedException | TimeoutException | NotConnectedException e) {
					LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_TF_LUMINAIRE_CHANGE_FAILURE, true);
					e.printStackTrace();
				}
				this.mqttController.publish(MqttController.TPC_LUMINAIRE_OUT_MAPPING, MqttController.MSG_LUMINAIRE_DONE);
			} else if (message.toString().equals(MqttController.MSG_LUMINAIRE_INITIAL_STATE)) {
				Luminaire luminaire = new Luminaire(0);
				luminaire.setColor(TinkerforgeController.COLOR_OFF);
				for (int i = 0; i <= this.numberOfLed; i++) {
					luminaire.setUid(i);
					try {
						this.tinkerforgeController.changeLuminaireSettings(luminaire);
					} catch (TimeoutException | NotConnectedException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(calculateWaitTime(this.numberOfLed));
				} catch (InterruptedException e) {
					LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_TF_LUMINAIRE_CHANGE_FAILURE, true);
					e.printStackTrace();
				}
				this.mqttController.publish(MqttController.TPC_LUMINAIRE_OUT_MAPPING, MqttController.MSG_LUMINAIRE_DONE);
			}
		}
		else if (topic.equals(MqttController.TPC_LUMINAIRE_IN_LUMINAIRE_CHANGED)) {
			 ArrayList<Luminaire> luminairesChanged = messageToArrayList(message.toString());
			 for(Luminaire l : luminairesChanged){
				 try {
					this.tinkerforgeController.changeLuminaireSettings(l);
				} catch (TimeoutException | NotConnectedException e) {
					LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_TF_LUMINAIRE_CHANGE_FAILURE, true);
					e.printStackTrace();
				}
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
	 * Funktion berechnet die Wartezeit nach dem Einschalten einer LED abhängig von der Position der LED auf dem Bus
	 * (je weiter die LED vom Master entfernt ist, desto länger muss gewartet werden).
	 * @param luminaireId
	 * @return
	 */
	private int calculateWaitTime(int luminaireId) {
		int actWaitTime = WAIT_FOR_LUMINAIRE_CHANGED_MIN;
		actWaitTime = actWaitTime + ((luminaireId / INCREASE_WAIT_TIME_AFTER_LUMINAIRES) * WAIT_FOR_LUMINAIRE_CHANGED_INCREASE);
		return actWaitTime;
	}
	
	/**
	 * Funktion wird aufgerufen, wenn alle benötigten Topics subscribed wurden.
	 */
	private void subscribeFinished() throws MqttPersistenceException, UnsupportedEncodingException, MqttException {
		this.mqttController.publish(MqttController.TPC_LUMINAIRE_OUT_SERVICE_AVAILABLE, MqttController.MSG_LUMINAIRE_IS_HERE + MqttController.MSG_PART_SPLIT_CHARACTER + MqttController.CLIENT_USERNAME
				+ MqttController.MSG_PART_SPLIT_CHARACTER + this.mqttController.getClientId(), MqttController.QOS_2, true);
	}
	
	/**
	 * Funktion wird ausgeführt, wenn das Programm geschlossen wird.
	 */
	public void handleExit() {
		if(this.mqttController != null){
			try {
				this.subscribedTopics.clear();
				this.mqttController.disconnect();
				LuminaireController.this.ledStripView.updateStatus(LedStripView.STATUS_MQTT_DISCONNECTED, false);
			} catch (UnsupportedEncodingException | MqttException e) {
				e.printStackTrace();
			}
		}
		if(this.tinkerforgeController != null) {
			this.tinkerforgeController.ipDisconnect();
		}
	}
}
