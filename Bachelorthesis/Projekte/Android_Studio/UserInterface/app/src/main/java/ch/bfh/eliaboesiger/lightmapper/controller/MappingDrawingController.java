package ch.bfh.eliaboesiger.lightmapper.controller;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.R;
import ch.bfh.eliaboesiger.lightmapper.component.ColorPicker;
import ch.bfh.eliaboesiger.lightmapper.component.LuminaireButton;
import ch.bfh.eliaboesiger.lightmapper.controller.util.Utils;
import ch.bfh.eliaboesiger.lightmapper.model.Coordinate;
import ch.bfh.eliaboesiger.lightmapper.model.Luminaire;
import ch.bfh.eliaboesiger.lightmapper.model.Scenery;

/**
 * Der MappingDrawingController stellt alle Funktionen, die in der MappingDrawingActivity
 * verwendet werden zur Verfügung und bietet für die Activity eine Schnittstelle zur Datenbank.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class MappingDrawingController {

    //Membervariabeln
    private MqttController mqttController;
    private DbController dbController;
    private Context context;
    private Bitmap selectedImage;
    private double xPadding = 0;
    private double yPadding = 0;
    private int yLast = 0;

    //Konstanten
    public static final int X_IN_BETWEEN_TRESHOLD_IN_PX = 30;
    public static final int PADDING_DIVIDE_FACTOR = 5;
    public static final int LUMINAIRE_RADIUS = 35;
    public static final int PADDING_IMAGE_VIEW = 30;

    /**
     * Konstruktor: MappingDrawingController
     * @param context
     */
    public MappingDrawingController(Context context){
        this.context = context;
        this.mqttController = MqttController.getInstance();
        this.dbController = DbController.getInstance(context);
    }

    /**
     * Funktion löscht ein Mapping
     * @param mappingId
     * @return
     */
    public Integer deleteMapping(Integer mappingId){
        return this.dbController.removeMapping(mappingId);
    }

    /**
     * Funktion holt alle Leuchtquellen (Luminaires) für eine Ansicht und fügt diese
     * zur imageView hinzu.
     * @param mappingId
     * @param imageView
     * @param colorPicker
     * @param brightness
     * @param width
     * @return
     */
    public ArrayList<LuminaireButton> addLuminairesToView(Integer mappingId, FrameLayout imageView, final ColorPicker colorPicker, final SeekBar brightness, int width){

        ArrayList<Luminaire> luminaires = this.dbController.getLuminairesWithMapping(mappingId);
        ArrayList<LuminaireButton> buttons = new ArrayList<>();
        this.yLast = 0;
        ArrayList<String> coordinatesMessage = new ArrayList<String>();

        if(luminaires != null && luminaires.size() != 0){

            int xMin = Utils.getxMin(luminaires);
            int xMax = Utils.getxMax(luminaires);
            int yMin = Utils.getyMin(luminaires);
            int yMax = Utils.getyMax(luminaires);

            for(final Luminaire l : luminaires){
                final  GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setStroke(2, Color.WHITE);
                LuminaireButton luminaireButton = new LuminaireButton(this.context);
                luminaireButton.setLuminaire(l);

                l.getCoordinates().setRadius(LUMINAIRE_RADIUS);

                if(l.getColor() != null && !l.getColor().isEmpty() && l.isOn() == Luminaire.LUMINAIRE_ON){
                    drawable.setColor(Color.parseColor(l.getColor()));
                    luminaireButton.setBackground(drawable);
                }else{
                    drawable.setColor(Color.TRANSPARENT);
                    luminaireButton.setBackground(drawable);
                }

                if(l.isOn() == Luminaire.LUMINAIRE_OFF){
                    drawable.setColor(Color.TRANSPARENT);
                    luminaireButton.setBackground(drawable);
                }

                l.setBrightness(Luminaire.LUMINAIRE_MAX_BRIGHTNESS);

                luminaireButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                            drawable.setColor(colorPicker.getColor());
                            view.setBackground(drawable);

                            l.setOn(Luminaire.LUMINAIRE_ON);
                            l.setColor(Utils.getHexColor(colorPicker.getColor()));
                            l.setBrightness(brightness.getProgress());
                            MappingDrawingController.this.dbController.updateLuminaire(l);

                        if( MappingDrawingController.this.mqttController.isConnected()){

                            String message = "["+l.getCoordinates().getUid()+";"+l.getColor()+";"+l.getBrightness()+";"+l.isOn()+"]";
                            MappingDrawingController.this.mqttController.publish(MqttController.TPC_UI_OUT_FUNCTION_LUMINAIRE_CHANGED, message);
                        }else{
                            //TODO: Fehlermeldung
                        }
                    }
                });

                luminaireButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                            drawable.setColor(Color.TRANSPARENT);
                            l.setOn(Luminaire.LUMINAIRE_OFF);
                            l.setColor(Utils.getHexColor(colorPicker.getColor()));
                            view.setBackground(drawable);
                            MappingDrawingController.this.dbController.updateLuminaire(l);

                        if( MappingDrawingController.this.mqttController.isConnected()){
                            String message = "["+l.getCoordinates().getUid()+";"+l.getColor()+";"+l.getBrightness()+";"+l.isOn()+"]";
                            MappingDrawingController.this.mqttController.publish(MqttController.TPC_UI_OUT_FUNCTION_LUMINAIRE_CHANGED, message);
                        }else{
                            //TODO: Fehlermeldung
                        }

                        return true;
                    }
                });
                buttons.add(luminaireButton);
                imageView.addView(luminaireButton, getLayoutParamsFor(l.getCoordinates(), imageView, width, xMin, xMax, yMin, yMax));

                String message = l.getCoordinates().getUid()+";"+l.getColor()+";"+l.getBrightness()+";"+l.isOn();
                coordinatesMessage.add(message);
            }

            if( MappingDrawingController.this.mqttController.isConnected()){
                MappingDrawingController.this.mqttController.publish(MqttController.TPC_UI_OUT_FUNCTION_LUMINAIRE_CHANGED, coordinatesMessage.toString());
            }else{
                //TODO: Fehlermeldung
            }
        }

        return buttons;
    }

    /**
     * Funktion setzt alle Parameter wie Höhe/Breite und Position des Leuchtquellen-Buttons.
     * @param coordinate
     * @param imageView
     * @return
     */
    private FrameLayout.LayoutParams getLayoutParamsFor(Coordinate coordinate, FrameLayout imageView, int width, int xMin, int xMax, int yMin, int yMax) {

        width = width - Utils.pxFromDp(this.context, PADDING_IMAGE_VIEW); //Padding links und rechts
        double xFactor = getCoordinateFactorForView((xMax-xMin), width, true);
        double yFactor = getCoordinateFactorForView((yMax-yMin), imageView.getLayoutParams().height, false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.TOP;
        params.topMargin = (int) Math.round((coordinate.getyCoordinate()-yMin) * yFactor)+(int) Math.round(this.xPadding)+20;
        params.leftMargin = (int) Math.round((coordinate.getxCoordinate()-xMin) * xFactor)+(int) Math.round(this.yPadding)+40;
        params.width = (int) Math.round(coordinate.getRadius());
        params.height = (int) Math.round(coordinate.getRadius());

        if (params.topMargin >= (this.yLast - X_IN_BETWEEN_TRESHOLD_IN_PX) && params.topMargin <= (this.yLast + X_IN_BETWEEN_TRESHOLD_IN_PX)) {
            params.topMargin =  this.yLast;
        }

        this.yLast = params.topMargin;

        return params;
    }

    /**
     * Funktion berechnet den Faktor um die Koordinaten auf der View richtig anzuzeigen.
     *
     * @param resolutionSize
     * @param sizeImageView
     * @return
     */
   private double getCoordinateFactorForView(int resolutionSize, int sizeImageView, boolean x){
       if(x){
           this.xPadding = (resolutionSize / PADDING_DIVIDE_FACTOR);
           double factor = (sizeImageView / (resolutionSize+(this.xPadding)));

           return factor;
       }else{
           this.yPadding = (resolutionSize / PADDING_DIVIDE_FACTOR);
           double factor = (sizeImageView / (resolutionSize+(this.yPadding)));
           return factor;
       }
   }

    /**
     * Nach dem selektieren des Bildes, werden zuerst alle Koordinaten die sich auf der
     * MappingDrawingActivity befinden verschickt.
     *
     * @param mappingId
     * @param selectedImage
     */
   public void sendCoordinates(Integer mappingId, Bitmap selectedImage){

       this.selectedImage = selectedImage;

       ArrayList<Luminaire> luminaires = this.dbController.getLuminairesWithMapping(mappingId);
       ArrayList<String> luminaireMessage = new ArrayList<>();
       for(Luminaire l : luminaires){
           String message = l.getId()+";"+l.getCoordinates().getxCoordinate()+";"+l.getCoordinates().getyCoordinate();
           luminaireMessage.add(message);
       }
       if( MappingDrawingController.this.mqttController.isConnected()){
           this.mqttController.publish(MqttController.TPC_UI_OUT_FUNCTION_CONVERTING_COORDINATES, luminaireMessage.toString());
       }
   }

    /**
     * Funktion wird aufgerufen wenn die Koordinaten auf dem Mqtt-Broker angekommen sind.
     * Nun wird das selektierte Bild als Bytearray an den Mqtt-Broker geschickt.
     */
    public void coordinatesReceived() {
        if( MappingDrawingController.this.mqttController.isConnected()){
            this.mqttController.publishByteArray(MqttController.TPC_UI_OUT_FUNCTION_CONVERTING_FILE, Utils.bitmapToByteArray(this.selectedImage), MqttController.QOS, false);
        }
    }

    /**
     * Funktion stellt alle gegebenen Leuchtquellen aus.
     *
     * @param luminaires - Liste der Leuchtquellen, die ausgeschaltet werden sollen
     */
    public void clearAllLuminaires(ArrayList<LuminaireButton> luminaires){
        ArrayList<String> luminaireMessage = new ArrayList<>();
        for(LuminaireButton lBtn : luminaires){
            Luminaire l = lBtn.getLuminaire();
            l.setOn(Luminaire.LUMINAIRE_OFF);
            String message = l.getCoordinates().getUid()+";"+l.getColor()+";"+l.getBrightness()+";"+l.isOn();
            luminaireMessage.add(message);
        }

        if( MappingDrawingController.this.mqttController.isConnected()){
            MappingDrawingController.this.mqttController.publish(MqttController.TPC_UI_OUT_FUNCTION_LUMINAIRE_CHANGED, luminaireMessage.toString());
        }
    }

    /**
     * Funktion gibt die sceneryId aus der aktiven Scenery/Beleuchtung zurück.
     *
     * @param sceneryId
     * @return
     */
    public Scenery getSceneryWithId(String sceneryId){
        return this.dbController.getSceneryWithId(sceneryId);
    }
}
