package ch.bfh.eliaboesiger.lightmapper.controller.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ch.bfh.eliaboesiger.lightmapper.R;
import ch.bfh.eliaboesiger.lightmapper.controller.MainController;
import ch.bfh.eliaboesiger.lightmapper.controller.MqttController;
import ch.bfh.eliaboesiger.lightmapper.controller.SceneryConfigController;
import ch.bfh.eliaboesiger.lightmapper.controller.util.Utils;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;
import ch.bfh.eliaboesiger.lightmapper.model.Service;


/**
 * Die SceneryConfigActivity stellt die entsprechende Activity dar
 * und handelt bei den entsprechenden Events.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class SceneryConfigActivity extends AppCompatActivity {

    //Membervariabeln
    private MainController mainController;
    private SceneryConfigController controller;
    private Scenery currentScenery;
    private int status;

    //GUI-Komponenten
    private Button saveButton;
    private Button checkConnectionButton;
    private TextView title;
    private TextView nameField;
    private TextView ipField;
    private ProgressBar progressBar;
    private TableLayout serviceLayoutTable;
    private TextView sceneryIdField;
    private Switch mappingOnOff;
    private AlertDialog.Builder builder;

    //Konstanten
    public static final String STATUS_SCENERY_EXTRA = "status-scenery";
    public static final int STATUS_NEW_SCENERY = 1;
    public static final int STATUS_CONFIG_SCENERY = 2;
    public static final String SAVE_BUTTON_START = "Erstellen und Mappen";
    public static final String SAVE_BUTTON_SAVE = "Speichern";
    public static final String SAVE_BUTTON_STOP = "Mapping stoppen";

    /**
     * Funktion wird aufgerufen, wenn die Activity erstellt wird.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenery_config);

        this.mainController = MainController.getInstance(this.getApplicationContext());
        this.controller = this.mainController.getSceneryConfigController(this);

        this.progressBar = ((ProgressBar) findViewById(R.id.sceneryConfigLoader));
        this.nameField = (TextView) findViewById(R.id.sceneryConfigName);
        this.ipField = (TextView) findViewById(R.id.sceneryConfigIpAdress);
        this.title = (TextView) findViewById(R.id.sceneryConfigTitle);
        this.saveButton = (Button) findViewById(R.id.sceneryConfigButton);
        this.checkConnectionButton = (Button) findViewById(R.id.sceneryConfigCheckConnection);
        this.serviceLayoutTable = (TableLayout)findViewById(R.id.sceneryConfigServiceLayout);
        this.sceneryIdField = (TextView) findViewById(R.id.sceneryConfigId);
        this.mappingOnOff = (Switch) findViewById(R.id.mappingOnOff);
    }

    /**
     * Funktion wird aufgerufen kurz bevor die Ansicht dem Benutzer angezeigt wird.
     * Hier werden alle UI-Komponenten mit den Intent-Parameter abgefüllt.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.mainController.setActiveActivity(this);
        enableScreen();

        this.progressBar.setVisibility(View.GONE);

        Intent intent = getIntent();

        this.status = intent.getIntExtra(STATUS_SCENERY_EXTRA, 1);
        final String sceneryId = intent.getStringExtra(SceneryActivity.EXTRA_SCENERY_ID);

        setConnectionEvent();

        if (this.status == STATUS_NEW_SCENERY) {
            this.saveButton.setText(SAVE_BUTTON_START);
            this.title.setText("Neue Beleuchtung");
            this.saveButton.setVisibility(View.GONE);
            this.mappingOnOff.setVisibility(View.GONE);

            //Button-Event
            this.saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(SceneryConfigActivity.this.saveButton.getText().toString().equals(SAVE_BUTTON_START)){
                        SceneryConfigActivity.this.currentScenery = validateActivity(false, true);

                        if (SceneryConfigActivity.this.currentScenery != null) {
                            SceneryConfigActivity.this.mainController.publish(MqttController.TPC_UI_OUT_MAPPING, MqttController.MSG_UI_START_MAPPING);
                            int sceneryId = SceneryConfigActivity.this.controller.addNewScenery(SceneryConfigActivity.this.currentScenery);
                            SceneryConfigActivity.this.currentScenery.setId(sceneryId);
                            SceneryConfigActivity.this.sceneryIdField.setText(Integer.toString(sceneryId));
                            SceneryConfigActivity.this.progressBar.setVisibility(View.VISIBLE);
                            SceneryConfigActivity.this.saveButton.setText(SAVE_BUTTON_STOP);

                            SceneryConfigActivity.this.disableScreen();
                        }
                    }else{
                        SceneryConfigActivity.this.mainController.publish(MqttController.TPC_UI_OUT_MAPPING, MqttController.MSG_UI_STOP_MAPPING);
                        int sceneryId = SceneryConfigActivity.this.controller.addNewScenery(SceneryConfigActivity.this.currentScenery);
                        SceneryConfigActivity.this.currentScenery.setId(sceneryId);
                        SceneryConfigActivity.this.sceneryIdField.setText(Integer.toString(sceneryId));
                        SceneryConfigActivity.this.progressBar.setVisibility(View.VISIBLE);
                        SceneryConfigActivity.this.saveButton.setText(SAVE_BUTTON_START);
                    }


                }
            });

        } else if (this.status == STATUS_CONFIG_SCENERY) {
            this.saveButton.setText(SAVE_BUTTON_SAVE);
            this.title.setText("Beleuchtung bearbeiten");
            this.mappingOnOff.setVisibility(View.VISIBLE);

            fillFormular(sceneryId);
            //addAdapterToSpinners();
            showAvailabilityOfServices();

            if(this.controller.isMqttConnectedWithIp(this.ipField.getText().toString())){
                this.checkConnectionButton.setVisibility(View.GONE);
                this.serviceLayoutTable.setVisibility(View.VISIBLE);
                this.mappingOnOff.setVisibility(View.VISIBLE);
            }else{
                this.checkConnectionButton.setVisibility(View.VISIBLE);
                this.serviceLayoutTable.setVisibility(View.GONE);
                this.mappingOnOff.setVisibility(View.GONE);
            }

            //Button-Event
            this.saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SceneryConfigActivity.this.currentScenery = validateActivity(false, false);

                    if (SceneryConfigActivity.this.currentScenery != null) {
                        SceneryConfigActivity.this.controller.updateScenery(SceneryConfigActivity.this.currentScenery);

                        if(SceneryConfigActivity.this.mappingOnOff.isChecked()){

                            if(SceneryConfigActivity.this.saveButton.getText().toString().equals(SAVE_BUTTON_SAVE)){
                                SceneryConfigActivity.this.mainController.publish(MqttController.TPC_UI_OUT_MAPPING, MqttController.MSG_UI_START_MAPPING);
                                SceneryConfigActivity.this.progressBar.setVisibility(View.VISIBLE);
                                SceneryConfigActivity.this.saveButton.setText(SAVE_BUTTON_STOP);
                                SceneryConfigActivity.this.disableScreen();
                            }else{
                                SceneryConfigActivity.this.mainController.publish(MqttController.TPC_UI_OUT_MAPPING, MqttController.MSG_UI_STOP_MAPPING);
                                SceneryConfigActivity.this.progressBar.setVisibility(View.VISIBLE);
                                SceneryConfigActivity.this.saveButton.setText(SAVE_BUTTON_SAVE);
                            }
                        }else{
                            if(SceneryConfigActivity.this.controller.isMqttConnectedWithIp(SceneryConfigActivity.this.ipField.getText().toString())){
                                Intent intent = new Intent(SceneryConfigActivity.this, MappingOverviewActivity.class);
                                intent.putExtra(SceneryActivity.EXTRA_SCENERY_ID, SceneryConfigActivity.this.sceneryIdField.getText().toString());
                                intent.putExtra(SceneryActivity.EXTRA_SCENERY_NAME, SceneryConfigActivity.this.currentScenery.getName());
                                startActivity(intent);
                            }else{
                                handleConnecting();
                            }

                        }
                    }
                }
            });
        }
    }

    /**
     * Funktion holt die Zustände der Services und des Agents und zeigt an, welche Services/Agent
     * vorhanden sind und welche nicht.
     */
    public void showAvailabilityOfServices() {

       // addAdapterToSpinners();
        List<Service> services = this.controller.getAllServices();
        System.out.println("services: "+services.size());
        boolean agentServiceAvailable = false;
        boolean cameraServiceAvailable = false;
        boolean luminaireServiceAvailable = false;
        boolean dataServiceAvailable = false;

        try {

            for (Service s : services) {
                if (s.getStatus().equals(Service.STATUS_AGENT)) {
                    if (s.isAvailable()) {
                        agentServiceAvailable = true;
                    }
                } else if (s.getStatus().equals(Service.STATUS_CAMERA_SERVICE)) {
                    if (s.isAvailable()) {
                        cameraServiceAvailable = true;
                    }
                } else if (s.getStatus().equals(Service.STATUS_LUMINAIRE_SERVICE)) {
                    if (s.isAvailable()) {
                        luminaireServiceAvailable = true;
                    }
                } else if (s.getStatus().equals(Service.STATUS_DATA_SERVICE)) {
                    if (s.isAvailable()) {
                        dataServiceAvailable = true;
                    }
                }
            }

            ImageView luminaireAvailableImage = (ImageView) findViewById(R.id.availableImageLedService);
            ImageView cameraAvailableImage = (ImageView) findViewById(R.id.availableImageCameraService);
            ImageView dataServiceAvailableImage = (ImageView) findViewById(R.id.availableImageDataService);
            ImageView agentAvailableImage = (ImageView) findViewById(R.id.availableImageAgent);

            if (cameraServiceAvailable) {
                cameraAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_available));
            } else {
                cameraAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_not_available));
            }

            if (luminaireServiceAvailable) {
                luminaireAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_available));
            } else {
                luminaireAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_not_available));
            }

            if (dataServiceAvailable) {
                dataServiceAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_available));

            } else {
                dataServiceAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_not_available));
            }

            if(agentServiceAvailable) {
                agentAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_available));
            }else{
                agentAvailableImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.service_not_available));
            }


        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Funktion validiert das Formular und gibt die Fehlermeldungen zurück.
     *
     * @return
     */
    public Scenery validateActivity(boolean forConnection, boolean newScenery) {
        boolean error = false;

        if (this.nameField.getText().toString().replaceAll("\\s+", "").equals("")) {
            this.nameField.setError("Name der Beleuchtung darf nicht leer sein.");
            error = true;
        }

        if (!Utils.validateIPAdress(this.ipField.getText().toString())) {
            this.ipField.setError("Bitte geben Sie eine gültige IP-Adresse ein.");
            error = true;
        }

        if(newScenery && !forConnection && !this.controller.allServicesAvailable()){
            Toast.makeText(this, "Nicht alle Services vorhanden", Toast.LENGTH_LONG).show();
            error = true;
        }

        if (!error) {
            Scenery scenery = new Scenery();
            scenery.setName(this.nameField.getText().toString());
            scenery.setBrokerIp(this.ipField.getText().toString());
            scenery.setBrokerPort(Scenery.PORT);

            if (!this.sceneryIdField.getText().toString().equals("")) {
                scenery.setId(Integer.parseInt(this.sceneryIdField.getText().toString()));
            }

            return scenery;
        }

        return null;
    }

    /**
     * Funktion füllt das Formular wenn eine scceneryId vorhanden ist.
     *
     * @param sceneryId
     */
    private void fillFormular(String sceneryId) {
        this.currentScenery = this.controller.getSceneryWithId(sceneryId);

        this.nameField.setText(this.currentScenery.getName());
        this.ipField.setText(this.currentScenery.getBrokerIp());
        this.sceneryIdField.setText(this.currentScenery.getId().toString());
    }

    private void setConnectionEvent() {
        //Button-Event
        this.checkConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SceneryConfigActivity.this.progressBar.setVisibility(View.INVISIBLE);
                SceneryConfigActivity.this.serviceLayoutTable.setVisibility(View.INVISIBLE);
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(SceneryConfigActivity.this.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if (validateActivity(true, true) != null) {
                    SceneryConfigActivity.this.handleConnecting();
                }
            }
        });
    }

    /**
     * Funktion handelt den Verbindungsaufbau. Wenn bereits eine Verbindung zum eingegebenen
     * MQTT-Broker aufgebaut ist, kann diese verwendet werden. Bei der Eingabe eines neuen
     * MQTT-Brokers wird eine neue Verbindung aufgebaut.
     * Beim Warten auf einen Verbindungsaufbau wird eine ProgressBar dargestellt.
     */
    private void handleConnecting(){
        String ip = this.ipField.getText().toString();

        if(!this.mainController.isMqttConnectedWithIp(ip)) {
            this.mainController.connect(ip, Scenery.PORT);
            this.serviceLayoutTable.setVisibility(View.GONE);
            this.progressBar.setVisibility(View.VISIBLE);

            if(this.status == STATUS_CONFIG_SCENERY){
                this.mappingOnOff.setVisibility(View.VISIBLE);
            }
        }else{
            showConnectionSucceed();
        }
    }

    /**
     * Funktion wird aufgerufen wenn der Verbindungsaufbau zum eingegebenen MQTT-Broker
     * erfolgreich war. Die Funktion stellt die Services dar.
     */
    public void showConnectionSucceed(){
        showAvailabilityOfServices();
        this.serviceLayoutTable.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.GONE);
        this.saveButton.setVisibility(View.VISIBLE);
        this.checkConnectionButton.setVisibility(View.GONE);
    }

    /**
     * Funktion wird aufgerufen wenn ein Verbindungsaufbau zum MQTT-Broker misslungen ist.
     * Es wird eine entsprechende Fehlermeldung ausgegeben.
     */
    public void showConnectionFailed(){
        if(this.status == STATUS_NEW_SCENERY){
            this.ipField.setError("IP-Adresse ist nicht erreichbar");
            this.progressBar.setVisibility(View.GONE);
            this.serviceLayoutTable.setVisibility(View.GONE);
        }else{
            Intent intent = new Intent(SceneryConfigActivity.this, MappingOverviewActivity.class);
            intent.putExtra(SceneryActivity.EXTRA_SCENERY_ID, this.sceneryIdField.getText().toString());
            intent.putExtra(SceneryActivity.EXTRA_SCENERY_NAME, SceneryConfigActivity.this.currentScenery.getName());
            intent.putExtra(SceneryActivity.EXTRA_CONNECTION_FAILED, true);
            startActivity(intent);
        }
    }

    /**
     * Funktion liefert die aktuelle Identifikation einer Beleuchtung zurück.
     * @return - Identifikation der Beleuchtung
     */
    public int getSceneryId(){
       return Integer.parseInt(this.sceneryIdField.getText().toString());
    }

    /**
     * Funktion wird aufgerufen wenn ein Mapping-Vorgang abgeschlossen wurde.
     * Die Funktion zeigt dem Benutzer ein Dialog mit den Anzahl erkannten Leuchtquellen.
     *
     * @param cntLuminaires - Anzahl erkannte Leuchtquellen des Mapping-Vorgangs
     */
    public void showMappingFinished(int cntLuminaires){

        this.builder = new AlertDialog.Builder(SceneryConfigActivity.this);
        this.builder.setTitle("Mapping abgeschlossen");

        final TextView text = new TextView(SceneryConfigActivity.this);
        text.setPadding(70,50,0,0);
        text.setText(cntLuminaires+" Leuchtquellen gefunden.");
        this.builder.setView(text);

        //Button-Event
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SceneryConfigActivity.this, MappingOverviewActivity.class);
                intent.putExtra(SceneryActivity.EXTRA_SCENERY_ID, SceneryConfigActivity.this.sceneryIdField.getText().toString());
                intent.putExtra(SceneryActivity.EXTRA_SCENERY_NAME, SceneryConfigActivity.this.currentScenery.getName());
                startActivity(intent);
            }
        });

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SceneryConfigActivity.this.progressBar.setVisibility(View.GONE);
                SceneryConfigActivity.this.builder.show();
            }
        });
    }

    /**
     * Funktion deaktiviert die UI-Komponenten während dem Mapping-Vorgang.
     */
    public void enableScreen(){
        this.nameField.setEnabled(true);
        this.ipField.setEnabled(true);
        this.serviceLayoutTable.setEnabled(true);
        this.saveButton.setEnabled(true);
    }

    /**
     * Funktion aktiviert die UI-Komponenten nach dem Mapping-Vorgang.
     */
    public void disableScreen(){
        this.nameField.setEnabled(false);
        this.ipField.setEnabled(false);
        this.serviceLayoutTable.setEnabled(false);
    }

    /**
     * Funktion wird aufgerufen wenn eine Verbindung abgebrochen wurde.
     * Die Services werden ausgeblente und der Benutzer muss sich erneut mit einem MQTT-Broker
     * verbinden.
     */
    public void showConnectionLost(){
        this.ipField.setError("IP-Addresse nicht mehr erreichbar.");
        this.serviceLayoutTable.setVisibility(View.GONE);
        this.checkConnectionButton.setVisibility(View.VISIBLE);
        this.saveButton.setVisibility(View.GONE);
    }

    /**
     * Funktion wird aufgerufen wenn der Benutzer die Android-Zurück-Taste drückt.
     * Aus der SceneryConfigActivity kann der Benutzer bei einer neuen Beleuchtung in die Ansicht
     * SceneryActivity zurück und bei einer Konfiguration einer bestehenden Beleuchtung in die
     * Ansicht MappingOverviewActivity.
     */
    @Override
    public void onBackPressed()
    {
       if(this.progressBar.getVisibility() != View.VISIBLE){
           if(this.status == STATUS_CONFIG_SCENERY){
               //back to mapping overview
               Intent intent = new Intent(SceneryConfigActivity.this, MappingOverviewActivity.class);
               intent.putExtra(SceneryActivity.EXTRA_SCENERY_ID, this.sceneryIdField.getText().toString());
               intent.putExtra(SceneryActivity.EXTRA_SCENERY_NAME, SceneryConfigActivity.this.currentScenery.getName());
               startActivity(intent);
           }else{
               //back to scenery
               Intent intent = new Intent(SceneryConfigActivity.this, SceneryActivity.class);
               startActivity(intent);
           }
       }
    }
}