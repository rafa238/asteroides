package com.example.asteroides;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PreferenciasFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final EditTextPreference fragmentos = (EditTextPreference) findPreference("fragmentos");
        fragmentos.setSummary("En cuantos trozos se divide un asteroide ("+ prefs.getString("fragmentos", "3") +")");
        fragmentos.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int valor;
                try{
                    valor = Integer.parseInt((String)newValue);
                }catch(Exception e){
                    Toast.makeText(getActivity(), "Ha de ser un numero", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(valor>=0 && valor <= 9){
                    fragmentos.setSummary("En cuantos trozos se divide un asteroide ("+ valor +")");
                    return true;
                }else{
                    Toast.makeText(getActivity(), "Maximo de fragmentos 9", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }
}
