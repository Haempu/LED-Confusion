package ch.bfh.eliaboesiger.lightmapper.model;


/**
 * Die Model-Klasse Coordinate bildet eine Koordinate ab.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 10.04.2018
 * @version 1.0
 */
public class Coordinate {

    //Membervariable
    private Integer id;
    private String uid;
    private Integer xCoordinate;
    private Integer yCoordinate;
    private Integer xResolution;
    private Integer yResolution;

    private Integer radius;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(Integer xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Integer getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(Integer yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public Integer getxResolution() {
        return xResolution;
    }

    public void setxResolution(Integer xResolution) {
        this.xResolution = xResolution;
    }

    public Integer getyResolution() {
        return yResolution;
    }

    public void setyResolution(Integer yResolution) {
        this.yResolution = yResolution;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }
}
