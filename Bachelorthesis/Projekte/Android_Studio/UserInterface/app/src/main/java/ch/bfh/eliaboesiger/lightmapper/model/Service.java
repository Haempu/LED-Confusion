package ch.bfh.eliaboesiger.lightmapper.model;

/**
 * Die Model-Klasse Service bildet ein Service oder auch einen Agent ab.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class Service {

    //Membervariabeln
    private String clientId;
    private String name;
    private String status;
    private boolean available;

    //Konstanten
    public static final String STATUS_CAMERA_SERVICE = "camera";
    public static final String STATUS_LUMINAIRE_SERVICE = "luminaire";
    public static final String STATUS_DATA_SERVICE = "dataProcessing";
    public static final String STATUS_AGENT = "agent";
    public static final String STATUS_PICTURE_CONVERTING_SERVICE = "pictureConverting";


    public Service(String clientId, String name, boolean available, String status){
        this.clientId = clientId;
        this.name = name;
        this.available = available;
        this.status = status;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return this.getClientId()+this.getName()+this.getStatus()+this.isAvailable();
    }
}
