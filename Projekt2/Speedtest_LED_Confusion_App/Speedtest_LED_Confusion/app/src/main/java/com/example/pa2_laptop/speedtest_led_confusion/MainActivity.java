package com.example.pa2_laptop.speedtest_led_confusion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private Button btnStart;
    private Button btnStop;
    private MqttController mqttController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mqttController = new MqttController();
        this.btnStart = (Button) findViewById(R.id.btnStart);
        this.btnStop = (Button) findViewById(R.id.btnStop);

        this.mqttController.connect(this.getApplicationContext());


        this.btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                mqttController.startLed();
            }
        });

        this.btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                mqttController.stopLed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mqttController.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mqttController.disconnect();
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);


        // Connect
        // tcp://broker.hivemq.com:1883
        // 192.168.178.29
        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.178.29:1883", clientId);

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }
            @Override
            public void connectionLost(Throwable throwable) {

            }
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                //setMessageNotification(s, new String(mqttMessage.getPayload()));
                //txtOutput.setText("Message arrived");
                //txtOutput.setText("" + new String(mqttMessage.getPayload()));
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    //Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    //Log.d(TAG, "onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //txtOutput.setText("Juhu");

                // Subscribe
                // Subscribed man im "onCreate" scheint iwas noch nicht zu funktionieren. Die App stürzt dann immer ab.
                String topic = "led";
                int qos = 1;
                try {
                    IMqttToken subToken = client.subscribe(topic, qos);
                    subToken.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // The message was published
                            //txtOutput.setText("Subscribed");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken,
                                              Throwable exception) {
                            // The subscription could not be performed, maybe the user was not
                            // authorized to subscribe on the specified topic e.g. using wildcards

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                // Publish
                //String topic = "foo/bar38476";
                String payload = "the payload";
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }*/
}
