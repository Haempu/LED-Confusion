package ch.bfh.bachelorthesis.ledmapper.controller;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Die Klasse MqttController ist für die Kommunikation zwischen dem CameraService und dem Agent verantwortlich. Sie deckt lediglich die Seite des
 * CameraService ab. Der MqttController meldet sich als Client auf dem Broker an und ermöglicht ein Subscribe an Topics. Der MqttController hält die
 * Verbindung zum Broker aufrecht und detektiert allfällige Verbinungsstörungen. Weiter ermöglicht er ein sauberes Trennen der Verbindung.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 27.04.2018
 * @version 1.0
 */
public class MqttController {

    //Membervariabeln
    private MqttAsyncClient mqttClient;

    private String clientId;
    private ArrayList<String> subscribedTpcs = new ArrayList<>();

    // Konstanten
    private static MqttController instance;

    // Broker Konfiguration
    public static final String CLIENT_USERNAME = "Android Kamera";
    private static final String BROKER_PROTOCOL = "tcp://";


    public static final int QOS_0 = 0;
    public static final int QOS_2 = 1;
    private static final int KEEP_ALIVE_INTERVAL = 12;
    private static final int CONNECTION_TIMEOUT = 4;


    // Diverse Topics/Messages
    public static final String TPC_HEAD = "ch/bfh/bachelorthesis/ledmapper/camera/";
    public static final String MSG_CAMERA_IS_HERE = "cameraIsHere";
    public static final String MSG_CAMERA_IS_GONE = "cameraIsGone";

    // Subscribe Topics
    public static final int COUNT_TPC_IN = 2; // Muss der Anzahl der subscribed Topcis entsprechen
    public static final String TPC_CAM_IN_DO_PICTURE = TPC_HEAD + "input/do_picture/";

    // Publish Topics
    public static final String TPC_CAM_OUT_PICTURE = TPC_HEAD + "output/picture/";
    public static final String TPC_CAM_OUT_SERVICE_AVAILABLE = TPC_HEAD + "output/service_available/";

    public static final String MSG_DO_PICTURE = "doPicture";

    // Spezielle Werte
    public static final String MSG_PART_SPLIT_CHARACTER = ";";
    public String brokerIp;

    /**
     * Konstruktor: Hier wird die Mqtt-Verbindung zum Broker hergestellt.
     */
    public MqttController () {
        this.clientId = MqttAsyncClient.generateClientId();
    }


    /**
     * Funktion gibt immer die gleiche Instanz der Klasse MqttController zurück,
     * damit alle über dieselbe Verbindung kommunizieren.
     * @return Instanz der Klasse MqttController.
     */

    public static synchronized MqttController getInstance(){
        if(instance == null) {
            instance = new MqttController();
        }

        return instance;
    }

