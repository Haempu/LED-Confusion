package ch.bfh.bachelorthesis.ledmapper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by PA2-Laptop on 20.03.2018.
 */
/**
 * Die Klasse CameraActivityController ist die Kontrollerklasse zur CameraActivity. Sie koordiniert ausserdem die Zusammenarbeit zwischen der Kamera
 * selber und dem Mqtt-Client.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 27.03.2018
 * @version 1.0
 */
public class CameraActivityController {

    // Constants
    private static final String BROKER_IP = "tcp://192.168.178.29";
    private static final String BROKER_PORT = "1883";
    private static final String TOPIC_HEAD = "ch/bfh/bachelorthesis/ledmapper/cam/";
    private static final String TOPIC_INPUT = "ch/bfh/bachelorthesis/ledmapper/cam/input/";
    private static final String TOPIC_OUT_PICTURE = "ch/bfh/bachelorthesis/ledmapper/cam/output/picture/";
    private static final String TOPIC_OUT_CONFIGURATION = "ch/bfh/bachelorthesis/ledmapper/cam/output/configuration/";
    private static final String MSG_DO_PICTURE = "do picture";
    private static final String MSG_SERVICE_IS_HERE = "service is here";
    private static final String MSG_LASTWILL = "service is gone";
    private static final int QOS = 0;
    private static final int BITMAP_QUALITY = 100;

    // Membervariables
    private Context  context;
    private TextureView cameraView;
    private CameraController cameraController;
    private MqttController mqttController;
    private File galleryFolder;
    private MqttCallback mqttCallback;
    private IMqttActionListener mqttListener;

    /**
     * Konstrukter welcher ein MqttController-, sowie ein CameraController-Objekt erstellt. Er übernimmt ebenfalls den Context,
     * sowie die cameraView.
     * @param context - Android-Context der View.
     * @param cameraView - Stellt das Bild der Kamera für den Benutzer dar.
     */
    public CameraActivityController(Context context, TextureView cameraView) {
        this.context = context;
        this.cameraView = cameraView;

        // Mqtt
        this.mqttController = new MqttController(this.context, TOPIC_OUT_CONFIGURATION, MSG_LASTWILL);

        // Camera
        this.cameraController = new CameraController(context, cameraView);
    }

    /**
     * Damit der CameraActivityController weiss, wann sich die View "onStart" befindet.
     */
    public void activityOnStart() {
        this.mqttCallback = new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Message arrived_______________________");
                handleIncomingMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        };

        this.mqttListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                subscribeToAllTopics();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        };
        this.mqttController.connect(BROKER_IP + ":" + BROKER_PORT, this.mqttCallback, this.mqttListener);
    }

    /**
     * Damit der CameraActivityController weiss, wann sich die View "onResume" befindet.
     */
    public void activityOnResume(Context context, TextureView cameraView, TextureView.SurfaceTextureListener surfaceTextureListener) {
        if (this.cameraView.isAvailable()) {
            try {
                if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    this.cameraController.setUpCamera();
                    this.cameraController.openCamera();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            this.cameraView.setSurfaceTextureListener(surfaceTextureListener);
        }

    }

    /**
     * Damit der CameraActivityController weiss, wann sich die View "onStop" befindet.
     */
    public void activityOnStop() {
        this.mqttController.disconnect();
        this.cameraController.closeCamera();
    }

    /**
     * Subscribed den CameraService-Klient zu allen seinen Topics.
     */
    private void subscribeToAllTopics() {
        IMqttActionListener listener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                mqttController.publish(TOPIC_OUT_CONFIGURATION, MSG_SERVICE_IS_HERE);
                // TODO handle
                Toast.makeText(context, "An Topic angemeldet", Toast.LENGTH_SHORT);
                System.out.println("________________subscribed");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                // TODO handle
                Toast.makeText(context, "An Topic anmelden nicht möglich", Toast.LENGTH_SHORT);
                System.out.println("_________________error on subscribing");
            }
        };

        try {
            this.mqttController.subscribe(TOPIC_HEAD + "#", QOS, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /* TODO: Nur zu Testzwecken. Speichert Bild auf Telefon.
    private void createImageGallery() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        this.galleryFolder = new File(storageDirectory, this.context.getResources().getString(R.string.app_name));
        if (!this.galleryFolder.exists()) {
            boolean wasCreated = this.galleryFolder.mkdirs();
            if (!wasCreated) {
                Log.e("CapturedImages", "Failed to create directory");
            }
        }
    }*/

    /**
     * Behandelt einkommende Nachrichten entsprechend ihres Inhalts.
     * @param topic - Topic, auf welchem die Nachricht angekommen ist.
     * @param message - Inhalt der Nachricht.
     */
    private void handleIncomingMessage(String topic, MqttMessage message) {
        String msg = message.toString();
        if (msg.equals(this.MSG_DO_PICTURE)) {
            Bitmap picture = this.cameraController.takePicture();
            sendPicture(picture);
            System.out.println("picture sended_______________");
        } else if (msg.equals("test")) {
            this.mqttController.publish(TOPIC_OUT_CONFIGURATION, "test resp");
        }
    }

    /**
     * Sendet ein Bild auf dem entsprechenden Topic
     * @param picture - Bitmap-Bild
     */
    private void sendPicture(Bitmap picture) {
        //String pictureString = picture.toString();
        this.mqttController.publish(this.TOPIC_OUT_PICTURE, bitmapToString(picture));
    }

    /**
     * Verarbeitet eine Bitmapdatei in einen String, welcher über Mqtt verschickt werden kann.
     * @param picture - Bitmap-Bild.
     * @return - String des Bildes.
     */
    private String bitmapToString(Bitmap picture) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, this.BITMAP_QUALITY, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * TODO: Wird wohl nicht mehr benötigt
     */
    public void takePicture() {

        this.cameraController.lockPreview();
        Bitmap picture = this.cameraController.takePicture();
        FileOutputStream outputPhoto = null;
        try {
            outputPhoto = new FileOutputStream(createImageFile(this.galleryFolder));
            //picture.compress(Bitmap.CompressFormat.PNG, 100, outputPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.cameraController.unlockPreview();
            try {
                if (outputPhoto != null) {
                    outputPhoto.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Meldet dem CameraControler, dass dieser die Kamera betriebsbereit machen soll.
     * @throws CameraAccessException
     * @throws SecurityException
     */
    public void setUpCamera() throws CameraAccessException, SecurityException {
        this.cameraController.setUpCamera();
    }

    /**
     * Meldet dem CameraController, dass dieser die Kamera öffnen soll.
     * @throws CameraAccessException
     * @throws SecurityException
     */
    public void openCamera() throws CameraAccessException, SecurityException {
        this.cameraController.openCamera();
    }



    // TODO: Wird wohl nicht mehr beöntigt.
    private File createImageFile(File galleryFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "image_" + timeStamp + "_";
        return File.createTempFile(imageFileName, ".jpg", galleryFolder);
    }
}
