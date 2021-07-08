package com.example.asteroides;

import android.app.Activity;
import android.os.Bundle;

public class Juego extends Activity{
    VistaJuego vistajuego;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.juego);
        vistajuego = (VistaJuego)findViewById(R.id.VistaJuegoods);
    }

    @Override
    protected void onPause() {
        vistajuego.getThread().pausar();
        vistajuego.desactivarSensores();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vistajuego.getThread().reanudar();
        vistajuego.activarSensores(this);
    }

    @Override
    protected void onDestroy() {
        vistajuego.getThread().detener();
        super.onDestroy();
    }
}