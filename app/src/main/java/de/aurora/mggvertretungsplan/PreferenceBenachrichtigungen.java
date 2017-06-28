package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import de.aurora.mggvertretungsplan.ui.LayoutSwitcher;

public class PreferenceBenachrichtigungen extends PreferenceActivity{

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		int themeID = sp.getInt("Theme", 0);
		setTheme(LayoutSwitcher.getTheme(themeID));
        super.onCreate(savedInstanceState);
        Toolbar toolbar;

        setContentView(R.layout.settings);
        
        toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("Benachrichtigungen");
		if(Build.VERSION.SDK_INT >= 21){
			toolbar.setElevation(25);
		}
		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
        getFragmentManager().beginTransaction().replace(R.id.content, new NotificationFragment()).commit();
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public boolean isValidFragment(String fragmentName){
        return super.isValidFragment(fragmentName);
    }

	//ActionBar zurück option
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	case android.R.id.home:
    	finish();
    break;
    
	default:	
    break;
    }
    return true;
	}


}
