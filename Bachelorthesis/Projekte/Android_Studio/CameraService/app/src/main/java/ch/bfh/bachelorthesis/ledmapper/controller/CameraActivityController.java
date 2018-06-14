package ch.bfh.bachelorthesis.ledmapper.controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.support.v4.app.ActivityCompat;
import android.view.TextureView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;

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
    public static final int BITMAP_QUALITY = 100;

    // Membervariables
    private Context  context;
    private TextureView cameraView;
    private CameraController cameraController;
    private MqttController mqttController;
    private MqttCallbackExtended mqttCallback;

    /**
     * Konstrukter welcher ein MqttController-, sowie ein CameraController-Objekt erstellt. Er übernimmt ebenfalls den Context,
     * sowie die cameraView.
     * @param context - Android-Context der View.
     */
    public CameraActivityController(Context context) {
        this.context = context;
        this.mqttController = new MqttController();
    }

    /**
     * Funktion setzt die Instanz der CameraView auf den Controller.
     * @param cameraView
     */
    public void setCameraView(TextureView cameraView){
        this.cameraView = cameraView;
        this.cameraController = new CameraController(this.context, cameraView);
    }

    /**
     * Damit der CameraActivityController weiss, wann sich die View "onResume" befindet.
     */
    public void activityOnResume(Context context, TextureView cameraView, TextureView.SurfaceTextureListener surfaceTextureListener) {
        System.out.println("onResume___________");
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
        this.cameraController.closeCamera();
    }

    /**
     * Funktion liefert den CameraController zurück.
     * @return
     */
    public CameraController getCameraController() {
        return this.cameraController;
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
}
