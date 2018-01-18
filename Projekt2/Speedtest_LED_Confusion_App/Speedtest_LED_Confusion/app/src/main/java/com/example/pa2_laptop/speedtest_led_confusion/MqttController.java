package com.example.pa2_laptop.speedtest_led_confusion;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

/**
 * Created by PA2-Laptop on 28.12.2017.
 */

public class MqttController {
    //private static final String BROKER = "tcp://192.168.178.29:1883";
    private static final String BROKER = "tcp://147.87.16.96:1883";
    private static final String TOPIC = "led";
    private static final int QOS = 1;
    private boolean running;
    private MqttAndroidClient client;
    private MqttCallback callback = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                if (payload.equals("done") && running) {
                    publish(TOPIC, "next");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        };

    public void connect(Context context) {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, BROKER, clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Connected");
                    subscribe(TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
            this.client.setCallback(this.callback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            IMqttToken disconToken = this.client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        }  catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            IMqttToken subToken = client.subscribe(topic, QOS);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("subscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("error on subscribing");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
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

    public void startLed() {
        this.running = true;
        this.publish(TOPIC, "next");
    }

    public void stopLed() {
        this.running = false;
    }
}
