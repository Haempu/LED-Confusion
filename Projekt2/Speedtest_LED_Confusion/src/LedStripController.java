

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.BrickletLEDStrip;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

/**
 * The Class controlls the remote control switch. The switch control will
 * connect by initlaizing an object of this class. The class can set the switch
 * control to on or off.
 * 
 * @author Elia Bösiger, Patrik Aebischer, Rosalie Truong
 * @date 21.04.2017
 *
 */
public class LedStripController {

	private static final String LED_STRIP_UID = "wSj";
	private static final short SINGLE_LED_LENGTH = 1;
	private static final int MAX_NUMBER_LED = 50;
	private static short[] ON = {65,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	//private static short[] R_ON = {1};
	private static short[] OFF = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	private int actNumberLed = 0;
	private DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	private LocalTime oldTime = LocalTime.now();
	private boolean toOff = false;
	
	
	// membervariables
	BrickletLEDStrip ls;

	/**
	 * Constructor: SwitchController
	 * 
	 * @param ipcon
	 * @throws UnknownHostException
	 * @throws AlreadyConnectedException
	 * @throws IOException
	 * @throws NotConnectedException
	 */
	public LedStripController(IPConnection ipcon)
			throws UnknownHostException, AlreadyConnectedException, IOException, NotConnectedException {

		this.ls = new BrickletLEDStrip(LED_STRIP_UID, ipcon);
	}
	
	public void controlLed(String message) {
		// Kontrolliert Zeit
		if (actNumberLed == 0) {
			LocalTime actTime = LocalTime.now();
			System.out.println("Vergangene Zeit: " + Duration.between(oldTime, actTime).toMillis() + "ms");
			oldTime = actTime;
		}
		if (message.equals("next") && !toOff) {
			try {
				ls.setRGBValues(actNumberLed, SINGLE_LED_LENGTH, ON, OFF, OFF);
				//ls.setRGBValues(5, (short) 3, R_ON, R_ON, R_ON);
			} catch (NotConnectedException | TimeoutException e) {
				e.printStackTrace();
			}
			actNumberLed++;
			if (actNumberLed >= MAX_NUMBER_LED) {
				actNumberLed = 0;
				toOff = true;
			}
		}
		else if(message.equals("next") && toOff) {
			try {
				ls.setRGBValues(actNumberLed, SINGLE_LED_LENGTH, OFF, OFF, OFF);
			} catch (NotConnectedException | TimeoutException e) {
				e.printStackTrace();
			}
			actNumberLed++;
			if (actNumberLed >= MAX_NUMBER_LED) {
				actNumberLed = 0;
				toOff = false;
			}
		}
	}
	
	/**
	 * The function sets the remote control switch to on
	 * 
	 * @throws TimeoutException
	 * @throws NotConnectedException
	 */
	/*public void switchOn() throws TimeoutException, NotConnectedException {
		rs.switchSocketC(Configuration.REMOTE_CONTROL_SWITCH_TYPE, (short) 1, BrickletRemoteSwitch.SWITCH_TO_ON);
	}*/

	/**
	 * The function sets the remote control switch to off
	 * 
	 * @throws TimeoutException
	 * @throws NotConnectedException
	 */
	/*public void switchOff() throws TimeoutException, NotConnectedException {
		rs.switchSocketC(Configuration.REMOTE_CONTROL_SWITCH_TYPE, (short) 1, BrickletRemoteSwitch.SWITCH_TO_OFF);
	}*/
}