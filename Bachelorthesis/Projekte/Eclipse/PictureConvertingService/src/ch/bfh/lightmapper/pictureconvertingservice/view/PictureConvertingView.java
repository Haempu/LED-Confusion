package ch.bfh.lightmapper.pictureconvertingservice.view;

import ch.bfh.lightmapper.pictureconvertingservice.controller.ConfigController;
import ch.bfh.lightmapper.pictureconvertingservice.controller.MqttController;
import ch.bfh.lightmapper.pictureconvertingservice.controller.PictureConvertingController;
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

public class PictureConvertingView extends Application {
	
	// Konstanten
	public static final String TEXT_START = "Start";
	public static final String TEXT_STOP = "Stop";
	public static final String TEXT_BROKER_IP = "MQTT Broker IP";
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
	private Stage primaryStage;
	private Button startStopButton = new Button(TEXT_START);
	private TextField brokerIpTextField = new TextField("0.0.0.0");
	private Text statusTitle = new Text("Status");
	private TextArea statusText = new TextArea();
	private PictureConvertingController pictureConvertingController;

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		this.pictureConvertingController = new PictureConvertingController(this);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				pictureConvertingController.handleExit();
			}
		}, "Shutdown-thread"));
			
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(MqttController.CLIENT_USERNAME);
		this.startStopButton.setMinWidth(VIEW_PREF_CONTENT_WIDTH);
		this.brokerIpTextField.setText(ConfigController.readIpAddress());
		
		VBox fullLayout = new VBox(VIEW_SPACING);
		fullLayout.setPadding(new Insets(VIEW_SPACING,VIEW_SPACING,VIEW_SPACING,VIEW_SPACING));
		fullLayout.getChildren().addAll(new Text(TEXT_BROKER_IP), this.brokerIpTextField, this.startStopButton, this.statusTitle, this.statusText);
		
		StackPane root = new StackPane();
		root.getChildren().add(fullLayout);
		Scene scene = new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
		
		setButtonEvents();
		
		this.primaryStage.setScene(scene);
		this.primaryStage.setResizable(false);
		this.primaryStage.show();
	}
	
	private void setButtonEvents() {
		
		  this.startStopButton.setOnAction(new EventHandler() {
			@Override
			public void handle(Event event) {
				switch(PictureConvertingView.this.startStopButton.getText()){
				case(TEXT_START):
					cleanStatus();
					PictureConvertingView.this.brokerIpTextField.setDisable(true);
					PictureConvertingView.this.pictureConvertingController.startConnection(PictureConvertingView.this.brokerIpTextField.getText());
					ConfigController.writeIpAddress(PictureConvertingView.this.brokerIpTextField.getText());
					PictureConvertingView.this.startStopButton.setText(TEXT_STOP);
					break;
				case(TEXT_STOP):
					PictureConvertingView.this.brokerIpTextField.setDisable(false);
					PictureConvertingView.this.pictureConvertingController.handleExit();
					PictureConvertingView.this.startStopButton.setDisable(true);
					try {
						Thread.sleep(VIEW_TIME_FOR_STOPPING_BROKER);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					PictureConvertingView.this.startStopButton.setText(TEXT_START);
					PictureConvertingView.this.startStopButton.setDisable(false);
					break;
				}
				
			}
		});
	}
	
	/**
	 * Löscht die bisherigen Ausgaben im Textfeld "Status".
	 */
	private void cleanStatus() {
		PictureConvertingView.this.statusText.clear();
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
		PictureConvertingView.this.statusText.appendText(error + text + "\n");
	}
	
	public static void  main(final String[] args) {
		launch(args);
	}
}
