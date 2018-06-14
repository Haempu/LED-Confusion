package ch.bfh.eliaboesiger.lightmapper.controller.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.R;
import ch.bfh.eliaboesiger.lightmapper.controller.MainController;
import ch.bfh.eliaboesiger.lightmapper.controller.SceneryController;
import ch.bfh.eliaboesiger.lightmapper.controller.util.Utils;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;

/**
 * Die SceneryActivity stellt die entsprechende Activity dar
 * und handelt bei den entsprechenden Events.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class SceneryActivity extends AppCompatActivity {

    //Membervariablen
    private MainController mainController;
    private SceneryController controller;
    private Scenery selectedScenery;

    private ProgressBar progressBar;
    private FloatingActionButton newSceneryButton;
    private LinearLayout layout;

    //Konstanten
    private static int SCENERY_BUTTON_HEIGHT_DP = 60;
    public static String EXTRA_SCENERY_ID = "id-scenery";
    public static String EXTRA_SCENERY_NAME = "name-scenery";
    public static String EXTRA_CONNECTION_FAILED = "connection-failed";

    /**
     * Funktion wird beim Erstellen der Activity aufgerufen.
     * Die Funktion initialisiert die Activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenery);

        this.progressBar = (ProgressBar) findViewById(R.id.sceneryProgressbar);
        this.newSceneryButton = (FloatingActionButton) findViewById(R.id.addSceneryButton);
        this.layout = (LinearLayout) findViewById(R.id.sceneryButtonLayout);

        this.mainController = MainController.getInstance(this.getApplicationContext());
        this.controller = this.mainController.getSceneryController(this);
        this.mainController.setActiveActivity(this);

        setButtonEvents();
    }

    /**
     * Funktion wird aufgerufen kurz bevor die Ansicht dem Benutzer angezeigt wird.
     * Hier werden alle UI-Komponenten mit den Intent-Parameter abgefüllt.
     */
    @Override
    protected  void onResume(){
        super.onResume();
        this.progressBar.setVisibility(View.GONE);

        this.selectedScenery = null;
        addSceneriesToView();
    }

    /**
     * Funktion setzt die Events auf den verfügbaren Buttons der Activity
     */
    private void setButtonEvents(){
        this.newSceneryButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SceneryActivity.this, SceneryConfigActivity.class);
                intent.putExtra(SceneryConfigActivity.STATUS_SCENERY_EXTRA, SceneryConfigActivity.STATUS_NEW_SCENERY);
                startActivity(intent);
            }
        });
    }

    /**
     * Funktion fügt alle Szenen auf die Activity.
     */
    private void addSceneriesToView(){
        this.layout.removeAllViews();

        ArrayList<Scenery> sceneries = this.controller.getAllSceneries();

        if(sceneries != null){
            for(final Scenery s : sceneries){
                Button sceneryButton = new Button(getApplicationContext());
                sceneryButton.setText(s.getName());
                sceneryButton.setTextColor(getResources().getColor(R.color.white));
                sceneryButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttonColor)));

                int btnHeight = Utils.pxFromDp(getApplicationContext(), SCENERY_BUTTON_HEIGHT_DP);
                sceneryButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, btnHeight));

                //Button-Event
                sceneryButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        SceneryActivity.this.selectedScenery = s;
                        if(!SceneryActivity.this.mainController.isMqttConnectedWithIp(s.getBrokerIp())){
                            SceneryActivity.this.progressBar.setVisibility(View.VISIBLE);
                            SceneryActivity.this.mainController.connect(s.getBrokerIp(), s.getBrokerPort());
                        }else{
                            showConnectionSucceed();
                        }
                    }
                });

                this.layout.addView(sceneryButton);
            }
        }else{
            TextView textView = new TextView(getApplicationContext());
            textView.setText("Keine Beleuchtungen vorhanden");
            this.layout.addView(textView);
        }
    }

    /**
     * Funktion wird bei einem erfolgreichen Verbindungsaufbau zum MQTT-Broker ausgeführt.
     * Die Funktion führt den Benutzer in die MappingOverviewActivity.
     */
    public void showConnectionSucceed() {
        this.progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(SceneryActivity.this, MappingOverviewActivity.class);
        intent.putExtra(EXTRA_SCENERY_ID, this.selectedScenery.getId().toString());
        intent.putExtra(EXTRA_SCENERY_NAME, this.selectedScenery.getName());
        intent.putExtra(EXTRA_CONNECTION_FAILED, false);
        this.selectedScenery = null;

        startActivity(intent);
    }

    /**
     * Funktion wird bei einem misslungenen Verbindungsaufbau zum MQTT-Broker ausgeführt.
     * Die Funktion führt den Benutzer in die MappingOverviewActivity mit dem Parameter
     * EXTRA_CONNECTION_FAILED = true, sodass die MappingOverviewActivity ausgeben kann, dass
     * keine Verbindung aufgebaut werden konnte.
     */
    public void showConnectionFailed(){
        this.progressBar.setVisibility(View.GONE);

        Intent intent = new Intent(SceneryActivity.this, MappingOverviewActivity.class);
        intent.putExtra(EXTRA_SCENERY_ID, this.selectedScenery.getId().toString());
        intent.putExtra(EXTRA_SCENERY_NAME, this.selectedScenery.getName());
        intent.putExtra(EXTRA_CONNECTION_FAILED, true);
        this.selectedScenery = null;

        startActivity(intent);

    }

    /**
     * Funktion wird aufgerufen wenn der Benutzer die Android-Zurück-Taste drückt.
     * Aus der SceneryActivity soll der Benutzer über den Zurück-Button nicht in eine andere
     * Activity gelangen.
     */
    @Override
    public void onBackPressed()
    {
        //do nothing!
    }
}
