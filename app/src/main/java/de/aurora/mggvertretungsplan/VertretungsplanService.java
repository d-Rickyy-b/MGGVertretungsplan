package de.aurora.mggvertretungsplan;

import java.util.Arrays;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

//import android.support.v4.app.NotificationCompat;


public class VertretungsplanService extends Service implements AsyncTaskCompleteListener<String>{

	private String klasse, ersteTabelle_saved, zweiteTabelle_saved;// erstesDatum, erstesDatum_akt, zweitesDatum, zweitesDatum_akt;
	//private String klasse, ersteTabelle_saved, zweiteTabelle_saved, erstesDatum, zweitesDatum, erstesDatum_akt;
	SharedPreferences sp;
	
	public VertretungsplanService() {
		
	}

		public IBinder onBind(Intent intent) {
			//für bounded services
			return null;
		}
		
		@Override
		public void onCreate()
		{
			Date d = new Date();
			CharSequence s  = DateFormat.format("H:mm:ss", d.getTime());
			Log.v("VertretungsplanService", System.currentTimeMillis()+ ": VertretungsplanService erstellt" ); //Information für LogCat
			Log.v("VertretungsplanService", s + " - VertretungsplanService erstellt" ); //Information für LogCat
		}
		
		@Override
		public int onStartCommand(Intent intent, int flags, int startId){
			Log.v("VertretungsplanService", "Vertretungsplan-Service gestartet" ); //Information für LogCat
			
			updateData(); 		 
    		//TODO Vibration zu Permissions hinzufügen
            //Nachdem unsere Methode abgearbeitet wurde, soll sich der Service selbst stoppen
			stopSelf();
			//um den Service laufen zu lassen bis er explizit gestoppt wird, geben wir "START_STICKY" zurück
			return START_STICKY;
		}
		
		@Override
		public void onDestroy(){
			//super.onDestroy();
			Log.v("VertretungsplanService", "Vertretungsplan-Service zerstört" );
		}
		
