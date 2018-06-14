package ch.bfh.eliaboesiger.lightmapper.controller;

import android.content.Context;

import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.model.Mapping;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;

/**
 * Der MappingOverviewController stellt alle Funktionen, die in der MappingOverviewActivity
 * verwendet werden zur Verfügung und bietet für die Activity eine Schnittstelle zur Datenbank.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class MappingOverviewController {

    //Membervariablen
    private MqttController mqttController;
    private DbController dbController;

    /**
     * Konstruktor: MappingOverviewController
     * @param context - Context der Activity
     */
    public MappingOverviewController(Context context){
        this.mqttController = MqttController.getInstance();
        this.dbController = DbController.getInstance(context);
    }

    /**
     * Funktion liefert alle Mappings einer Beleuchtung zurück.
     * @param sceneryId
     * @return
     */
    public ArrayList<Mapping> getAllMappings(Integer sceneryId){
        return this.dbController.getMappings(sceneryId);
    }

    /**
     * Funktion fügt einer Beleuchtung ein neues Mapping hinzu.
     * @param sceneryId - Identifikation der Beleuchtung
     * @param name - Name des Mappings
     */
    public void addNewMapping(Integer sceneryId, String name){
        Mapping mapping = new Mapping(name, sceneryId);
        this.dbController.insertMapping(mapping);
    }

    /**
     * Funktion löscht eine Beleuchtung
     * @param id
     */
    public void removeScenery(Integer id){
        this.dbController.removeScenery(id);
    }
}
