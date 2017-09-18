package de.aurora.mggvertretungsplan.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.aurora.mggvertretungsplan.R;

public class PreferenceFragmentMain extends PreferenceFragment{
	
	public PreferenceFragmentMain(){
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_main);
	}
}
