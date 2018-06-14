package ch.bfh.bachelorthesis.ledmapper.controller.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import ch.bfh.bachelorthesis.ledmapper.R;
import ch.bfh.bachelorthesis.ledmapper.controller.MainController;
import ch.bfh.bachelorthesis.ledmapper.controller.util.Utils;

/**
 * Die Klasse ConfigActivity stellt die Einstellungs-Seite des Kamera-Services dar.
 * Die Activity beinhaltet eine TextView für die Eingabe der IP-Adresse und
 * ein Button um die Verbindung zum MQTT-Broker aufzubauen.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 10.05.2018
 * @version 1.0
 */
public class ConfigActivity extends AppCompatActivity {

    //Membervariablen
    private MainController mainController;
    private TextView ipTextView;
    private ProgressBar progressBar;
    private Button configButton;

    /**
     * Funktion wird aufgerufen, wenn die Activity erstellt wird.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        this.mainController = MainController.getInstance(this.getApplicationContext());
        this.ipTextView =  (TextView) findViewById(R.id.configIpAdress);
        this.progressBar = (ProgressBar) findViewById(R.id.configLoader);
        this.configButton = (Button) findViewById(R.id.configButton);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Funktion wird aufgerufen kurz bevor die Ansicht dem Benutzer angezeigt wird.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.progressBar.setVisibility(View.GONE);

        if(!this.mainController.isMqttConnected()) {
            this.configButton.setText("Verbinden");
            ((TextView) findViewById(R.id.configIpAdress)).setVisibility(View.VISIBLE);

            //Button-Event
            this.configButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (validateIp()) {
                        ConfigActivity.this.progressBar.setVisibility(View.VISIBLE);
                        ConfigActivity.this.mainController.connect(ConfigActivity.this.ipTextView.getText().toString());
                    }
                }
            });
        }else{
            this.ipTextView.setVisibility(View.GONE);
            this.configButton.setText("Verbindung beenden");
            //Button-Event
            this.configButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   ConfigActivity.this.mainController.disconnect();
                   onResume();
                }
            });
        }
    }

    /**
     * Funktion führt die Validierung der IP-Adresse durch.
     * Wenn die Validierung misslingt, wird ein Fehler auf der TextView ausgegeben.
     * @return
     */
    public boolean validateIp(){
        if(!Utils.validateIPAdress(this.ipTextView.getText().toString())){
            this.ipTextView.setError("Bitte geben Sie eine gültige IP-Adresse ein.");
            return false;
        }
        return true;
    }

    /**
     * Funktion wird aufgerufen, wenn die Verbindung zum MQTT-Broker aufgebaut werden konnte.
     */
    public void showConnectionSucceed(){
        this.progressBar.setVisibility(View.INVISIBLE);

        Intent cameraActivityInt = new Intent(ConfigActivity.this, CameraActivity.class);
        startActivity(cameraActivityInt);
    }

    /**
     * Funktion wird aufgerufen, wenn die Verbindung zum MQTT-Broker nicht aufgebaut werden konnte.
     */
    public void showConnectionFailed(){
        this.progressBar.setVisibility(View.GONE);

        this.ipTextView.setError("IP-Adresse ist nicht erreichbar.");
    }
}
