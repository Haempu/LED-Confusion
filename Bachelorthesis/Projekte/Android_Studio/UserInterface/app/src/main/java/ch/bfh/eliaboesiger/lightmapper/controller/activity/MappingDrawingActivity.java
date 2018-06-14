package ch.bfh.eliaboesiger.lightmapper.controller.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.R;
import ch.bfh.eliaboesiger.lightmapper.component.ColorPicker;
import ch.bfh.eliaboesiger.lightmapper.component.LuminaireButton;
import ch.bfh.eliaboesiger.lightmapper.controller.MainController;
import ch.bfh.eliaboesiger.lightmapper.controller.MappingDrawingController;
import ch.bfh.eliaboesiger.lightmapper.controller.listener.LEDTouchListener;
import ch.bfh.eliaboesiger.lightmapper.model.Luminaire;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;
import ch.bfh.eliaboesiger.lightmapper.model.Service;

/**
 * Die MappingDrawingActivity stellt die entsprechende Activity dar
 * und handelt bei den entsprechenden Events.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class MappingDrawingActivity extends AppCompatActivity {

    //Membervariabeln
    private MainController mainController;
    private MappingDrawingController controller;
    private final int SELECT_PHOTO = 1;
    private int screenWidth = 0;
    private  ArrayList<LuminaireButton> luminaires;

    //GUI-Komponenten
    private TextView sceneryId;
    private TextView mappingId;
    private TextView sceneryName;
    private TextView title;
    private ProgressBar progressBar;
    private View ledLayout;
    private ColorPicker colorPicker;
    private Switch onOffSwitch;

    /**
     * Funktion wird aufgerufen, wenn die Activity erstellt wird.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping_drawing);

        this.mainController = MainController.getInstance(this.getApplicationContext());
        this.controller = this.mainController.getMappingDrawingController(this);
        this.progressBar =  ((ProgressBar)findViewById(R.id.drawingingProgressBar));
        this.onOffSwitch = ((Switch)findViewById(R.id.onOffSwitch));
        this.progressBar.setVisibility(View.GONE);
        this.mappingId = ((TextView)findViewById(R.id.mappingDrawingId));
        this.sceneryId = ((TextView)findViewById(R.id.sceneryDrawingId));
        this.sceneryName = ((TextView)findViewById(R.id.sceneryDrawingName));
        this.title = ((TextView)findViewById(R.id.titleDrawing));
        this.ledLayout = findViewById(R.id.ledLayout);
    }

    /**
     * Funktion wird aufgerufen kurz bevor die Ansicht dem Benutzer angezeigt wird.
     * Hier werden alle UI-Komponenten mit den Intent-Parameter abgefüllt.
     */
    @Override
    protected void onResume(){
        super.onResume();

        this.mainController.setActiveActivity(this);

        String mappingId = getIntent().getStringExtra(MappingOverviewActivity.EXTRA_ID_MAPPING);
        String sceneryId = getIntent().getStringExtra(SceneryActivity.EXTRA_SCENERY_ID);
        String sceneryName = getIntent().getStringExtra(SceneryActivity.EXTRA_SCENERY_NAME);

        this.mappingId.setText(mappingId);
        this.sceneryId.setText(sceneryId);
        this.sceneryName.setText(sceneryName);
        this.title.setText(getIntent().getStringExtra(MappingOverviewActivity.EXTRA_NAME_MAPPING));

        this.colorPicker = (ColorPicker)findViewById(R.id.colorPicker);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.screenWidth = metrics.widthPixels;

       this.luminaires = this.controller.addLuminairesToView(Integer.parseInt(mappingId),
                (FrameLayout)this.ledLayout, colorPicker, (SeekBar) findViewById(R.id.brightnessValue), this.screenWidth);

       this.ledLayout.setOnTouchListener(new LEDTouchListener(this.luminaires, this.colorPicker,  this.onOffSwitch, this.getApplicationContext(), (SeekBar) findViewById(R.id.brightnessValue)));
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
        Integer mappingId = Integer.parseInt(((TextView)findViewById(R.id.mappingDrawingId)).getText().toString());

        switch (item.getItemId()) {
            case R.id.actionAddImageMapping:
                for(Service s : this.mainController.getAvailableServices()){
                    if(s.getStatus().equals(Service.STATUS_PICTURE_CONVERTING_SERVICE)){
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                        return true;
                    }

                }
                Toast.makeText(this, "Bildumwandlungs-Service nicht vorhanden", Toast.LENGTH_LONG).show();
                return true;
            case R.id.actionDeleteMapping:
                this.controller.clearAllLuminaires(this.luminaires);
                Integer sceneryId = MappingDrawingActivity.this.controller.deleteMapping(mappingId);
                Scenery scenery = this.controller.getSceneryWithId(sceneryId.toString());

                Intent deleteIntent = new Intent(MappingDrawingActivity.this, MappingOverviewActivity.class);
                deleteIntent.putExtra(SceneryActivity.EXTRA_SCENERY_ID, sceneryId.toString());
                deleteIntent.putExtra(SceneryActivity.EXTRA_SCENERY_NAME, scenery.getName());
                startActivity(deleteIntent);
                return true;

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_mapping_drawing, menu);
        return true;
    }

    /**
     * Funktion wird aufgerufen wenn der Benutzer ein Bild für die Darstellung auf der Lichterszeneri ausgewählt hat.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Integer mappingId = Integer.parseInt(((TextView)findViewById(R.id.mappingDrawingId)).getText().toString());

                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        this.controller.sendCoordinates(mappingId, selectedImage);
                        this.progressBar.setVisibility(View.VISIBLE);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * Funktion führt die onResume-Funktion erneut aus um alle Systemkomponenten neu abzufüllen.
     */
    public void reloadActivity(){
        onResume();
        this.progressBar.setVisibility(View.GONE);
    }

    /**
     * Funktion wird aufgerufen wenn der Benutzer die Android-Zurück-Taste drückt.
     * Aus der MappingDrawing Activity kann der Benutzer nur in die Ansicht MappingOverviewActivity
     * zurück.
     */
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(MappingDrawingActivity.this, MappingOverviewActivity.class);
        intent.putExtra(SceneryActivity.EXTRA_SCENERY_ID, this.sceneryId.getText().toString());
        intent.putExtra(SceneryActivity.EXTRA_SCENERY_NAME, this.sceneryName.getText().toString());
        intent.putExtra(SceneryActivity.EXTRA_CONNECTION_FAILED, false);
        startActivity(intent);
    }
}
