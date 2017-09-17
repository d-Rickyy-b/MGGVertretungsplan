package de.aurora.mggvertretungsplan.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.aurora.mggvertretungsplan.R;

public class MainOptionFragment extends PreferenceFragment{
	
	public MainOptionFragment(){
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferenceheaders);
	}
}
