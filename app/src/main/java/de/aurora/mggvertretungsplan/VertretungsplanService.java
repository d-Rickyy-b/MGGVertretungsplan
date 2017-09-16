package de.aurora.mggvertretungsplan;

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
import android.util.Log;

import java.util.ArrayList;


public class VertretungsplanService extends Service implements AsyncTaskCompleteListener<String> {

    private SharedPreferences sp;
    private String klasse;

    public VertretungsplanService() {

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateData();
        //TODO Vibration zu Permissions hinzufügen
        stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    private void updateData() {
        Log.v("VertretungsplanService", "UpdateData");
        if (activeConnection()) {
            sp = PreferenceManager.getDefaultSharedPreferences(this);
            klasse = sp.getString("KlasseGesamt", "5a");

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.vertretungsplan_url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notification(String ticker, String titel, String text) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        Log.i("VertretungsplanService", "Notification!");
        @SuppressWarnings("deprecation")
        android.support.v7.app.NotificationCompat.Builder notification = (android.support.v7.app.NotificationCompat.Builder) new android.support.v7.app.NotificationCompat.Builder(this)
                .setContentTitle(titel)
                .setContentText(text)
                .setTicker(ticker)
                .setColor(getResources().getColor(R.color.accentColor))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        //.setVibrate(new long[]{0,300,200,300})
        //.setLights(Color.WHITE, 1000, 5000)

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification.build());
    }

    //Überprüft Internetverbindung (true = vorhandene Verbindung, false = keine Verbindung)
    private boolean activeConnection() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }


    public void onTaskComplete(String website_html) {
        Log.v("VertretungsplanService", "Check auf Vertretungen");
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<ArrayList<String>> tableOne, tableTwo, tableOne_saved, tableTwo_saved;

        CancellationDays cancellationDays = hilfsMethoden.parseTimetable(website_html, klasse);
        tableOne = cancellationDays.getFirstDay();
        tableTwo = cancellationDays.getSecondDay();

        tableOne_saved = hilfsMethoden.getArrayList(sp.getString("ersteTabelle", ""));
        tableTwo_saved = hilfsMethoden.getArrayList(sp.getString("zweiteTabelle", ""));

        int anzahlAusfaelle = tableOne.size() + tableTwo.size();

        if (anzahlAusfaelle > 0) {
            // TODO Testen ob Tabelle 1 = Tabelle 2
            int count1 = hilfsMethoden.getDifferencesCount(tableOne, tableOne_saved);
            int count2 = hilfsMethoden.getDifferencesCount(tableTwo, tableTwo_saved);
            int gesamt = (count1 + count2);
            if (gesamt > 1) {
                notification("Stundenplan Änderung!", "MGG Vertretungsplan", gesamt + " Änderungen!");
            } else if (gesamt == 1) {
                notification("Stundenplan Änderung!", "MGG Vertretungsplan", "Eine Änderung!");
            }
        }
        //TODO neue Listen speichern?

    }

}