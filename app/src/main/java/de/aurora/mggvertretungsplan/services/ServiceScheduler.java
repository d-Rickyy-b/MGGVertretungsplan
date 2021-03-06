package de.aurora.mggvertretungsplan.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import de.aurora.mggvertretungsplan.util.Logger;

/**
 * Created by Rico on 14.11.2017.
 */

public class ServiceScheduler {
    private static final String TAG = "ServiceScheduler";
    private static final String WORK_TAG = "ScheduleDownloaderWorker";

    /**
     * Method to schedule the periodic execution of DownloadTimeTableWorker
     */
    private void scheduleService() {
        Logger.d(TAG, "Scheduling DownloadTimeTableWorker");
        long repeatInterval = 30L;

        // Only run task when network is connected
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest backgroundWork = new PeriodicWorkRequest.Builder(DownloadTimeTableWorker.class, repeatInterval, TimeUnit.MINUTES)
                .addTag(WORK_TAG)
                .setConstraints(constraints)
                .build();

        WorkManager workManager = WorkManager.getInstance();
        workManager.cancelAllWorkByTag(WORK_TAG);
        workManager.enqueue(backgroundWork);

        Logger.d(TAG, "Work scheduled!");
    }

    /**
     * Method to unschedule the DownloadTimeTableWorker
     */
    private void unscheduleService() {
        Logger.d(TAG, "Unscheduling DownloadTimeTableWorker");
        WorkManager workManager = WorkManager.getInstance();
        workManager.cancelAllWorkByTag(WORK_TAG);
    }

    /**
     * Method to schedule or unschedule the background service depending on the settings
     *
     * @param context Current application context to get the shared preferences from
     */
    public void schedule(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if (sp.getBoolean("notification", true)) {
            Logger.d(TAG, "Scheduling background work");
            scheduleService();
        } else {
            Logger.d(TAG, "Cancelling ScheduleDownloaderWorker, because user does not want notifications!");
            unscheduleService();
        }
    }

}
