package ch.bfh.bachelorthesis.ledmapper.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Surface;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ch.bfh.bachelorthesis.ledmapper.controller.activity.CameraActivity;
import ch.bfh.bachelorthesis.ledmapper.controller.activity.ConfigActivity;
import ch.bfh.bachelorthesis.ledmapper.controller.util.Utils;

/**
 * Die Klasse MainController ist der Hauptcontroller und beinhaltet alle Controller, die mit den
 * Activities kommunizieren. Zudem handelt er die angekommenen MQTT-Nachrichten und teilt sie dem
 * jeweiligen Controller zu.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 10.04.2018
 * @version 1.0
 */
public class MainController {

    //Membervariablen
    private MqttController mqttController;
    private MqttCallbackExtended mqttCallback;
    private CameraActivityController cameraActivityController;
    private ConfigActivity configActivity;
    private CameraActivity cameraActivity;

    //Konstanten
    public static final String PORT = "1883";
    private static MainController instance;

    /**
     * Konstruktor
     * @param context - Context der Applikation
     */
    public MainController(Context context){
        this.mqttController = MqttController.getInstance();
        this.cameraActivityController = new CameraActivityController(context);


        this.mqttCallback = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("connectComplete");
            }

            @Override
            public void connectionLost(Throwable cause) {
                MainController.this.cameraActivity.showNoConnection();
                System.out.println("connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                System.out.println("message arrived: "+topic+message.toString());
                handleIncomingMessage(topic, message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("deliveryComplete");

            }
        };
    }

    /**
     * Funktion gibt immer die gleiche Instanz der Klasse MainController zurück,
     * damit alle über dieselbe Verbindung kommunizieren.
     * @param context - Context der Applikation
     * @return Instanz der Klasse MainController.
     */
    public static synchronized MainController getInstance(Context context){
        if(instance == null){
            instance = new MainController(context);
        }

        return instance;
    }

    /**
     * Hier wird die Mqtt-Verbindung zum Broker aufgebaut.
     * @param ip
     */
    public void connect(String ip){
        this.mqttController.connect(ip, PORT, this.mqttCallback, this);
    }

    public void disconnect(){
        if(isMqttConnected()){
            this.mqttController.disconnect();
        }
    }

    /**
     * Funktion handelt die erhaltenen Nachrichten
     * @param topic
     * @param message
     */
    private void handleIncomingMessage(String topic, String message){
        String msg = message.toString();
        if (msg.equals(MqttController.MSG_DO_PICTURE)) {
            Bitmap picture = this.cameraActivityController.getCameraController().takePicture();
            sendPicture(picture);
            System.out.println("picture sended_______________");
        }
    }

    /**
     * Funktion wird aufgerufen, wenn die Verbindung zum MQTT-Broker aufgebaut werden konnte.
     */
    public void onConnectionSucceed(){
        MainController.this.configActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainController.this.configActivity.showConnectionSucceed();
            }
        });
    }

    /**
     * Funktion wird aufgerufen wenn die Verbindung zum MQTT-Broker abgebrochen wurde.
     */
    public void onConnectionFailed(){
        MainController.this.configActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainController.this.configActivity.showConnectionFailed();
            }
        });
    }

    /**
     * Sendet ein Bild auf dem entsprechenden Topic
     * @param picture - Bitmap-Bild
     */
    private void sendPicture(Bitmap picture) {
        if(isMqttConnected()){
            mqttController.publishByteArray(MqttController.TPC_CAM_OUT_PICTURE, Utils.bitmapToByteArray(picture), MqttController.QOS_0, false);
        }
    }

    /**
     * Funktion überprüft ob eine Verbindung zum MQTT-Broker vorhanden ist
     *
     * @return true: Verbindung besteht, false: keine Verbindung
     */
    public boolean isMqttConnected(){
        if(this.mqttController != null && this.mqttController.isConnected()){
            return true;
        }

        return false;
    }


    public CameraActivityController getCameraActivityController(CameraActivity cameraActivity) {
        this.cameraActivity = cameraActivity;
        return cameraActivityController;
    }

    public void setConfigActivity(ConfigActivity configActivity){
        this.configActivity = configActivity;
    }
}
