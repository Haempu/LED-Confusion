package ch.bfh.lightmapper.agentservice.view;

import java.util.ArrayList;

import ch.bfh.lightmapper.agentservice.controller.AgentController;
import ch.bfh.lightmapper.agentservice.controller.ConfigController;
import ch.bfh.lightmapper.agentservice.controller.MqttController;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Die Klasse "AgentView" ist für die Kontrolle und das Darstellen der View des Agent-Service zuständig.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class AgentView extends Application {
	
	// Konstanten
	public static final String TEXT_START = "Start";
	public static final String TEXT_STOP = "Stop";
	public static final String TEXT_BROKER_IP = "MQTT Broker IP";
	public static final String SPLIT_CHARACTER = ";";
	public static final String STATUS_MQTT_CONNECTED = "Mit Mqtt-Broker verbunden";
	public static final String STATUS_MQTT_DISCONNECTED = "Von Mqtt-Broker abgemeldet";
	public static final String STATUS_MQTT_CONNECTION_LOST = "Verbindung zu Mqtt-Broker verloren";
	public static final String STATUS_MQTT_SUBCSCRIBE_FINISHED = "An allen Mqtt-Topics angemeldet";
	public static final String STATUS_MQTT_SUBCSCRIBE_FAILURE = "Anmeldung an Mqtt-Topic fehlgeschlagen";
	public static final String STATUS_MQTT_CONNECTION_FAILURE = "Mqtt-Verbindungsfehler";
	public static final String STATUS_MQTT_FAILURE = "Allgemeiner MQTT-Fehler";
	public static final double VIEW_SPACING = 20;
	public static final double VIEW_WIDTH = 300;
	public static final double VIEW_HEIGHT = 300;
	public static final double VIEW_PREF_CONTENT_WIDTH = 280;
	public static final int VIEW_TIME_FOR_STOPPING_BROKER = 400;
	
	
	// Membervariablen
	private AgentController agentController;

	// UI-Komponenten
	private Stage primaryStage;
	private Button startStopButton = new Button(TEXT_START);
	private TextField brokerIpTextField = new TextField("0.0.0.0");
	private Text statusTitle = new Text("Status");
	private TextArea statusText = new TextArea();

	/**
	 * Funktion initialisiert die View und zeigt sie dem Benutzer an.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		this.agentController = new AgentController(this);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				agentController.handleExit();
			}
		}, "Shutdown-thread"));
			
		// View-Aufbau
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(MqttController.CLIENT_USERNAME);
		this.startStopButton.setMinWidth(VIEW_PREF_CONTENT_WIDTH);
		fillTextFields(ConfigController.readConfigFile());
		
		VBox fullLayout = new VBox(VIEW_SPACING);
		fullLayout.setPadding(new Insets(VIEW_SPACING, VIEW_SPACING, VIEW_SPACING, VIEW_SPACING));
		fullLayout.getChildren().addAll(new Text(TEXT_BROKER_IP), this.brokerIpTextField, this.startStopButton, this.statusTitle, this.statusText);
		
		StackPane root = new StackPane();
		root.getChildren().add(fullLayout);
		Scene scene = new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
		
		setButtonEvents();
		
		this.primaryStage.setScene(scene);
		this.primaryStage.setResizable(false);
		this.primaryStage.show();
	}
	
	/**
	 * Entscheidet, was bei welchem Button Event gemacht werden muss. Gibt Befehl zum Auslesen des Konfigurationsfiles
	 * und zum Starten oder Beenden des gesamten Services.
	 */
	private void setButtonEvents() {
		  this.startStopButton.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				switch(AgentView.this.startStopButton.getText()){
				case(TEXT_START):
					cleanStatus();
					AgentView.this.brokerIpTextField.setDisable(true);
					AgentView.this.agentController.startConnection(AgentView.this.brokerIpTextField.getText());
					ConfigController.writeConfigFile(TEXT_BROKER_IP + SPLIT_CHARACTER + AgentView.this.brokerIpTextField.getText());
					AgentView.this.startStopButton.setText(TEXT_STOP);
					break;
				case(TEXT_STOP):
					AgentView.this.brokerIpTextField.setDisable(false);
					AgentView.this.agentController.handleExit();
					AgentView.this.startStopButton.setDisable(true);
					try {
						Thread.sleep(VIEW_TIME_FOR_STOPPING_BROKER);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					AgentView.this.startStopButton.setText(TEXT_START);
					AgentView.this.startStopButton.setDisable(false);
					break;
				}
				
			}
		});
	}
	
	/**
	 * Füllt die Textfelder der View entsprechend dem Konfigurationsfeld ab.
	 * @param text - Array bestehend aus jeweils einer Zeile des Konfigurationsfiles
	 */
	private void fillTextFields(ArrayList<String> text){
		for (int i = 0; i < text.size(); i++) {
			if (text.get(i).contains(TEXT_BROKER_IP)) {
				this.brokerIpTextField.setText(text.get(i).substring(TEXT_BROKER_IP.length() + 1));
			}
		}
	}
	
	/**
	 * Löscht die bisherigen Ausgaben im Textfeld "Status".
	 */
	private void cleanStatus() {
		AgentView.this.statusText.clear();
	}
	
	/**
	 * Funktion setzt einen neuen Text in das Textfeld "Status". Dient zum Informieren des Benutzers über den Systemzustand.
	 * @param text - Auszugebender Text.
	 * @param fault - true: Übergebene Nachricht ist eine Fehlernachricht, sonst false.
	 */
	public void updateStatus(String text, boolean fault) {
		String error = "Info: \t";
		if (fault) {
			error = "Fehler:\t";
		}
		else {
			error = "Info: \t";
		}
		AgentView.this.statusText.appendText(error + text + "\n");
	}
	
	/**
	 * Funktion wird beim Starten der Applikation ausgeführt.
	 * @param args
	 */
	public static void  main(final String[] args) {
		launch(args);
	}
}
