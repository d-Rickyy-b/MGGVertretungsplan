package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class PreferenceKlasse extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    KlassenFragment klassenFragment;
	Toolbar toolbar;
    public String klasseGesamt_saved;
	
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("Klasse");
		if(Build.VERSION.SDK_INT >= 21){
			toolbar.setElevation(25);
		}
		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        klasseGesamt_saved = PreferenceManager.getDefaultSharedPreferences(this).getString("KlasseGesamt", "5a");
        klassenFragment = new KlassenFragment();
		getFragmentManager().beginTransaction().replace(R.id.content, klassenFragment).commit();
        getFragmentManager().executePendingTransactions();
        correctClassPicker();
    }


	//ActionBar zurück option
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
    	        finish();
                break;
	        default:
                break;
        }
        return true;
	}

    //Wenn eine Einstellung geändert wird
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
        ListPreference listPref = (ListPreference) klassenFragment.findPreference("Klasse");
        listPref.setEnabled(status);
        listPref.setSelectable(status);
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
