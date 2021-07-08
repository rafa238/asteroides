package com.example.asteroides;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.Vector;

public class VistaJuego extends View implements SensorEventListener {

    //Thread y tiempo
    private ThreadJuego thread = new ThreadJuego();
    private static int PERIODO_PROCESO = 50;
    private long ultimoProceso = 0;
    //Asteroides
    private Vector<Grafico> asteroides;
    private int numAsteroides = 5;
    private int numFragmentos = 3;
    //NAVE
    private Grafico nave;
    private int giroNave;
    private double aceleracionNave;
    private static final int MAX_VELOCIDAD_NAVE = 20;
    //MISIL
    private Vector<Grafico> misiles;
    private static int PASO_VELOCIDAD_MISIL = 12;
    //private boolean misilActivo = false;
    private Vector<Integer> tiempoMisiles;
    Drawable drawableMisil;
    //Incremento estandar de giro y aceleracion
    private static final int PASO_GIRO_NAVE = 13;
    private static final float PASO_ACELERACION_NAVE = 0.5f;
    //DISPARO y touch
    private float mX = 0, mY = 0;
    private boolean disparo = false;
    //PREFERENCIAS
    SharedPreferences pref;
    String tipoControl;
    Boolean musica;

    SensorManager mSensorManager;

    SoundPool soundPool;
    int idDisparo, idExplosion;

