package ch.bfh.lightmapper.dataprocessingservice.controller;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Die Klasse "ConfigController" ist für das korrekte Speichern und Auslesen des Konfigurationsfiles zuständig.
 * Sollte kein solches File vorhanden sein, erstellt diese Klasse einen neuen Pfad mit dem entsprechenden File.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class ConfigController {
	
	private static final String PROJECT_DIR = System.getProperty("user.dir");
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String CONFIG_DIR = PROJECT_DIR+FILE_SEPARATOR+"config"+FILE_SEPARATOR;
	public static final String CONFIG_FILE = "config.txt";

	/**
	 * Liest das Konfigurationsfile aus.
	 * @return - Konfigurationsfile als StringArray
	 */
	public static ArrayList<String> readConfigFile() {
		ArrayList<String> returnArr = new ArrayList<String>();
		File configDir = new File(CONFIG_DIR);
		configDir.mkdirs();
		
		File configFile = new File(CONFIG_DIR+CONFIG_FILE);
		
		Scanner input;
		try {
			if(!configFile.exists()){
				FileWriter fw = new FileWriter(CONFIG_DIR+CONFIG_FILE);
				fw.close();
			}
			
			input = new Scanner(new File(CONFIG_DIR+CONFIG_FILE));

			while(input.hasNextLine()){
				returnArr.add(input.nextLine());
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnArr;
		
	}
	
	/**
	 * Überschreibt das alte Konfigurationsfile.
	 * @param configFile - Konfigurationsfile als String
	 */
	public static void writeConfigFile(String configFile) {
		try {
			File configDir = new File(CONFIG_DIR);
			configDir.mkdirs();

			FileWriter fw = new FileWriter(CONFIG_DIR+CONFIG_FILE);
			fw.write(configFile);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
