package ch.bfh.project2.view;

import org.opencv.core.Core;

import ch.bfh.project2.controller.LightDetectionController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * The LightDetectionView Class shows an JavaFx GUI to the User. The main part
 * of the GUI is the screen of the computer integrated camera. On this screen
 * all lights will be detected and marked.
 * 
 * @author Aebischer Patrik, Bösiger Elia
 * @date 26.12.2017
 * @version 1.0
 *
 */
public class LightDetectionView extends Application {

	/**
	 * The main class for a JavaFX application. It creates and handles the main
	 * window with its resources (style, graphics, etc.).
	 * 
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LightDetection.fxml"));
			// store the root element so that the controllers can use it
			BorderPane root = (BorderPane) loader.load();
			// set a whitesmoke background
			root.setStyle("-fx-background-color: whitesmoke;");
			// create and style a scene
			Scene scene = new Scene(root, 900, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created
			// scene
			primaryStage.setTitle("Project 2: Light Detection");
			primaryStage.setScene(scene);
			// show the GUI
			primaryStage.show();

			// set the proper behavior on closing the application
			LightDetectionController controller = loader.getController();
			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					controller.setClosed();
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start method of the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		launch(args);
	}
}