    public VistaJuego(Context contexto, AttributeSet attrs){
        super(contexto, attrs);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        tipoControl = pref.getString("controles", "Pantalla Tactil");
        musica = pref.getBoolean("musica", true);
        //INICIALIZAR GRAFICOS
        misiles = new Vector<>();
        tiempoMisiles = new Vector<>();
        Drawable drawableNave, drawableAsteroide;
        boolean vectorial = pref.getString("graficos", "1").equals("0");
        if(vectorial){
            Path pathAsteroide = new Path();
            pathAsteroide.moveTo((float)0.3, (float)0.0);
            pathAsteroide.lineTo((float)0.6, (float)0.0);
            pathAsteroide.lineTo((float)0.6, (float)0.3);
            pathAsteroide.lineTo((float)0.8, (float)0.2);
            pathAsteroide.lineTo((float)1.0, (float)0.4);
            pathAsteroide.lineTo((float)0.8, (float)0.6);
            pathAsteroide.lineTo((float)0.9, (float)0.9);
            pathAsteroide.lineTo((float)0.8, (float)1.0);
            pathAsteroide.lineTo((float)0.4, (float)1.0);
            pathAsteroide.lineTo((float)0.0, (float)0.6);
            pathAsteroide.lineTo((float)0.0, (float)0.2);
            pathAsteroide.lineTo((float)0.3, (float)0.0);
            ShapeDrawable dAsteroide = new ShapeDrawable(new PathShape(pathAsteroide,1,1));
            dAsteroide.getPaint().setColor(Color.WHITE);
            dAsteroide.getPaint().setStyle(Paint.Style.STROKE);
            dAsteroide.setIntrinsicWidth(50);
            dAsteroide.setIntrinsicHeight(50);
            drawableAsteroide = dAsteroide;

            Path pathNave = new Path();
            pathNave.moveTo((float)0.0,(float)0.0);
            pathNave.lineTo((float)1.0,(float)0.5);
            pathNave.lineTo((float)0.0,(float)1.0);
            pathNave.lineTo((float)0.0,(float)0.0);
            ShapeDrawable dNave = new ShapeDrawable(new PathShape(pathNave,1,1));
            dNave.getPaint().setColor(Color.WHITE);
            dNave.getPaint().setStyle(Paint.Style.STROKE);
            dNave.setIntrinsicWidth(50);
            dNave.setIntrinsicHeight(50);
            drawableNave = dNave;

            ShapeDrawable dMisil = new ShapeDrawable(new RectShape());
            dMisil.getPaint().setColor(Color.WHITE);
            dMisil.getPaint().setStyle(Paint.Style.STROKE);
            dMisil.setIntrinsicWidth(15);
            dMisil.setIntrinsicHeight(3);
            drawableMisil= dMisil;

            setBackgroundColor(Color.BLACK);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }else{
            drawableAsteroide = contexto.getResources().getDrawable(R.drawable.asteroide1);
            drawableNave = contexto.getResources().getDrawable(R.drawable.nave);
            drawableMisil = contexto.getResources().getDrawable(R.drawable.misil1);
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        nave = new Grafico(this, drawableNave);
        nave.setIncX(0.0);
        nave.setIncY(0.0);
        nave.setAngulo(0);
        nave.setRotacion(0);

        asteroides = new Vector<Grafico>();
        for(int i=0; i<numAsteroides; i++){
            Grafico asteroide = new Grafico(this, drawableAsteroide);
            asteroide.setIncY(Math.random()*4-2);
            asteroide.setIncX(Math.random() * 4 -2);
            asteroide.setAngulo((int)(Math.random() * 360));
            asteroide.setRotacion((int)(Math.random() * 8 -4));
            asteroides.add(asteroide);
        }

        //soniditos
        if (musica){
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            idDisparo = soundPool.load(contexto, R.raw.disparo, 0);
            idExplosion = soundPool.load(contexto, R.raw.explosion, 0);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for(Grafico asteroide : asteroides){
            do {
                asteroide.setCenX((int) (Math.random() * w));
                asteroide.setCenY((int) (Math.random() * h));
            }while(asteroide.distacia(nave) < (w + h) / 5);
        }
        nave.setCenX(w/2);
        nave.setCenY(h/2);
        ultimoProceso = System.currentTimeMillis();
        thread.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (asteroides){
            for(Grafico asteroides:asteroides){
                asteroides.dibujaGrafico(canvas);
            }
        }
        nave.dibujaGrafico(canvas);
        /*if(misilActivo){
            misil.dibujaGrafico(canvas);
        }*/

        for(int m=0; m<misiles.size(); m++){
            Grafico misil = misiles.get(m);
            misil.dibujaGrafico(canvas);
        }
    }

    protected void actualizaFisica(){
        long ahora = System.currentTimeMillis();
        if(ultimoProceso + PERIODO_PROCESO > ahora){
            return;
        }
        double factorMov = (ahora - ultimoProceso) / PERIODO_PROCESO;
        ultimoProceso = ahora;
        nave.setAngulo((int)(nave.getAngulo() + giroNave * factorMov));
        double nIncX = nave.getIncX() + aceleracionNave * Math.cos(Math.toRadians(nave.getAngulo())) * factorMov;
        double nIncY = nave.getIncY() + aceleracionNave * Math.sin(Math.toRadians(nave.getAngulo())) * factorMov;

        if(Math.hypot(nIncX,nIncY) <= MAX_VELOCIDAD_NAVE){
            nave.setIncX(nIncX);
            nave.setIncY(nIncY);
        }
        nave.incrementarPos(factorMov);
        for(Grafico asteroide : asteroides){
            asteroide.incrementarPos(factorMov);
        }
        //ACTUALIZAMOS POCISION DEL MISIL
        /*if(misilActivo){
            misil.incrementarPos(factorMov);
            tiempoMisil -= factorMov;
            if(tiempoMisil < 0){
                misilActivo = false;
            }else{
                for(int i=0; i<asteroides.size(); i++)
                if(misil.verificaColision(asteroides.elementAt(i))){
                    destruyeAsteroide(i);
                    break;
                }
            }
        }*/
        for(int m=0; m < misiles.size(); m++){
            Grafico misil = misiles.get(m);
            misil.incrementarPos(factorMov);
            //tiempoMisiles.set(m, (int) (tiempoMisiles.get(m) - factorMov));
            tiempoMisiles.setElementAt((int)(tiempoMisiles.get(m) - factorMov),m);
            if(tiempoMisiles.get(m) < 0){
                tiempoMisiles.remove(m);
                misiles.remove(m);
            }else{
                for(int i=0; i<asteroides.size(); i++) {
                    if (misil.verificaColision(asteroides.elementAt(i))) {
                        destruyeAsteroide(i);
                        misiles.remove(m);
                        tiempoMisiles.remove(m);
                        break;
                    }
                }
            }
        }
    }

    private void destruyeAsteroide(int i){
        synchronized (asteroides){
            asteroides.remove(i);
        }
        this.postInvalidate();
        if (musica) soundPool.play(idExplosion, 1,1,0,0,1);

    }

    private void activaMisil(){
        Grafico misil = new Grafico(this,drawableMisil);
        misil.setCenX(nave.getCenX());
        misil.setCenY(nave.getCenY());
        misil.setAngulo(nave.getAngulo());
        misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
        misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo())) * PASO_VELOCIDAD_MISIL);
        misiles.add(misil);
        //int tiempoMisil  = (int)Math.min(this.getWidth() / Math.abs(misil.getIncY()), this.getHeight() / Math.abs(misil.getIncY())) - 2;
        //misilActivo = true;
        tiempoMisiles.add(80);
        if (musica) soundPool.play(idDisparo, 1,1,1,0,1);

    }
    float valorInicial;
    boolean hayValor = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float valorX = event.values[0];
        float valorY = event.values[1];
        float valorZ = event.values[2];

        System.out.println(valorZ);
        if(valorY > 1){
            giroNave = +PASO_GIRO_NAVE;
        }else if(valorY < -1){
            giroNave = -PASO_GIRO_NAVE;
        }else{
            giroNave = 0;
        }

        if(!hayValor){
            valorInicial = valorZ;
            hayValor = true;
        }
        if(valorZ > 5){
            aceleracionNave = (valorZ - valorInicial)/9;
        }else{
            aceleracionNave = 0;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class ThreadJuego extends Thread{
        private boolean pausa, corriendo;

        public synchronized  void pausar(){
            pausa = true;
        }
        public synchronized  void reanudar(){
            pausa = false;
            notify();
        }
        public synchronized  void detener(){
            corriendo = false;
            if (pausa) reanudar();
        }
        @Override
        public void run() {
            corriendo = true;
            super.run();
            while (corriendo){
                actualizaFisica();
                synchronized (this){
                    while(pausa){
                        try{
                            wait();
                        }catch(Exception e){
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        boolean procesada = true;
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_UP:
                aceleracionNave = +PASO_ACELERACION_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                giroNave = -PASO_GIRO_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                giroNave = +PASO_GIRO_NAVE;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                activaMisil();
                break;
            default:
                procesada = false;
                break;
        }
        return procesada;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        boolean procesada = true;
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_UP:
                aceleracionNave = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                giroNave = 0;
                break;
            default:
                procesada = false;
                break;
        }
        return procesada;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if(tipoControl.equals("Pantalla Tactil")){
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    disparo = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if(dy<6 && dx>6){
                        giroNave = Math.round((x - mX) / 2);
                        disparo = false;
                    } else if(dx<6 && dy>6){
                        aceleracionNave = Math.round((mY - y) / 20);
                        disparo = false;
                        if(y > mY){
                            aceleracionNave = 0;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    giroNave = 0;
                    aceleracionNave = 0;
                    if(disparo){
                        activaMisil();
                    }
                    break;
            }
            mX=x;
            mY=y;
            return true;
        }else{
            return false;
        }
    }

    public ThreadJuego getThread() {
        return thread;
    }

    public void activarSensores(Context contexto){
        if(tipoControl.equals("Sensores de Movimiento")) {
            mSensorManager = (SensorManager) contexto.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> listSensor = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (!listSensor.isEmpty()) {
                Sensor acelerometroSensor = listSensor.get(0);
                mSensorManager.registerListener(this, acelerometroSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    public void desactivarSensores(){
        if(tipoControl.equals("Sensores de Movimiento")) {
            mSensorManager.unregisterListener(this);
        }
    }
}
