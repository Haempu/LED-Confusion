package ch.bfh.lightmapper.ledstripservice.controller;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.BrickMaster;
import com.tinkerforge.BrickletLEDStrip;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

import ch.bfh.lightmapper.ledstripservice.model.Luminaire;

public class TinkerforgeController {
	// Konstanten
	public static final String COLOR_ON = "#FFFFFF";
	public static final String COLOR_OFF = "#000000";

	
	// Membervariablen
	private IPConnection ipConnection;
	private BrickMaster brickMaster;
	private BrickletLEDStrip brickletLedStrip;
	
	
	/**
	 * Stellt eine Verbindung mit den Tinkerforgekomponenten Masterbrick und LED-Stripbricklet her.
	 * @param masterBrickIp - IP-Adresse des Masterbricks
	 * @param masterBrickPort - Port für den Masterbrick
	 * @param masterBrickUid - Uid (vergeben von Tinkerforge) für den Masterbrick
	 * @param ledStripUid - Uid (vergeben von Tinkerforge) für den LED-Stripbricklet
	 * @throws NumberFormatException
	 * @throws UnknownHostException
	 * @throws AlreadyConnectedException
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws NotConnectedException
	 */
	public void connectToTinkerforge(String masterBrickIp, String masterBrickPort, String masterBrickUid, String ledStripUid) throws NumberFormatException, UnknownHostException, AlreadyConnectedException, IOException, TimeoutException, NotConnectedException {
		this.ipConnection = new IPConnection();
		this.ipConnection.connect(masterBrickIp, Integer.parseInt(masterBrickPort));
		this.brickMaster = new BrickMaster(masterBrickUid, this.ipConnection);
		
		this.brickletLedStrip = new BrickletLEDStrip(ledStripUid, this.ipConnection);
		this.brickletLedStrip.setChipType(BrickletLEDStrip.CHIP_TYPE_WS2801);
		this.brickletLedStrip.setChannelMapping(BrickletLEDStrip.CHANNEL_MAPPING_RGB);
		
		if((this.brickMaster.getConnectionType() == BrickMaster.CONNECTION_TYPE_NONE) || (!this.brickletLedStrip.getIdentity().uid.equals(ledStripUid))){
			throw new NotYetConnectedException();
		}
	}
	
	/**
	 * Trennt die Verbdinugn zu den Tinkerforgekomponenten.
	 */
	public void ipDisconnect() {
		if (this.ipConnection != null) {
			try {
				this.ipConnection.disconnect();
				this.ipConnection = null;
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Ändert die Einstellungen (Helligkeit und Farbe) eines einzelnen LEDs.
	 * @param luminaire - Luminaireobjekt des entsprechenden LEDs
	 * @throws TimeoutException
	 * @throws NotConnectedException
	 */
	public void changeLuminaireSettings(Luminaire luminaire) throws TimeoutException, NotConnectedException {
		short[] red = new short[16];
		short[] green = new short[16];
		short[] blue = new short[16];
		if (luminaire.isOn()) {
			double brightness = (double) (luminaire.getBrightness()) / 100;
			String color = luminaire.getColor();
			// Entfernt # im String der Farbe.
			color = color.substring(1);
			// Konvertierung ist so nötig, damit die Helligkeit (Wert zwischen 0 und 1) korrekt hinzugerechnet werden kann.
			red[0] = (short) ((double) (Short.parseShort(color.substring(0, 2), 16)) * brightness);
			green[0] = (short) ((double) (Short.parseShort(color.substring(2, 4), 16)) * brightness);
			blue[0] = (short) ((double) (Short.parseShort(color.substring(4, 6), 16)) * brightness);
		}
		else {
			red[0] = 0;
			green[0] = 0;
			blue[0] = 0;
		}
		this.brickletLedStrip.setRGBValues(luminaire.getUid(), (short) (1), red, green, blue);
	}
	
	/* TODO: Remove falls das mit dem auslesen der Farbe nicht geht.
	private RGBValues getPhysicalLuminaireColor(int luminaireNr) throws TimeoutException, NotConnectedException {
		RGBValues color = null;
		color = this.brickletLedStrip.getRGBValues(luminaireNr,  (short) (1));
		return color;
	}*/
}
