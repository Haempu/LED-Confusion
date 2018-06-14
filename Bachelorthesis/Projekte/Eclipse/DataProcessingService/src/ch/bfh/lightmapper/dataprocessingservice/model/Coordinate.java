package ch.bfh.lightmapper.dataprocessingservice.model;

/**
 * Die Klasse "Coordinate" stellt eine Koordinate dar. Eine Koordinate besteht aus
 * X- und Y-Koordinate, Raduis des Leuchtmittels, sowie die Auflösung des gesamten Bildes.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class Coordinate {
	
	private Integer xCoordinate;
	private Integer yCoordinate;
	private Integer radius;
	private Resolution resolution;
	
	public Integer getxCoordinate() {
		return this.xCoordinate;
	}
	public void setxCoordinate(Integer xCoordinate) {
		this.xCoordinate = xCoordinate;
	}
	public Integer getyCoordinate() {
		return this.yCoordinate;
	}
	public void setyCoordinate(Integer yCoordinate) {
		this.yCoordinate = yCoordinate;
	}
	public Integer getRadius() {
		return this.radius;
	}
	public void setRadius(Integer radius) {
		this.radius = radius;
	}
	
	public Resolution getResolution() {
		return resolution;
	}
	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}

}
