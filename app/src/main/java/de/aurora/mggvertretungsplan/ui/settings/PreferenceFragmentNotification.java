package de.aurora.mggvertretungsplan.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.aurora.mggvertretungsplan.R;

public class PreferenceFragmentNotification extends PreferenceFragment {

    public PreferenceFragmentNotification() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_notification);
    }
}
