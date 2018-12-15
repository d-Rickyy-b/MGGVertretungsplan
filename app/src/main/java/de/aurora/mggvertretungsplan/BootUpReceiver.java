package de.aurora.mggvertretungsplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import de.aurora.mggvertretungsplan.services.BackgroundService;

public class BootUpReceiver extends BroadcastReceiver {
    private final static String TAG = "BootUpReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootUpReceiver called!");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            if (sp.getBoolean("notification", true)) {
                long interval = (long) Integer.valueOf(sp.getString("AbrufIntervall", "1800000"));
                long firstStart = System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS * 30;

                Intent intentsOpen = new Intent(context, BackgroundService.class);
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, intentsOpen, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if (null == alarmManager) {
                    Log.e("BootUpReceiver", "Couldn't get AlarmManager instance");
                    return;
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
            }
        }
    }

}