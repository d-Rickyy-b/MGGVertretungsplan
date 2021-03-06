package de.aurora.mggvertretungsplan.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;
import de.aurora.mggvertretungsplan.parsing.BaseParser;
import de.aurora.mggvertretungsplan.parsing.BaseParser.ParsingCompleteListener;
import de.aurora.mggvertretungsplan.parsing.MGGParser;
import de.aurora.mggvertretungsplan.parsing.ParsingTask;
import de.aurora.mggvertretungsplan.util.Logger;
import de.aurora.mggvertretungsplan.util.NotificationHelper;
import de.aurora.mggvertretungsplan.util.StorageUtilities;

import static de.aurora.mggvertretungsplan.networking.ConnectionManager.isConnectionActive;

public class DownloadTimeTableWorker extends Worker implements ParsingCompleteListener {
    private final static String TAG = "DownloadTimeTableWorker";
    private BaseParser parser;
    private SharedPreferences sp;

    public DownloadTimeTableWorker(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        startService();
        Logger.d(TAG, "Finished doing work!");
        return Result.success();
    }

    private void startService() {
        Logger.d(TAG, "DownloadTimeTableWorker started!");
        parser = new MGGParser();
        updateData();
    }

    private void updateData() {
        Logger.d(TAG, "UpdateData");
        if (isConnectionActive(getApplicationContext())) {
            sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            try {
                ParsingTask parsingTask = new ParsingTask(this, parser);
                parsingTask.startParsing();
                parsingTask.get();
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        } else {
            Logger.d(TAG, "No internet Connection.");
        }
    }

    @Override
    public void onParsingComplete(TimeTable timeTable) {
        Logger.d(TAG, "Parsing complete - Checking for changes");

        if (timeTable == null) {
            Logger.d(TAG, "TimeTable is null");
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
                Logger.e(TAG, e.getMessage());
                return;
            }
        }

        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());

        TimeTable diffTimeTable = timeTable.getTotalDifferences(timeTable_saved, class_name);
        int totalDiffs = diffTimeTable.getTotalCancellations(class_name);
        Logger.d(TAG, String.format(Locale.getDefault(), "Total differences: %d", totalDiffs));

        if (totalDiffs <= 0) {
            Logger.d(TAG, "Not sending a notification!");
            return;
        }

        if (sp.getBoolean("show_detailed_notifications", true)) {
            // If the user wants beautiful, daily seperated notifications -> Notify indivitually
            for (TimeTableDay ttd : diffTimeTable.getAllDays()) {
                if (ttd.getElementsCount(class_name) > 0) {
                    Logger.d(TAG, "Notifying user");
                    notificationHelper.notifyChange(ttd);
                }
            }
        } else {
            // If the user does not want detailed notifications -> Notify about total number of changes
            Context context = getApplicationContext();
            String ticker = context.getResources().getString(R.string.notification_cancellations_ticker);
            String title = context.getResources().getString(R.string.notification_cancellations_title);
            String info = context.getResources().getQuantityString(R.plurals.notification_cancellations_info, totalDiffs, totalDiffs);
            notificationHelper.notifyChanges(ticker, title, info);
        }
        saveData(timeTable);
    }

    private void saveData(TimeTable timeTable) {
        Logger.d(TAG, "Saving data.json to disk");
        try {
            StorageUtilities.writeToFile(getApplicationContext(), timeTable.toJSON().toString());
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

}
