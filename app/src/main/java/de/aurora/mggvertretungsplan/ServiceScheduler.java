package de.aurora.mggvertretungsplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Rico on 14.11.2017.
 */

public class ServiceScheduler {

    private void scheduleService(Context context, long firstStartFromNow, long interval) {
        long firstStart = System.currentTimeMillis() + firstStartFromNow;
        Intent intentsOpen = new Intent(context, BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intentsOpen, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = getAlarmManager(context);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
        Log.d("ServiceScheduler", "Alarm scheduled!");
    }

    private void unscheduleService(Context context) {
        Log.d("ServiceScheduler", "Cancel service execution!");
        AlarmManager alarmManager = getAlarmManager(context);
        Intent intentsOpen = new Intent(context, BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intentsOpen, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);
    }


    void setAlarmManager(Context context, long firstStartFromNow) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if (sp.getBoolean("notification", true)) {
            String interval_s = sp.getString("AbrufIntervall", "1800000");
            long interval = Long.valueOf(interval_s);

            Date date = new Date(System.currentTimeMillis() + firstStartFromNow);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);

            Log.d("ServiceScheduler", String.format("Scheduling BackgroundService - Interval: %s - Next start: %s", interval_s, dateFormat.format(date)));
            scheduleService(context, firstStartFromNow, interval);
        } else {
            Log.d("ServiceScheduler", "Cancelling BackgroundService");
            unscheduleService(context);
        }
    }


    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }


}
