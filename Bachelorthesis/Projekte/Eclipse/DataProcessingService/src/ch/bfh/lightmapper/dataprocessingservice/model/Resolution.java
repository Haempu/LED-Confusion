package ch.bfh.lightmapper.dataprocessingservice.model;

/**
 * Die Klasse "Resolution" stellt die Auflösung des jeweiligen Bildes dar.
 * 
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 *
 */
public class Resolution {
	
	private Integer id;
	private Integer xResolution;
	private Integer yResolution;
	
	public Integer getId() {
		return this.id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getxResolution() {
		return this.xResolution;
	}
	public void setxResolution(Integer xResolution) {
		this.xResolution = xResolution;
	}
	public Integer getyResolution() {
		return this.yResolution;
	}
	public void setyResolution(Integer yResolution) {
		this.yResolution = yResolution;
	}

}
