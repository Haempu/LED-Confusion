package ch.bfh.eliaboesiger.lightmapper.controller.listener;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;

import java.util.ArrayList;

import ch.bfh.eliaboesiger.lightmapper.component.ColorPicker;
import ch.bfh.eliaboesiger.lightmapper.component.LuminaireButton;
import ch.bfh.eliaboesiger.lightmapper.controller.DbController;
import ch.bfh.eliaboesiger.lightmapper.controller.MqttController;
import ch.bfh.eliaboesiger.lightmapper.controller.util.Utils;
import ch.bfh.eliaboesiger.lightmapper.model.Luminaire;

/**
 * Die LEDTouchListener implementiert den TouchListener, sodass erkannt wird ob eine virtuelle
 * Lichtquelle auf dem Bildschirm berührt wird.
 *
 * @author Aebischer Patrik, Elia Bösiger
 * @date 23.03.2018
 * @version 1.0
 */
public class LEDTouchListener implements View.OnTouchListener{

    //Membervariabeln
    private ArrayList<LuminaireButton> luminaires;
    private ColorPicker colorPicker;
    private Switch onOffSwitch;
    private Context context;
    private DbController dbController;
    private MqttController mqttController;
    private SeekBar brightness;
    private float lastX = 0.0f;
    private float lastY = 0.0f;

    //Konstanten
    public static final double CHANGED_FROM_LAST_TOUCH_TRESHOLD = 3.5;

    /**
     * Konstruktor: LEDTouchListener
     * @param luminaires - Lichtquellen, die als Buttons in einer ArrayList sind.
     * @param colorPicker - ColorPicker, der die aktuelle Farbe beinhaltet.
     */
    public LEDTouchListener(ArrayList<LuminaireButton> luminaires, ColorPicker colorPicker, Switch onOffSwitch, Context context, SeekBar brightness){
        this.luminaires = luminaires;
        this.colorPicker = colorPicker;
        this.onOffSwitch = onOffSwitch;
        this.context = context;
        this.brightness = brightness;
        this.dbController = DbController.getInstance(context);
        this.mqttController = MqttController.getInstance();
    }

    /**
     * Wenn die ImageView in der die virtuellen Lichtquellen vorhanden sind berührt wird, wird
     * die Funktion onTouch ausgeführt.
     * @param v
     * @param event
     * @return
     */
        @Override
        public boolean onTouch(View v, MotionEvent event){

            if(!inCircle(event.getX(), event.getY(), lastX, lastY, CHANGED_FROM_LAST_TOUCH_TRESHOLD)) {

                for (LuminaireButton luminaireButton : this.luminaires) {
                    if(inCircle(event.getX(), event.getY(), luminaireButton.getX(), luminaireButton.getY(), luminaireButton.getWidth()*1.4)) {

                            Luminaire l = luminaireButton.getLuminaire();

                            if(!l.getColor().equals(this.colorPicker.getColor())) {

                                GradientDrawable d = new GradientDrawable();
                                d.setShape(GradientDrawable.OVAL);
                                d.setStroke(2, Color.WHITE);

                                if(this.onOffSwitch.isChecked()) {
                                    d.setColor(this.colorPicker.getColor());

                                    luminaireButton.setBackground(d);

                                    l.setColor(Utils.getHexColor(this.colorPicker.getColor()));
                                    l.setBrightness(this.brightness.getProgress());
                                    l.setOn(Luminaire.LUMINAIRE_ON);
                                }else{
                                    d.setColor(Color.TRANSPARENT);
                                    luminaireButton.setBackground(d);
                                    l.setOn(Luminaire.LUMINAIRE_OFF);
                                }

                                this.dbController.updateLuminaire(l);

                                if(this.mqttController.isConnected()){

                                    String message = "[" + l.getCoordinates().getUid() + ";" + l.getColor() + ";" + l.getBrightness() + ";" + l.isOn() + "]";
                                this.mqttController.publish(MqttController.TPC_UI_OUT_FUNCTION_LUMINAIRE_CHANGED, message);
                            }
                        }
                    }
                }
            }

            this.lastX = event.getX();
            this.lastY = event.getY();

            return true;
        }

    /**
     * Fuktion überprüft ob sich der berührte "Fleck" auf einer virtuellen Lichtquelle befindet.
     *
     * @param x - aktuelle Fingerposition x
     * @param y - aktuelle Fingerposition y
     * @param circleCenterX - Position von der virtuellen Lichtquelle x
     * @param circleCenterY - Position von der virtuellen Lichtquelle y
     * @param circleRadius - Radius der Lichtquelle
     * @return - true: wenn der berührte "Fleck" auf der virtuellen Lichtquelle ist
     */
        private boolean inCircle(float x, float y, float circleCenterX, float circleCenterY, double circleRadius) {
            double dx = Math.pow(x - circleCenterX, 2);
            double dy = Math.pow(y - circleCenterY, 2);

            if ((dx + dy) < Math.pow(circleRadius, 2)) {
                return true;
            } else {
                return false;
            }
        }
    }

