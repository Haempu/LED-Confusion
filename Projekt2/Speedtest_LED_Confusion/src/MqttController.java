import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttController {

	
	private static final int QOS = 1;
	private MqttAsyncClient client;
	private MqttCallback callback = new MqttCallback() {
		
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			// TODO Auto-generated method stub
			//System.out.println("Arrived message: " + message.toString());
			MasterController.messageArrived(topic, message.toString());
		}
		
		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void connectionLost(Throwable arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void connectClient(String broker) throws MqttException {
		String clientId = MqttClient.generateClientId();
		this.client = new MqttAsyncClient(broker, clientId);
		System.out.println("Connecting client...");
		try {
			IMqttToken conToken;
            conToken = client.connect(this, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken arg0) {
					// TODO Auto-generated method stub
					System.out.println("Client connected");
					subscribe("led");
					
				}
				
				@Override
				public void onFailure(IMqttToken arg0, Throwable arg1) {
					// TODO Auto-generated method stub
					System.out.println("Connection failed");
				}
			});
			this.client.setCallback(this.callback);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnectClient() throws MqttException {
		this.client.disconnect();
	}
	
	public void subscribe(String topic) {
		try {
			this.client.subscribe(topic, QOS);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Subscribed to: " + topic);
	}
	
	public void publish(String topic, String payload) {
		byte[] encodedPayload = new byte[0];
		try {
			encodedPayload = payload.getBytes("UTF-8");
			MqttMessage message = new MqttMessage(encodedPayload);
			this.client.publish(topic, message);
		} catch (UnsupportedEncodingException | MqttException e) {
			e.printStackTrace();
		}
	}
	
	
	/*private MqttClient client;
	
	MqttController(String broker) throws MqttException{
		this.client = new MqttClient(broker, MqttClient.generateClientId());
	}
	
	private MqttCallback callback = new MqttCallback() {
		
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			// TODO Auto-generated method stub
			System.out.println("Arrived message: " + message.toString());
			MasterController.messageArrived(topic, message.toString());
			System.out.println("Nach message Arrived");
		}
		
		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub
			System.out.println("deliveryComplete");
			try {
				MqttController.this.publish("led", "done");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void connectionLost(Throwable cause) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void connectClient(String broker) throws MqttException{
		this.client.connect();
		this.client.setCallback(callback);
	}
	
	public void subscribeClient(String topic) throws MqttException{
		this.client.subscribe(topic);
		System.out.println("Subscribed to " + topic);
	}
	
	public void publish(String topic, String message) throws UnsupportedEncodingException, MqttException {
		System.out.println("publishing message: " + message);
		byte[] encodedPayload = new byte[0];
		encodedPayload = message.getBytes("UTF-8");
		MqttMessage MqttMessage = new MqttMessage(encodedPayload);
		System.out.println("before publishing");
		this.client.publish(topic, encodedPayload, 0, false);
		System.out.println("message published");
	}

	public void disconnectClient() throws MqttException {
		this.client.disconnect();
	}*/

}
