package com.example.asteroides;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.PersistableBundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static AlmacenPuntuaciones almacen = new AlmacenPuntuacionesArray();
    TextView titulo;
    Button btJugar;
    Button btAceca;
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        titulo = (TextView)findViewById(R.id.app_titulo);
        //Animaciones de pruebas
        /*Animation animacion = AnimationUtils.loadAnimation(this, R.anim.giro_con_zoom);
        titulo.startAnimation(animacion);
        btJugar = (Button)findViewById(R.id.button01);
        Animation animacion01 = AnimationUtils.loadAnimation(this,R.anim.aparecer);
        btJugar.startAnimation(animacion01);
        btAceca = (Button)findViewById(R.id.button02);
        Animation animacion02 = AnimationUtils.loadAnimation(this,R.anim.dezplazamiento_derecha);
        btAceca.startAnimation(animacion02);*/
        mp = MediaPlayer.create(this, R.raw.sonidito);
        mp.start();
    }

    public void lanzarPuntuaciones(View view){
        Intent intent = new Intent(this, Puntuaciones.class);
        startActivity(intent);
    }
    public void lanzarJuego(View view){
        Intent intent = new Intent(this, Juego.class);
        startActivity(intent);
    }

    public void salir(View view){
        this.finish();
    }
    public  void lanzarPreferencias(View v){
        Intent intent = new Intent(this, PreferenciasActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                lanzarPreferencias(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle guardarEstado) {
        super.onSaveInstanceState(guardarEstado);
        if(mp != null) {
            int pos = mp.getCurrentPosition();
            guardarEstado.putInt("posicion", pos);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle estadoGuardado) {
        super.onRestoreInstanceState(estadoGuardado);
        if (estadoGuardado != null && mp != null ){
            int pos = estadoGuardado.getInt("posicion");
            mp.seekTo(pos);
        }

    }
}
