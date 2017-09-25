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
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;
import de.aurora.mggvertretungsplan.parsing.MGGParser;
import de.aurora.mggvertretungsplan.parsing.WebsiteParser;


public class BackgroundService extends Service implements AsyncTaskCompleteListener<String> {

    private SharedPreferences sp;

    public BackgroundService() {

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
        Log.d("BackgroundService", "UpdateData");
        if (isConnectionActive()) {
            sp = PreferenceManager.getDefaultSharedPreferences(this);

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.vertretungsplan_url));
            } catch (Exception e) {
                Log.e(String.format("%s_service", getString(R.string.app_name)), e.getMessage());
            }
        }
    }

    private void notification(String ticker, String titel, String text) {
        if (sp.getBoolean("notification", true)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            Log.d("BackgroundService", "Sending notification!");

            int color;
            if (Build.VERSION.SDK_INT >= 23)
                color = getResources().getColor(R.color.accentColor, getTheme());
            else
                //noinspection deprecation
                color = getResources().getColor(R.color.accentColor);

            //TODO replace deprecated method
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "")
                    .setContentTitle(titel)
                    .setContentText(text)
                    .setTicker(ticker)
                    .setColor(color)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            //.setVibrate(new long[]{0,300,200,300})
            //.setLights(Color.WHITE, 1000, 5000)

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification.build());
            Log.d("BackgroundService", "Notification sent");
        }
    }

    // Checks for an active connection
    private boolean isConnectionActive() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }


    public void onTaskComplete(String website_html) {
        if (website_html.equals("")) {
            return;
        }

        Log.d("BackgroundService", "Checking for changes");
        WebsiteParser websiteParser = new MGGParser();
        String class_name = sp.getString("KlasseGesamt", "5a");

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<ArrayList<String>> tableOne_saved, tableTwo_saved;

        TimeTable timeTable = websiteParser.parse(website_html, class_name);

        if (timeTable == null)
            return;

        TimeTable timeTable_saved = new TimeTable();
        tableOne_saved = hilfsMethoden.getArrayList(sp.getString("tableOne", ""));
        String dayOne_date = sp.getString("firstDate", "01.01.");
        TimeTableDay dayOne_saved = new TimeTableDay(dayOne_date, tableOne_saved);

        tableTwo_saved = hilfsMethoden.getArrayList(sp.getString("tableTwo", ""));
        String dayTwo_date = sp.getString("secondDate", "01.01.");
        TimeTableDay dayTwo_saved = new TimeTableDay(dayTwo_date, tableTwo_saved);

        timeTable_saved.addDay(dayOne_saved);
        timeTable_saved.addDay(dayTwo_saved);

        // Compare new data with old data
        int totalDiffs = timeTable.getTotalDifferences(timeTable_saved);
        Log.d("BackgroundService", String.format("Total differences: %s", totalDiffs));

        String ticker = getResources().getString(R.string.notification_cancellations_ticker);
        String title = getResources().getString(R.string.notification_cancellations_title);
        String infoOne = getResources().getString(R.string.notification_cancellations_infoOne);
        String infoMany = getResources().getString(R.string.notification_cancellations_infoMany);

        if (totalDiffs > 1) {
            notification(ticker, title, String.format(infoMany, totalDiffs));
        } else if (totalDiffs == 1) {
            notification(ticker, title, infoOne);
        }
        //TODO Save downloaded data
    }

}