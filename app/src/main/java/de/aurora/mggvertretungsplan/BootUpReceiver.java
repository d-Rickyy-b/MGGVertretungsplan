package de.aurora.mggvertretungsplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            if (sp.getBoolean("notification", true)) {
                long interval = (long) Integer.valueOf(sp.getString("AbrufIntervall", "1800000"));
                long firstStart = System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS * 30;

                Intent intentsOpen = new Intent(context, BackgroundService.class);
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, intentsOpen, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
            }
        }
    }

}