package ch.bfh.lightmapper.agentservice.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

/**
 * Die Klasse MqttController ist für die Kommunikation zwischen den einzelnen
 * Services verantwortlich. Der MqttController meldet sich als Client auf dem
 * Broker an und ermöglicht ein Subscribe an Topics. Der MqttController hält die
 * Verbindung zum Broker aufrecht und detektiert allfällige Verbinungsstörungen.
 * Weiter ermöglicht er ein sauberes Trennen der Verbindung.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class MqttController {
	// Konstanten
	// Generell
	public static final int QOS_0 = 0;
	public static final int QOS_2 = 2;
	public static final String BROKER_PROTOCOL = "tcp://";
	public static final int BROKER_PORT = 1883;
	public static final String CLIENT_USERNAME = "Agent";
	public static final String TPC_HEAD = "ch/bfh/bachelorthesis/ledmapper/";
	public static final int KEEP_ALIVE_INTERVAL = 12;
	public static final int COUNT_TPC_IN = 15; // Muss der Anzahl der
												// subscribed Topcis entsprechen
	public static final int CONNECTION_TIMEOUT = 4;

	// Agent
	public static final String MSG_AGENT = "agent";

	// User Interface
	public static final String TPC_UI_IN_COORDINATES = TPC_HEAD + "ui/input/coordinates/";
	public static final String TPC_UI_IN_AVAILABLE_SERVICES = TPC_HEAD + "ui/input/available_services/";
	public static final String TPC_UI_IN_COORDINATES_RECEIVED = TPC_HEAD + "ui/input/coordinates_received/";
	public static final String TPC_UI_IN_CONVERTED_FILE = TPC_HEAD + "ui/input/converted_file/";
	public static final String TPC_UI_OUT_MAPPING = TPC_HEAD + "ui/output/mapping/";
	public static final String TPC_UI_OUT_LUMINAIRE_CHANGED = TPC_HEAD + "ui/output/luminaire_changed/";
	public static final String TPC_UI_OUT_CONVERTING_COORDINATES = TPC_HEAD + "ui/output/converting_coordinates/";
	public static final String TPC_UI_OUT_CONVERTING_FILE = TPC_HEAD + "ui/output/converting_file/";
	public static final String TPC_UI_OUT_SERVICE_AVAILABLE = TPC_HEAD + "ui/output/service_available/";
	public static final String MSG_UI_START_MAPPING = "startMapping";
	public static final String MSG_UI_STOP_MAPPING = "stopMapping";
	public static final String MSG_UI_IS_HERE = "uiIsHere";
	public static final String MSG_UI_IS_GONE = "uiIsGone";

	// Kamera-Service
	public static final String TPC_CAMERA_IN_DO_PICTURE = TPC_HEAD + "camera/input/do_picture/";
	public static final String TPC_CAMERA_OUT_PICTURE = TPC_HEAD + "camera/output/picture/";
	public static final String TPC_CAMERA_OUT_SERVICE_AVAILABLE = TPC_HEAD + "camera/output/service_available/";
	public static final String MSG_CAMERA_DO_PICTURE = "doPicture";
	public static final String MSG_CAMERA_IS_HERE = "cameraIsHere";
	public static final String MSG_CAMERA_IS_GONE = "cameraIsGone";

	// Datenverarbeitungs-Service
	public static final String TPC_DATA_PROCESSING_IN_REFERENCE_PICTURE = TPC_HEAD
			+ "data_processing/input/reference_picture/";
	public static final String TPC_DATA_PROCESSING_IN_COMPARSION_PICTURE = TPC_HEAD
			+ "data_processing/input/comparsion_picture/";
	public static final String TPC_DATA_PROCESSING_OUT_COORDINATE = TPC_HEAD + "data_processing/output/coordinate/";
	public static final String TPC_DATA_PROCESSING_OUT_STATUS = TPC_HEAD + "data_processing/output/status/";
	public static final String TPC_DATA_PROCESSING_OUT_SERVICE_AVAILABLE = TPC_HEAD
			+ "data_processing/output/service_available/";
	public static final String MSG_DATA_PROCESSING_REFERENCE_PICTURE_DONE = "referencePictureDone";
	public static final String MSG_DATA_PROCESSING_REDO_PICTURE = "redoPicture";
	public static final String MSG_DATA_PROCESSING_MAPPING_DONE = "mappingDone";
	public static final String MSG_DATA_PROCESSING_IS_HERE = "dataProcessingIsHere";
	public static final String MSG_DATA_PROCESSING_IS_GONE = "dataProcessingIsGone";

	// Leuchtquellenservice
	public static final String TPC_LUMINAIRE_IN_MAPPING = TPC_HEAD + "luminaire/input/mapping/";
	public static final String TPC_LUMINAIRE_IN_LUMINAIRE_CHANGED = TPC_HEAD + "luminaire/input/luminaire_changed/";
	public static final String TPC_LUMINAIRE_OUT_MAPPING = TPC_HEAD + "luminaire/output/mapping/";
	public static final String TPC_LUMINAIRE_OUT_SERVICE_AVIALABLE = TPC_HEAD + "luminaire/output/service_available/";
	public static final String MSG_LUMINAIRE_INITIAL_STATE = "doInitialState";
	public static final String MSG_LUMINAIRE_NEXT_LED = "next";
	public static final String MSG_LUMINAIRE_DONE = "done";
	public static final String MSG_LUMINAIRE_IS_HERE = "luminaireIsHere";
	public static final String MSG_LUMINAIRE_IS_GONE = "luminaireIsGone";
	
	// Bildumwandlungs-Service
	public static final String TPC_PICTURE_CONVERTING_IN_CONVERTING_FILE= TPC_HEAD + "picture_converting/input/converting_file/";
	public static final String TPC_PICTURE_CONVERTING_IN_CONVERTING_COORDINATES= TPC_HEAD + "picture_converting/input/converting_coordinates/";
	public static final String TPC_PICTURE_CONVERTING_OUT_COORDINATES_RECEIVED = TPC_HEAD + "picture_converting/output/coordinates_received/";
	public static final String TPC_PICTURE_CONVERTING_OUT_CONVERTED_FILE = TPC_HEAD + "picture_converting/output/converted_file/";
	public static final String TPC_PICTURE_CONVERTING_OUT_SERVICE_AVAILABLE = TPC_HEAD + "picture_converting/output/service_available/";
	public static final String MSG_PICTURE_CONVERTING_IS_HERE = "pictureConvertingIsHere";
	public static final String MSG_PICTURE_CONVERTING_IS_GONE = "pictureConvertingIsGone";

	// Spezielle Werte
	public static final String MSG_PART_COORDINATES = "coordinates";
	public static final String MSG_PART_SPLIT_CHARACTER = ";";

	// Membervariables
	private String clientId;
	private MqttAsyncClient mqttClient;

	/**
	 * Konstruktor, welcher eine ClientID generiert.
	 */
	public MqttController() {
		this.clientId = MqttAsyncClient.generateClientId();
	}

	/**
	 * Funktion meldet sich beim Mqtt-Broker an und startet die Verbindung.
	 * 
	 * @param broker - IP-Adresse des MQTT-Brokers.
	 * @param connectListener - ConnectionListener für das Verbinden mit dem Broker.
	 * @param callback - Callback welcher aufgerufen wird, wenn eine neue Nachricht ankommt.
	 * @throws MqttException
	 */
	public void connect(String broker, IMqttActionListener connectListener, MqttCallback callback) throws MqttException {
		IMqttToken token = null;
		this.mqttClient = new MqttAsyncClient(BROKER_PROTOCOL + broker + ":" + BROKER_PORT, this.clientId);
		token = this.mqttClient.connect(setConnectOptions(), callback, connectListener);
		this.mqttClient.setCallback(callback);
	}

	/**
	 * Meldet den Client von allen Topics ab und schliesst die Verbindung.
	 * @throws MqttException 
	 * @throws UnsupportedEncodingException 
	 */
	public void disconnect() throws MqttException, UnsupportedEncodingException {
		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			this.mqttClient.unsubscribe("#");
			publish(TPC_UI_IN_AVAILABLE_SERVICES, (new ArrayList<String>().toString()));
			this.mqttClient.disconnect();
		}
	}

	/**
	 * Ermöglicht dem Client das "Subscriben" an einem bestimmten Topic.
	 * 
	 * @param topic
	 *            - Gewünschtes Topic
	 * @param qos
	 *            - Gewünschte Qualität des Services für dieses Topic (0,1,2).
	 * @param listener
	 *            - Registiert ob Anmeldung an Topic erfolgreich war.
	 * @throws MqttException
	 */
	private void subscribe(String topic, int qos, IMqttActionListener listener) throws MqttException {
		IMqttToken subToken = this.mqttClient.subscribe(topic, qos);
		subToken.setActionCallback(listener);
	}

	/**
	 * Ermöglicht das Veröffentlichen von Nachrichten.
	 * 
	 * @param topic
	 *            - Topic, unter welchem die Nachricht veröffentlicht werden
	 *            soll.
	 * @param payload
	 *            - Zu veröffentlichende Nachricht.
	 * @throws UnsupportedEncodingException 
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 */
	public void publish(String topic, String payload) throws UnsupportedEncodingException, MqttPersistenceException, MqttException {
		byte[] encodedPayload = new byte[0];
		encodedPayload = payload.getBytes("UTF-8");
		MqttMessage message = new MqttMessage(encodedPayload);
		this.mqttClient.publish(topic, message);
	}

	/**
	 * Ermöglicht das Veröffentlichen von Nachrichten.
	 * 
	 * @param topic
	 *            - Topic, unter welchem die Nachricht veröffentlicht werden
	 *            soll.
	 * @param message
	 *            - Zu veröffentlichende Mqtt-Message.
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 */
	public void publish(String topic, MqttMessage message) throws MqttPersistenceException, MqttException {
		this.mqttClient.publish(topic, message);
	}

	/**
	 * Ermöglicht das Veröffentlichen von Nachrichten.
	 * 
	 * @param topic
	 *            - Topic, unter welchem die Nachricht veröffentlicht werden
	 *            soll.
	 * @param payload
	 *            - Payload, der zu veröffentlichenden Nachricht.
	 * @param qos
	 *            - Quality of service der Verbindung.
	 * @param retained
	 *            - true: Nachricht wird auch dann einem Subscriber zugestellt,
	 *            wenn er sich erst nachträglich anmeldet.
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 * @throws UnsupportedEncodingException 
	 */
	public void publish(String topic, String payload, int qos, boolean retained) throws MqttPersistenceException, MqttException, UnsupportedEncodingException {
		byte[] encodedPayload = new byte[0];
		encodedPayload = payload.getBytes("UTF-8");
		this.mqttClient.publish(topic, encodedPayload, qos, retained);
	}

	/**
	 * Gibt eine Klasse welche Verbinungsoptionen enthält zurück.
	 * 
	 * @return - MqttConnectOptions, welche Verbinungsoptionen enthält.
	 */
	private MqttConnectOptions setConnectOptions() {
		MqttConnectOptions connectOptions = new MqttConnectOptions();
		connectOptions.setUserName(CLIENT_USERNAME);
		// Wenn Verbdinung zu Agent verloren geht, wird dem UI eine leere Liste von verfügbaren Services geschickt.
		connectOptions.setWill(TPC_UI_IN_AVAILABLE_SERVICES, (new ArrayList<String>()).toString().getBytes(), QOS_2, true);
		connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		connectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        connectOptions.setConnectionTimeout(CONNECTION_TIMEOUT);
		connectOptions.setCleanSession(true);
		return connectOptions;
	}

	/**
	 * Funktion subscirbed alle Topics, die vom Agent benötigt werden.
	 * 
	 * @param subscribeListener - SubscribeListener für das subscriben an Topics.
	 * @throws MqttException
	 */
	public void subscribeToAllTopics(IMqttActionListener subscribeListener) throws MqttException {
		/*
		 * Alle hier aufgeführten Topics werden subscribed. Wenn hier etwas
		 * abgeändert wird muss der Parameter COUNT_TPC_IN auch angepasst
		 * werden.
		 */
		subscribe(TPC_UI_OUT_MAPPING, QOS_0, subscribeListener);
		subscribe(TPC_UI_OUT_LUMINAIRE_CHANGED, QOS_0, subscribeListener);
		subscribe(TPC_UI_OUT_SERVICE_AVAILABLE, QOS_0, subscribeListener);
		subscribe(TPC_UI_OUT_CONVERTING_COORDINATES, QOS_0, subscribeListener);
		subscribe(TPC_UI_OUT_CONVERTING_FILE, QOS_0, subscribeListener);
		subscribe(TPC_CAMERA_OUT_PICTURE, QOS_0, subscribeListener);
		subscribe(TPC_CAMERA_OUT_SERVICE_AVAILABLE, QOS_0, subscribeListener);
		subscribe(TPC_LUMINAIRE_OUT_MAPPING, QOS_0, subscribeListener);
		subscribe(TPC_LUMINAIRE_OUT_SERVICE_AVIALABLE, QOS_0, subscribeListener);
		subscribe(TPC_DATA_PROCESSING_OUT_COORDINATE, QOS_0, subscribeListener);
		subscribe(TPC_DATA_PROCESSING_OUT_STATUS, QOS_0, subscribeListener);
		subscribe(TPC_DATA_PROCESSING_OUT_SERVICE_AVAILABLE, QOS_0, subscribeListener);
		subscribe(TPC_PICTURE_CONVERTING_OUT_COORDINATES_RECEIVED, QOS_0, subscribeListener);
		subscribe(TPC_PICTURE_CONVERTING_OUT_CONVERTED_FILE, QOS_0, subscribeListener);
		subscribe(TPC_PICTURE_CONVERTING_OUT_SERVICE_AVAILABLE, QOS_0, subscribeListener);
	}
	
	/**
	 * Gibt die ClientId zurück.
	 * @return: ClientId
	 */
	public String getClientId() {
		return this.clientId;
	}
}
