package de.aurora.mggvertretungsplan;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class NotificationFragment extends PreferenceFragment {

    public NotificationFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notification_preferences);
    }
}
