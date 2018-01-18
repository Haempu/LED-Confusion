import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.NotConnectedException;

public class MasterController {
	//private static final String BROKER = "tcp://192.168.178.29:1883";
	private static final String BROKER = "tcp://147.87.16.96:1883";
	//private static final String BROKER = "tcp://iot.eclipse.org:1883";
	private static final String TOPIC = "led";
	private static MqttController mqttController;
	private static TinkerforgeController tfController;
	private static LedStripController lsController;
		
	/**
	 * Generates a connection with mqtt and tinkerforge
	 */
	public MasterController() {
		try {
			this.mqttController = new MqttController();
			this.mqttController.connectClient(BROKER);
			//this.mqttController.subscribeClient(TOPIC);
		} catch (MqttException e) {
			System.out.println("Mqtt could not be connected");
		}
	
		try {
			this.tfController = new TinkerforgeController();
			this.lsController = new LedStripController(tfController.ipcon);

		} catch (AlreadyConnectedException | IOException | NotConnectedException e) {
			System.out.println("IPConnection could not be started");
		}
		
		
		/*
		// TODO wo rufen wir das auf? Ist bis jetzt nur zu Testzwecken hier.
		try {
			this.mqttController.publish(TOPIC_TEMP, String.valueOf(tempController.getTemperature()));
		} catch (UnsupportedEncodingException | MqttException | TimeoutException | NotConnectedException e) {
			e.printStackTrace();
		}*/
	}

	public static void messageArrived(String topic, String message) {
		if (message.equals("next")) {
			lsController.controlLed(message);
			mqttController.publish(TOPIC, "done");
		}
		
		
	/*
		try {
			if (topic.equals(TOPIC_SWITCH)) {
				if (message.equals(ON)) {
					switchController.switchOn();
				}
				else if (message.equals(OFF)) {
					switchController.switchOff();
				}
			}
		} catch (NotConnectedException | TimeoutException e) {
			e.printStackTrace();
		}
		System.out.println("" + message);*/
	}
	
	/**
	 * Function handles when the programm is closed
	 */
	public void handleExit() {
		try {
			this.mqttController.disconnectClient();
			this.tfController.ipDisconnect();
		} catch (MqttException | NotConnectedException e) {
			System.out.println("Could not disconnect: " + e.getMessage());
		}
	}
}