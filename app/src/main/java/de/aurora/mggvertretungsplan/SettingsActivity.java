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
import android.view.View;
import android.widget.Toast;

import de.aurora.mggvertretungsplan.ui.settings.PreferenceFragmentMain;
import de.aurora.mggvertretungsplan.ui.theming.ThemeManager;


public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private PreferenceFragmentMain preferenceFragmentMain;
    private String klasseGesamt_saved;
    private SharedPreferences sp;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        int themeID = sp.getInt("Theme", 0);
        setTheme(ThemeManager.getTheme(themeID));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);

        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_name);

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        // Toolbar 'back' option
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), R.string.toast_settings_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        preferenceFragmentMain = new PreferenceFragmentMain();
        getFragmentManager().beginTransaction().replace(R.id.content, preferenceFragmentMain).commit();

        klasseGesamt_saved = sp.getString("KlasseGesamt", "5a");
        getFragmentManager().executePendingTransactions();
        correctClassPicker();
    }


    // When saved data gets changed
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals("Klassenstufe") || key.equals("Klasse")) {
            String klasseGesamt = getClassName(sp);
            sp.edit().putString("KlasseGesamt", klasseGesamt).apply();
        } else if (key.equals("color")) {
            int color = sp.getInt("color", 0);
            int themeID = ThemeManager.getThemeID(color);
            sp.edit().putInt("Theme", themeID).apply();
            findViewById(android.R.id.content).invalidate();
            setTheme(ThemeManager.getTheme(themeID));
            recreate();
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
        ListPreference classPref = (ListPreference) preferenceFragmentMain.findPreference("Klasse");
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
        sp.registerOnSharedPreferenceChangeListener(this);
        correctClassPicker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

}
