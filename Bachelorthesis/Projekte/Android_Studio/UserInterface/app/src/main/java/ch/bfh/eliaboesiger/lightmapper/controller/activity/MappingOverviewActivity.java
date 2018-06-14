package ch.bfh.eliaboesiger.lightmapper.controller.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.R;
import ch.bfh.eliaboesiger.lightmapper.controller.MainController;
import ch.bfh.eliaboesiger.lightmapper.controller.MappingOverviewController;
import ch.bfh.eliaboesiger.lightmapper.controller.util.Utils;
import ch.bfh.eliaboesiger.lightmapper.model.Mapping;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;

/**
 * Die MappingOverviewActivity stellt die entsprechende Activity dar
 * und handelt bei den entsprechenden Events.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class MappingOverviewActivity extends AppCompatActivity {

    //Membervariablen
    private MainController mainController;
    private MappingOverviewController controller;

    private TextView sceneryId;
    private TextView sceneryName;
    private TextView mappingTitle;
    private TextView errorTextView;
    private FloatingActionButton newMappingButton;
    private LinearLayout layout;

    //Konstanten
    private static int MAPPING_BUTTON_HEIGHT_DP = 60;
    public static String EXTRA_ID_MAPPING = "id-mapping";
    public static String EXTRA_NAME_MAPPING = "name-mapping";

    /**
     * Funktion wird beim Erstellen der Activity aufgerufen.
     * Die Funktion initialisiert die Activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping_overview);

        this.sceneryId =  ((TextView) findViewById(R.id.mappingOverviewSceneryId));
        this.mappingTitle = ((TextView) findViewById(R.id.mappingTitle));
        this.newMappingButton = (FloatingActionButton) findViewById(R.id.addMappingButton);
        this.layout = (LinearLayout) findViewById(R.id.mappingsButtonLayout);
        this.sceneryName =  ((TextView) findViewById(R.id.mappingOverviewSceneryName));
        this.errorTextView = new TextView(getApplicationContext());

        this.mainController = MainController.getInstance(this.getApplicationContext());
        this.controller = this.mainController.getMappingOverviewController(this);

        setButtonEvents();
    }

    /**
     * Funktion wird aufgerufen kurz bevor die Ansicht dem Benutzer angezeigt wird.
     * Hier werden alle UI-Komponenten mit den Intent-Parameter abgefüllt.
     */
    @Override
    protected void onResume(){
        super.onResume();
        this.mainController.setActiveActivity(this);

        Intent intent = getIntent();

        this.sceneryId.setText(intent.getStringExtra(SceneryActivity.EXTRA_SCENERY_ID));
        this.sceneryName.setText(intent.getStringExtra(SceneryActivity.EXTRA_SCENERY_NAME));
        this.mappingTitle.setText(intent.getStringExtra(SceneryActivity.EXTRA_SCENERY_NAME)+": Szenerien");

        if(!intent.getBooleanExtra(SceneryActivity.EXTRA_CONNECTION_FAILED, false)){
            this.newMappingButton.setVisibility(View.VISIBLE);
            addMappingsToView();
        }else{
            this.newMappingButton.setVisibility(View.GONE);
            this.errorTextView.setText("Verbindung konnte nicht aufgebaut werden");
            this.layout.removeView(this.errorTextView);
            this.layout.addView(this.errorTextView);
        }
    }

    /**
     * Funktion setzt die Events auf den verfügbaren Buttons der Activity
     */
    private void setButtonEvents(){
        this.newMappingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MappingOverviewActivity.this);
                builder.setTitle("Neue Szenerie erstellen");

                final EditText input = new EditText(MappingOverviewActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                //Button-Event
                builder.setPositiveButton("Erstellen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer sceneryId = Integer.parseInt(MappingOverviewActivity.this.sceneryId.getText().toString());
                        MappingOverviewActivity.this.controller.addNewMapping(sceneryId, input.getText().toString());
                        addMappingsToView();
                    }
                });

                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    /**
     * Funktion fügt alle Mappings auf die Activity.
     */
    private void addMappingsToView(){
        this.layout.removeAllViews();

        Integer sceneryId = Integer.parseInt(MappingOverviewActivity.this.sceneryId.getText().toString());
        ArrayList<Mapping> mappings = this.controller.getAllMappings(sceneryId);

        if(mappings != null){
            for(final Mapping m : mappings){
                Button mappingButton = new Button(getApplicationContext());
                mappingButton.setText(m.getName());
                mappingButton.setTextColor(getResources().getColor(R.color.white));
                mappingButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttonColor)));
                int btnHeight = Utils.pxFromDp(getApplicationContext(), MAPPING_BUTTON_HEIGHT_DP);
                mappingButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, btnHeight));

                //Button-Event
                mappingButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MappingOverviewActivity.this, MappingDrawingActivity.class);
                        intent.putExtra(EXTRA_ID_MAPPING, m.getId().toString());
                        intent.putExtra(EXTRA_NAME_MAPPING, m.getName());
                        intent.putExtra(SceneryActivity.EXTRA_SCENERY_ID,  MappingOverviewActivity.this.sceneryId.getText().toString());
                        intent.putExtra(SceneryActivity.EXTRA_SCENERY_NAME, MappingOverviewActivity.this.sceneryName.getText().toString());
                        startActivity(intent);
                    }
                });

                this.layout.addView(mappingButton);
            }
        }else{
            TextView textView = new TextView(getApplicationContext());
            textView.setText("Keine Szenerien vorhanden");
            this.layout.addView(textView);
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
        String sceneryId = this.sceneryId.getText().toString();

        switch (item.getItemId()) {
            case R.id.actionSettings:
                if(sceneryId != null && !sceneryId.equals("")){
                    Intent newIntent = new Intent(MappingOverviewActivity.this, SceneryConfigActivity.class);
                    newIntent.putExtra(SceneryActivity.EXTRA_SCENERY_ID, sceneryId);
                    newIntent.putExtra(SceneryConfigActivity.STATUS_SCENERY_EXTRA, SceneryConfigActivity.STATUS_CONFIG_SCENERY);
                    startActivity(newIntent);
                }

                return true;

            case R.id.actionDelete:

                if(sceneryId != null && !sceneryId.equals("")){
                    this.controller.removeScenery(Integer.parseInt(sceneryId));

                    Intent deleteIntent = new Intent(MappingOverviewActivity.this, SceneryActivity.class);
                    startActivity(deleteIntent);
                }
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
        getMenuInflater().inflate(R.menu.menu_activity_mapping_overview, menu);
        return true;
    }

    /**
     * Funktion wird aufgerufen wenn der Benutzer die Android-Zurück-Taste drückt.
     * Aus der MappingOverviewActivity Activity kann der Benutzer nur in die Ansicht SceneryActivity
     * zurück.
     */
    @Override
    public void onBackPressed()
    {
        //back to scenery
        Intent intent = new Intent(MappingOverviewActivity.this, SceneryActivity.class);
        startActivity(intent);
    }
}
