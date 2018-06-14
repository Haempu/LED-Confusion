package ch.bfh.eliaboesiger.lightmapper.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.model.Coordinate;
import ch.bfh.eliaboesiger.lightmapper.model.Luminaire;
import ch.bfh.eliaboesiger.lightmapper.model.Mapping;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;

/**
 * Der DBController ist die Schnittstelle zur SQLLite-Datenbank.
 * In dieser Klasse werden alle Tabellen erstellt und Zugriff auf die
 * Tabellen gewährleistet.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 10.04.2018
 * @version 1.0
 */
public class DbController extends SQLiteOpenHelper {

    public static DbController instance;

    public static final String DB_NAME = "ligthmapper.db";
    public static final int DB_VERSION = 1;

    public static final String TAB_SCENERY = "tab_scenery";
    public static final String TAB_SCENERY_COLUMN_ID = "scenery_id";
    public static final String TAB_SCENERY_COLUMN_NAME = "scenery_name";
    public static final String TAB_SCENERY_COLUMN_BROKER_IP = "scenery_broker_ip";
    public static final String TAB_SCENERY_COLUMN_BROKER_PORT = "scenery_broker_port";

    public static final String TAB_MAPPING = "tab_mapping";
    public static final String TAB_MAPPING_COLUMN_ID = "mapping_id";
    public static final String TAB_MAPPING_COLUMN_NAME = "mapping_name";
    public static final String TAB_MAPPING_COLUMN_FK_SCENERY = "mapping_fk_scenery";

    public static final String TAB_COORDINATE = "tab_coordinate";
    public static final String TAB_COORDINATE_COLUMN_ID = "coordinate_id";
    public static final String TAB_COORDINATE_COLUMN_UID = "coordinate_uid";
    public static final String TAB_COORDINATE_COLUMN_X = "coordinate_x";
    public static final String TAB_COORDINATE_COLUMN_Y = "coordinate_y";
    public static final String TAB_COORDINATE_COLUMN_RESOLUTION_X = "coordinate_x_resolution";
    public static final String TAB_COORDINATE_COLUMN_RESOLUTION_Y = "coordinate_y_resolution";
    public static final String TAB_COORDINATE_COLUMN_RADIUS = "coordinate_radius";
    public static final String TAB_COORDINATE_FK_SCENERY = "coordinate_fk_scenery";

    public static final String TAB_LUMINAIRE = "tab_luminaire";
    public static final String TAB_LUMINAIRE_COLUMN_ID = "luminaire_id";
    public static final String TAB_LUMINAIRE_COLUMN_COLOR = "luminaire_color";
    public static final String TAB_LUMINAIRE_COLUMN_BRIGHTNESS= "luminaire_brightness";
    public static final String TAB_LUMINAIRE_COLUMN_ON = "luminaire_on";
    public static final String TAB_LUMINAIRE_COLUMN_FK_COORDINATE = "luminaire_fk_coordinate";
    public static final String TAB_LUMINAIRE_COLUMN_FK_MAPPING= "luminaire_fk_mapping";


    public static final String SQL_CREATE_SCENERY_TABLE = "create table "+TAB_SCENERY+" ("
            +TAB_SCENERY_COLUMN_ID+" integer primary key autoincrement, "
            +TAB_SCENERY_COLUMN_NAME+" text not null,"
            +TAB_SCENERY_COLUMN_BROKER_IP+" text not null, "
            +TAB_SCENERY_COLUMN_BROKER_PORT+" text not null);";

    public static final String SQL_CREATE_MAPPING_TABLE = "create table "+TAB_MAPPING+" ("
            +TAB_MAPPING_COLUMN_ID+" integer primary key autoincrement, "
            +TAB_MAPPING_COLUMN_NAME+" text not null,"
            +TAB_MAPPING_COLUMN_FK_SCENERY+" integer,"
            +"FOREIGN KEY ("+TAB_MAPPING_COLUMN_FK_SCENERY+") "
            +"REFERENCES "+TAB_SCENERY+"("+TAB_SCENERY_COLUMN_ID+"));";

