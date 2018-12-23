package de.aurora.mggvertretungsplan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.aurora.mggvertretungsplan.services.ServiceScheduler;
import de.aurora.mggvertretungsplan.util.Logger;

public class BootUpReceiver extends BroadcastReceiver {
    private final static String TAG = "BootUpReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "BootUpReceiver called!");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            ServiceScheduler serviceScheduler = new ServiceScheduler();
            serviceScheduler.schedule(context);
        }
    }

}
