package de.aurora.mggvertretungsplan;

//import java.util.List;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;



public class PreferenceWithHeaders extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	MainOptionFragment1 mainOptionFragment1;
	//SubOptionFragment subOptionFragment;
	public String klasseGesamt_saved;
	ListPreference listPref;
	Toolbar toolbar;
	
	
		@SuppressLint("NewApi")
		@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.settings);
	        
	        toolbar = (Toolbar) findViewById(R.id.toolbar);
//			setSupportActionBar(toolbar);
			//toolbar.setAlpha(20);
			toolbar.setTitle("Einstellungen");

			if(Build.VERSION.SDK_INT >= 21){
				toolbar.setElevation(25);
			}

			//Wenn auf den Pfeil oben links geklickt, dann "Einstellungen übernommen"
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v("Vertretungsplan","ToolBar zurück!");
			    	Toast.makeText(getApplicationContext(), "Einstellungen übernommen!", Toast.LENGTH_SHORT).show();
					finish();
				}
			});


	        mainOptionFragment1 = new MainOptionFragment1();
			//MainoptionFragment in Framelayout anzeigen
	        getFragmentManager().beginTransaction().replace(R.id.content, mainOptionFragment1).commit();
	        
	        klasseGesamt_saved = PreferenceManager.getDefaultSharedPreferences(this).getString("KlasseGesamt", "5a");
	        getFragmentManager().executePendingTransactions();
	        listPref = (ListPreference) mainOptionFragment1.findPreference("Klasse");
			correctKlassPicker();
			
	    }
	    

		//Toolbar zurück option
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	switch (item.getItemId()) {
		case android.R.id.home:
	    	Log.v("Vertretungsplan","ActionBar zurück!");
	    	Toast.makeText(getApplicationContext(), "Einstellungen übernommen!", Toast.LENGTH_SHORT).show();
			finish();
	    break;
	    
		default:	
	    break;
	    }
	    return true;
		}
	    
	    //Wenn (gespeicherte) Daten geändert werden
	    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    	System.out.println("##### SP changed: " + key + " #####");
	            if (key.equals("Klassenstufe")) {
            		String klasseGesamt = checkOnK1K2(sharedPreferences);
	            	sharedPreferences.edit().putString("KlasseGesamt", klasseGesamt).commit();
	            }
	            
	            if (key.equals("Klasse")) {
	            	String klasseGesamt = checkOnK1K2(sharedPreferences);
	            	sharedPreferences.edit().putString("KlasseGesamt", klasseGesamt).commit();
	            }
	            
	        }
	    
	    
	    private String checkOnK1K2(SharedPreferences sharedPreferences){
	    	String klassenstufe="",klasse;
	    	klassenstufe = sharedPreferences.getString("Klassenstufe", "5");
	    	if(klassenstufe.equals("K1") || klassenstufe.equals("K2")){
				klasse =  "";
				ListPreference listPref = (ListPreference) mainOptionFragment1.findPreference("Klasse");
				listPref.setEnabled(false);
				listPref.setSelectable(false);
			}else{
				klasse = sharedPreferences.getString("Klasse", "a");
				ListPreference listPref = (ListPreference) mainOptionFragment1.findPreference("Klasse");
				listPref.setEnabled(true);
				listPref.setSelectable(true);
			}
	    	System.out.println("klassenstufe+klasse: " + klassenstufe+klasse);
        	return klassenstufe+klasse;
	    }
	    
	    private void correctKlassPicker(){
	    	if(klasseGesamt_saved.equals("K1") || klasseGesamt_saved.equals("K2")){
	        	if (listPref!=null){
					System.out.println("Saved==K1.K2");
	        		listPref.setEnabled(false);
					listPref.setSelectable(false);
				}else{
					System.out.println("listpref==null");
				}
	        	
			}else{
				if (listPref!=null){
					System.out.println("Saved!=K1.K2");
					listPref.setEnabled(true);
					listPref.setSelectable(true);
				}else{
					System.out.println("listpref==null");
				}
			} 
	    }
	    
	    @Override
	    protected void onResume() {
	        super.onResume();
	        System.out.println("onResume()");
	        //listener erstellen
	        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	        correctKlassPicker();

	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        System.out.println("onPause()");
	        //listener beenden
	        PreferenceManager.getDefaultSharedPreferences(this)
	                .unregisterOnSharedPreferenceChangeListener(this);
	    }
	    
	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        //listener beenden
	    }


		public static class MainOptionFragment1 extends PreferenceFragment{

			public MainOptionFragment1(){
			}

			public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				addPreferencesFromResource(R.xml.preferenceheaders);
			}
		}

	    
	}
