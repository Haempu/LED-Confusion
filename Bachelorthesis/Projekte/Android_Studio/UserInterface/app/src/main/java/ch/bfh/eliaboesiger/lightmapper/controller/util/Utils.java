package ch.bfh.eliaboesiger.lightmapper.controller.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Pattern;

import ch.bfh.eliaboesiger.lightmapper.model.Coordinate;
import ch.bfh.eliaboesiger.lightmapper.model.Luminaire;

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
     * Funktion rechnet Pixel in DP um.
     *
     * @param context - Context der Activiry
     * @param dp - DP, die in Pixel umgerechnet werden
     * @return - Anzahl Pixel
     */
    public static int pxFromDp(final Context context, final int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

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
     * Funktion rechnet eine Integer Farbe in einen Hex-Code um.
     * @param color - Farbe als Integer
     * @return - Farbe als Hex-Code (Bsp.: #FF0000 --> Rot)
     */
    public static String getHexColor(int color){
        return String.format("#%06X", (0xFFFFFF & color));
    }

    /**
     * Funktion liefert den maximalen X-Wert einer Koordinaten-Liste zurück.
     * @param coordinates - Liste aller Koordinaten
     * @return maximaler X-Wert
     */
    public static int getxMax(ArrayList<Luminaire> coordinates){
        Integer maxCoordinate = null;
        for(Luminaire l: coordinates){
            if(maxCoordinate == null){
                maxCoordinate = l.getCoordinates().getxCoordinate();
            }else if(l.getCoordinates().getxCoordinate() > maxCoordinate){
                maxCoordinate = l.getCoordinates().getxCoordinate();
            }
        }

        return maxCoordinate;
    }

    /**
     * Funktion liefert den maximalen Y-Wert einer Koordinaten-Liste zurück.
     * @param coordinates - Liste aller Koordinaten
     * @return maximaler Y-Wert
     */
    public static int getyMax(ArrayList<Luminaire> coordinates){
        Integer maxCoordinate = null;
        for(Luminaire l: coordinates){
            if(maxCoordinate == null){
                maxCoordinate = l.getCoordinates().getyCoordinate();
            }else if(l.getCoordinates().getyCoordinate() > maxCoordinate){
                maxCoordinate = l.getCoordinates().getyCoordinate();
            }
        }

        return maxCoordinate;
    }

    /**
     * Funktion liefert den minimalen X-Wert einer Koordinaten-Liste zurück.
     * @param coordinates - Liste aller Koordinaten
     * @return minimaler X-Wert
     */
    public static int getxMin(ArrayList<Luminaire> coordinates){
        Integer minCoordinate = null;
        for(Luminaire l: coordinates){
            if(minCoordinate == null){
                minCoordinate = l.getCoordinates().getxCoordinate();
            }else if(l.getCoordinates().getxCoordinate() < minCoordinate){
                minCoordinate = l.getCoordinates().getxCoordinate();
            }
        }

        return minCoordinate;
    }

    /**
     * Funktion liefert den minimalen Y-Wert einer Koordinaten-Liste zurück.
     * @param coordinates - Liste aller Koordinaten
     * @return minimaler Y-Wert
     */
    public static int getyMin(ArrayList<Luminaire> coordinates){
        Integer minCoordinate = null;
        for(Luminaire l: coordinates){
            if(minCoordinate == null){
                minCoordinate = l.getCoordinates().getyCoordinate();
            }else if(l.getCoordinates().getyCoordinate() < minCoordinate){
                minCoordinate = l.getCoordinates().getyCoordinate();
            }
        }

        return minCoordinate;
    }

    /**
     * Verarbeitet eine Bitmapdatei in einen String, welcher über Mqtt verschickt werden kann.
     * @param picture - Bitmap-Bild.
     * @return - String des Bildes.
     */
    public static byte[] bitmapToByteArray(Bitmap picture) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        return byteArray;
    }
}