    /**
     * Funktion meldet sich beim Mqtt-Broker an und startet die Verbindung.
     * @param mqttCallback - Callback, der die Nachrichten zustellen kann.
     */
    public void connect(final String ip, String port, MqttCallbackExtended mqttCallback, final MainController mainController){

        IMqttActionListener mqttListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                subscribeToAllTopics();
                mainController.onConnectionSucceed();
                MqttController.this.brokerIp = ip;

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                mainController.onConnectionFailed();
                exception.printStackTrace();
            }
        };

        IMqttToken token = null;
        try {
            this.mqttClient  = new MqttAsyncClient(BROKER_PROTOCOL + ip + ":" + port, this.clientId,
                    new MemoryPersistence());
            token = this.mqttClient.connect(chooseConnectOptions(), mqttCallback, mqttListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        this.mqttClient.setCallback(mqttCallback);
    }

    /**
     * Meldet den Client von allen Topics ab und schliesst die Verbindung.
     */
    public void disconnect() {
        try {
            publish(TPC_CAM_OUT_SERVICE_AVAILABLE, MSG_CAMERA_IS_GONE +
                    MSG_PART_SPLIT_CHARACTER + CLIENT_USERNAME +
                    MSG_PART_SPLIT_CHARACTER + this.clientId, QOS_2, true);
            this.mqttClient.unsubscribe("#");
            IMqttToken disconToken = this.mqttClient.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failure on disconnect");
                }
            });
        }  catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ermöglicht dem Client das "Subscriben" an einem bestimmten Topic.
     * @param topic - Gewünschtes Topic
     * @param qos - Gewünschte Qualität des Services für dieses Topic (0,1,2).
     * @param listener - Registiert ob Anmeldung an Topic erfolgreich war.
     * @throws MqttException
     */
    public void subscribe(String topic, int qos, IMqttActionListener listener) throws MqttException {
        IMqttToken subToken = this.mqttClient.subscribe(topic, qos);
        subToken.setActionCallback(listener);
        System.out.println("subscribe");
    }

    /**
     * Ermöglicht das Veröffentlichen von Nachrichten.
     * @param topic - Topic, unter welchem die Nachricht veröffentlicht werden soll.
     * @param payload - Zu veröffentlichende Nachricht.
     */
    public void publish(String topic, String payload, int qos, boolean retained) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            this.mqttClient.publish(topic, encodedPayload, qos, retained);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishByteArray(String topic, byte[] payload, int qos, boolean retained) {
        try {
            this.mqttClient.publish(topic, payload, qos, retained);
        } catch ( MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ermöglicht das Veröffentlichen von Nachrichten.
     * @param topic - Topic, unter welchem die Nachricht veröffentlicht werden soll.
     * @param payload - Zu veröffentlichende Nachricht.
     */
    public void publish(String topic, String payload) {
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            this.mqttClient.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }



    /**
     * Gibt eine Klasse welche Verbinungsoptionen enthält zurück.
     * @return - MqttConnectOptions, welche Verbinungsoptionen enthält.
     */
    private MqttConnectOptions chooseConnectOptions() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(CLIENT_USERNAME);
        // Last will -topic and -message,  QOS. No retain message (message sended when client has subscribed)
        connectOptions.setWill(TPC_CAM_OUT_SERVICE_AVAILABLE, (MSG_CAMERA_IS_GONE +
                MSG_PART_SPLIT_CHARACTER + CLIENT_USERNAME +
                MSG_PART_SPLIT_CHARACTER + this.clientId).getBytes(), QOS_2, true);
        connectOptions.setAutomaticReconnect(false);
        connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        connectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        connectOptions.setConnectionTimeout(CONNECTION_TIMEOUT);
        connectOptions.setCleanSession(true);

        return connectOptions;
    }

    /**
     * Funktion subscribed von allen Topics, die das User Interface interessieren.
     */
    private void subscribeToAllTopics(){

        IMqttActionListener subscribeListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                for(String tpc : asyncActionToken.getTopics()){
                    MqttController.this.subscribedTpcs.add(tpc);
                }

                MqttController.this.subscribeFininished();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                exception.printStackTrace();
            }
        };

        try {
            // Alle hier aufgeführten Topics werden subscribed
            subscribe(TPC_CAM_IN_DO_PICTURE, QOS_0, subscribeListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funktion wird aufgerufen, wenn alle benötigten Topics subscribed wurden.
     */
    private void subscribeFininished(){
        publish(TPC_CAM_OUT_SERVICE_AVAILABLE, MSG_CAMERA_IS_HERE +
                MSG_PART_SPLIT_CHARACTER + CLIENT_USERNAME +
                MSG_PART_SPLIT_CHARACTER + this.clientId, QOS_2, true);
    }

    /**
     * Funktion überprüft ob eine MQTT-Verbindung aufgebaut ist.
     * @return
     */
    public boolean isConnected(){
        if(this.mqttClient != null &&this.mqttClient.isConnected()){
            return true;
        }

        return false;
    }
}