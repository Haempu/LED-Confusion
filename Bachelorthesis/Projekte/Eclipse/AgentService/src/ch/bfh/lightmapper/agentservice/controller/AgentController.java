package ch.bfh.lightmapper.agentservice.controller;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import ch.bfh.lightmapper.agentservice.view.AgentView;

/**
 * Die Klasse AgentController ist für die Kontrolle des Services "Agent" zuständig. Sie bestimmt,
 * was bei welcher einkommenden Nachricht passiert und leitet die entsprechenden Befehle ein.
 * Der AgentController ist die "Zentrale" des Ledmappers. Bei ihm kommen sämtliche Nachrichten zusammen.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class AgentController {
	// Konstanten
	public static final String IS_HERE = "IsHere";
	public static final String IS_GONE = "IsGone";

	
	// Membervariabeln
	private AgentView agentView;
	private MqttCallback mqttCallback;
	private IMqttActionListener brokerConnectListener;
	private IMqttActionListener topicSubscribeListener;
	private MqttController mqttController;
    private ArrayList<String> coordinates = new ArrayList<String>();
    private HashSet<String> availableServices = new HashSet<String>();
    private ArrayList<String> subscribedTopics = new ArrayList<String>();
    private int actLuminaire = 0;
    private boolean referencePicture = false;
    private boolean isMapping = false;
	
    /**
     * Konstruktor für die Klasse Agentcontroller.
     * @param agentView - View des Agent-Service
     */
    public AgentController(AgentView agentView) {
    	this.agentView = agentView;
		this.mqttController = new MqttController();
    }
    
    /**
     * Funktion instanziiert den MqttController und stellt eine Verbindung zum Broker her.
     * @param - IP-Adresse des Brokers
     */
	public void startConnection(String brokerIp){
		// Initialisierung
		this.subscribedTopics.clear();
		
		this.mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            	AgentController.this.agentView.updateStatus(AgentView.STATUS_MQTT_CONNECTION_LOST, true);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
            	try {
					handleIncomingMessage(topic, message);
				} catch (MqttException | UnsupportedEncodingException e) {
					AgentController.this.agentView.updateStatus(AgentView.STATUS_MQTT_FAILURE, true);
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
					AgentController.this.subscribedTopics.add(tpc);
				}

				if (MqttController.COUNT_TPC_IN == AgentController.this.subscribedTopics.size()) {
					AgentController.this.agentView.updateStatus(AgentView.STATUS_MQTT_SUBCSCRIBE_FINISHED, false);
					try {
						AgentController.this.subscribeFinished();
					} catch (UnsupportedEncodingException | MqttException e) {
						AgentController.this.agentView.updateStatus(AgentView.STATUS_MQTT_FAILURE, true);
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				AgentController.this.agentView.updateStatus(AgentView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
				exception.printStackTrace();
			}
		};
        
        this.brokerConnectListener = new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				agentView.updateStatus(AgentView.STATUS_MQTT_CONNECTED, false);
				try {
					AgentController.this.mqttController.subscribeToAllTopics(AgentController.this.topicSubscribeListener);
				} catch (MqttException e) {
					AgentController.this.agentView.updateStatus(AgentView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				agentView.updateStatus(AgentView.STATUS_MQTT_CONNECTION_FAILURE, true);
			}
        };
        try {
			this.mqttController.connect(brokerIp, this.brokerConnectListener, this.mqttCallback);
		} catch (MqttException e) {
			agentView.updateStatus(AgentView.STATUS_MQTT_CONNECTION_FAILURE, true);
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
		
		// Entscheidung, was bei welcher einkommenden Nachricht getan werden soll.
		switch (topic) {
		// Befehl zum Starten des Mappingvorgangs vom UI
		case MqttController.TPC_UI_OUT_MAPPING:
			if (message.toString().equals(MqttController.MSG_UI_START_MAPPING)){
				this.actLuminaire = 0;
				this.referencePicture = true;
				this.isMapping = true;
				this.coordinates.clear();
				this.mqttController.publish(MqttController.TPC_LUMINAIRE_IN_MAPPING, MqttController.MSG_LUMINAIRE_INITIAL_STATE);
			}
			else if (message.toString().equals(MqttController.MSG_UI_STOP_MAPPING)) {
				this.isMapping = false;
				this.mqttController.publish(MqttController.TPC_UI_IN_COORDINATES, this.coordinates.toString());
				this.mqttController.publish(MqttController.TPC_LUMINAIRE_IN_MAPPING, MqttController.MSG_LUMINAIRE_INITIAL_STATE);
			}
			break;
		// Befehl zum ändern eines oder mehrerern Leuchtmitteln
		case MqttController.TPC_UI_OUT_LUMINAIRE_CHANGED:
			this.mqttController.publish(MqttController.TPC_LUMINAIRE_IN_LUMINAIRE_CHANGED, message);
			break;
		// Koordinaten, auf welche ein zu konvertierendes Bild schlussendlich konvertiert werden sollen.
		case MqttController.TPC_UI_OUT_CONVERTING_COORDINATES:
			this.mqttController.publish(MqttController.TPC_PICTURE_CONVERTING_IN_CONVERTING_COORDINATES, message);
			break;
		// Zu konvertierendes Bild (Bild in voller Grösse)
		case MqttController.TPC_UI_OUT_CONVERTING_FILE:
			this.mqttController.publish(MqttController.TPC_PICTURE_CONVERTING_IN_CONVERTING_FILE, message);
			break;
		// Ob User Interface anwesend ist oder nicht
		case MqttController.TPC_UI_OUT_SERVICE_AVAILABLE:
			if (message.toString().contains(MqttController.MSG_UI_IS_HERE)) {
				this.availableServices.add(message.toString().replace(IS_HERE, ""));
			}
			else if (message.toString().contains(MqttController.MSG_UI_IS_GONE)) {
				this.availableServices.remove(message.toString().replaceAll(IS_GONE, ""));
			}
			this.mqttController.publish(MqttController.TPC_UI_IN_AVAILABLE_SERVICES, this.availableServices.toString(), MqttController.QOS_0, true);
			break;
		// Bild vom Kamera-Service
		case MqttController.TPC_CAMERA_OUT_PICTURE:
			if (isMapping) {
				if (this.referencePicture) {
					this.mqttController.publish(MqttController.TPC_DATA_PROCESSING_IN_REFERENCE_PICTURE, message);
				} else {
					this.mqttController.publish(MqttController.TPC_DATA_PROCESSING_IN_COMPARSION_PICTURE, message);
				}
			}
			break;
		// Ob Kamera-Services anwesend ist oder nicht
		case MqttController.TPC_CAMERA_OUT_SERVICE_AVAILABLE:
			if (message.toString().contains(MqttController.MSG_CAMERA_IS_HERE)) {
				this.availableServices.add(message.toString().replace(IS_HERE, ""));
			}
			else if (message.toString().contains(MqttController.MSG_CAMERA_IS_GONE)) {
				this.availableServices.remove(message.toString().replaceAll(IS_GONE, ""));
			}
			this.mqttController.publish(MqttController.TPC_UI_IN_AVAILABLE_SERVICES, this.availableServices.toString(), MqttController.QOS_0, true);
			break;
		// Nächstes LED wurde eingeschaltet
		case MqttController.TPC_LUMINAIRE_OUT_MAPPING:
			this.mqttController.publish(MqttController.TPC_CAMERA_IN_DO_PICTURE, MqttController.MSG_CAMERA_DO_PICTURE);
			break;
		// Ob Leuchtquellen-Service anwesend ist oder nicht
		case MqttController.TPC_LUMINAIRE_OUT_SERVICE_AVIALABLE:
			if (message.toString().contains(MqttController.MSG_LUMINAIRE_IS_HERE)) {
				this.availableServices.add(message.toString().replace(IS_HERE, ""));
			}
			else if (message.toString().contains(MqttController.MSG_LUMINAIRE_IS_GONE)) {
				this.availableServices.remove(message.toString().replaceAll(IS_GONE, ""));
			}
			this.mqttController.publish(MqttController.TPC_UI_IN_AVAILABLE_SERVICES, this.availableServices.toString(), MqttController.QOS_0, true);
			break;
		// Koordinaten vom Datenverarbeitungs-Service
		case MqttController.TPC_DATA_PROCESSING_OUT_COORDINATE:
			String coordinate = actLuminaire + MqttController.MSG_PART_SPLIT_CHARACTER + message.toString();
			this.coordinates.add(coordinate);
			this.referencePicture = true;
			this.actLuminaire++;
			this.mqttController.publish(MqttController.TPC_LUMINAIRE_IN_MAPPING, MqttController.MSG_LUMINAIRE_INITIAL_STATE);
			break;
		// Status vom Datenverarbeitungs-Service
		case MqttController.TPC_DATA_PROCESSING_OUT_STATUS:
			if (message.toString().equals(MqttController.MSG_DATA_PROCESSING_REFERENCE_PICTURE_DONE)) {
				this.referencePicture = false;
				this.mqttController.publish(MqttController.TPC_LUMINAIRE_IN_MAPPING, MqttController.MSG_LUMINAIRE_NEXT_LED + actLuminaire);
			}
			else if (message.toString().equals(MqttController.MSG_DATA_PROCESSING_REDO_PICTURE)) {
				this.mqttController.publish(MqttController.TPC_CAMERA_IN_DO_PICTURE, MqttController.MSG_CAMERA_DO_PICTURE);
			}
			else if (message.toString().equals(MqttController.MSG_DATA_PROCESSING_MAPPING_DONE)) {
				this.mqttController.publish(MqttController.TPC_UI_IN_COORDINATES, this.coordinates.toString());
				this.mqttController.publish(MqttController.TPC_LUMINAIRE_IN_MAPPING, MqttController.MSG_LUMINAIRE_INITIAL_STATE);
				this.isMapping = false;
			}
			break;
		// Ob Datenverarbeitungs-Services anwesend ist oder nicht
		case MqttController.TPC_DATA_PROCESSING_OUT_SERVICE_AVAILABLE:
			if (message.toString().contains(MqttController.MSG_DATA_PROCESSING_IS_HERE)) {
				this.availableServices.add(message.toString().replace(IS_HERE, ""));
			}
			else if (message.toString().contains(MqttController.MSG_DATA_PROCESSING_IS_GONE)) {
				this.availableServices.remove(message.toString().replaceAll(IS_GONE, ""));
			}
			this.mqttController.publish(MqttController.TPC_UI_IN_AVAILABLE_SERVICES, this.availableServices.toString(), MqttController.QOS_0, true);
			break;
		// Koordinaten wurden vom Bildverarbeitungsservice übernommen.
		case MqttController.TPC_PICTURE_CONVERTING_OUT_COORDINATES_RECEIVED:
			this.mqttController.publish(MqttController.TPC_UI_IN_COORDINATES_RECEIVED, message);
			break;
		// Konvertiertes File vom Bildverarbeitungsservice wird an UI weitergeleitet.
		case MqttController.TPC_PICTURE_CONVERTING_OUT_CONVERTED_FILE:
			this.mqttController.publish(MqttController.TPC_UI_IN_CONVERTED_FILE, message);
			break;
		// Ob Bildverarbeitungsservice anwesend ist oder nicht
		case MqttController.TPC_PICTURE_CONVERTING_OUT_SERVICE_AVAILABLE:
			if (message.toString().contains(MqttController.MSG_PICTURE_CONVERTING_IS_HERE)) {
				this.availableServices.add(message.toString().replace(IS_HERE, ""));
			}
			else if (message.toString().contains(MqttController.MSG_PICTURE_CONVERTING_IS_GONE)) {
				this.availableServices.remove(message.toString().replaceAll(IS_GONE, ""));
			}
			this.mqttController.publish(MqttController.TPC_UI_IN_AVAILABLE_SERVICES, this.availableServices.toString(), MqttController.QOS_0, true);
			break;
		}
	}
	
	/**
	 * Funktion wird aufgerufen wenn alle Topics subscribed sind.
	 * @throws MqttException 
	 * @throws UnsupportedEncodingException 
	 * @throws MqttPersistenceException 
	 */
	private void subscribeFinished () throws MqttPersistenceException, UnsupportedEncodingException, MqttException {
		this.availableServices.add(MqttController.MSG_AGENT + MqttController.MSG_PART_SPLIT_CHARACTER + MqttController.CLIENT_USERNAME
									+ MqttController.MSG_PART_SPLIT_CHARACTER + this.mqttController.getClientId());
		this.mqttController.publish(MqttController.TPC_UI_IN_AVAILABLE_SERVICES, this.availableServices.toString(), MqttController.QOS_0, true);
	}
	
	/**
	 * Funktion wird ausgeführt, wenn das Programm geschlossen wird.
	 */
	public void handleExit() {
		if(this.mqttController != null){
			try {
				this.subscribedTopics.clear();
				this.mqttController.disconnect();
				AgentController.this.agentView.updateStatus(AgentView.STATUS_MQTT_DISCONNECTED, false);
			} catch (MqttException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}		
	}
}