    public static final String SQL_CREATE_COORDINATE_TABLE = "create table "+TAB_COORDINATE+" ("
            +TAB_COORDINATE_COLUMN_ID+" integer primary key autoincrement, "
            +TAB_COORDINATE_COLUMN_X+" integer not null, "
            +TAB_COORDINATE_COLUMN_Y+" integer not null,"
            +TAB_COORDINATE_COLUMN_UID+" text not null,"
            +TAB_COORDINATE_COLUMN_RESOLUTION_X+" integer not null, "
            +TAB_COORDINATE_COLUMN_RESOLUTION_Y+" integer not null,"
            +TAB_COORDINATE_COLUMN_RADIUS+" integer not null,"
            +TAB_COORDINATE_FK_SCENERY+" integer not null,"
            +"FOREIGN KEY ("+TAB_COORDINATE_FK_SCENERY+") "
            +"REFERENCES "+TAB_SCENERY+"("+TAB_SCENERY_COLUMN_ID+"));";

    public static final String SQL_CREATE_LUMINAIRE_TABLE = "create table "+TAB_LUMINAIRE+" ("
            +TAB_LUMINAIRE_COLUMN_ID+" integer primary key autoincrement, "
            +TAB_LUMINAIRE_COLUMN_COLOR+" text, "
            +TAB_LUMINAIRE_COLUMN_BRIGHTNESS+" integer,"
            +TAB_LUMINAIRE_COLUMN_ON+" integer,"
            +TAB_LUMINAIRE_COLUMN_FK_COORDINATE+" integer,"
            +TAB_LUMINAIRE_COLUMN_FK_MAPPING+" integer,"
            +"FOREIGN KEY ("+TAB_LUMINAIRE_COLUMN_FK_COORDINATE+") "
            +"REFERENCES "+TAB_COORDINATE+"("+TAB_COORDINATE_COLUMN_ID+") "
            +"FOREIGN KEY ("+TAB_LUMINAIRE_COLUMN_FK_MAPPING+") "
            +"REFERENCES "+TAB_MAPPING+"("+TAB_MAPPING_COLUMN_ID+"));";

