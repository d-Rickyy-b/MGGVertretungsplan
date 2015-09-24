package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String>, SwipeRefreshLayout.OnRefreshListener{

	//hier werden öffentliche Variablen erstellt
	//public String string, start, stop, klasse, Datum1, Datum2, tagStr, monatStr, jahrStr, response, datumHeute;
	//public int intervall,abrufDatumInt, index, jahr;
	private String klasse;
	private int jahr;
	//private static String PREFS_NAME = "MyPreferencesFile";
	public AdView adView;  //private InterstitialAd interstitial;
	SharedPreferences sp;
	private SwipeRefreshLayout mSwipeLayout;
	private recycleViewAdapter recycle_adapter;
	RecyclerView recyclerView;
	
	
	//TODO ArrayList als klassenvariable

	//Hier werden private Variablen erstellt
	private Menu optionsMenu;
	Toolbar toolbar;
	ActionBar actionbar; 
	
	/** Das wird ausgeführt sobald die Anwendung gestartet wird **/
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main4);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1,R.color.refresh_progress_2,R.color.refresh_progress_3);
		
		//setTitle("Vertretungsplan");
		
		toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);
		toolbar.setAlpha(20);
		toolbar.setTitle("Vertretungsplan");
//		toolbar.setSubtitle("Hallo");
		toolbar.showOverflowMenu();


		//toolbar.setLogo(R.drawable.ic_launcher);
		if(Build.VERSION.SDK_INT >= 21){
			toolbar.setElevation(25);
		}
		//toolbar.setLogo(R.drawable.ic_launcher);
		
		actionbar = getSupportActionBar();
		
		recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		adBanner1();
		
		//Zeigt die gespeicherten Daten in der Mitte an
		System.out.println("------ onCreate ------");
		//sp = getSharedPreferences(PREFS_NAME,0);
		gespeicherteDatenAnzeigen();	 
		//serviceProvider();
		
	}

	//Wenn Menüknopf gedrückt wird.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
			case KeyEvent.KEYCODE_MENU:
				return true;
			case KeyEvent.KEYCODE_BACK:
				onBackPressed();
				return true;
		}
		return false;
	}

	//Wenn Menüknopf losgelassen wird
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch(keyCode){
			case KeyEvent.KEYCODE_MENU:
				//Overflow menü öffnen/schließen
				if(toolbar.isOverflowMenuShowing()) {
					toolbar.dismissPopupMenus();
				} else {
					toolbar.showOverflowMenu();
				}
				return false;
		}
		return true;
	}


	public void onRefresh() {
		Log.v("Vertretungsplan","Auf refresh-Button geklickt!");
    	initialisierung();
	}
		
	
	public void serviceProvider(){
		//TODO Alarmmanager Code eingefügt 18.05.14
		if(sp.getBoolean("notification", true)){	//Einstellung abrufen		->		Standard: false
			AlarmManager(Integer.valueOf(sp.getString("AbrufIntervall", "1800000")));//Wenn eingestellt, dann AlarmManager starten -> Notifications
		}else{
			AlarmManagerBeenden();				//Wenn nicht eingestellt, dann AlarmManager beenden -> keine Notifications
		}
	}
	
	public void gespeicherteDatenAnzeigen(){
		TextView aktualisiertAmTextView = (TextView) findViewById(R.id.listText);
		String AbrufDatum = sp.getString("AbrufDatum", "Noch nie aktualisiert!");
		aktualisiertAmTextView.setText("Zuletzt aktualisiert am: " + AbrufDatum); //Die untere textview wird mit dem letuten AbrufDatum gefüllt
		
		String[][] ersteTabelleArr, zweiteTabelleArr; 
		String erDatum, zwDatum;
		
		//Nur Liste aktualisieren wenn schon mal aktualisiert wurde.
		//if(!AbrufDatum.equals("Noch nie aktualisiert!")){
			GregorianCalendar calender = new GregorianCalendar();
			jahr = calender.get(GregorianCalendar.YEAR);	//Jahr abfragen
			
			erDatum = sp.getString("erstesDatum", "01.01.2015");
			zwDatum = sp.getString("zweitesDatum", "01.01.2015");
			klasse = sp.getString("KlasseGesamt", "5a"); //abrufen der gespeicherten Klasse
			setTitle("Vertretungsplan ("+klasse+")");
			
			//boolean zusammenfassen = sp.getBoolean("zusammenfassen", true);
			ersteTabelleArr = stringToArray(sp.getString("ersteTabelle", ""));
			zweiteTabelleArr = stringToArray(sp.getString("zweiteTabelle", ""));
			
			anzeigen(ersteTabelleArr, zweiteTabelleArr, erDatum, zwDatum, sp.getBoolean("AktTagAnzeigen", true));
	}
	
	//Menü erstellen
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Öffnet Menu und fügt einträge hinzu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings, menu);
		return super.onCreateOptionsMenu(menu);
	}

	//Werbebanner hinzufügen
	public void adBanner1(){
		adView = (AdView)this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
		//.addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB")
		.build();
		
		adView.loadAd(adRequest);
	   }
	
	//wenn Menuitem selected
	//Aktion die ausgeführt werden soll, wenn action bar item geklickt wurde (auch aktualisieren)
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_settings:
				Log.v("Vertretungsplan", "Einstellungen werden aufgerufen!"); //Information für LogCat
				//Intent intent = new Intent(getApplicationContext(),de.aurora.mggvertretungsplan.PreferenceBenachrichtigungen.class);
				Intent intent = new Intent(getApplicationContext(),de.aurora.mggvertretungsplan.PreferenceWithHeaders.class);

				startActivity(intent);
				break;
			case R.id.action_webview:
				Log.v("Vertretungsplan", "Website wird aufgerufen!"); //Information für LogCat
				Intent intent2 = new Intent(getApplicationContext(),de.aurora.mggvertretungsplan.webView_Activity.class);
				startActivity(intent2);
				break;
			default:
				break;
		}

	    
	    if (item.getItemId() == R.id.action_info){
		    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	// Dialogeigenschaften bestimmen (Titel, Inhalt, Icon)
		    	builder
		    	.setIcon(R.drawable.ic_menu_info_details)
		    	.setTitle("MGG Vertretungsplan v" + getString(R.string.version))
		    	.setMessage("Programmiert von Rico Jambor");

		    	// Dialog erstellen und anzeigen
		    	AlertDialog dialog = builder.create();
		    	dialog.show();
		}
	    
	    return super.onOptionsItemSelected(item);
	  } 
	
	//AlarmManager Starten! -> Hintergrund Prozess
	public void AlarmManager(int intervall){
		//intervall = Zeit in Stunden, interval = Zeit in Minuten
		long interval = (long) intervall; 
		Log.v("Vertretungsplan", "Intervall (Minuten): " + ((interval/1000)/60));
		
		//Wann soll der Service das erste Mal gestartet werden?
		long firstStart = (long) (System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS*30); //30 Min nach setzen
		Date d = new Date();
		
		CharSequence s  = DateFormat.format("dd.MM.yy H:mm:ss", (d.getTime()+1800000));
		Log.v("Vertretungsplan", "Erster Start: " + s);
		
		Intent intentsOpen = new Intent(this, VertretungsplanService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this,0, intentsOpen, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
		Log.v("Vertretungsplan","AlarmManager gesetzt");
	}
	
	//beendet AlarmManager
	public void AlarmManagerBeenden()
	{
		Intent intentsOpen = new Intent(this, VertretungsplanService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this,0, intentsOpen, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
        //Intent intent = new Intent(context, Alarm.class);
        //PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		Log.v("VertretungsplanService","AlarmManager beendet");
    }
	

	/** Überprüft Internetverbindung **/
	public boolean aktiveVerbindung(){
				final ConnectivityManager conMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
				final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
				
				if(activeNetwork != null && activeNetwork.isConnected()){
					return true;
				} else{
					return false;
				}
			}
	
	/** überprüfen ob Klasse ausgewählt, ob Internetverbinding besteht, gibt Befehl zum Runterladen **/
	private void initialisierung(){
		Log.v("Vertretungsplan", "Netzwerkprüfung gestartet!" ); //Information für LogCat
		
		//hilfsMethoden hm = new hilfsMethoden();
		if (aktiveVerbindung()) { //wenn Verbingung vorhanden und wenn online...
			//if (false) { 
		    	//... dann wird die HTML runtergeladen
				adBanner1();
				
				//TODO ausblenden
				System.out.println("##### Intervall: " + sp.getString("AbrufIntervall", "1") + " #####");
				System.out.println("##### notification: " + sp.getBoolean("notification", true) + " #####");
				System.out.println("##### Klasse: " + sp.getString("KlasseGesamt", "") + " #####");
				
				//TODO setRefreshActionButtonState(true);//Aktualisierungs Animation wird gestartet
				mSwipeLayout.setRefreshing(true);
				Log.v("Vertretungsplan", "Verbindung vorhanden!"); //Information für LogCat
				
				klasse = sp.getString("KlasseGesamt", "5a"); //abrufen der gespeicherten Klasse
				
				sp = PreferenceManager.getDefaultSharedPreferences(this);
				
				try{
					//new DownloadWebPageTask().execute(getString(R.string.Url1));
					new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,getString(R.string.Url1));
				}catch(Exception e){
					//TODO setRefreshActionButtonState(false);

					mSwipeLayout.setRefreshing(false);
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
		}else {
		    //wenn offline, dann Bemerkung!

			mSwipeLayout.setRefreshing(false);
			Log.v("Vertretungsplan","Ein Fehler beim Downloaden (initialisierung) ist aufgetreten!");
			Toast.makeText(getApplicationContext(), "Keine Internetverbindung!", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	/** Macht aus dem String ein Array **/
	private String[][] stringToArray(String inputString){
		hilfsMethoden hm = new hilfsMethoden();
		String[][] outputArray = hm.stringKuerzen(inputString, klasse);
		return outputArray;
	}
	
	/** Initialisieren einer ArrayList, Hinzufügen von Items, Adapter an ListView binden**/
	private void anzeigen(String[][] ersteTabelleArr, String[][] zweiteTabelleArr, String erstesDatum,String zweitesDatum,boolean aktTagAnzeigen){
		hilfsMethoden hm = new hilfsMethoden(); 
		ListView listView = (ListView) findViewById(R.id.listView);
		
		ArrayList<Vertretungen> list = new ArrayList<>();
		
		//Date d = new Date();
		
		//CharSequence s  = DateFormat.format("dd.MM.yy", (d.getTime()));
		//CharSequence time  = DateFormat.format("dd.MM.yy H:mm:ss", (d.getTime()));
		//CharSequence s1 = DateFormat.format("dd.MM", d.getTime());
		
		long unixTime = System.currentTimeMillis() / 1000L;
		String AbrufDatum = new SimpleDateFormat("HH").format((unixTime+3600) * 1000L).toString();
		Date d = new Date();
		String tagAkt = (String) DateFormat.format("dd",d);		//Aktuellen Tag rausfinden
		String monatAkt = (String) DateFormat.format("MM",d);	//Aktuellen Monat rausfinden
		
//		System.out.println("HALLOOOOOOOOOO: " + AbrufDatum);
//		System.out.println("HALLOOOOOOOOO2: " + tagAkt);
		
		int monat1 = Integer.valueOf(erstesDatum.substring(3,5)), monat2 = Integer.valueOf(zweitesDatum.substring(3,5));
		int tag1 = Integer.valueOf(erstesDatum.substring(0,2)), tag2 = Integer.valueOf(zweitesDatum.substring(0,2));
		//String time1 = time.toString();
		//time1=time1.substring(0,1);
		String time1 = AbrufDatum;
		
		//Name des Tags bekommen
			String erstesDatumName = hm.getAnyDayByName(jahr, Integer.valueOf(erstesDatum.substring(3,5)), Integer.valueOf(erstesDatum.substring(0,2)));
			String zweitesDatumName = hm.getAnyDayByName(jahr, Integer.valueOf(zweitesDatum.substring(3,5)), Integer.valueOf(zweitesDatum.substring(0,2)));
		
		//TODO Freitag wird angezeigt!!!
		//Überprüft welcher Tag zuerst kommt, zeigt dann in richtiger Reihenfolge an.
			
			//Wenn der erste Tag vor dem zweiten kommt im gleichen Monat (normalfall), oder wenn der erste Monat vor dem zweiten kommt 
		if((tag1<tag2 && monat1==monat2) || (tag1>tag2 && monat1<monat2)){
			//Wenn Tag angezeigt werden soll, dann anzeigen 
			//ODER Wenn Tag nicht angezeigt werden soll, aber es noch vor 16 Uhr ist, dann anzeigen
			//ODER Wenn der Tag1 kleiner als der aktuelle ist, aber der aktuelle Monat kleiner als der monat1 ist 
			if(aktTagAnzeigen || 
				(!aktTagAnzeigen && Integer.valueOf(time1)<16 && tag1==Integer.valueOf(tagAkt) && monat1==Integer.valueOf(monatAkt)) ||
				(tag1>Integer.valueOf(tagAkt) && monat1==Integer.valueOf(monatAkt)) || 
				(tag1<Integer.valueOf(tagAkt) && monat1>Integer.valueOf(monatAkt))){
				//(20>Integer.valueOf(tagAkt) && Integer.valueOf(monatAkt)==monat1)
				
			
			//Tag 1	
			list.add(new Vertretungen("","","","","",erstesDatum,erstesDatumName));
			for(int i=0;i<ersteTabelleArr.length;i++){
				list.add(new Vertretungen(ersteTabelleArr[i][0]+". Stunde",hm.abkuerzung(ersteTabelleArr[i][2]),ersteTabelleArr[i][4],ersteTabelleArr[i][5],ersteTabelleArr[i][6],"",""));
			}
			list.add(new Vertretungen("","","","","","",""));				//Leerzeile
			} 
			
			//Tag 2
			list.add(new Vertretungen("","","","","",zweitesDatum,zweitesDatumName));
			for(int i=0;i<zweiteTabelleArr.length;i++){
				list.add(new Vertretungen(zweiteTabelleArr[i][0]+". Stunde",hm.abkuerzung(zweiteTabelleArr[i][2]),zweiteTabelleArr[i][4],zweiteTabelleArr[i][5],zweiteTabelleArr[i][6],"",""));
			}
			
		//Wenn der aktuelle Tag nicht angezeigt werden soll und das heutige Datum gleich dem ersten Datum ist, dann:	
		
			//wenn der erste Tag Nach dem 2. kommt (im selben Monat), oder wenn der erste Tag vor dem zweiten kommt, aber der erste Monat nach dem zweiten
		} else if ((tag1>tag2 && monat1==monat2) || (tag1<tag2 && monat1>monat2)){
			//Wenn Tag angezeigt werden soll, dann anzeigen
			if(aktTagAnzeigen || 
				(!aktTagAnzeigen && Integer.valueOf(time1)<16 && tag2==Integer.valueOf(tagAkt) && monat2==Integer.valueOf(monatAkt)) || 
				(!aktTagAnzeigen && tag2>Integer.valueOf(tagAkt) && monat2==Integer.valueOf(monatAkt)) || 
				(!aktTagAnzeigen && tag2<Integer.valueOf(tagAkt) && monat2>Integer.valueOf(monatAkt))
					){
				System.out.println("Das sollte nicht sein!");
			//Tag 1
				System.out.println("aktTag:" + aktTagAnzeigen + " | !aktTag&&val(time1)<16: " + (!aktTagAnzeigen && Integer.valueOf(time1)<16) + " |1: " + (tag2>Integer.valueOf(tagAkt)) + " |2: " + (tag2>Integer.valueOf(tagAkt) && monat2==Integer.valueOf(monatAkt)) + " |3: " + (tag2<Integer.valueOf(tagAkt) && monat2>Integer.valueOf(monatAkt)));
			list.add(new Vertretungen("","","","","",zweitesDatum,zweitesDatumName));
			for(int i=0;i<zweiteTabelleArr.length;i++){
				list.add(new Vertretungen(zweiteTabelleArr[i][0]+". Stunde",hm.abkuerzung(zweiteTabelleArr[i][2]),zweiteTabelleArr[i][4],zweiteTabelleArr[i][5],zweiteTabelleArr[i][6],"",""));
			}
			
			list.add(new Vertretungen("","","","","","",""));				//Leerzeile
			} 
			
			//Tag 2
			list.add(new Vertretungen("","","","","",erstesDatum,erstesDatumName));
			for(int i=0;i<ersteTabelleArr.length;i++){
				list.add(new Vertretungen(ersteTabelleArr[i][0]+". Stunde",hm.abkuerzung(ersteTabelleArr[i][2]),ersteTabelleArr[i][4],ersteTabelleArr[i][5],ersteTabelleArr[i][6],"",""));
			}
			
		}
		System.out.println("tag1: " + tag1 + " | tag2: " + tag2 + " |--| monat1: " + monat1 + " monat2: " + monat2);
		vertretungsplanArrayAdapter adapter = new vertretungsplanArrayAdapter(MainActivity.this,R.id.listView, list);
		//recycleViewAdapter adapter2 = new recycleViewAdapter();
//		recycle_adapter = new recycleViewAdapter(getApplicationContext(), list);
		
//		recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//		recyclerView.setAdapter(recycle_adapter);
		listView.setAdapter(adapter);
		mSwipeLayout.setRefreshing(false);
	}
	
	/** Wird aufgerufen wenn die Website heruntergeladen wurde **/
    public void onTaskComplete(String html)
	    {
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String beideTabellen, ersteTabelle, zweiteTabelle, erstesDatum,  zweitesDatum; //erstesDatumName,zweitesDatumName,tagStr
    	String[][] ersteTabelleArr, zweiteTabelleArr;
    	//boolean zusammenfassen = sp.getBoolean("zusammenfassen", true);
    	
    	TextView aktualisiertAmTextView = (TextView) findViewById(R.id.listText);
    	
    	//Markante Stellen im HTML code werden definiert
    	final String 	startPunkt = "___-1\"></a><h2 class=\"tabber_title\">", 
    					stoppPunkt = "</table><div style=\"clear:both;\"></div></div><div style=\"clear:both;\"></div>",	// geändert am 02.02.14 von </tbody></table><div style=\"clear:both;\"></div></div><div style || </table><div style=\"clear:both;\"></div>
    					trennPunkt = "___-2\"></a><h2 class=\"tabber_title\">";
    	
    	try{
    		/** ##############################	Anfang	##################################	**/
    		/** Beschneiden der Tabellen, Speichern in Strings, Auslesen des Datums aus html **/
    		beideTabellen = html.substring(html.indexOf(startPunkt), html.indexOf(stoppPunkt));			//kürzen des html Dokuments von vorne bis zur Tabelle
	    	ersteTabelle = beideTabellen.substring(0, beideTabellen.indexOf(trennPunkt)); //Von beideTabellen wird die erste Tabelle abgeschnitten 
	    	zweiteTabelle = beideTabellen.substring(beideTabellen.indexOf(trennPunkt), beideTabellen.length()); //Von beideTabellen wird die zweite Tabelle abgeschnitten
    		
	    	erstesDatum = ersteTabelle.substring(36,42); 	//Datum auslesen -> Datum steht an bestimmter Stelle im Quellcode
	    	zweitesDatum = zweiteTabelle.substring(36,42);	//Datum auslesen -> Datum steht an bestimmter Stelle im Quellcode
	    	/** ##############################	Ende	###################################	**/
	    	
	    	
	    	/** ##############################	Anfang	##################################	**/
    		/** "zuletzt aktualisiert" Teil. Unixzeit wird abgerufen, als Datum ausgegeben **/
	    	long unixTime = System.currentTimeMillis() / 1000L;
			//String AbrufDatum = DateFormat.format("dd.MM.yyyy  HH:mm", unixTime * 1000L).toString();
	    	//TODO String AbrufDatum = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(unixTime * 1000L).toString();
//	    	String AbrufDatum = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(unixTime * 1000L).toString(); 
	    	hilfsMethoden hm = new hilfsMethoden();
	    	String AbrufDatum = hm.getFormattedDate(System.currentTimeMillis());
	    	
			aktualisiertAmTextView.setText("Zuletzt aktualisiert" + AbrufDatum);
			/** ##############################	Ende	###################################	**/
			
	    	
			//String anzeigeText = "Heute ist " + tagStr + " der " + datumHeute + " \n" + " \n" + erstesDatum + jahr + " " + erstesDatumName + 
			//	" \n"+ ersteTabelle + " \n" + " \n" + " \n" + zweitesDatum + jahr + " " + zweitesDatumName +" \n"+ zweiteTabelle; //Hier werden die Tabellen in eine Form gebracht
			//AUSGEHEBELT hauptTextView.setText(anzeigeText); //Hier wird der gesamte Text in der großen TextView ausgegeben. 
			
			//Tabelle als Array wiedergeben
			ersteTabelleArr = stringToArray(ersteTabelle);
			zweiteTabelleArr = stringToArray(zweiteTabelle);
			
			//Anzeigenamen oben anzeigen
			setTitle("Vertretungsplan ("+klasse+")"); //TODO toolbar.setTitle?
			anzeigen(ersteTabelleArr, zweiteTabelleArr,erstesDatum,zweitesDatum,sp.getBoolean("AktTagAnzeigen", true));
			
			/** Speichern der abgerufenen Daten **/
			Log.v("Vertretungsplan","Daten werden gespeichert!");
	    	
	    	SharedPreferences.Editor editor = sp.edit();
	    	editor.putString("erstesDatum", erstesDatum);		//Datum als String "dd.MM"
	    	editor.putString("zweitesDatum", zweitesDatum);		//Datum als String "dd.MM"
	    	editor.putString("AbrufDatum", AbrufDatum);			//Datum als String "dd.MM.yyyy HH:mm"
	    	editor.putString("ersteTabelle", ersteTabelle);		//Erste Tabelle (aus html)
			editor.putString("zweiteTabelle", zweiteTabelle);	//Zweite Tabelle (aus html)
			editor.commit();
			Log.v("Vertretungsplan","Daten wurden erfolgreich gespeichert!");
			/** ############################## **/
			
			//TODO setRefreshActionButtonState(false); //Ladeanimation wird gestoppt
    	}catch (Exception e){
    		//TODO setRefreshActionButtonState(false);
    		//Log.v("Vertretungsplan","Fehler bei DownloadWebPageTask OnPostExecution");
    		e.printStackTrace();
    		//Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    		Toast.makeText(getApplicationContext(), "Ein interner Fehler ist aufgetreten!", Toast.LENGTH_SHORT).show();
		}
	        // parse the data here and show some results
	    }
	
	  public void onPause() {
		 if (adView != null) {
		      adView.pause();
		    }
	    super.onPause();
	  }

	  @Override
	  //Wenn die Hauptaktivität wieder aufgerufen wird
	  public void onResume() {
	    super.onResume();
	    System.out.println("------ onResume ------");
	    if (adView != null) {
	        adView.resume();
	      }
	    
	    initialisierung();
	    serviceProvider();
	  }

	  @Override
	  //Wenn die Aktivität beendet wird
	  public void onDestroy(){
		  super.onDestroy();
		  if (adView != null) {
		      adView.destroy();
		    }
	  }
	 
	/*
	 * Code by Rico Jambor
	 */
}
