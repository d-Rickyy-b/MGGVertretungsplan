package de.aurora.mggvertretungsplan;

import android.app.Notification;
import android.app.NotificationChannel;
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
    private final static String CHANNEL_NAME = "default";

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

    private void updateData() {
        Log.d("BackgroundService", "UpdateData");
        if (isConnectionActive()) {
            sp = PreferenceManager.getDefaultSharedPreferences(this);

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.vertretungsplan_url));
            } catch (Exception e) {
                Log.e("BackgroundService", e.getMessage());
            }
        }
    }

    private void notification(String ticker, String titel, String text) {
        if (sp.getBoolean("notification", true)) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d("BackgroundService", "Sending notification!");

            int color;
            if (Build.VERSION.SDK_INT >= 23)
                color = getResources().getColor(R.color.accentColor, getTheme());
            else
                //noinspection deprecation
                color = getResources().getColor(R.color.accentColor);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT > 26) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_NAME, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Benachrichtigungen über Änderungen des Vertretungsplans");
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_NAME)
                    .setContentTitle(titel)
                    .setContentText(text)
                    .setTicker(ticker)
                    .setColor(color)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            //.setVibrate(new long[]{0,300,200,300})
            //.setLights(Color.WHITE, 1000, 5000)

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
        ArrayList<ArrayList<String>> table;

        TimeTable timeTable = websiteParser.parse(website_html, class_name);

        if (timeTable == null)
            return;

        TimeTable timeTable_saved = new TimeTable();
        int count = sp.getInt("TT_Changes_Count", timeTable.getCount());

        for (int i = 0; i < count; i++) {
            table = JsonUtilities.getArrayList(sp.getString("table" + i, ""));
            String date = sp.getString("Date" + i, "01.01.");

            if (table != null && !table.isEmpty()) {
                TimeTableDay day = new TimeTableDay(date, table);
                timeTable_saved.addDay(day);
            }
        }

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