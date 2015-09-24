package de.aurora.mggvertretungsplan;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class 	SubOptionFragment extends PreferenceFragment{
	
	public SubOptionFragment(){
		
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.notification_preferences);
		}
}
