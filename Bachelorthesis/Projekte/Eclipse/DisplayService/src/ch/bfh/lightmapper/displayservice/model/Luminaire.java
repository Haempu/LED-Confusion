package ch.bfh.lightmapper.displayservice.model;

/**
 * Die Model-Klasse "Luminaire" beinhaltet alle Attribute einer Leuchtquelle.
 * 
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class Luminaire {

	//Membervariablen
	private int uid;
	private String color;
	private int brightness;
	private boolean on;
	
	//Konstanten
	public static final int LUMINAIRE_ON = 1;
	public static final int LUMINAIRE_OFF = 0;
	
	public Luminaire(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}
	
	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}
}
