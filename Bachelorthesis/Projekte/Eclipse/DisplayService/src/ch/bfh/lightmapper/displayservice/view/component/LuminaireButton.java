package ch.bfh.lightmapper.displayservice.view.component;

import ch.bfh.lightmapper.displayservice.model.Luminaire;
import javafx.scene.control.Button;
/**
 * Die Klasse "Luminaire-Button" erweitert die JavaFxKlasse Button.
 * Zusätzlich zu den Funktionen und Attributen der Button-Klasse wird jedem Objekt eine Luminaire zugewiesen.
 * 
 * @author Aebischer Patrik, Bösiger Elia
 * @date 01.06.2018
 * @version 1.0
 */
public class LuminaireButton extends Button {
	
	private Luminaire luminaire;

	public Luminaire getLuminaire() {
		return luminaire;
	}

	public void setLuminaire(Luminaire luminaire) {
		this.luminaire = luminaire;
	}
}
