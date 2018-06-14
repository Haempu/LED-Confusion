package ch.bfh.bachelorthesis.ledmapper.controller.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import ch.bfh.bachelorthesis.ledmapper.controller.CameraActivityController;
import ch.bfh.bachelorthesis.ledmapper.controller.activity.CameraActivity;

/**
 * Die Utils-Klase besteht aus statischen Funktionen, die von allen Klassen verwendet werden können.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 31.03.2018
 * @version 1.0
 */
public class Utils {
    //Pattern für eine IP-Adresse

    private static final Pattern PATTERN_IP_ADRESS = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Die Funktion validiert einen String.
     * Der eingegebene String muss dem PATTERN_IP_ADRESS entsprechen.
     * @param ip - IP-Adresse als String
     * @return - true: Wenn es sich um eine IPv4-Adress handelt.
     */
    public static boolean validateIPAdress(final String ip) {
        return PATTERN_IP_ADRESS.matcher(ip).matches();
    }

    /**
     * Verarbeitet eine Bitmapdatei in einen String, welcher über Mqtt verschickt werden kann.
     * @param picture - Bitmap-Bild.
     * @return - String des Bildes.
     */
    public static byte[] bitmapToByteArray(Bitmap picture) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, CameraActivityController.BITMAP_QUALITY, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}
