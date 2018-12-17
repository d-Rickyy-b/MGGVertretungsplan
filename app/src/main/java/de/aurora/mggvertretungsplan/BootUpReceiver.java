package de.aurora.mggvertretungsplan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.aurora.mggvertretungsplan.services.ServiceScheduler;

public class BootUpReceiver extends BroadcastReceiver {
    private final static String TAG = "BootUpReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootUpReceiver called!");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            ServiceScheduler serviceScheduler = new ServiceScheduler();
            serviceScheduler.schedule(context);
        }
    }

}
