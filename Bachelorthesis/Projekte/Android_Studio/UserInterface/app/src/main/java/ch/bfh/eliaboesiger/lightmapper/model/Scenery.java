package ch.bfh.eliaboesiger.lightmapper.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Die Model-Klasse Scenery bildet eine Beleuchtung/Szenarium ab.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 10.04.2018
 * @version 1.0
 */
public class Scenery {

    public static final String PORT = "1883";

    //Membervariabeln
    private Integer id;
    private String name;
    private String brokerIp;
    private String brokerPort;
    private ArrayList<Mapping> mappings;
    private ArrayList<Coordinate> coordinates;

    public Scenery(){}

    public Scenery(Integer id, String name, String brokerIp, String brokerPort){
        this.id = id;
        this.name = name;
        this.brokerIp = brokerIp;
        this.brokerPort = brokerPort;
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

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public String getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(String brokerPort) {
        this.brokerPort = brokerPort;
    }

    public ArrayList<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(ArrayList<Mapping> mappings) {
        this.mappings = mappings;
    }
}
