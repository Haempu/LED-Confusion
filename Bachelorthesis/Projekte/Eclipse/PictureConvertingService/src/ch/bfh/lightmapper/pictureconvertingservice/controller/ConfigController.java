package ch.bfh.lightmapper.pictureconvertingservice.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ConfigController {
	
	private static final String IP_DEFAULT = "0.0.0.0";
	private static final String PROJECT_DIR = System.getProperty("user.dir");
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String CONFIG_DIR = PROJECT_DIR+FILE_SEPARATOR+"config"+FILE_SEPARATOR;
	public static final String CONFIG_FILE = "config.txt";

	
	public static String readIpAddress(){
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
				return input.nextLine();
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return IP_DEFAULT;
		
	}
	
	/**
	 * Überschreibt das alte Konfigurationsfile.
	 * @param configFile - Konfigurationsfile als String
	 */
	public static void writeIpAddress(String ip){
		try {
			File configDir = new File(CONFIG_DIR);
			configDir.mkdirs();

			FileWriter fw = new FileWriter(CONFIG_DIR+CONFIG_FILE);
			fw.write(ip);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		
	}

}
