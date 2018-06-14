package ch.bfh.lightmapper.pictureconvertingservice.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import ch.bfh.lightmapper.pictureconvertingservice.view.PictureConvertingView;

/**
 * Die Klasse PictureConvertingController ist für die Kontrolle des Services "Bildumwandlung" zuständig. Sie bestimmt,
 * was bei welcher einkommenden Nachricht passiert und leitet die entsprechenden Befehle ein.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 27.04.2018
 * @version 1.0
 */
public class PictureConvertingController {
	// Membervariabeln
	private MqttController mqttController;
	private ConvertingController convertingController;
	private MqttCallback mqttCallback;
	private IMqttActionListener brokerConnectListener;
	private IMqttActionListener topicSubscribeListener;
	private PictureConvertingView pictureConvertingView;
    private ArrayList<String> subscribedTopics = new ArrayList<String>();
    private ArrayList<String> savedCoordinates = new ArrayList<String>();

	
	/**
	 * Konstruktor instanziiert alle nötigen Controller.
	 */
	public PictureConvertingController(PictureConvertingView pictureConvertingView) {
		this.pictureConvertingView = pictureConvertingView;
		this.convertingController = new ConvertingController();
	}
	
    /**
     * Funktion instanziiert den MqttController stellt eine Verbindung zum Broker her.
     */
	public void startConnection(String brokerIp){
		// Initialisierung
		this.subscribedTopics.clear();
		this.mqttController = new MqttController();
		
		this.mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            	cause.printStackTrace();
            	PictureConvertingController.this.pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_CONNECTION_LOST, true);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
            	try {
					handleIncomingMessage(topic, message);
				} catch (MqttException | UnsupportedEncodingException e) {
					PictureConvertingController.this.pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_FAILURE, true);
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
					PictureConvertingController.this.subscribedTopics.add(tpc);
				}

				if (MqttController.COUNT_TPC_IN == PictureConvertingController.this.subscribedTopics.size()) {
					PictureConvertingController.this.pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_SUBCSCRIBE_FINISHED, false);
					try {
						PictureConvertingController.this.subscribeFinished();
					} catch (UnsupportedEncodingException | MqttException e) {
						PictureConvertingController.this.pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_FAILURE, true);
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				PictureConvertingController.this.pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
				exception.printStackTrace();
			}
		};
        
        this.brokerConnectListener = new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_CONNECTED, false);
				try {
					PictureConvertingController.this.mqttController.subscribeToAllTopics(PictureConvertingController.this.topicSubscribeListener);
				} catch (MqttException e) {
					PictureConvertingController.this.pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_CONNECTION_FAILURE, true);
			}
        };
        try {
			this.mqttController.connect(brokerIp, this.brokerConnectListener, this.mqttCallback);
		} catch (MqttException e) {
			pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_CONNECTION_FAILURE, true);
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
	private void handleIncomingMessage(String topic, MqttMessage message) throws UnsupportedEncodingException, MqttPersistenceException, MqttException {
		if (topic.contains(MqttController.TPC_PICTURE_CONVERTING_IN_CONVERTING_COORDINATES)) {
			this.savedCoordinates.clear();
			this.savedCoordinates = messageToArrayList(message.toString());
			this.mqttController.publish(MqttController.TPC_PICTURE_CONVERTING_OUT_COORDINATES_RECEIVED, MqttController.MSG_PICTURE_CONVERTING_DONE);
		}
		else if (topic.contains(MqttController.TPC_PICTURE_CONVERTING_IN_CONVERTING_FILE)) {
			String convertedFile = this.convertingController.convertPicture(this.savedCoordinates, message.getPayload());
			this.mqttController.publish(MqttController.TPC_PICTURE_CONVERTING_OUT_CONVERTED_FILE, convertedFile);
		}
		/*
		// TODO gefällt mir noch nicht, da wir in einen undefinierten Systemzustand fallen können. Vlt Daten löschen von UI aus und mit deliveryComplete abfragen?
		if (topic.contains(MqttController.TPC_PICTURE_CONVERTING_IN_CONVERTING_COORDINATES)) {
			if (this.savedPicture != null) {
				String convertedFile = this.convertingController.convertPicture(messageToArrayList(message.toString()), this.savedPicture);
				this.mqttController.publish(MqttController.TPC_PICTURE_CONVERTING_OUT_CONVERTED_FILE, convertedFile);
			}
			else {
				this.savedCoordinates.clear();
				this.savedCoordinates = messageToArrayList(message.toString());
			}
			
			// TODO testzwecke
			//this.coordinates.add("test");
			//this.picture = new byte[] {'b', 'a'};
		}
		else if (topic.contains(MqttController.TPC_PICTURE_CONVERTING_IN_CONVERTING_FILE)) {
			if (!this.savedCoordinates.isEmpty()) {
				String convertedFile = this.convertingController.convertPicture(this.savedCoordinates, message.getPayload());
				this.mqttController.publish(MqttController.TPC_PICTURE_CONVERTING_OUT_CONVERTED_FILE, convertedFile);
			}
			else {
				this.savedPicture = null;
				this.savedPicture = message.getPayload();
			}
		}*/
	}
	
	/**
	 * Wandelt den String der Message in eine ArrayList um.
	 * @param message - String einer MqttMessage
	 * @return
	 */
	private ArrayList<String> messageToArrayList(String message) {
		
		message = message.substring(1, message.length()-1);
		message = message.replaceAll("\\s+", "");
        String[] coordinates = message.split(",");
        ArrayList<String> returnValue = new ArrayList<String>();
        for (int i = 0; i < coordinates.length; i++) {
        	returnValue.add(coordinates[i]);
        }
        return returnValue;
	}
	
	/**
     * Funktion wird aufgerufen, wenn alle benötigten Topics subscribed wurden.
	 * @throws MqttException 
	 * @throws UnsupportedEncodingException 
	 * @throws MqttPersistenceException 
     */
    private void subscribeFinished() throws MqttPersistenceException, UnsupportedEncodingException, MqttException{
        this.mqttController.publish(MqttController.TPC_PICTURE_CONVERTING_OUT_SERVICE_AVAILABLE, MqttController.MSG_PICTURE_CONVERTING_IS_HERE +
        		MqttController.MSG_PART_SPLIT_CHARACTER + MqttController.CLIENT_USERNAME +
        		MqttController.MSG_PART_SPLIT_CHARACTER + this.mqttController.getClientId(), MqttController.QOS_2, true);
    }
	
    /**
	 * Funktion wird ausgeführt, wenn das Programm geschlossen wird.
	 */
	public void handleExit() {
		if(this.mqttController != null){
			try {
				this.subscribedTopics.clear();
				this.mqttController.disconnect();
				PictureConvertingController.this.pictureConvertingView.updateStatus(PictureConvertingView.STATUS_MQTT_DISCONNECTED, false);
			} catch (MqttException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}		
	}

}
