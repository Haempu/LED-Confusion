package ch.bfh.bachelorthesis.ledmapper.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.Collections;

/**
 * Die Klasse CameraController ist für den Zugriff auf die Hardware der Kamera zuständig.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 27.04.2018
 * @version 1.0
 */

public class CameraController {

    // Membervariables
    private CameraManager cameraManager;
    private int cameraFacing;
    private String cameraId;
    private Size previewSize;
    private HandlerThread backgroundThread;
    private android.os.Handler backgroundHandler;
    private CameraDevice.StateCallback stateCallback;
    private CameraDevice cameraDevice;
    private TextureView cameraView;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;
    private CameraCaptureSession cameraCaptureSession;
    private Context context;
    private int currentRotation;

    private ImageReader imageReader;

    /**
     * Konstruktor
     * @param context - Android-Context, in welchem die Kamera laufen soll.
     * @param cameraView - TextureView, welche darstellt, was von der Kamera aufgenommen wird.
     */
    public CameraController(Context context, TextureView cameraView) {
        this.context = context;
        this.cameraView = cameraView;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
        createBackgroundThread();
    }

    /**
     * Bereitet die Kamera für den Betrieb vor.
     * @throws CameraAccessException
     * @throws SecurityException
     */
    public void setUpCamera() throws CameraAccessException, SecurityException {
        openBackgroundThread();

        imageReader = ImageReader.newInstance(cameraView.getWidth(), cameraView.getHeight(), ImageFormat.YUV_420_888, 2);

        for (String cameraId : this.cameraManager.getCameraIdList()) {
            CameraCharacteristics cameraCharacteristics = this.cameraManager.getCameraCharacteristics(cameraId);
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == this.cameraFacing) {
                StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                this.previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                this.cameraId = cameraId;
            }
        }
    }

    /**
     * Öffnet eine betriebsbereite Kamera
     * @throws CameraAccessException
     * @throws SecurityException
     */
    public void openCamera() throws CameraAccessException, SecurityException {
        openBackgroundThread();
        this.cameraManager.openCamera(this.cameraId, this.stateCallback, this.backgroundHandler);
    }

    /**
     * Schliesst die Kamera
     */
    public void closeCamera() {
        if (this.cameraCaptureSession != null) {
            this.cameraCaptureSession.close();
            this.cameraCaptureSession = null;
        }

        if (this.cameraDevice != null) {
            this.cameraDevice.close();
            this.cameraDevice = null;
        }
        closeBackgroundThread();
    }

    /**
     * Erstellt eine Bitmapdatei aus dem aktuellen Stream der Kamera.
     * @return - Bitmap-Bild
     */
    public Bitmap takePicture() {
        return this.cameraView.getBitmap(112, 200);
    }

    /**
     * Öffnet einen Hintergrundthread und Handler, in welchem die Kamera läuft.
     */
    private void openBackgroundThread() {
        this.backgroundThread = new HandlerThread("camera_background_thread");
        this.backgroundThread.start();
        this.backgroundHandler = new android.os.Handler(backgroundThread.getLooper());
    }

    /**
     * Schliesst den Hintergrundthread und Handler, in welchem die Kamera läuft.
     */
    private void closeBackgroundThread() {
        if (this.backgroundHandler != null) {
            this.backgroundThread.quitSafely();
            this.backgroundThread = null;
            this.backgroundHandler = null;
        }
    }

    /**
     * Erstellt den expliziten Hintergrundthread, in welchem die Kamera läuft.
     */
    private void createBackgroundThread() {
        this.stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                CameraController.this.cameraDevice = cameraDevice;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                cameraDevice.close();
                CameraController.this.cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                cameraDevice.close();
                CameraController.this.cameraDevice = null;
            }
        };
    }

    /**
     * Erstellt einen Livestream der Kamera in der cameraView der CameraActivity.
     */
    private void createPreviewSession() {

        try {
            SurfaceTexture surfaceTexture = cameraView.getSurfaceTexture();
            assert surfaceTexture != null;
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            this.cameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null) {
                        return;
                    }
                    try {
                        captureRequest = captureRequestBuilder.build();
                        CameraController.this.cameraCaptureSession = cameraCaptureSession;
                        CameraController.this.cameraCaptureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, this.backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
