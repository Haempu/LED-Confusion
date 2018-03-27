package ch.bfh.bachelorthesis.ledmapper;

import android.content.Context;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;


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

    // Constants
    private static final String CLIENT_USERNAME = "camera service";
    private static final int KEEP_ALIVE_INTERVAL = 12; // ATENTION: Values below 12 secounds won't work!

    // Membervariables
    private MqttAndroidClient mqttClient;
    private String clientId;
    private String willTopic;
    private String willMsg;
    private Context context;

    /**
     * Konstruktor
     * @param context - Android-Context, in welchem der MqttController laufen soll.
     * @param willTopic - Last will topic.
     * @param willMsg - Last will Nachricht.
     */
    public MqttController (Context context, String willTopic, String willMsg) {
        this.context = context;
        this.willTopic = willTopic;
        this.willMsg = willMsg;
        this.clientId = MqttClient.generateClientId();
    }

    /**
     * Meldet sich als Client an einem Mqtt-Broker an.
     * @param broker - IP-Adresse des gewünschten Brokers
     * @param callback - MqttCallback, welcher auf Nachrichten vom Broker reagieren kann.
     * @param listener IMqttActionListener, welcher meldet, ob die Anmeldung des Clients erfolgreich war.
     */
    public void connect(String broker, MqttCallback callback, IMqttActionListener listener) {
        this.mqttClient = new MqttAndroidClient(this.context, broker, this.clientId);

        IMqttToken token = null;
        try {
            token = this.mqttClient.connect(setConnectOptions());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        token.setActionCallback(listener);
        this.mqttClient.setCallback(callback);
    }

    /**
     * Meldet den Client von allen Topics ab und schliesst die Verbindung.
     */
    public void disconnect() {
        try {
            publish(willTopic, willMsg + " disconnect");
            this.mqttClient.unsubscribe("#");
            IMqttToken disconToken = this.mqttClient.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // TODO handle
                    System.out.println("Disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // TODO handle
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
     * Erstellt eine Klasse welche Verbinungsoptionen enthält.
     * @return - MqttConnectOptions, welche Verbinungsoptionen enthält.
     */
    private MqttConnectOptions setConnectOptions() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(this.CLIENT_USERNAME);
        // Last will -topic and -message,  QOS. No retain message (message sended when client has subscribed)
        connectOptions.setWill(this.willTopic, this.willMsg.getBytes(), 0, false);
        connectOptions.setAutomaticReconnect(false);
        connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        connectOptions.setKeepAliveInterval(this.KEEP_ALIVE_INTERVAL);
        connectOptions.setCleanSession(true);
        return connectOptions;


        /* TODO
        Falls Abwesenheit des Geräts unter 12 Sekunden detektiert werden muss, kann folgendes weiterhelfen:
                next: 60000 ist Zeit, nachder der nächste Alarm reinkommt. Wird von MQTT benutzt um ein Ping zu senden. wird dieser nicht an den Broker geschickt,
        interpretiert dieser das als abwesender client und trennt die Verbingung. Warum 400 steht weiss ich nicht. Das macht keinen Sinn und ist wohl
        das Problem wenn die Zahl zu klein wird kann der Broker nicht reagieren. Wird die App neu gestartet gibt es beim 1. Ping einen Fehler
        weil die Zahl irgendwie zu klein ist. Geht app nur in den Hintergrund und kommt wieder, funktioniert es genau so wie es sollte...


In diesem Beispiel hats geklappt. Hat aber auch schon mit Zahlen über 402 nicht geklappt...
        03-27 16:22:42.845 18635-18635/ch.bfh.bachelorthesis.ledmapper D/AlarmPingSender: Sending Ping at:1522160562845
03-27 16:22:42.850 18635-18635/ch.bfh.bachelorthesis.ledmapper D/AlarmPingSender: Schedule next alarm at 1522160563252
03-27 16:22:42.851 18635-18635/ch.bfh.bachelorthesis.ledmapper D/AlarmPingSender: Alarm scheule using setExactAndAllowWhileIdle, next: 402
03-27 16:22:47.870 18635-18635/ch.bfh.bachelorthesis.ledmapper D/AlarmPingSender: Sending Ping at:1522160567869
03-27 16:22:47.875 18635-18635/ch.bfh.bachelorthesis.ledmapper D/AlarmPingSender: Schedule next alarm at 1522160627875
03-27 16:22:47.876 18635-18635/ch.bfh.bachelorthesis.ledmapper D/AlarmPingSender: Alarm scheule using setExactAndAllowWhileIdle, next: 60000
         */
    }
}