		public void updateData(){
			Log.v("VertretungsplanService", "UpdateData");
			if (aktiveVerbindung()) {
			sp = PreferenceManager.getDefaultSharedPreferences(this);
			
			klasse = sp.getString("KlasseGesamt", "5a");
			ersteTabelle_saved = sp.getString("ersteTabelle", "");
			zweiteTabelle_saved = sp.getString("zweiteTabelle", "");
			//erstesDatum = sp.getString("erstesDatum", "01.01.2015");
			//zweitesDatum = sp.getString("zweitesDatum", "01.01.2015");
			//String url = getString(R.string.Url1);
			//TODO RAUSNEHMEN!!!
			
//			notification("Stundenplan Änderung!", "MGG Vertretungsplan ", 2 + " Änderungen!"); //Push mit der Nachricht "Es fällt etwas aus!"
			
				try{
					//DownloadWebPageTask dwt = new DownloadWebPageTask();
					//new DownloadWebPageTask().execute(getString(R.string.Url1));
					//notification("Stundenplan Änderung!", "MGG Vertretungsplan", 1 + " Änderung!"); //Push mit der Nachricht "Es fällt etwas aus!"
					new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,getString(R.string.Url1));
				}catch(Exception e){
					e.printStackTrace();
					//Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}
	
		// prepare intent which is triggered if the
		// notification is selected
		public void notification(String ticker, String titel, String text){
			
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
			Log.i("VertretungsplanService", "Notification!");
			// build notification
			@SuppressWarnings("deprecation")
			Notification n  = new Notification.Builder(this)
			        .setContentTitle(titel)
			        .setContentText(text)
			        .setTicker(ticker)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setContentIntent(pIntent)
			        .setAutoCancel(true)
			        .getNotification();
			//TODO statt .getNotification() -> .build() ... Android Version 14 (IceCreamSandwich) -> Version 16 (JellyBean)
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			n.flags |=Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(0, n); 
	}

		//Überprüft Internetverbindung (true = vorhandene Verbindung, false = keine Verbindung)
		public boolean aktiveVerbindung(){
					final ConnectivityManager conMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
					final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
					
					if(activeNetwork != null && activeNetwork.isConnected()){
						return true;
					} else{
						return false;
					}
				}
	
		
		public void onTaskComplete(String html)
	    {
			Log.v("VertretungsplanService", "onTaskComplete");
			String zwischenString, beideTabellen, ersteTabelle, zweiteTabelle;
	    	hilfsMethoden hm = new hilfsMethoden();
	    		
	    	int htmlLaenge = html.length();
	    	//int jahr;
	    	//boolean a, b;
	    	
	    	final String 	startPunkt = "___-1\"></a><h2 class=\"tabber_title\">", 
	    					stoppPunkt = "</table><div style=\"clear:both;\"></div></div><div style=\"clear:both;\"></div>",	// geändert am 02.02.14 von </tbody></table><div style=\"clear:both;\"></div></div><div style || </table><div style=\"clear:both;\"></div>
    						trennPunkt = "___-2\"></a><h2 class=\"tabber_title\">";
	
	    	
	    	try{
	    		//ersteTabelleAlt = sp.getString("ersteTabelle", "");
	    		//zweiteTabelleAlt = sp.getString("zweiteTabelle", "");
	    		
	    		zwischenString = html.substring(html.indexOf(startPunkt), htmlLaenge);				 //kürzen des html Dokuments von vorne bis zur Tabelle
		    	beideTabellen = zwischenString.substring(0, zwischenString.indexOf(stoppPunkt));		//kürzen des html Dokuments von hinten bis zur Tabelle -> hiermit ist nurnoch die Tabelle in HTML übrig
		    			  
		    	ersteTabelle = beideTabellen.substring(0, beideTabellen.indexOf(trennPunkt)); //Von beideTabellen wird die erste Hälfte abgeschnitten 
		    	zweiteTabelle = beideTabellen.substring(beideTabellen.indexOf(trennPunkt), beideTabellen.length()); //Von beideTabellen wird die zweite Hälfte abgeschnitten
		    	
		    	//erstesDatum_akt = ersteTabelle.substring(36,42); //Datum auslesen -> Datum steht an bestimmter Stelle im Quellcode
		    	//zweitesDatum_akt = zweiteTabelle.substring(36,42);
		    	
		    	
		    	int[] abstand1 = hm.haeufigkeit(ersteTabelle, klasse);
		    	int[] abstand2 = hm.haeufigkeit(zweiteTabelle, klasse);
		    	
		    	int anzahl1 = abstand1.length;  //anzahl beschreibt die Haeufigkeit, wie oft die gesuchte Klasse im String vorkommt -> wie viele Ausfälle gemeldet sind 
		    	int anzahl2 = abstand2.length;
		    	
		    	//boolean zusammenfassen = sp.getBoolean("zusammenfassen", true);
		    	boolean faelltEtwasAus = false;
		    	int anzahlAusfaelle = 0;
		    	
		    	//Wenn anzahl1 nicht 0 ist, dann gespeicherte Tabelle mit aktueller vergleichen.
		    	if (anzahl1!=0) {
		    	
		    		String[][] sArray1 = hm.stringKuerzen(ersteTabelle,klasse);
		    		String[][] sArray1_1 = hm.stringKuerzen(ersteTabelle_saved,klasse);
		    		
		    		
		    		if (!Arrays.deepEquals(sArray1, sArray1_1)){
		    			faelltEtwasAus = true;
		    			
		    			anzahlAusfaelle+=sArray1.length;
		    			/*
		    			for (int j = 0; j < anzahl1; j++) {
			    			//if (sArray1[j][3].equals("---") && sArray1[j][5].equals("---") && ersteTabelle!= ersteTabelle_saved) {
			    			if (sArray1[j][3].equals("---") && sArray1[j][5].equals("---") && erstesDatum_akt!=erstesDatum) {
			    				anzahlAusfaelle+=sArray1.length; //Diese Zeile Addiert JEDES mal die Länge der Tabelle zu dem Wert dazu, wenn eine ZEILE ausfällt
		    				}else{
								//Es fällt nichts aus!
							}
						}*/
		    		}
		    	}
		    		
		    	//Wenn anzahl2 nicht 0 ist, dann gespeicherte Tabelle mit aktueller vergleichen.
		    	if (anzahl2!=0) {
		    	
		    		String[][] sArray2 = hm.stringKuerzen(zweiteTabelle,klasse);
		    		String[][] sArray2_1 = hm.stringKuerzen(zweiteTabelle_saved,klasse);
		    		
		    		if (!Arrays.deepEquals(sArray2, sArray2_1)){
		    			faelltEtwasAus = true;
		    			
		    			//Wenn sich Arrays (neu und gespeichert) unterscheiden, dann Länge des neuen Array als Anzahl der Ausfälle
		    			anzahlAusfaelle+=sArray2.length;
		    			/*
		    			for (int j = 0; j < anzahl2; j++) {
			    			//if (sArray2[j][3].equals("---") && sArray2[j][5].equals("---") && zweiteTabelle != zweiteTabelle_saved) {
			    			if (sArray2[j][3].equals("---") && sArray2[j][5].equals("---") ){// && zweitesDatum_akt!=zweitesDatum) {
			    				anzahlAusfaelle+=sArray2.length; //Diese Zeile Addiert JEDES mal die Länge der Tabelle zu dem Wert dazu, wenn eine ZEILE ausfällt
		    				}else{
		    					//Es fällt nichts aus!
							}
			    		}*/
		    		}
	    		}
		    	
		    	
		    	if(faelltEtwasAus){
		    		if(anzahlAusfaelle>1){
		    			notification("Stundenplan Änderung!", "MGG Vertretungsplan", anzahlAusfaelle + " Änderungen!"); //Push mit der Nachricht "Es fällt etwas aus!"
		    		}else if(anzahlAusfaelle==1){
		    			notification("Stundenplan Änderung!", "MGG Vertretungsplan", anzahlAusfaelle + " Änderung!"); //Push mit der Nachricht "Es fällt etwas aus!"
		    		}else {
		    			Log.v("VertretungsplanService", "Fehler!");
					}
		    	}
		    	
		    	//TODO Abrufdatum	
		   		
	    	}catch (Exception e){
	    		System.out.println("Fehler");
	    		e.printStackTrace();
	    		//Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();				
	    	}
	    }
		
}