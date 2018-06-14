package ch.bfh.lightmapper.ledstripservice.model;

public class Luminaire {

	private int uid;
	private String color;
	private int brightness;
	private boolean on;
	
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
