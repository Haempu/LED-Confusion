package ch.bfh.eliaboesiger.lightmapper.controller;

import android.content.Context;

import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.model.Scenery;

/**
 * Der SceneryController stellt alle Funktionen, die in der SceneryActivity
 * verwendet werden zur Verfügung und bietet für die Activity eine Schnittstelle zur Datenbank.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class SceneryController {

    //Membervariablen
    private MqttController mqttController;
    private DbController dbController;

    /**
     * Konstruktor: SceneryController
     * @param context - Context der Activity
     */
    public SceneryController(Context context){
        this.mqttController = MqttController.getInstance();
        this.dbController = DbController.getInstance(context);
    }

    /**
     * Funktion returniert alle vorhandenen Szenarien.
     *
     * @return sceneries
     */
    public ArrayList<Scenery> getAllSceneries(){
        return this.dbController.getSceneries();
    }

    /**
     * Funktion fügt eine neue Beleuchtung in die Datenbank.
     * @param scenery
     */
    public void addNewScenery(Scenery scenery){
        this.dbController.insertScenery(scenery);
    }

    /**
     * Funktion löscht eine Beleuchtung/Szenarium aus der Datenbank.
     * @param id
     */
    public void removeScenery(Integer id){
        this.dbController.removeScenery(id);
    }
}
