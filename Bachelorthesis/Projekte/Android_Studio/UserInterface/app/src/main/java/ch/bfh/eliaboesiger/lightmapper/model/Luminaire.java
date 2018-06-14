package ch.bfh.eliaboesiger.lightmapper.model;


/**
 * Die Model-Klasse Luminaire bildet eine Leuchtquelle ab.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 10.04.2018
 * @version 1.0
 */
public class Luminaire {

    //Konstanten
    public static final int LUMINAIRE_ON = 1;
    public static final int LUMINAIRE_OFF = 0;
    public static final int LUMINAIRE_MIN_BRIGHTNESS = 0;
    public static final int LUMINAIRE_MAX_BRIGHTNESS = 100;

    //Membervariable
    private Integer id;
    private String color;
    private Integer brightness;
    private Coordinate coordinates;
    private Integer on;

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    public Integer isOn() {
        return on;
    }

    public void setOn(Integer on) {
        this.on = on;
    }
}
