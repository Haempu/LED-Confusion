package ch.bfh.lightmapper.dataprocessingservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ch.bfh.lightmapper.dataprocessingservice.model.Coordinate;
import ch.bfh.lightmapper.dataprocessingservice.model.Resolution;

/**
 * Die Klasse ComparsionController vergleicht zwei Bilder und findet heraus, ob sich im zweiten Bild ein neuer weisser Punkt befindet.
 * Wenn ja, gibt sie die entsprechenden Koordinaten zurück, wenn nicht, fordert sie ein weiters Bild an.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class ComparsionController {

	// Membervariablen
	private Mat referencePicture;
	private Resolution resolution;
	private int redos = 0;
	
	
	public static final double COLOR_WHITE = 255.0;
	public static final int MAX_REDOS = 5;

	/**
	 * Konstruktor lädt die OpenCV-Library
	 */
	public ComparsionController() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	}
	
	/**
	 * Speichert ein erhaltenes Bild in der Membervariabel referencePicture.
	 * @param message - MqttMessage eines Bildes.
	 */
	public void savePicture(MqttMessage message, int colorThreshold) {
		this.redos = 0;
		
		this.referencePicture = Imgcodecs.imdecode(new MatOfByte(message.getPayload()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);	
		this.referencePicture = convertToBlackWhite(this.referencePicture, colorThreshold);
		
		this.resolution = new Resolution();
		this.resolution.setxResolution(this.referencePicture.cols());
		this.resolution.setyResolution(this.referencePicture.rows());
	}
	
	/**
	 * Vergleicht das zuvor mit "savePicture" gespeicherte Bild und gibt eine Liste aller Punkte zurück, die sich von Bild1 zu Bild2 verändert haben.
	 * @param message - MqttMessage eines Bildes.
	 * @return Liste aller Punkte, die sich von Bild1 zu Bild2 verändert haben.
	 */
	public String[] comparsionPicture(MqttMessage message, int colorThreshold) {
		Mat comparePicture = Imgcodecs.imdecode(new MatOfByte(message.getPayload()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
		
		
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		Mat blackWhitePicture = convertToBlackWhite(comparePicture, colorThreshold);
		
		Imgproc.findContours(blackWhitePicture, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		String returnMessage = "";
		for (int i = 0; i < contours.size(); i++) {
			Coordinate coordinate = calculateCentre(contours.get(i));
			if (checkPixelsChange(blackWhitePicture, coordinate)) {
				returnMessage = returnMessage + coordinate.getxCoordinate() + MqttController.MSG_PART_SPLIT_CHARACTER 
				+ coordinate.getyCoordinate() + MqttController.MSG_PART_SPLIT_CHARACTER 
				+ coordinate.getRadius() + MqttController.MSG_PART_SPLIT_CHARACTER 
				+ coordinate.getResolution().getxResolution() + MqttController.MSG_PART_SPLIT_CHARACTER 
				+ coordinate.getResolution().getyResolution();
				return new String[] {MqttController.TPC_DATA_PROCESSING_OUT_COORDINATE,  returnMessage}; 
			}
		}
		if (redos == MAX_REDOS) {
			redos = 0;
			return new String[] {MqttController.TPC_DATA_PROCESSING_OUT_STATUS, MqttController.MSG_DATA_PROCESSING_MAPPING_DONE};
		}
		else {
			redos++;
			return new String[] {MqttController.TPC_DATA_PROCESSING_OUT_STATUS, MqttController.MSG_DATA_PROCESSING_REDO_PICTURE};
		}
	}

	/**
	 * Konvertiert ein Bild in nur schwarze und weisse Punkte. 
	 * @param picture - Zu konvertierendes Bild.
	 * @param colorThreshold - Schwellwert, bei welchem die Schwarz/Weiss-Grenze gezogen wird.
	 * @return Konvertiertes schwarz/weiss Bild.
	 */
	private Mat convertToBlackWhite(Mat picture, int colorThreshold) {
		Mat blurredImage = new Mat();
		Mat grayImage = new Mat();
		Mat mask = new Mat();
		
		Imgproc.blur(picture, blurredImage, new Size(11, 11));
		Imgproc.cvtColor(blurredImage, grayImage, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(grayImage, mask, colorThreshold, COLOR_WHITE, Imgproc.THRESH_BINARY);
		return mask;
	}
	
	/**
	 * Gibt die Koordinaten des Mittelpunktes einer Kontur (OpenCv-Sruktur, hier das jeweilige Leuchtmittel) zurück.
	 * @param conture - Kontur eines einzelnen Leuchtmittels.
	 * @return Mittelpunktkoordinaten eines einzelnen Leuchtmittels.
	 */
	private Coordinate calculateCentre(MatOfPoint conture) {
		double xMin = conture.toList().get(0).x;
		double xMax = conture.toList().get(0).x;
		double yMin = conture.toList().get(0).y;
		double yMax = conture.toList().get(0).y;
		
		for(Point p : conture.toList()){
			if(p.x < xMin){
				xMin = p.x;
			}
			if(p.x > xMax){
				xMax = p.x;
			}
			if(p.y < yMin){
				yMin = p.y;
			}
			if(p.y > yMax){
				yMax = p.y;
			}
		}
		
		Coordinate coordinate = new Coordinate();
		coordinate.setxCoordinate((int) Math.round((xMin + ((xMax-xMin) / 2))));
		coordinate.setyCoordinate((int) Math.round((yMin + ((yMax-yMin) / 2))));
		coordinate.setRadius((int) Math.round((xMax-xMin) / 2));
		coordinate.setResolution(this.resolution);
		return coordinate;
	}
	
	/**
	 * Klärt ab, ob sich ein gefundener Punkt im vergleich zum Referenzbild verändert hat, was bedeutet, dass ein neues Leuchtmittel gefunden wurde.
	 * @param picture - Aktuelles Bild.
	 * @param coordinate - Koordinate, die auf eine Änderung überprüft werden soll.
	 * @return true: Auf dem Referenzbild sind nicht die gleichen Farbwerte wie auf dem neuen Bild, sonst: false.
	 */
	private boolean checkPixelsChange(Mat picture, Coordinate coordinate) {
		return this.referencePicture.get(coordinate.getyCoordinate(), coordinate.getxCoordinate())[0] != picture.get(coordinate.getyCoordinate(), coordinate.getxCoordinate())[0];
	}
}
