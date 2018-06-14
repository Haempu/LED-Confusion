package ch.bfh.eliaboesiger.lightmapper.controller;


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
 * @date 27.03.2018
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
    private static final String CLIENT_USERNAME = "user interface";
    private static final String BROKER_PROTOCOL = "tcp://";

    public static final int QOS = 0;
    private static final int KEEP_ALIVE_INTERVAL = 12;
    private static final int CONNECTION_TIMEOUT_SECONDS = 4;

    // Diverse Topics/Messages
    public static final String TPC_HEAD = "ch/bfh/bachelorthesis/ledmapper/ui/";
    public static final String MSG_SERVICE_IS_HERE = "user interface is here";
    public static final String MSG_LASTWILL = "user interface is gone";

    // Subscribe Topics
    public static final int COUNT_TPC_IN = 4; //Muss der Anzahl der subscribed Topcis entsprechen
    public static final String TPC_UI_IN_COORDINATES = TPC_HEAD+"input/coordinates/";
    public static final String TPC_UI_IN_AVAILABLE_SERVICES = TPC_HEAD+"input/available_services/";
    public static final String TPC_UI_IN_CONVERTED_FILE = TPC_HEAD+"input/converted_file/";

    // Publish Topics
    public static final String TPC_UI_OUT_MAPPING = TPC_HEAD+"output/mapping/";
    public static final String TPC_UI_OUT_SERVICE_AVAILABLE = TPC_HEAD+"output/service_available/";
    public static final String TPC_UI_OUT_FUNCTION_LUMINAIRE_CHANGED = TPC_HEAD+"output/luminaire_changed/";
    public static final String TPC_UI_OUT_FUNCTION_CONVERTING_COORDINATES = TPC_HEAD+"output/converting_coordinates/";
    public static final String TPC_UI_OUT_FUNCTION_CONVERTING_FILE = TPC_HEAD+"output/converting_file/";
    public static final String TPC_UI_OUT_FUNCTION_COORDINATES_RECEIVED = TPC_HEAD+"input/coordinates_received/";


    public static final String MSG_UI_START_MAPPING = "startMapping";
    public static final String MSG_UI_STOP_MAPPING = "stopMapping";

    public static final String OPTION_MSG_SPLIT = ";";

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
    public MqttAsyncClient connect(String ip, String port, MqttCallbackExtended mqttCallback, final MainController mainController){

        IMqttActionListener mqttListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                subscribeToAllTopics();
                mainController.onConnectionSucceed();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                exception.printStackTrace();
                mainController.onConnectionFailed();
            }
        };

        try {

            if(this.mqttClient != null){
                /*this.mqttClient.disconnect();
                this.mqttClient.close();*/
                this.mqttClient = null;
            }

            IMqttToken token = null;

            this.mqttClient  = new MqttAsyncClient(BROKER_PROTOCOL+ip+":"+port, this.clientId,
                    new MemoryPersistence());
            token = this.mqttClient.connect(getConnectOptions(), mqttCallback, mqttListener);

        } catch (MqttException e) {
            e.printStackTrace();
        }

        this.mqttClient.setCallback(mqttCallback);

        return this.mqttClient;
    }

    /**
     * Meldet den Client von allen Topics ab und schliesst die Verbindung.
     */
    public void disconnect() {
        try {
            publish(TPC_UI_OUT_SERVICE_AVAILABLE, MSG_LASTWILL);
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

    public void publishByteArray(String topic, byte[] payload, int qos, boolean retained) {
        try {
            this.mqttClient.publish(topic, payload, qos, retained);
        } catch ( MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Erstellt eine Klasse welche Verbinungsoptionen enthält.
     * @return - MqttConnectOptions, welche Verbinungsoptionen enthält.
     */
    private MqttConnectOptions getConnectOptions() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(this.CLIENT_USERNAME);
        // Last will -topic and -message,  QOS. No retain message (message sended when client has subscribed)
        connectOptions.setWill(TPC_UI_OUT_SERVICE_AVAILABLE, MSG_LASTWILL.getBytes(), QOS, false);
        connectOptions.setAutomaticReconnect(false);
        connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        connectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        connectOptions.setCleanSession(true);
        connectOptions.setConnectionTimeout(CONNECTION_TIMEOUT_SECONDS);

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

                if(COUNT_TPC_IN == MqttController.this.subscribedTpcs.size()){
                    MqttController.this.subscribeFininished();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                //TODO:
                exception.printStackTrace();
            }
        };

        try {
            subscribe(TPC_UI_IN_COORDINATES, QOS, subscribeListener);
            subscribe(TPC_UI_IN_AVAILABLE_SERVICES, QOS, subscribeListener);
            subscribe(TPC_UI_IN_CONVERTED_FILE, QOS, subscribeListener);
            subscribe(TPC_UI_OUT_FUNCTION_COORDINATES_RECEIVED, QOS, subscribeListener);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funktion wird aufgerufen, wenn alle benötigten Topics subscribed wurden.
     */
    private void subscribeFininished(){
        System.out.println("Subscribe Finnished");
        /*publish(TPC_DATA_PROCESSING_OUT_CONFIGURATION, MSG_DATA_PROCESSING_IS_HERE +
                MSG_PART_SPLIT_CHARACTER + CLIENT_USERNAME +
                MSG_PART_SPLIT_CHARACTER + this.clientId, QOS_2, true);*/
    }

    /**
     * Funktion überprüft ob eine MQTT-Verbindung aufgebaut ist.
     * @return
     */
    public boolean isConnected(){
        if(this.mqttClient != null && this.mqttClient.isConnected()){
            return true;
        }
        return false;
    }

    /**
     * Funktion überprüft ob eine MQTT-Broker zur gegebenen IP-Adresse besteht
     *
     * @param ip - IP-Adresse
     * @return true: Verbindung besteht, false: keine Verbindung zur gegebenen IP-Adresse
     */
    public boolean isConnectedWithIp(String ip){
        if(this.mqttClient != null && this.mqttClient.isConnected() && this.mqttClient.getServerURI().contains(ip)){
            return true;
        }

        return false;
    }
}
