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
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.parsing.BaseParser;
import de.aurora.mggvertretungsplan.parsing.BaseParser.ParsingCompleteListener;
import de.aurora.mggvertretungsplan.parsing.MGGParser;
import de.aurora.mggvertretungsplan.parsing.ParsingTask;


public class BackgroundService extends Service implements ParsingCompleteListener {
    private final static String TAG = "BackgroundService";
    private final static String CHANNEL_NAME = "default";
    private BaseParser websiteParser;
    private SharedPreferences sp;

    public BackgroundService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start Service");
        websiteParser = new MGGParser();
        updateData();
        stopSelf();
        return START_STICKY;
    }

    private void updateData() {
        Log.d(TAG, "UpdateData");
        if (isConnectionActive()) {
            sp = PreferenceManager.getDefaultSharedPreferences(this);

            try {
                ParsingTask parsingTask = new ParsingTask(this, websiteParser);
                parsingTask.startParsing();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.d(TAG, "No internet Connection. Scheduling next alarm in 10 mins.");
            long tenMinsInMillis = 60 * 10 * 1000;
            ServiceScheduler serviceScheduler = new ServiceScheduler();
            serviceScheduler.setAlarmManager(getApplicationContext(), tenMinsInMillis);
        }
    }

    private void notification(String ticker, String titel, String text) {
        if (sp.getBoolean("notification", true)) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d(TAG, "Sending notification!");

            int color;
            if (Build.VERSION.SDK_INT >= 23)
                color = getResources().getColor(R.color.colorAccent, getTheme());
            else
                //noinspection deprecation
                color = getResources().getColor(R.color.colorAccent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null)
                return;

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_NAME, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Notifications about changes of the timetable");
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
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_NAME);

            //.setVibrate(new long[]{0,300,200,300})
            //.setLights(Color.WHITE, 1000, 5000)

            notificationManager.notify(0, notification.build());
            Log.d(TAG, "Notification sent");
        }
    }

    // Checks for an active connection
    private boolean isConnectionActive() {
        try {
            final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

            return null != activeNetwork && activeNetwork.isConnected();
        } catch (NullPointerException e) {
            Log.e("MainActivity", e.getMessage());
            return false;
        }
    }

    @Override
    public void onParsingComplete(TimeTable timeTable) {
        Log.d(TAG, "Parsing complete - Checking for changes");

        if (timeTable == null) {
            Log.d(TAG, "TimeTable is null");
            return;
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String class_name = sp.getString("KlasseGesamt", "5a");


        TimeTable timeTable_saved = new TimeTable();
        String data = StorageUtilities.readFile(this);

        if (data.isEmpty()) {
            timeTable = new TimeTable();
        } else {
            try {
                JSONArray jsonArray = new JSONArray(data);
                timeTable = new TimeTable(jsonArray);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return;
            }
        }

        // Compare new data with old data
        int totalDiffs = timeTable.getTotalDifferences(timeTable_saved, class_name);
        Log.d(TAG, String.format("Total differences: %d", totalDiffs));

        String ticker = getResources().getString(R.string.notification_cancellations_ticker);
        String title = getResources().getString(R.string.notification_cancellations_title);
        String infoOne = getResources().getString(R.string.notification_cancellations_infoOne);
        String infoMany = getResources().getString(R.string.notification_cancellations_infoMany);

        if (totalDiffs == 1) {
            notification(ticker, title, String.format(infoOne, 1));
        } else if (totalDiffs > 1) {
            notification(ticker, title, String.format(infoMany, totalDiffs));
        }

        saveData(timeTable);
    }

    private void saveData(TimeTable timeTable) {
        Log.d(TAG, "Saving data.json to disk");
        try {
            StorageUtilities.writeToFile(this, timeTable.toJSON().toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}