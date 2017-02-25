package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class PreferenceBenachrichtigungen extends PreferenceActivity{

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
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
