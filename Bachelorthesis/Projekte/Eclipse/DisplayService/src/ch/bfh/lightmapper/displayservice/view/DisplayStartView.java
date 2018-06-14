package ch.bfh.lightmapper.displayservice.view;

import java.util.ArrayList;

import ch.bfh.lightmapper.displayservice.controller.CommunicationController;
import ch.bfh.lightmapper.displayservice.controller.ConfigController;
import ch.bfh.lightmapper.displayservice.controller.MqttController;
import javafx.application.Application;
import javafx.application.Platform;
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
 * Die Klasse "DisplayStartView" stellt die Verbindung zum eingegeben MQTT-Broker her.
 * Bei einem erfolgreichen Verbindungsaufbau wird die "SceneryView" angezeigt.
 * 
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class DisplayStartView extends Application {

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
	private CommunicationController communicationController;
	private SceneryView sceneryView;
	
	// UI-Komponenten
	private Stage primaryStage;
	private Button startStopButton = new Button(TEXT_START);
	private TextField brokerIpTextField = new TextField("0.0.0.0");
	private Text statusTitle = new Text("Status");
	private TextArea statusText = new TextArea();

	/**
	 * Funktion initialisiert und statet die Ansicht.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		this.communicationController = CommunicationController.getInstance();
		this.sceneryView = new SceneryView(this);
		this.communicationController.setDisplayStartView(this);
		this.communicationController.setSceneryView(this.sceneryView);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				communicationController.handleExit();
			}
		}, "Shutdown-thread"));

		// View-Aufbau
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(MqttController.CLIENT_USERNAME);
		this.startStopButton.setMinWidth(VIEW_PREF_CONTENT_WIDTH);
		fillTextFields(ConfigController.readConfigFile());

		VBox fullLayout = new VBox(VIEW_SPACING);
		fullLayout.setPadding(new Insets(VIEW_SPACING, VIEW_SPACING, VIEW_SPACING, VIEW_SPACING));
		fullLayout.getChildren().addAll(new Text(TEXT_BROKER_IP), this.brokerIpTextField, this.startStopButton,
				this.statusTitle, this.statusText);

		StackPane root = new StackPane();
		root.getChildren().add(fullLayout);
		Scene scene = new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);

		setButtonEvents();
		setExitEvent();

		this.primaryStage.setScene(scene);
		this.primaryStage.setResizable(false);
		this.primaryStage.show();
		

	}
	
	/**
	 * Funktion fängt Event ab, wenn der Benutzer eine View schliesst.
	 */
	private void setExitEvent() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				DisplayStartView.this.communicationController.handleExit();
			}
		}, "Shutdown-thread"));
	}

	/**
	 * Entscheidet, was bei welchem Button Event gemacht werden muss. Gibt
	 * Befehl zum Auslesen des Konfigurationsfiles und zum Starten oder Beenden
	 * des gesamten Services.
	 */
	private void setButtonEvents() {
		this.startStopButton.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				cleanStatus();
				DisplayStartView.this.communicationController
						.startConnection(DisplayStartView.this.brokerIpTextField.getText());
				ConfigController.writeConfigFile(
						TEXT_BROKER_IP + SPLIT_CHARACTER + DisplayStartView.this.brokerIpTextField.getText());
			}
		});
	}

	/**
	 * Füllt die Textfelder der View entsprechend dem Konfigurationsfeld ab.
	 * 
	 * @param text
	 *            - Array bestehend aus jeweils einer Zeile des
	 *            Konfigurationsfiles
	 */
	private void fillTextFields(ArrayList<String> text) {
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
		DisplayStartView.this.statusText.clear();
	}

	/**
	 * Funktion setzt einen neuen Text in das Textfeld "Status". Dient zum
	 * Informieren des Benutzers über den Systemzustand.
	 * 
	 * @param text
	 *            - Auszugebender Text.
	 * @param fault
	 *            - true: Übergebene Nachricht ist eine Fehlernachricht, sonst
	 *            false.
	 */
	public void updateStatus(String text, boolean fault) {
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					String error = "Info: \t";
					if (fault) {
						error = "Fehler:\t";
					} else {
						error = "Info: \t";
					}
					DisplayStartView.this.statusText.appendText(error + text + "\n");				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Nach einem erfolgreichen Verbindungsaufbau wird dem Benutzer die "SceneryView" angezeigt.
	 */
	public void showConnectionSucceed() {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						DisplayStartView.this.sceneryView.start(DisplayStartView.this.primaryStage);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
	}

	/**
	 * Funktion wird beim Starten der Java-Applikation ausgeführt.
	 * @param args
	 */
	public static void main(final String[] args) {
		launch(args);
	}
}
