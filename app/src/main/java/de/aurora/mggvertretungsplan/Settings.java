package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    MainOptionFragment mainOptionFragment;
    public String klasseGesamt_saved;
    ListPreference classPref;
    Toolbar toolbar;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Einstellungen");

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        //Wenn auf den Pfeil oben links geklickt:
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Einstellungen übernommen!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        mainOptionFragment = new MainOptionFragment();
        getFragmentManager().beginTransaction().replace(R.id.content, mainOptionFragment).commit();

        klasseGesamt_saved = PreferenceManager.getDefaultSharedPreferences(this).getString("KlasseGesamt", "5a");
        getFragmentManager().executePendingTransactions();
        classPref = (ListPreference) mainOptionFragment.findPreference("Klasse");
        correctClassPicker();
    }


    //Toolbar zurück option
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(getApplicationContext(), "Einstellungen übernommen!", Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    //Wenn (gespeicherte) Daten geändert werden
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("Klassenstufe") || key.equals("Klasse")) {
            String klasseGesamt = getClassName(sharedPreferences);
            sharedPreferences.edit().putString("KlasseGesamt", klasseGesamt).apply();
        }
    }

    private String getClassName(SharedPreferences sharedPreferences) {
        String klassenstufe = sharedPreferences.getString("Klassenstufe", "5");
        if (klassenstufe.equals("K1") || klassenstufe.equals("K2")) {
            setClassPrefStatus(false);
            return klassenstufe;
        } else {
            setClassPrefStatus(true);
            return klassenstufe + sharedPreferences.getString("Klasse", "a");
        }
    }

    private void setClassPrefStatus(boolean status) {
        ListPreference classPref = (ListPreference) mainOptionFragment.findPreference("Klasse");
        classPref.setEnabled(status);
        classPref.setSelectable(status);
    }

    private void correctClassPicker() {
        if (klasseGesamt_saved.equals("K1") || klasseGesamt_saved.equals("K2")) {
            setClassPrefStatus(false);
        } else {
            setClassPrefStatus(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        correctClassPicker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
