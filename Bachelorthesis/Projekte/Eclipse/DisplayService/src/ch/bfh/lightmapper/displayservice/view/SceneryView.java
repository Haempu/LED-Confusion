package ch.bfh.lightmapper.displayservice.view;

import java.util.ArrayList;

import ch.bfh.lightmapper.displayservice.controller.CommunicationController;
import ch.bfh.lightmapper.displayservice.controller.LuminaireController;
import ch.bfh.lightmapper.displayservice.model.Luminaire;
import ch.bfh.lightmapper.displayservice.view.component.LuminaireButton;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Die Klasse "SceneryView" zeigt die virtuellen Leuchtquellen an. Die
 * Leuchtquellen können nach dem Mapping-Vorgang über die MQTT-Schnittstelle
 * konfiguriert werden.
 *
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class SceneryView extends Application {

	// Membervariablen
	private CommunicationController communicationController;
	private DisplayStartView startView;

	// UI-Komponenten
	private Stage primaryStage;
	private HBox fullLayout = new HBox(20);
	private GridPane ledLayout = new GridPane();
	private ArrayList<LuminaireButton> luminaires = new ArrayList<LuminaireButton>();
	private LuminaireController ledController;

	// Konstanten
	public static final String APP_TITLE = "Bildschirm-Service";
	public static final int LED_COLUMNS = 8;
	public static final int LED_ROWS = 5;
	public static final String BACKGROUND_COLOR = "#000000";
	public static final String COLOR_ON = "#FFFFFF";
	public static final String COLOR_OFF = "#000000";
	public static final String CIRCLE_BUTTON_STYLE = "-fx-background-radius: 5em; " + "-fx-min-width: 80px; "
			+ "-fx-min-height: 80px; " + "-fx-max-width: 80px; " + "-fx-max-height: 80px;";
	public static final int VIEW_TIME_FOR_STOPPING_BROKER = 400;

	/**
	 * Konstruktor: UserView
	 * 
	 * @param communicationController
	 *            - Schnittstelle zum MQTTController (beinhaltet Callbacks)
	 * @param startView
	 *            - StartView zum Aufbau der MQTT-Verbindung
	 */
	public SceneryView(DisplayStartView startView) {
		this.communicationController = CommunicationController.getInstance();
		this.startView = startView;
	}

	/**
	 * Funktion initialisiert und statet die Ansicht.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		this.ledController = new LuminaireController(this);
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(APP_TITLE);

		this.ledLayout.setHgap(50);
		this.ledLayout.setVgap(50);

		fillLayoutWithLeds();

		// Style fullLayout
		this.fullLayout.getChildren().addAll(this.ledLayout);
		this.fullLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
		this.fullLayout.setAlignment(Pos.CENTER);
		this.fullLayout.setPadding(new Insets(90, 90, 90, 90));

		StackPane root = new StackPane();
		root.getChildren().add(this.fullLayout);
		root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

		Scene scene = new Scene(root, 1400, 900);
		setEscapeEvent(scene);

		this.primaryStage.setScene(scene);
		this.primaryStage.setFullScreen(true);

		setExitEvent();

	}

	/**
	 * Funktion fängt Event ab, wenn der Benutzer eine View schliesst.
	 */
	private void setExitEvent() {

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				SceneryView.this.communicationController.handleExit();
			}
		}, "Shutdown-thread"));
	}

	/**
	 * Funktion fängt Event ab, wenn der Benutzer die Vollbildansicht beendet.
	 * 
	 * @param scene
	 */
	private void setEscapeEvent(Scene scene) {
		scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					try {
						SceneryView.this.communicationController.handleExit();
						try {
							Thread.sleep(VIEW_TIME_FOR_STOPPING_BROKER);
							SceneryView.this.startView.start(SceneryView.this.primaryStage);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * Funktion füllt das ledLayout mit allen Leuchtquellen (als
	 * LuminaireButtons)
	 */
	public void fillLayoutWithLeds() {
		int idCounter = 0;
		for (int i = 0; i < LED_COLUMNS; i++) {
			for (int j = 0; j < LED_ROWS; j++) {

				LuminaireButton ledButton = new LuminaireButton();
				ledButton.setLuminaire(new Luminaire(idCounter));
				ledButton.setStyle(CIRCLE_BUTTON_STYLE);

				/*
				 * if(idCounter == -1){
				 * ledButton.getLuminaire().setColor(COLOR_ON); }else{
				 * ledButton.getLuminaire().setColor(BACKGROUND_COLOR);
				 * 
				 * }
				 */
				ledButton.getLuminaire().setColor(COLOR_OFF);
				ledButton.getLuminaire().setOn(false);
				this.ledLayout.add(ledButton, i, j);
				this.luminaires.add(ledButton);
				changeLuminaireSettings(ledButton.getLuminaire());
				idCounter++;
			}
		}
	}

	/**
	 * Funktion kann eine Leuchtquelle verändern.
	 * 
	 * @param led
	 *            - Leuchtquelle, die verändert werden soll
	 */
	public void changeLuminaireSettings(Luminaire led) {
		for (LuminaireButton l : this.luminaires) {
			if (l.getLuminaire().getUid() == led.getUid()) {
				if(led.isOn() && led.getColor() != null){
					l.setStyle(CIRCLE_BUTTON_STYLE + " -fx-background-color: " + led.getColor() + ";");
				}else if(!led.isOn()){
					l.setStyle(CIRCLE_BUTTON_STYLE + " -fx-background-color: " + COLOR_OFF + ";");
				}
				break;
			}
		}
	}

	/**
	 * Funktion gibt die Anzahl Leuchtquellen zurück.
	 * 
	 * @return
	 */
	public int getNumberOfLuminaires() {
		return this.luminaires.size();
	}
}
