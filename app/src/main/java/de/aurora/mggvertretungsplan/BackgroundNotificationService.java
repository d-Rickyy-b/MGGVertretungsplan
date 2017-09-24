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
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;
import de.aurora.mggvertretungsplan.parsing.MGGParser;
import de.aurora.mggvertretungsplan.parsing.WebsiteParser;


public class BackgroundNotificationService extends Service implements AsyncTaskCompleteListener<String> {

    private SharedPreferences sp;

    public BackgroundNotificationService() {

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
        Log.d("BackgroundNotificationService", "UpdateData");
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
            Log.d("BackgroundNotificationService", "Sending notification!");

            //TODO replace deprecated method
            NotificationCompat.Builder notification = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
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
    }

    // Checks for an active connection
    private boolean isConnectionActive() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }


    public void onTaskComplete(String website_html) {
        Log.d("BackgroundNotificationService", "Checking for changes");
        WebsiteParser websiteParser = new MGGParser();
        String class_name = sp.getString("KlasseGesamt", "5a");

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<ArrayList<String>> tableOne_saved, tableTwo_saved;

        //TODO What if Day is null??
        TimeTable timeTable = websiteParser.parse(website_html, class_name);
        TimeTableDay dayOne = timeTable.getDay(0);
        TimeTableDay dayTwo = timeTable.getDay(1);

        tableOne_saved = hilfsMethoden.getArrayList(sp.getString("tableOne", ""));
        String dayOne_date = sp.getString("firstDate", "01.01.");
        TimeTableDay dayOne_saved = new TimeTableDay(dayOne_date, tableOne_saved);

        tableTwo_saved = hilfsMethoden.getArrayList(sp.getString("tableTwo", ""));
        String dayTwo_date = sp.getString("secondDate", "01.01.");
        TimeTableDay dayTwo_saved = new TimeTableDay(dayTwo_date, tableTwo_saved);

        int diffs_one, diffs_two;

        // TODO - fixed to 2 days
        if (dayOne.isSameDay(dayOne_saved)) {
            diffs_one = dayOne.getDifferences(dayOne_saved);
        } else if (dayOne.isSameDay(dayTwo_saved)) {
            diffs_one = dayOne.getDifferences(dayTwo_saved);
        } else {
            diffs_one = dayOne.getSize();
        }

        if (dayTwo.isSameDay(dayTwo_saved)) {
            diffs_two = dayTwo.getDifferences(dayTwo_saved);
        } else {
            diffs_two = dayTwo.getSize();
        }

        int changeCount = diffs_one + diffs_two;

        if (changeCount > 0) {
            if (changeCount > 1) {
                notification("Stundenplan Änderung!", "MGG Vertretungsplan", String.format("%s Änderungen!", changeCount));
            } else if (changeCount == 1) {
                notification("Stundenplan Änderung!", "MGG Vertretungsplan", "Eine Änderung!");
            }
            //TODO Save downloaded data
        }

    }

}