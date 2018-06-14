package ch.bfh.eliaboesiger.lightmapper.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Die Model-Klasse Mapping bildet ein Mapping ab.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 10.04.2018
 * @version 1.0
 */
public class Mapping {

    //Membervariabeln
    private Integer id;
    private String name;
    private ArrayList<Luminaire> luminaires;
    private Integer sceneryId;

    public Mapping(String name, Integer sceneryId){
        this.name = name;
        this.sceneryId = sceneryId;
    }

    public Mapping(Integer id, String name, Integer sceneryId){
        this.id = id;
        this.name = name;
        this.sceneryId = sceneryId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Luminaire> getLuminaires() {
        return luminaires;
    }

    public void setLuminaires(ArrayList<Luminaire> luminaires) {
        this.luminaires = luminaires;
    }

    public Integer getSceneryId() {
        return sceneryId;
    }

    public void setSceneryId(Integer sceneryId) {
        this.sceneryId = sceneryId;
    }

}
