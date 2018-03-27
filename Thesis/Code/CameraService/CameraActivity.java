package ch.bfh.bachelorthesis.ledmapper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Die Klasse CameraActivity wird vom AndroidSystem aufgerufen und startet den CameraActivityController. Ausserdem
 * teilt sie diesem mit in welchem Zustand sie ist.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 27.03.2018
 * @version 1.0
 */

public class CameraActivity extends Activity {

    // Foto machen inspiriert von: https://android.jlelse.eu/the-least-you-can-do-with-camera2-api-2971c8c81b8b
    // Constants
    private static int CAMERA_REQUEST_CODE = 200;

    // Membervariables
    private CameraActivityController cameraActivityController;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private TextureView cameraView;
    private Button btnCapture;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, this.CAMERA_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, this.CAMERA_REQUEST_CODE);

        setContentView(R.layout.activity_camera);
        this.cameraView = (TextureView) findViewById(R.id.camera_view);

        this.cameraActivityController = new CameraActivityController(this, this.cameraView);

        // TODO: weg wenn fertig.
        this.btnCapture = (Button) findViewById((R.id.btnCapture));
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraActivityController.takePicture();
            }
        });

        this.surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                try {
                    cameraActivityController.setUpCamera();
                    cameraActivityController.openCamera();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.cameraActivityController.activityOnStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.cameraActivityController.activityOnResume(this, this.cameraView, this.surfaceTextureListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.cameraActivityController.activityOnStop();
    }
}