package ch.bfh.bachelorthesis.ledmapper.controller.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.WindowManager;

import ch.bfh.bachelorthesis.ledmapper.controller.CameraActivityController;
import ch.bfh.bachelorthesis.ledmapper.R;
import ch.bfh.bachelorthesis.ledmapper.controller.MainController;

/**
 * Die Klasse CameraActivity wird vom AndroidSystem aufgerufen und startet den CameraActivityController. Ausserdem
 * teilt sie diesem mit in welchem Zustand sie ist.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 27.03.2018
 * @version 1.0
 */
public class CameraActivity extends AppCompatActivity {

    // Foto machen inspiriert von: https://android.jlelse.eu/the-least-you-can-do-with-camera2-api-2971c8c81b8b
    // Constants
    private static final int CAMERA_REQUEST_CODE = 200;

    // Membervariables
    private CameraActivityController cameraActivityController;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private TextureView cameraView;

    //Membervariablen
    private MainController mainController;
    private CameraActivityController controller;
    private Menu menu;

    private boolean viewInit = false;

    /**
     * Funktion wird aufgerufen, wenn die Activity erstellt wird.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
           initView();
        }

    }

    /**
     * Funktion wird aufgerufen kurz bevor die Ansicht dem Benutzer angezeigt wird.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if(this.viewInit){
                this.cameraActivityController.activityOnResume(this, this.cameraView, this.surfaceTextureListener);
            }
        }
    }


    /**
     * Funktion wird aufgerufen kurz bevor die Ansicht gestoppt wird.
     */
    @Override
    protected void onStop() {
        super.onStop();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if(this.viewInit){
                this.cameraActivityController.activityOnStop();
            }
        }
    }

    /**
     * Funktion wird aufgerufen, wenn eine Funktion (Bild laden oder Mapping Löschen) in der Topbar
     * angeklickt wird.
     *
     * @param item - Ausgewählte Funktion
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case(R.id.actionSettings):
                Intent settingsActivity = new Intent(CameraActivity.this, ConfigActivity.class);
                startActivity(settingsActivity);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Funktion setzt das entsprechende Menu in die Topbar der App.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_camera, menu);
        this.menu = menu;

        if(this.menu != null) {
            MenuItem item = this.menu.getItem(0);

            if (this.mainController != null && this.mainController.isMqttConnected()) {
                item.setIcon(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.service_available));
            } else {
                item.setIcon(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.service_not_available));
            }
        }

        return true;
    }

    /**
     * Funktion initialisiert die UI-Komponenten.
     */
    private void initView(){
        setContentView(R.layout.activity_camera);

        this.mainController = MainController.getInstance(this.getApplicationContext());
        this.controller = this.mainController.getCameraActivityController(this);

        this.cameraView = (TextureView) findViewById(R.id.camera_view);
        this.controller.setCameraView(this.cameraView);

        this.cameraActivityController = this.mainController.getCameraActivityController(this);
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.viewInit = true;
    }


    /**
     * Funktion wird aufgerufen wenn das Kamera-Permission geändert hat.
     * Wenn die Permission für die Kamera garantiert sind, wird die View initialisiert.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView();
                    this.onResume();
                }else{
                    this.finish();
                }
                return;
            }
        }
    }

    /**
     * Funktion wird aufgerufen, wenn die Verbindung zum MQTT-Broker abgebrochen wurde.
     */
    public void showNoConnection(){
        if(this.menu != null){
            MenuItem item = this.menu.getItem(0);
            item.setIcon(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.service_not_available));
        }
    }
}