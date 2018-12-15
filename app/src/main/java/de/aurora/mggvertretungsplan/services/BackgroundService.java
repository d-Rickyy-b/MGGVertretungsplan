package de.aurora.mggvertretungsplan.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONArray;
import org.json.JSONException;

import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.parsing.BaseParser;
import de.aurora.mggvertretungsplan.parsing.BaseParser.ParsingCompleteListener;
import de.aurora.mggvertretungsplan.parsing.MGGParser;
import de.aurora.mggvertretungsplan.parsing.ParsingTask;
import de.aurora.mggvertretungsplan.util.NotificationHelper;
import de.aurora.mggvertretungsplan.util.StorageUtilities;

import static de.aurora.mggvertretungsplan.networking.ConnectionManager.isConnectionActive;


public class BackgroundService extends JobService implements ParsingCompleteListener {
    public static final int JOB_ID = 0x01;
    private final static String TAG = "BackgroundService";
    private BaseParser websiteParser;
    private SharedPreferences sp;
    private JobParameters jobParameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        this.jobParameters = params;
        startService();
        return true; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // whether or not you would like JobScheduler to automatically retry your failed job.
        return true;
    }

    public void startService() {
        Log.d(TAG, "BackgroundService started!");
        websiteParser = new MGGParser();
        updateData();
    }

    private void updateData() {
        Log.d(TAG, "UpdateData");
        if (isConnectionActive(getApplicationContext())) {
            sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            try {
                ParsingTask parsingTask = new ParsingTask(this, websiteParser);
                parsingTask.startParsing();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                jobFinished(jobParameters, true);
            }
        } else {
            //Log.d(TAG, "No internet Connection. Scheduling next alarm in 10 mins.");
            Log.d(TAG, "No internet Connection.");
            jobFinished(jobParameters, true);
            //long tenMinsInMillis = 60 * 10 * 1000;
            //ServiceScheduler serviceScheduler = new ServiceScheduler();
            //serviceScheduler.setAlarmManager(getApplicationContext(), tenMinsInMillis);
            //serviceScheduler.scheduleSingleAlarm(getApplicationContext(), tenMinsInMillis);
        }
    }

    @Override
    public void onParsingComplete(TimeTable timeTable) {
        Log.d(TAG, "Parsing complete - Checking for changes");

        if (timeTable == null) {
            Log.d(TAG, "TimeTable is null");
            return;
        }

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String class_name = sp.getString("KlasseGesamt", "5a");


        TimeTable timeTable_saved = new TimeTable();
        String data = StorageUtilities.readFile(getApplicationContext());

        if (!data.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(data);
                timeTable_saved = new TimeTable(jsonArray);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                jobFinished(jobParameters, true);
                return;
            }
        }

        // Compare new data with old data
        int totalDiffs = timeTable.getTotalDifferences(timeTable_saved, class_name);

        // Get new cancellations
        // new_cancellations = ...

        // Get removed cancellations
        // removed_cancellations = ...

        // Get changed cancellations
        // changed_cancellations = ...

        // "x neue Ausfälle"
        // "x Änderung/en am Vertretugnsplan"
        // "..."


        Log.d(TAG, String.format("Total differences: %d", totalDiffs));

        String ticker = getResources().getString(R.string.notification_cancellations_ticker);
        String title = getResources().getString(R.string.notification_cancellations_title);
        String infoOne = getResources().getString(R.string.notification_cancellations_infoOne);
        String infoMany = getResources().getString(R.string.notification_cancellations_infoMany);

        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

        if (totalDiffs == 1) {
            notificationHelper.notifyChanges(ticker, title, String.format(infoOne, 1));
        } else if (totalDiffs > 1) {
            notificationHelper.notifyChanges(ticker, title, String.format(infoMany, totalDiffs));
        }

        saveData(timeTable);
        jobFinished(jobParameters, true);
    }

    private void saveData(TimeTable timeTable) {
        Log.d(TAG, "Saving data.json to disk");
        try {
            StorageUtilities.writeToFile(getApplicationContext(), timeTable.toJSON().toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}