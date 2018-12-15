package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import de.aurora.mggvertretungsplan.ui.settings.PreferenceFragmentMain;
import de.aurora.mggvertretungsplan.ui.theming.ThemeManager;


public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private PreferenceFragmentMain preferenceFragmentMain;
    private SharedPreferences sp;
    private Toolbar toolbar;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        int themeID = sp.getInt("Theme", 0);
        setTheme(ThemeManager.getTheme(themeID));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);

        toolbar = findViewById(R.id.toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

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

            if (Build.VERSION.SDK_INT >= 16) {
                Intent intent = new Intent(this, SettingsActivity.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.fadein, R.anim.fadeout);
                startActivity(intent, options.toBundle());
                finish();
            } else {
                recreate();
            }
        }
    }

    private String getClassName(SharedPreferences sharedPreferences) {
        String grade = sharedPreferences.getString("Klassenstufe", "5");
        if (grade.equals("K1") || grade.equals("K2")) {
            setClassPrefStatus(false);
            return grade;
        } else {
            setClassPrefStatus(true);
            return grade + sharedPreferences.getString("Klasse", "a");
        }
    }

    private void setClassPrefStatus(boolean status) {
        ListPreference classPref = (ListPreference) preferenceFragmentMain.findPreference("Klasse");
        classPref.setEnabled(status);
        classPref.setSelectable(status);
    }

    // Checks which class is saved in the settings and en-/disables the access to the class picker.
    // When a class like "K1" or "K2" is selected, users shouldn't be able to choose 'a' as class.
    private void correctClassPicker() {
        String totalClass_saved = sp.getString("KlasseGesamt", "5a");

        if (totalClass_saved.equals("K1") || totalClass_saved.equals("K2")) {
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
