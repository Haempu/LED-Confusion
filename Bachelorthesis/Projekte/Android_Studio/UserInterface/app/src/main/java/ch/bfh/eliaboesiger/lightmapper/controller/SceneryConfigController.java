package ch.bfh.eliaboesiger.lightmapper.controller;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ch.bfh.eliaboesiger.lightmapper.model.Scenery;
import ch.bfh.eliaboesiger.lightmapper.model.Service;

/**
 * Der SceneryConfigController stellt alle Funktionen, die in der SceneryConfigActivity
 * verwendet werden zur Verfügung und bietet für die Activity eine Schnittstelle zur Datenbank.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class SceneryConfigController {

    //Membervariabeln
    private MqttController mqttController;
    private DbController dbController;
    private ArrayList<Service> services = new ArrayList<>();

    /**
     * Konstruktor: SceneryConfigController
     *
     * @param context
     */
    public SceneryConfigController(Context context){
        this.mqttController = MqttController.getInstance();
        this.dbController = DbController.getInstance(context);
    }

    /**
     * Fügt eine neue Beleuchtung/Scenery hinzu.
     *
     * @param scenery
     * @return
     */
    public int addNewScenery(Scenery scenery){
        return this.dbController.insertScenery(scenery);
    }

    /**
     * Funktion updated eine Beleuchtung/Scenery
     * @param scenery
     */
    public void updateScenery(Scenery scenery){
        this.dbController.updateScenery(scenery);
    }

    /**
     * Funktion liefert alle Services zurück.
     * @return - Liste aller Services
     */
    public List<Service> getAllServices(){
        return this.services;
    }


    /**
     * Funktion überprüft ob alle für das User Interface benötigten Services zur Verfügung stehen.
     * @return true: wenn die Services zur Verfügung stehen, false: Wenn nicht
     */
    public boolean allServicesAvailable() {
        boolean dataService = false;
        boolean cameraService = false;
        boolean luminaireService = false;

        for(Service s : this.services){
            if(s.getStatus().equals(Service.STATUS_LUMINAIRE_SERVICE)) {
                luminaireService = true;
            }else if(s.getStatus().equals(Service.STATUS_CAMERA_SERVICE)){
                cameraService = true;
            }else if(s.getStatus().equals(Service.STATUS_DATA_SERVICE)) {
                dataService = true;
            }
        }

        if(dataService && cameraService && luminaireService){
            return true;
        }

        return false;
    }

    /**
     * Funktion wird nach dem Erhalten der Message aller Services ausgeführt.
     * Somit kann immer der aktuelle Stand der aktuell  verfügbaren Services abgelegt werden.
     * @param message - MQTT-Message mit allen Services
     */
    public void addServicesFromMqtt(String message){
        this.services.clear();
        message = message.substring(1, message.length()-1);
        String[] availableServices = message.split(",");

        for(String service : availableServices){

            if(service.length() != 0){
             String[] splittedSrv = service.split(MqttController.OPTION_MSG_SPLIT);
                this.services.add(new Service(splittedSrv[2],splittedSrv[1], true, splittedSrv[0].replaceAll("\\s+","")));
            }
        }
    }

    /**
     * Funktion gibt eine Beleuchtung zurück
     * @param sceneryId - Identifikation der Beleuchtung
     * @return - Beleuchtung (Scenery)
     */
    public Scenery getSceneryWithId(String sceneryId) {
        return this.dbController.getSceneryWithId(sceneryId);
    }

    /**
     * Funktion liefert zurück, ob das User Interface mit der gegebenen IP-Adresse eine Verbindung
     * zum MQTT-Broker aufgebaut hat.
     * @param ip - IPv4 Adresse
     * @return true: Verbindungsaufbau besteht, false: kein vorhandener Verbindungsaufbau.
     */
    public boolean isMqttConnectedWithIp(String ip){
        return this.mqttController.isConnectedWithIp(ip);
    }

    /**
     * Funktion löscht alle Services aus der Liste.
     */
    public void clearServices(){
        if(this.services != null){
            this.services.clear();
        }
    }
}
