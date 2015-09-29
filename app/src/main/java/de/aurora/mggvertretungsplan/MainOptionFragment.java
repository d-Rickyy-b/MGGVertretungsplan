package de.aurora.mggvertretungsplan;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class MainOptionFragment extends PreferenceFragment{
	
	public MainOptionFragment(){
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferenceheaders);
		}
}
