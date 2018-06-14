package ch.bfh.lightmapper.dataprocessingservice.view;

import java.text.NumberFormat;
import java.util.ArrayList;

import ch.bfh.lightmapper.dataprocessingservice.controller.ConfigController;
import ch.bfh.lightmapper.dataprocessingservice.controller.DataProcessingController;
import ch.bfh.lightmapper.dataprocessingservice.controller.MqttController;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
/**
 * Die Klasse "DataProcessingView" ist für die Kontrolle und das Darstellen der View des Datenverarbeitungs-Service zuständig.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class DataProcessingView extends Application {
	
	// Konstanten
	public static final String TEXT_START = "Start";
	public static final String TEXT_STOP = "Stop";
	public static final String TEXT_BROKER_IP = "MQTT Broker IP";
	public static final String TEXT_COLOR_THRESHOLD = "Schwarz/Weiss Schwellwert (0 - 255)";
	public static final String SPLIT_CHARACTER = ";";
	public static final String NEW_LINE_CHARACTER = "\n";
	public static final String STATUS_MQTT_CONNECTED = "Mit Mqtt-Broker verbunden";
	public static final String STATUS_MQTT_DISCONNECTED = "Von Mqtt-Broker abgemeldet";
	public static final String STATUS_MQTT_CONNECTION_LOST = "Verbindung zu Mqtt-Broker verloren";
	public static final String STATUS_MQTT_SUBCSCRIBE_FINISHED = "An allen Mqtt-Topics angemeldet";
	public static final String STATUS_MQTT_SUBCSCRIBE_FAILURE = "Anmeldung an Mqtt-Topic fehlgeschlagen";
	public static final String STATUS_MQTT_CONNECTION_FAILURE = "Mqtt-Verbindungsfehler";
	public static final String STATUS_MQTT_FAILURE = "Allgemeiner MQTT-Fehler";
	
	public static final double VIEW_SPACING = 20;
	public static final double VIEW_WIDTH = 300;
	public static final double VIEW_HEIGHT = 400;
	public static final double VIEW_PREF_CONTENT_WIDTH = 280;
	public static final int VIEW_TIME_FOR_STOPPING_BROKER = 400;
	public static final int VIEW_THRESHOLD_MIN = 0;
	public static final int VIEW_THRESHOLD_MAX = 255;
	public static final int VIEW_PREF_THRESHOLD_TEXT_FIELD_WIDTH = 35;
	
	// Membervariablen
	private Stage primaryStage;
	private Button startStopButton = new Button(TEXT_START);
	private TextField brokerIpTextField = new TextField("0.0.0.0");
	private TextField colorThresholdTextField = new TextField("170");
	private Slider colorThresholdSlider = new Slider();
	private Text statusTitle = new Text("Status");
	private TextArea statusText = new TextArea();
	private DataProcessingController dataProcessingController;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		this.dataProcessingController = new DataProcessingController(this);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				dataProcessingController.handleExit();
			}
		}, "Shutdown-thread"));
			
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(MqttController.CLIENT_USERNAME);
		this.colorThresholdTextField.setPrefWidth(VIEW_PREF_THRESHOLD_TEXT_FIELD_WIDTH);
		this.startStopButton.setMinWidth(VIEW_PREF_CONTENT_WIDTH);
		fillTextFields(ConfigController.readConfigFile());
		
		this.colorThresholdTextField.setAlignment(Pos.CENTER_RIGHT);
		this.colorThresholdSlider.setMin(VIEW_THRESHOLD_MIN);
		this.colorThresholdSlider.setMax(VIEW_THRESHOLD_MAX);
		this.colorThresholdSlider.setPrefWidth(VIEW_PREF_CONTENT_WIDTH - VIEW_SPACING - VIEW_PREF_THRESHOLD_TEXT_FIELD_WIDTH);
		
		// TODO nicht mit binding sondern einfach Text überprüfen und jeweils zuweisen. Dann einfach text abfragen.
		this.colorThresholdSlider.setValue(Double.valueOf(this.colorThresholdTextField.getText()));
		this.colorThresholdTextField.textProperty().bindBidirectional(this.colorThresholdSlider.valueProperty(), NumberFormat.getIntegerInstance());
		
		
		

		HBox thresholdLayout = new HBox(VIEW_SPACING);
		thresholdLayout.minWidth(VIEW_PREF_CONTENT_WIDTH);
		VBox fullLayout = new VBox(VIEW_SPACING);
		fullLayout.setPadding(new Insets(VIEW_SPACING,VIEW_SPACING,VIEW_SPACING,VIEW_SPACING));
		
		thresholdLayout.getChildren().addAll(this.colorThresholdSlider, this.colorThresholdTextField);
		fullLayout.getChildren().addAll(new Text(TEXT_BROKER_IP), this.brokerIpTextField,
										new Text(TEXT_COLOR_THRESHOLD), thresholdLayout,
										this.startStopButton, this.statusTitle, this.statusText);
		
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
				switch(DataProcessingView.this.startStopButton.getText()){
				case(TEXT_START):
					cleanStatus();
					DataProcessingView.this.brokerIpTextField.setDisable(true);
					DataProcessingView.this.colorThresholdTextField.setDisable(true);
					DataProcessingView.this.colorThresholdSlider.setDisable(true);
					DataProcessingView.this.dataProcessingController.startConnection(DataProcessingView.this.brokerIpTextField.getText(),
																					DataProcessingView.this.colorThresholdTextField.getText());
					ConfigController.writeConfigFile(TEXT_BROKER_IP + SPLIT_CHARACTER + DataProcessingView.this.brokerIpTextField.getText() + NEW_LINE_CHARACTER +
												TEXT_COLOR_THRESHOLD + SPLIT_CHARACTER + DataProcessingView.this.colorThresholdTextField.getText());
					DataProcessingView.this.startStopButton.setText(TEXT_STOP);
					break;
				case(TEXT_STOP):
					DataProcessingView.this.brokerIpTextField.setDisable(false);
					DataProcessingView.this.colorThresholdTextField.setDisable(false);
					DataProcessingView.this.colorThresholdSlider.setDisable(false);
					DataProcessingView.this.dataProcessingController.handleExit();
					DataProcessingView.this.startStopButton.setDisable(true);
					try {
						Thread.sleep(VIEW_TIME_FOR_STOPPING_BROKER);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					DataProcessingView.this.startStopButton.setText(TEXT_START);
					DataProcessingView.this.startStopButton.setDisable(false);
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
			else if (text.get(i).contains(TEXT_COLOR_THRESHOLD)) {
				this.colorThresholdTextField.setText(text.get(i).substring(TEXT_COLOR_THRESHOLD.length() + 1));
			}
		}
	}
	
	/**
	 * Löscht die bisherigen Ausgaben im Textfeld "Status".
	 */
	private void cleanStatus() {
		DataProcessingView.this.statusText.clear();
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
		DataProcessingView.this.statusText.appendText(error + text + "\n");
	}
	
	public static void  main(final String[] args) {
		launch(args);
	}
}
