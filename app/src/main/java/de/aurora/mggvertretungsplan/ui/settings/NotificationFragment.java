package de.aurora.mggvertretungsplan.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.aurora.mggvertretungsplan.R;

public class NotificationFragment extends PreferenceFragment {

    public NotificationFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notification_preferences);
    }
}
