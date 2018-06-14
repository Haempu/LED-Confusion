package ch.bfh.lightmapper.displayservice.controller;

import ch.bfh.lightmapper.displayservice.model.Luminaire;
import ch.bfh.lightmapper.displayservice.view.SceneryView;

public class LuminaireController {

	// Membervariablen
	// private MQTTController mqttController = new MQTTController();
	private SceneryView gui;

	public LuminaireController(SceneryView gui) {
		this.gui = gui;
	}

	public void setLuminaireSettings(Luminaire led) {
		gui.changeLuminaireSettings(led);
	}
}