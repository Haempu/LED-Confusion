package ch.bfh.lightmapper.pictureconvertingservice.controller;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Die Klasse ConvertingController konvertiert das Bild von seiner vollen Grösse so, dass es mit der Anzahl verfügbaren LEDs möglichst gut angezeigt werden kann.
 * Im Prinzip wird die Qualtität so lange heruntergeschraubt, bis das Bild nur noch aus wenigen Pixeln besteht.
 * @author Patrik Aebischer, Elia Bösiger
 *
 */
public class ConvertingController {
	
	// Konstanten
	public static final int ID = 0;
	public static final int X = 1;
	public static final int Y = 2;
	
	/**
	 * Konstruktor lädt die OpenCV-Library.
	 */
	public ConvertingController(){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	}
	
	/**
	 * Funktion konvertiert ein beliebiges (grösseres) Bild auf die vorhandene Anzahl an leuchtmitteln. Sie weisst dazu jeder Leuchtmittelkoordinate
	 * einen entsprechenden Farbwert zu.
	 * @param coordinates - Koordinaten aller vorhandenen Leuchtmittel.
	 * @param picture - Bild, welches auf die Leuchtmittelanzahl konvertiert werden soll.
	 * @return Koordinaten aller Leuchtmittel mit den entsprechenden Farbwerten.
	 */
	public String convertPicture(ArrayList<String> coordinates, byte[] picture) {
		// TODO nur zu testzwecken
		//"C:\Users\PA2-Laptop\Documents\EclipseProjects\PictureConvertingService\resitaly.png"
		//String s = System.getProperty("file.separator");
		//String path = "C:"+s+"Users"+s+"PA2-Laptop"+s+"Documents"+s+"EclipseProjects"+s+"PictureConvertingService"+s+"res"+s+"italy.jpg";
		//System.out.println(path);
		//Mat originalPicture = loadImage(path);
		/*coordinates.clear();
		coordinates.add("1;1;2");
		coordinates.add("2;4;2");
		coordinates.add("3;7;2");*/
		
		
		// Stringarray von allen Leuchtmitteln mit jeweils den Werten id, X-Koordinate, Y-Koordinate
		ArrayList<int[]> luminaires = new ArrayList<int[]>();
		for (int i = 0; i < coordinates.size(); i++) {
			String[] strLuminaire = coordinates.get(i).split(MqttController.MSG_PART_SPLIT_CHARACTER);
			int[] intLuminaire = new int[3];
			for (int k = 0; k < intLuminaire.length; k++) {
				intLuminaire[k] = Integer.parseInt(strLuminaire[k]);
			}
			luminaires.add(intLuminaire);
		}
		
		Mat originalPicture = Imgcodecs.imdecode(new MatOfByte(picture), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
		int originalWidth = originalPicture.width();
		int originalHeight = originalPicture.height();
		System.out.println("Width: " + originalWidth);
		System.out.println("Height: " + originalHeight);
		
		// Erste Koordinate auslesen und die den min/max-Variablen zuweisen.
		int maxXCoordinate = luminaires.get(0)[X];
		int minXCoordinate = maxXCoordinate;
		int maxYCoordinate = luminaires.get(0)[Y];
		int minYCoordinate = maxYCoordinate;
		
		// Berechnet jeweils die max und min X- und Y-Koordinate.
		for (int i = 1; i < luminaires.size(); i++) {
			if (maxXCoordinate < luminaires.get(i)[X]) {
				maxXCoordinate = luminaires.get(i)[X];
			}
			else if (minXCoordinate > luminaires.get(i)[X]) {
				minXCoordinate = luminaires.get(i)[X];
			}
			if (maxYCoordinate < luminaires.get(i)[Y]) {
				maxYCoordinate = luminaires.get(i)[Y];
			}
			else if (minYCoordinate > luminaires.get(i)[Y]) {
				minYCoordinate = luminaires.get(i)[Y];
			}
		}
		double xScale = (double) (originalWidth) / ((double) (maxXCoordinate) - (double) (minXCoordinate) + 1);
		double yScale = (double) (originalHeight) / ((double) (maxYCoordinate) - (double) (minYCoordinate) + 1);

		ArrayList<String> newLuminaires = new ArrayList<String>();
		System.out.println("X: " + (maxXCoordinate-minXCoordinate));
		System.out.println("Y: " + (maxYCoordinate-minYCoordinate));
		System.out.println("Xscale: " + xScale);
		System.out.println("Yscale: " + yScale);

		// Ermittelt die Farbe zum jeweiligen Pixel. Skaliert dazu "unser" Darstellungsfläche,
		// welche aus Luminaires besteht, auf die Grösse des Bildes, welches geladen wurde.
		for (int i = 0; i < luminaires.size(); i++) {
			int haha = (int)  ((luminaires.get(i)[Y] - minYCoordinate) * yScale);
			haha =(int) ((luminaires.get(i)[X] - minXCoordinate) * xScale);

			double[] colors = originalPicture.get((int) ((luminaires.get(i)[Y] - minYCoordinate) * yScale), (int) ((luminaires.get(i)[X] - minXCoordinate) * xScale));
			String newLuminaire = luminaires.get(i)[0] + MqttController.MSG_PART_SPLIT_CHARACTER + "#" +
								getHexColor((int) colors[2]) + getHexColor((int) colors[1]) + getHexColor((int) colors[0]);
			newLuminaires.add(newLuminaire);
			System.out.println("r: " + colors[2]);
			System.out.println("g: " + colors[1]);
			System.out.println("b: " + colors[0]);
		}
		System.out.println(newLuminaires.toString());
		System.out.println("Anzahl LED: " + newLuminaires.size());
		return newLuminaires.toString();
	}
	
	private static String getHexColor(int color){
        return String.format("%02X", (0xFFFFFF & color));
    }
	// TODO: nur zu testzwecken um Matobjekt zu laden
	public Mat loadImage(String file) {
			File input = new File(file);
			BufferedImage image;
			try {
				image = ImageIO.read(input);
				// Here we convert into *supported* format
				BufferedImage imageCopy =
				    new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				imageCopy.getGraphics().drawImage(image, 0, 0, null);

				byte[] data = ((DataBufferByte) imageCopy.getRaster().getDataBuffer()).getData();  
				Mat img = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC3);
				img.put(0, 0, data);           
				Imgcodecs.imwrite(file, img);
				return img;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
				return null;
			}
	}
}
