

import java.io.IOException;
import java.net.UnknownHostException;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.BrickMaster;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;

/**
 * The TinkerforgeController controls the connection and the methodes for the communication with tinkerforge.
 * 
 * @author Elia Bösiger, Patrik Aebischer, Rosalie Truong
 * @date 21.04.2017
 *
 */
public class TinkerforgeController {

	// membervariables
	private static final String MASTER_BRICK_UID = "62gerK";
	private static final String MASTER_BRICK_HOST = "localhost";
	private static final int MASTER_BRICK_PORT = 4223;
	IPConnection ipcon = new IPConnection(); // Create IP connection
	BrickMaster master = new BrickMaster(MASTER_BRICK_UID, ipcon);
	

	public TinkerforgeController() throws UnknownHostException, AlreadyConnectedException, IOException {
		ipConnect();
	}

	/**
	 * Function connects to the remote control switch
	 * 
	 * @throws UnknownHostException
	 * @throws AlreadyConnectedException
	 * @throws IOException
	 */
	private void ipConnect() throws UnknownHostException, AlreadyConnectedException, IOException {
		this.ipcon.connect(MASTER_BRICK_HOST, MASTER_BRICK_PORT); // Connect
	}

	/**
	 * Function connects to the remote control switch
	 * 
	 * 
	 * @throws NotConnectedException
	 */
	public void ipDisconnect() throws NotConnectedException {
		this.ipcon.disconnect();
	}

}