    /**
     * Konstruktor
     * @param context
     */
    public DbController(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Funktion gibt immer die gleiche Instanz der Klasse DbController zurück,
     * damit alle Zugriffe auf die gleich DBController Instanz funktionieren.
     * @return Instanz der Klasse DbController.
     */
    public static synchronized DbController getInstance(Context context){
        if(instance == null){
            instance = new DbController(context);
        }
        return instance;
    }

    /**
     * Funktion wird beim erstellen der Datenbank aufgerufen.
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_SCENERY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MAPPING_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_COORDINATE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_LUMINAIRE_TABLE);
    }

    /**
     * Funktion wird beim Upgrade der Datenbank aufgerufen.
     * @param sqLiteDatabase
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    /* --------------------------- Scenery Queries ---------------------------------------------*/

    /**
     * Funktion fügt eine neue Beleuchtung/Szenario zur Datebank.
     * @param scenery
     */
    public int insertScenery(Scenery scenery){
        ContentValues sceneryValues = new ContentValues();
        sceneryValues.put(DbController.TAB_SCENERY_COLUMN_NAME, scenery.getName());
        sceneryValues.put(DbController.TAB_SCENERY_COLUMN_BROKER_IP, scenery.getBrokerIp());
        sceneryValues.put(DbController.TAB_SCENERY_COLUMN_BROKER_PORT, scenery.getBrokerPort());

        int insertId = (int)(this.getWritableDatabase().insert(DbController.TAB_SCENERY, null, sceneryValues));

        return insertId;
    }

    /**
     * Funktion liefert alle Szenarien/Beleuchtungen zurück.
     * @return
     */
    public ArrayList<Scenery> getSceneries(){
        Cursor c = this.getReadableDatabase().rawQuery("SELECT "+TAB_SCENERY_COLUMN_ID+","
                +TAB_SCENERY_COLUMN_NAME+","+TAB_SCENERY_COLUMN_BROKER_IP+","
                +TAB_SCENERY_COLUMN_BROKER_PORT+" FROM "+TAB_SCENERY+";", null);

        ArrayList<Scenery> sceneries = new ArrayList<>();

        if (c.moveToFirst()){
            do {

                sceneries.add(new Scenery(c.getInt(0), c.getString(1), c.getString(2), c.getString(3)));

            } while(c.moveToNext());
        }
        c.close();

        if(sceneries != null && sceneries.size() != 0){
            return sceneries;
        }

        return null;
    }

    /**
     * Funktion löscht ein Szenarium/Beleuchtung mit der gegebenen ID.
     * @param id
     */
    public void removeScenery(Integer id){
        removeCoordinatesWithScenery(id);
        remvoveAllMappingsWithScenery(id);
        this.getWritableDatabase().delete(TAB_SCENERY, TAB_SCENERY_COLUMN_ID + "=" + id, null);
    }

    /**
     * Funktion macht ein Update auf einem Szenarium/Beleuchtung.
     * @param scenery
     */
    public void updateScenery(Scenery scenery) {
        String strFilter = TAB_SCENERY_COLUMN_ID+"=" + scenery.getId();

        ContentValues args = new ContentValues();
        args.put(TAB_SCENERY_COLUMN_NAME, scenery.getName());
        args.put(TAB_SCENERY_COLUMN_BROKER_IP, scenery.getBrokerIp());
        args.put(TAB_SCENERY_COLUMN_BROKER_PORT, scenery.getBrokerPort());

        this.getWritableDatabase().update(TAB_SCENERY, args, strFilter, null);
    }

    /**
     * Funktion gibt ein Szenarium/Beleuchtung mit der gegebenen ID zurück.
     * @param sceneryId
     * @return
     */
    public Scenery getSceneryWithId(String sceneryId) {
        Cursor c = this.getReadableDatabase().rawQuery("SELECT "+TAB_SCENERY_COLUMN_ID+","
                +TAB_SCENERY_COLUMN_NAME+","+TAB_SCENERY_COLUMN_BROKER_IP+","
                +TAB_SCENERY_COLUMN_BROKER_PORT+" FROM "+TAB_SCENERY+" WHERE "+TAB_SCENERY_COLUMN_ID+"="+sceneryId+";", null);

        Scenery scenery = null;
        if (c.moveToFirst()){
            do {
                scenery = new Scenery(c.getInt(0), c.getString(1), c.getString(2), c.getString(3));

            } while(c.moveToNext());
        }
        c.close();

        return scenery;
    }

    /* --------------------------- Mapping Queries ---------------------------------------------*/

    /**
     * Funktion fügt ein neues Mapping zur Datenbank hinzu.
     * @param mapping
     */
    public int insertMapping(Mapping mapping){
        ContentValues mappingValues = new ContentValues();
        mappingValues.put(DbController.TAB_MAPPING_COLUMN_NAME, mapping.getName());
        mappingValues.put(DbController.TAB_MAPPING_COLUMN_FK_SCENERY, mapping.getSceneryId());

        int insertId = (int) this.getWritableDatabase().insert(DbController.TAB_MAPPING, null, mappingValues);
        this.getWritableDatabase().close();

        ArrayList<Coordinate> coordinates = getCoordinatesWithScenery(mapping.getSceneryId());

        System.out.println("coordinates size: "+coordinates.size());

        for(Coordinate c : coordinates){
            Luminaire l = new Luminaire();
            l.setCoordinates(c);
            l.setBrightness(255);
            l.setColor("#FFFFFF");
            insertLuminaire(insertId, l);
        }


        return insertId;
    }

    /**
     * Funktion gibt alle Mappings für eine Scenery/Beleuchtung zurück.
     * @param sceneryId
     * @return
     */
    public ArrayList<Mapping> getMappings(Integer sceneryId){
        Cursor c = this.getReadableDatabase().rawQuery("SELECT "+TAB_MAPPING_COLUMN_ID+","
                +TAB_MAPPING_COLUMN_NAME+","+TAB_MAPPING_COLUMN_FK_SCENERY+" FROM "+TAB_MAPPING+" " +
                "WHERE "+TAB_MAPPING_COLUMN_FK_SCENERY+"="+sceneryId+";", null);

        ArrayList<Mapping> mappings = new ArrayList<>();

        if (c.moveToFirst()){
            do {

                mappings.add(new Mapping(c.getInt(0), c.getString(1), c.getInt(2)));

            } while(c.moveToNext());
        }
        c.close();

        if(mappings != null && mappings.size() != 0){
            return mappings;
        }

        return null;
    }


    /**
     * Funktion gibt ein Mapping mit der gegebenen ID zurück.
     * @param mappingId
     * @return
     */
    public Mapping getMappingWithId(Integer mappingId) {

        Cursor c = this.getReadableDatabase().rawQuery("SELECT "+TAB_MAPPING_COLUMN_ID+","
                +TAB_MAPPING_COLUMN_NAME+","+TAB_MAPPING_COLUMN_FK_SCENERY+" FROM "+TAB_MAPPING+" " +
                "WHERE "+TAB_MAPPING+"="+mappingId+";", null);

        if (c.moveToFirst()){
            do {
                return new Mapping(c.getInt(0), c.getString(1), c.getInt(2));

            } while(c.moveToNext());
        }
        c.close();

        return null;
    }

    /**
     * Funktion löscht ein Szenarium/Beleuchtung mit der gegebenen ID.
     * @param id
     * @return
     */
    public Integer removeMapping(Integer id){
        removeLuminairesWithMapping(id);
        Integer sceneryId = null;
        Cursor c = this.getReadableDatabase().rawQuery("SELECT "+TAB_MAPPING_COLUMN_FK_SCENERY+" " +
                "FROM "+TAB_MAPPING+" WHERE "+TAB_MAPPING_COLUMN_ID + "=" + id + ";", null);

        ArrayList<Mapping> mappings = new ArrayList<>();

        if (c.moveToFirst()){
            do {
                sceneryId = c.getInt(0);
            } while(c.moveToNext());
        }
        c.close();

        this.getWritableDatabase().delete(TAB_MAPPING, TAB_MAPPING_COLUMN_ID + "=" + id, null);

        return sceneryId;
    }

    public void remvoveAllMappingsWithScenery(Integer sceneryId){
        ArrayList<Mapping> mappings = getMappings(sceneryId);

        if(mappings != null && mappings.size() != 0){
            for(Mapping m : mappings){
                removeMapping(m.getId());
            }
        }
        this.getWritableDatabase().delete(TAB_MAPPING, TAB_MAPPING_COLUMN_FK_SCENERY + "=" + sceneryId, null);
    }

    /* --------------------------- Coordinate Queries ------------------------------------------*/

    /**
     * Funktion fügt eine Koordinate mit der Scenery/Beleuchtungs ID zur Datenbank hinzu.
     * @param sceneryId
     * @param coordinate
     */
    public int insertCoordinateWithScenery(Integer sceneryId, Coordinate coordinate){
        ContentValues coordinateValues = new ContentValues();
        coordinateValues.put(DbController.TAB_COORDINATE_COLUMN_UID, coordinate.getUid());
        coordinateValues.put(DbController.TAB_COORDINATE_COLUMN_X, coordinate.getxCoordinate());
        coordinateValues.put(DbController.TAB_COORDINATE_COLUMN_Y, coordinate.getyCoordinate());
        coordinateValues.put(DbController.TAB_COORDINATE_COLUMN_RESOLUTION_X, coordinate.getxResolution());
        coordinateValues.put(DbController.TAB_COORDINATE_COLUMN_RESOLUTION_Y, coordinate.getyResolution());
        coordinateValues.put(DbController.TAB_COORDINATE_COLUMN_RADIUS, coordinate.getRadius());
        coordinateValues.put(DbController.TAB_COORDINATE_FK_SCENERY, sceneryId);

        long insertId = this.getWritableDatabase().insert(DbController.TAB_COORDINATE, null, coordinateValues);

        return (int) insertId;
    }

    /**
     * Funktion gibt alle Koordinaten-Objekte für eine Scenery/Beleuchtung zurück.
     * @param sceneryId
     * @return
     */
    public ArrayList<Coordinate> getCoordinatesWithScenery(Integer sceneryId){
        Cursor c = this.getReadableDatabase().rawQuery("SELECT "+TAB_COORDINATE_COLUMN_X+","+TAB_COORDINATE_COLUMN_Y+","
                +TAB_COORDINATE_COLUMN_RESOLUTION_X+","+TAB_COORDINATE_COLUMN_RESOLUTION_Y+","
                +TAB_COORDINATE_COLUMN_ID+","+TAB_COORDINATE_COLUMN_RADIUS+", "+TAB_COORDINATE_COLUMN_UID+" "+
                "FROM "+TAB_COORDINATE+" "+
                "WHERE "+TAB_COORDINATE_FK_SCENERY + "=" + sceneryId + ";", null);

        ArrayList<Coordinate> coordinates = new ArrayList<>();

        if (c.moveToFirst()){
            do {
                Coordinate coordinate = new Coordinate();
                coordinate.setxCoordinate(c.getInt(0));
                coordinate.setyCoordinate(c.getInt(1));
                coordinate.setxResolution(c.getInt(2));
                coordinate.setyResolution(c.getInt(3));
                coordinate.setId(c.getInt(4));
                coordinate.setRadius(c.getInt(5));
                coordinate.setUid(c.getString(6));

                coordinates.add(coordinate);
            } while(c.moveToNext());
        }
        c.close();

        return coordinates;
    }

    /**
     * Funktion löscht alle Koordinaten für eine Scenery/Beleuchtung
     * @param sceneryId
     */
    public void removeCoordinatesWithScenery(Integer sceneryId){
        this.getWritableDatabase().delete(TAB_COORDINATE, TAB_COORDINATE_FK_SCENERY + "=" + sceneryId, null);
    }

    /* --------------------------- Luminaire Queries -------------------------------------------*/

    /**
     * Funktion fügt eine Lichtquelle zu einem Mapping hinzu.
     * @param mappingId
     * @param luminaire
     */
    public void insertLuminaire(Integer mappingId, Luminaire luminaire){
        ContentValues coordinateValues = new ContentValues();
        coordinateValues.put(DbController.TAB_LUMINAIRE_COLUMN_COLOR, luminaire.isOn());
        coordinateValues.put(DbController.TAB_LUMINAIRE_COLUMN_COLOR, luminaire.getColor());
        coordinateValues.put(DbController.TAB_LUMINAIRE_COLUMN_BRIGHTNESS, luminaire.getBrightness());
        coordinateValues.put(DbController.TAB_LUMINAIRE_COLUMN_FK_MAPPING, mappingId);
        coordinateValues.put(DbController.TAB_LUMINAIRE_COLUMN_FK_COORDINATE, luminaire.getCoordinates().getId());

        long insertId = this.getWritableDatabase().insert(DbController.TAB_LUMINAIRE, null, coordinateValues);
    }

    /**
     * Funktion gibt alle Lichtquellen (Luminaire-Objekte) für ein Mapping zurück.
     * @param mappingId
     * @return
     */
    public ArrayList<Luminaire> getLuminairesWithMapping(Integer mappingId){
        Cursor c = this.getReadableDatabase().rawQuery("SELECT "+TAB_COORDINATE+"."+TAB_COORDINATE_COLUMN_X+","
                +TAB_COORDINATE+"."+TAB_COORDINATE_COLUMN_Y+","
                +TAB_COORDINATE+"."+TAB_COORDINATE_COLUMN_RESOLUTION_X+","
                +TAB_COORDINATE+"."+TAB_COORDINATE_COLUMN_RESOLUTION_Y+","
                +TAB_COORDINATE+"."+TAB_COORDINATE_COLUMN_RADIUS+","
                +TAB_COORDINATE+"."+TAB_COORDINATE_COLUMN_UID+","
                +TAB_LUMINAIRE+"."+TAB_LUMINAIRE_COLUMN_ID+","
                +TAB_LUMINAIRE+"."+TAB_LUMINAIRE_COLUMN_COLOR+","
                +TAB_LUMINAIRE+"."+TAB_LUMINAIRE_COLUMN_BRIGHTNESS+","
                +TAB_LUMINAIRE+"."+TAB_LUMINAIRE_COLUMN_ON+" "+
                "FROM "+TAB_LUMINAIRE+" "+
                "INNER JOIN "+TAB_COORDINATE+" ON "+TAB_LUMINAIRE+"."+TAB_LUMINAIRE_COLUMN_FK_COORDINATE+"="+TAB_COORDINATE+"."+TAB_COORDINATE_COLUMN_ID+" "+
                "WHERE "+TAB_LUMINAIRE_COLUMN_FK_MAPPING + "=" + mappingId + " "+
                "ORDER BY "+TAB_COORDINATE_COLUMN_Y+";", null);

        ArrayList<Luminaire> luminaires = new ArrayList<>();

        if (c.moveToFirst()){
            do {
                Coordinate coordinate = new Coordinate();
                coordinate.setxCoordinate(c.getInt(0));
                coordinate.setyCoordinate(c.getInt(1));
                coordinate.setxResolution(c.getInt(2));
                coordinate.setyResolution(c.getInt(3));
                coordinate.setRadius(c.getInt(4));
                coordinate.setUid(c.getString(5));

                Luminaire luminaire = new Luminaire();
                luminaire.setId(c.getInt(6));
                luminaire.setColor(c.getString(7));
                luminaire.setBrightness(c.getInt(8));
                luminaire.setOn(c.getInt(9));
                luminaire.setCoordinates(coordinate);

                luminaires.add(luminaire);
            } while(c.moveToNext());
        }
        c.close();

        return luminaires;
    }

    /**
     * Funktion updatet eine Leuchtquelle mit Farbe und Helligkeit.
     * @param luminaire
     */
    public void updateLuminaire(Luminaire luminaire) {
        ContentValues luminaireValues = new ContentValues();
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_ID, luminaire.getId());
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_ON, luminaire.isOn());
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_COLOR, luminaire.getColor());
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_BRIGHTNESS, luminaire.getBrightness());

        this.getWritableDatabase().update(TAB_LUMINAIRE, luminaireValues, TAB_LUMINAIRE_COLUMN_ID + "=" + luminaire.getId(), null);
    }

    /**
     * Funktion update eine Leuchtquelle mit der Farbe und der Id.
     * @param luminaire
     */
    public void updateLuminaireColor(Luminaire luminaire) {
        ContentValues luminaireValues = new ContentValues();
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_ID, luminaire.getId());
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_COLOR, luminaire.getColor());
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_ON, Luminaire.LUMINAIRE_ON);
        luminaireValues.put(DbController.TAB_LUMINAIRE_COLUMN_BRIGHTNESS, Luminaire.LUMINAIRE_MAX_BRIGHTNESS);

        this.getWritableDatabase().update(TAB_LUMINAIRE, luminaireValues, TAB_LUMINAIRE_COLUMN_ID + "=" + luminaire.getId(), null);
    }

    /**
     * Funktion löscht alle konfigurierten Lichtquellen, die in einem Mapping vorhanden sind.
     * @param mappingId
     */
    public void removeLuminairesWithMapping(Integer mappingId){
        this.getWritableDatabase().delete(TAB_LUMINAIRE, TAB_LUMINAIRE_COLUMN_FK_MAPPING + "=" + mappingId, null);
    }
}
