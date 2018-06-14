package ch.bfh.lightmapper.dataprocessingservice.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import ch.bfh.lightmapper.dataprocessingservice.view.DataProcessingView;

/**
 * Die Klasse DataProcessingController ist für die Kontrolle des Services "DataProcessing" zuständig. Sie bestimmt,
 * was bei welcher einkommenden Nachricht passiert und leitet die entsprechenden Befehle ein.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class DataProcessingController {
	// Membervariabeln
	private MqttController mqttController;
	private ComparsionController comparsionController;
	private MqttCallback mqttCallback;
	private IMqttActionListener brokerConnectListener;
	private IMqttActionListener topicSubscribeListener;
	private DataProcessingView dataProcessingView;
    private ArrayList<String> subscribedTopics = new ArrayList<String>();
    private int colorThreshold;
	
	/**
	 * Konstruktor instanziiert alle nötigen Kontroller.
	 */
	public DataProcessingController(DataProcessingView dataProcessingView) {
		this.dataProcessingView = dataProcessingView;
		this.comparsionController = new ComparsionController();
		this.mqttController = new MqttController();
	}
	
	/**
	 * Funktion instanziiert den MqttController stellt eine Verbindung zum Broker her.
	 * @param brokerIp - IP des MqttBrokers.
	 */
	public void startConnection(String brokerIp, String threshold){
		// Initialisierung
		this.subscribedTopics.clear();
		this.colorThreshold = Integer.parseInt(threshold);
		
		this.mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            	DataProcessingController.this.dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_CONNECTION_LOST, true);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
            	try {
					handleIncomingMessage(topic, message);
				} catch (MqttException | UnsupportedEncodingException e) {
					DataProcessingController.this.dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_FAILURE, true);
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
					DataProcessingController.this.subscribedTopics.add(tpc);
				}

				if (MqttController.COUNT_TPC_IN == DataProcessingController.this.subscribedTopics.size()) {
					DataProcessingController.this.dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_SUBCSCRIBE_FINISHED, false);
					try {
						DataProcessingController.this.subscribeFinished();
					} catch (UnsupportedEncodingException | MqttException e) {
						DataProcessingController.this.dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_FAILURE, true);
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				DataProcessingController.this.dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
				exception.printStackTrace();
			}
		};
        
        this.brokerConnectListener = new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
				dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_CONNECTED, false);
				try {
					DataProcessingController.this.mqttController.subscribeToAllTopics(DataProcessingController.this.topicSubscribeListener);
				} catch (MqttException e) {
					DataProcessingController.this.dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_SUBCSCRIBE_FAILURE, true);
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
				dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_CONNECTION_FAILURE, true);
			}
        };
        try {
			this.mqttController.connect(brokerIp, this.brokerConnectListener, this.mqttCallback);
		} catch (MqttException e) {
			dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_CONNECTION_FAILURE, true);
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
		if (topic.equals(MqttController.TPC_DATA_PROCESSING_IN_REFERENCE_PICTURE)) {
			this.comparsionController.savePicture(message, this.colorThreshold);
			this.mqttController.publish(MqttController.TPC_DATA_PROCESSING_OUT_STATUS, MqttController.MSG_DATA_PROCESSING_REFERENCE_PICTURE_DONE);
		}
		else if (topic.equals(MqttController.TPC_DATA_PROCESSING_IN_COMPARSION_PICTURE)) {
			String[] answer = this.comparsionController.comparsionPicture(message, this.colorThreshold);
			this.mqttController.publish(answer[0], answer[1]);
		}
	}
	
	/**
     * Funktion wird aufgerufen, wenn alle benötigten Topics subscribed wurden.
	 * @throws MqttException 
	 * @throws UnsupportedEncodingException 
	 * @throws MqttPersistenceException 
     */
    private void subscribeFinished() throws MqttPersistenceException, UnsupportedEncodingException, MqttException{
        this.mqttController.publish(MqttController.TPC_DATA_PROCESSING_OUT_SERVICE_AVAILABLE, MqttController.MSG_DATA_PROCESSING_IS_HERE +
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
				DataProcessingController.this.dataProcessingView.updateStatus(DataProcessingView.STATUS_MQTT_DISCONNECTED, false);
			} catch (MqttException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}		
	}
}
