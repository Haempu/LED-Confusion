package ch.bfh.eliaboesiger.lightmapper.component;

import android.content.Context;

import ch.bfh.eliaboesiger.lightmapper.model.Luminaire;


/**
 * Die Klasse Luminaire-Button ist eine erweiterte AppCompatButton-Klasse.
 * Nebst den Funktionen und Attributen eines Buttons beinhaltet der LuminaireButton
 * ein Luminaire-Objekt.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 15.04.2018
 * @version 1.0
 */
public class LuminaireButton extends android.support.v7.widget.AppCompatButton {

    private Luminaire luminaire;

    public LuminaireButton(Context context) {
        super(context);
    }

    public Luminaire getLuminaire() {
        return luminaire;
    }

    public void setLuminaire(Luminaire luminaire) {
        this.luminaire = luminaire;
    }
}
