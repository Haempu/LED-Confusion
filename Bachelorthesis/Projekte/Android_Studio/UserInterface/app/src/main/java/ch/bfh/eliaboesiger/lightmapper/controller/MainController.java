package ch.bfh.eliaboesiger.lightmapper.controller;

import android.app.Activity;
import android.content.Context;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ch.bfh.eliaboesiger.lightmapper.controller.activity.MappingDrawingActivity;
import ch.bfh.eliaboesiger.lightmapper.controller.activity.MappingOverviewActivity;
import ch.bfh.eliaboesiger.lightmapper.controller.activity.SceneryActivity;
import ch.bfh.eliaboesiger.lightmapper.controller.activity.SceneryConfigActivity;
import ch.bfh.eliaboesiger.lightmapper.model.Coordinate;
import ch.bfh.eliaboesiger.lightmapper.model.Luminaire;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;
import ch.bfh.eliaboesiger.lightmapper.model.Service;

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

    //Membervariabeln
    private MappingOverviewController mappingOverviewController;
    private MappingDrawingController mappingDrawingController;
    private SceneryController sceneryController;
    private SceneryConfigController sceneryConfigController;

    private MappingOverviewActivity mappingOverviewActivity;
    private MappingDrawingActivity mappingDrawingActivity;
    private SceneryActivity sceneryActivity;
    private SceneryConfigActivity sceneryConfigActivity;
    private Activity activeActivity;

    private MqttController mqttController;
    private DbController dbController;
    private MqttCallbackExtended mqttCallback;

    //Konstanten
    private static MainController instance;

    /**
     * Konstruktor: MainController
     */
    public MainController(Context context){
        this.mqttController = MqttController.getInstance();
        this.mappingOverviewController = new MappingOverviewController(context);
        this.mappingDrawingController = new MappingDrawingController(context);
        this.sceneryController = new SceneryController(context);
        this.sceneryConfigController = new SceneryConfigController(context);

        this.dbController = DbController.getInstance(context);

        this.mqttCallback = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("connectComplete");
            }

            @Override
            public void connectionLost(Throwable cause) {

                if(MainController.this.sceneryConfigController != null){
                    MainController.this.sceneryConfigController.clearServices();

                    if(MainController.this.activeActivity.getClass() == SceneryConfigActivity.class){
                        MainController.this.activeActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainController.this.sceneryConfigActivity.showConnectionLost();
                            }
                        });
                    }
                }
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
     * @param port
     */
    public void connect(String ip, String port){
        if(this.mqttController.isConnected() && !this.mqttController.isConnectedWithIp(ip)){
            this.mqttController.disconnect();
        }

        if(!this.mqttController.isConnectedWithIp(ip)){
            this.mqttController.connect(ip, port, this.mqttCallback, this);
        }
    }

    /**
     * Funktion gibt das Topic und die Message an den MqttController weiter.
     * @param topic
     * @param message
     */
    public void publish(String topic, String message){
        this.mqttController.publish(topic, message);
    }

    public void onConnectionSucceed(){

        if(this.activeActivity.getClass() == SceneryConfigActivity.class) {

            MainController.this.sceneryConfigActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainController.this.sceneryConfigActivity.showConnectionSucceed();
                }
            });
        }else if(this.activeActivity.getClass() == SceneryActivity.class){
            MainController.this.sceneryActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainController.this.sceneryActivity.showConnectionSucceed();
                }
            });
        }
    }

    public void onConnectionFailed(){

        if(this.activeActivity.getClass() == SceneryConfigActivity.class){
            MainController.this.sceneryConfigActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainController.this.sceneryConfigActivity.showConnectionFailed();
                }
            });
        }else if(this.activeActivity.getClass() == SceneryActivity.class){
            MainController.this.sceneryActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainController.this.sceneryActivity.showConnectionFailed();
                }
            });
        }
    }

    /**
     * Funktion handelt die erhaltenen Nachrichten
     * @param topic
     * @param message
     */
    private void handleIncomingMessage(String topic, String message){

        switch(topic){
            case(MqttController.TPC_UI_IN_AVAILABLE_SERVICES):
                MainController.this.sceneryConfigController.addServicesFromMqtt(message);

                if(MainController.this.sceneryConfigActivity != null){
                    MainController.this.sceneryConfigActivity.showAvailabilityOfServices();
                }

                break;
            case(MqttController.TPC_UI_IN_COORDINATES):

                message = message.substring(1, message.length()-1).replaceAll("\\s+","");
                String[] coordinates = message.split(",");

                for(String coordinate : coordinates){
                    if(!coordinate.isEmpty()) {
                        String[] attr = coordinate.split(MqttController.OPTION_MSG_SPLIT);
                        Coordinate c = new Coordinate();

                        c.setRadius(6);
                        c.setUid(attr[0]);
                        c.setxCoordinate(Integer.parseInt(attr[1]));
                        c.setyCoordinate(Integer.parseInt(attr[2]));
                        c.setxResolution(Integer.parseInt(attr[4]));
                        c.setyResolution(Integer.parseInt(attr[5]));

                        MainController.this.dbController.insertCoordinateWithScenery(MainController.this.sceneryConfigActivity.getSceneryId(), c);
                    }
                }

                MainController.this.sceneryConfigActivity.showMappingFinished(coordinates.length);
                break;

            case(MqttController.TPC_UI_IN_CONVERTED_FILE):
                message = message.substring(1, message.length()-1).replaceAll("\\s+","");
                String[] luminaires = message.split(",");

                for(String luminaire : luminaires){
                    String[] attr = luminaire.split(MqttController.OPTION_MSG_SPLIT);
                    Luminaire l = new Luminaire();
                    l.setId(Integer.parseInt(attr[0]));
                    l.setColor(attr[1]);
                    MainController.this.dbController.updateLuminaireColor(l);
                }

                if(this.activeActivity.getClass() == MappingDrawingActivity.class){
                    MainController.this.mappingDrawingActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainController.this.mappingDrawingActivity.reloadActivity();
                        }
                    });
                }
                break;

            case(MqttController.TPC_UI_OUT_FUNCTION_COORDINATES_RECEIVED):
                this.mappingDrawingController.coordinatesReceived();
                break;
        }
    }

    /**
     * Funktion holt den MappingOverviewController
     * @return
     */
    public MappingOverviewController getMappingOverviewController(MappingOverviewActivity mappingOverviewActivity) {
        this.mappingOverviewActivity = mappingOverviewActivity;
        return mappingOverviewController;
    }

    /**
     * Funktion setzt den MappingOverviewController
     * @param mappingOverviewController
     */
    public void setMappingOverviewController(MappingOverviewController mappingOverviewController) {
        this.mappingOverviewController = mappingOverviewController;
    }

    /**
     * Funktion holt den MappingDrawingController
     * @return
     */
    public MappingDrawingController getMappingDrawingController(MappingDrawingActivity mappingDrawingActivity) {
        this.mappingDrawingActivity = mappingDrawingActivity;
        return mappingDrawingController;
    }

    /**
     * Funktion setzt den MappingDrawingController
     * @param mappingDrawingController
     */
    public void setMappingDrawingController(MappingDrawingController mappingDrawingController) {
        this.mappingDrawingController = mappingDrawingController;
    }

    /**
     * Funktion holt den SceneryController
     * @return
     */
    public SceneryController getSceneryController(SceneryActivity sceneryActivity) {
        this.sceneryActivity = sceneryActivity;
        return sceneryController;
    }

    /**
     * Funktion setzt den SceneryController
     * @param sceneryController
     */
    public void setSceneryController(SceneryController sceneryController) {
        this.sceneryController = sceneryController;
    }

    /**
     * Funktion holt den SceneryConfigController
     * @return
     */
    public SceneryConfigController getSceneryConfigController(SceneryConfigActivity sceneryConfigActivity) {
        if(sceneryConfigActivity != null) {
            this.sceneryConfigActivity = sceneryConfigActivity;
        }
        return sceneryConfigController;
    }

    /**
     * Funktion setzt den SceneryConfigController
     * @param sceneryConfigController
     */
    public void setSceneryConfigController(SceneryConfigController sceneryConfigController) {
        this.sceneryConfigController = sceneryConfigController;
    }

    /**
     * Funktion gibt alle vorhandenen Services zurück.
     *
     * @return - Liste aller verfügbaren Services
     */
    public List<Service> getAvailableServices(){
        if(this.sceneryConfigController != null){
            return this.sceneryConfigController.getAllServices();
        }

        return null;
    }

    /**
     * Funktion setzt die im Moment aktive Activity.
     *
     * @param activity
     */
    public void setActiveActivity(Activity activity){
        this.activeActivity = activity;
    }

    /**
     * Funktion überprüft ob eine MQTT-Broker zur gegebenen IP-Adresse besteht
     *
     * @param ip - IP-Adresse
     * @return true: Verbindung besteht, false: keine Verbindung zur gegebenen IP-Adresse
     */
    public boolean isMqttConnectedWithIp(String ip){
        if(this.mqttController.isConnectedWithIp(ip)){
            return true;
        }

        return false;
    }
}
