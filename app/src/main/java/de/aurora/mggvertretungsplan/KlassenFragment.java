package de.aurora.mggvertretungsplan;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class KlassenFragment extends PreferenceFragment {

    public KlassenFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.class_preferences);
    }
}
