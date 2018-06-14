package ch.bfh.lightmapper.ledstripservice.view;

import java.text.NumberFormat;
import java.util.ArrayList;

import ch.bfh.lightmapper.ledstripservice.controller.ConfigController;
import ch.bfh.lightmapper.ledstripservice.controller.LuminaireController;
import ch.bfh.lightmapper.ledstripservice.controller.MqttController;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LedStripView extends Application {
// TODO: in allen View-Klassen noch Textvalidierung machen.
	
	// Konstanten
	public static final String TEXT_START = "Start";
	public static final String TEXT_STOP = "Stop";
	public static final String TEXT_BROKER_IP = "MQTT Broker IP";
	public static final String TEXT_MASTER_BRICK_IP = "Master Brick IP";
	public static final String TEXT_MASTER_BRICK_PORT = "Master Brick Port";
	public static final String TEXT_MASTER_BRICK_UID = "Master Brick UID";
	public static final String TEXT_LED_STRIP_UID = "LED-Strip UID";
	public static final String TEXT_NUMBER_OF_LED = "Mindestanzahl vorhandener LEDs";
	public static final String TEXT_MAPPING_BRIGHTNESS = "Mappinghelligkeit in %";
	public static final String SPLIT_CHARACTER = ";";
	public static final String NEW_LINE_CHARACTER = "\n";
	public static final String STATUS_MQTT_CONNECTED = "Mit Mqtt-Broker verbunden";
	public static final String STATUS_MQTT_DISCONNECTED = "Von Mqtt-Broker abgemeldet";
	public static final String STATUS_MQTT_CONNECTION_LOST = "Verbindung zu Mqtt-Broker verloren";
	public static final String STATUS_MQTT_SUBCSCRIBE_FINISHED = "An allen Mqtt-Topics angemeldet";
	public static final String STATUS_MQTT_SUBCSCRIBE_FAILURE = "Anmeldung an Mqtt-Topic fehlgeschlagen";
	public static final String STATUS_MQTT_CONNECTION_FAILURE = "Mqtt-Verbindungsfehler";
	public static final String STATUS_MQTT_FAILURE = "Allgemeiner MQTT-Fehler";
	public static final String STATUS_TF_CONNECTED = "Mit Tinkerforge-Komponenten verbunden";
	public static final String STATUS_TF_CONNECTION_FAILURE = "Verbindung zu Tinkerforge nicht möglich";
	public static final String STATUS_TF_LUMINAIRE_CHANGE_FAILURE = "Ändern des gewünschten LEDs nicht möglich";
	
	public static final double VIEW_SPACING = 20;
	public static final double VIEW_WIDTH = 500;
	public static final double VIEW_HEIGHT = 500;
	public static final double VIEW_PREF_CONTENT_WIDTH = 180;
	public static final int VIEW_TIME_FOR_STOPPING_BROKER = 400;
	public static final int VIEW_BRIGHTNESS_MIN = 1;
	public static final int VIEW_BRIGHTNESS_MAX = 100;
	public static final int VIEW_PREF_THRESHOLD_TEXT_FIELD_WIDTH = 35;
	public static final int VIEW_COL_PERCENT_WIDTH = 50;
	
	// Membervariablen
	private Stage primaryStage;
	private Button startStopButton = new Button(TEXT_START);
	private TextField brokerIpTextField = new TextField("0.0.0.0");
	private TextField masterBrickIpTextField = new TextField("0.0.0.0");
	private TextField masterBrickPortTextField = new TextField("4223");
	private TextField masterBrickUidTextField = new TextField("Brick Uid");
	private TextField ledStripUidIpTextField = new TextField("Bricklet Uid");
	private TextField numberOfLedTextField = new TextField("50");
	private TextField mappingBrightnessTextField = new TextField("1");
	private Slider mappingBrightnessSlider = new Slider();
	private Text statusTitle = new Text("Status");
	private TextArea statusText = new TextArea();
	private LuminaireController luminaireController = new LuminaireController(this);

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				luminaireController.handleExit();
			}
		}, "Shutdown-thread"));
			
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(MqttController.CLIENT_USERNAME);
		this.mappingBrightnessTextField.setPrefWidth(VIEW_PREF_THRESHOLD_TEXT_FIELD_WIDTH);
		this.startStopButton.setPrefWidth(VIEW_PREF_CONTENT_WIDTH);
		fillTextFields(ConfigController.readConfigFile());
		
		this.mappingBrightnessTextField.setAlignment(Pos.CENTER_RIGHT);
		this.mappingBrightnessSlider.setMin(VIEW_BRIGHTNESS_MIN);
		this.mappingBrightnessSlider.setMax(VIEW_BRIGHTNESS_MAX);
		this.mappingBrightnessSlider.setValue(Double.parseDouble(this.mappingBrightnessTextField.getText()));
		this.mappingBrightnessTextField.textProperty().bindBidirectional(this.mappingBrightnessSlider.valueProperty(), NumberFormat.getIntegerInstance());
		
		GridPane fullLayout = new GridPane();
		VBox leftSideLayout = new VBox(VIEW_SPACING);
		VBox rightSideLayout = new VBox(VIEW_SPACING);
		HBox brightnessLayout = new HBox(VIEW_SPACING);
		VBox buttonLayout = new VBox(VIEW_SPACING);
		VBox bottomLayout = new VBox(VIEW_SPACING);
		ColumnConstraints col = new ColumnConstraints();
		col.setPercentWidth(VIEW_COL_PERCENT_WIDTH);
		fullLayout.getColumnConstraints().addAll(col);
		leftSideLayout.setPadding(new Insets(VIEW_SPACING,VIEW_SPACING,VIEW_SPACING,VIEW_SPACING));
		rightSideLayout.setPadding(new Insets(VIEW_SPACING,VIEW_SPACING,VIEW_SPACING,VIEW_SPACING));
		brightnessLayout.setPadding(new Insets(VIEW_SPACING,VIEW_SPACING,VIEW_SPACING,VIEW_SPACING));
		buttonLayout.setAlignment(Pos.CENTER);
		bottomLayout.setPadding(new Insets(VIEW_SPACING,VIEW_SPACING,VIEW_SPACING,VIEW_SPACING));
		
		Region regionLeft = new Region();
		Region regionRight = new Region();
		brightnessLayout.setHgrow(regionLeft, Priority.ALWAYS);
		brightnessLayout.setHgrow(regionRight, Priority.ALWAYS);
		
		leftSideLayout.getChildren().addAll(new Text(TEXT_BROKER_IP), this.brokerIpTextField,
											new Text(TEXT_MASTER_BRICK_IP), this.masterBrickIpTextField,
											new Text(TEXT_MASTER_BRICK_PORT), this.masterBrickPortTextField);
		rightSideLayout.getChildren().addAll(new Text(TEXT_MASTER_BRICK_UID), this.masterBrickUidTextField,
											new Text(TEXT_LED_STRIP_UID), this.ledStripUidIpTextField,
											new Text(TEXT_NUMBER_OF_LED), this.numberOfLedTextField);
		brightnessLayout.getChildren().addAll(new Text(TEXT_MAPPING_BRIGHTNESS), regionLeft, this.mappingBrightnessSlider, regionRight, this.mappingBrightnessTextField);
		buttonLayout.getChildren().addAll(this.startStopButton);
		bottomLayout.getChildren().addAll(this.statusTitle, this.statusText);
		fullLayout.add(leftSideLayout, 0, 0);
		fullLayout.add(rightSideLayout, 1, 0);
		fullLayout.add(brightnessLayout, 0, 1, 2, 1);
		fullLayout.add(buttonLayout, 0, 2, 2, 1);
		fullLayout.add(bottomLayout, 0, 3, 2, 1);
		
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
				switch(LedStripView.this.startStopButton.getText()){
				case(TEXT_START):
					cleanStatus();
					LedStripView.this.brokerIpTextField.setDisable(true);
					LedStripView.this.masterBrickIpTextField.setDisable(true);
					LedStripView.this.masterBrickPortTextField.setDisable(true);
					LedStripView.this.masterBrickUidTextField.setDisable(true);
					LedStripView.this.ledStripUidIpTextField.setDisable(true);
					LedStripView.this.numberOfLedTextField.setDisable(true);
					LedStripView.this.mappingBrightnessTextField.setDisable(true);
					LedStripView.this.mappingBrightnessSlider.setDisable(true);
					LedStripView.this.luminaireController.startConnection(LedStripView.this.brokerIpTextField.getText(),
																		LedStripView.this.masterBrickIpTextField.getText(),
																		LedStripView.this.masterBrickPortTextField.getText(),
																		LedStripView.this.masterBrickUidTextField.getText(),
																		LedStripView.this.ledStripUidIpTextField.getText(),
																		LedStripView.this.numberOfLedTextField.getText(),
																		LedStripView.this.mappingBrightnessTextField.getText());
					ConfigController.writeConfigFile(TEXT_BROKER_IP + SPLIT_CHARACTER + LedStripView.this.brokerIpTextField.getText() + NEW_LINE_CHARACTER +
											TEXT_MASTER_BRICK_IP + SPLIT_CHARACTER + LedStripView.this.masterBrickIpTextField.getText() + NEW_LINE_CHARACTER +
											TEXT_MASTER_BRICK_PORT + SPLIT_CHARACTER + LedStripView.this.masterBrickPortTextField.getText() + NEW_LINE_CHARACTER +
											TEXT_MASTER_BRICK_UID + SPLIT_CHARACTER + LedStripView.this.masterBrickUidTextField.getText() + NEW_LINE_CHARACTER +
											TEXT_LED_STRIP_UID + SPLIT_CHARACTER + LedStripView.this.ledStripUidIpTextField.getText() + NEW_LINE_CHARACTER +
											TEXT_NUMBER_OF_LED + SPLIT_CHARACTER + LedStripView.this.numberOfLedTextField.getText() + NEW_LINE_CHARACTER +
											TEXT_MAPPING_BRIGHTNESS + SPLIT_CHARACTER + LedStripView.this.mappingBrightnessTextField.getText());
					LedStripView.this.startStopButton.setText(TEXT_STOP);
					break;
				case(TEXT_STOP):
					LedStripView.this.brokerIpTextField.setDisable(false);
					LedStripView.this.masterBrickIpTextField.setDisable(false);
					LedStripView.this.masterBrickPortTextField.setDisable(false);
					LedStripView.this.masterBrickUidTextField.setDisable(false);
					LedStripView.this.ledStripUidIpTextField.setDisable(false);
					LedStripView.this.numberOfLedTextField.setDisable(false);
					LedStripView.this.mappingBrightnessTextField.setDisable(false);
					LedStripView.this.mappingBrightnessSlider.setDisable(false);
					LedStripView.this.luminaireController.handleExit();
					LedStripView.this.startStopButton.setDisable(true);
					try {
						Thread.sleep(VIEW_TIME_FOR_STOPPING_BROKER);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					LedStripView.this.startStopButton.setText(TEXT_START);
					LedStripView.this.startStopButton.setDisable(false);
					break;
				}
				
			}
		});
	}
	
	private void fillTextFields(ArrayList<String> text){
		for (int i = 0; i < text.size(); i++) {
			if (text.get(i).contains(TEXT_BROKER_IP)) {
				this.brokerIpTextField.setText(text.get(i).substring(TEXT_BROKER_IP.length() + 1));
			}
			else if (text.get(i).contains(TEXT_MASTER_BRICK_IP)) {
				this.masterBrickIpTextField.setText(text.get(i).substring(TEXT_MASTER_BRICK_IP.length() + 1));
			}
			else if (text.get(i).contains(TEXT_MASTER_BRICK_PORT)) {
				this.masterBrickPortTextField.setText(text.get(i).substring(TEXT_MASTER_BRICK_PORT.length() + 1));
			}
			else if (text.get(i).contains(TEXT_MASTER_BRICK_UID)) {
				this.masterBrickUidTextField.setText(text.get(i).substring(TEXT_MASTER_BRICK_UID.length() + 1));
			}
			else if (text.get(i).contains(TEXT_LED_STRIP_UID)) {
				this.ledStripUidIpTextField.setText(text.get(i).substring(TEXT_LED_STRIP_UID.length() + 1));
			}
			else if (text.get(i).contains(TEXT_NUMBER_OF_LED)) {
				this.numberOfLedTextField.setText(text.get(i).substring(TEXT_NUMBER_OF_LED.length() + 1));
			}
			else if (text.get(i).contains(TEXT_MAPPING_BRIGHTNESS)) {
				this.mappingBrightnessTextField.setText(text.get(i).substring(TEXT_MAPPING_BRIGHTNESS.length() + 1));
			}
		}
	}
	
	/**
	 * Löscht die bisherigen Ausgaben im Textfeld "Status".
	 */
	private void cleanStatus() {
		LedStripView.this.statusText.clear();
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
		LedStripView.this.statusText.appendText(error + text + "\n");
	}
	
	public static void  main(final String[] args) {
		launch(args);
	}
}
