package de.aurora.mggvertretungsplan.datamodel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.aurora.mggvertretungsplan.util.Logger;

/**
 * Created by Rico on 19.09.2017.
 */

public class TimeTable {
    private static final String TAG = "TimeTable";
    private final ArrayList<TimeTableDay> timeTableDays = new ArrayList<>();

    public TimeTable() {

    }

    public TimeTable(JSONArray jsonArray) {
        Logger.d(TAG, "Creating new TimeTable object from JSON");
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonDay = (JSONObject) jsonArray.get(i);
                TimeTableDay ttd = new TimeTableDay(jsonDay);
                addDay(ttd);
            } catch (JSONException e) {
                Logger.e(TAG, e.getMessage());
            }
        }
        Logger.d(TAG, "TimeTable object created");
    }

    // Adds a day to the right place via insertionsort
    public void addDay(TimeTableDay ttd) {
        int index = 0;
        for (int i = 0; i < timeTableDays.size(); i++) {
            if (ttd.getDate().before(timeTableDays.get(i).getDate())) {
                break;
            }
            index++;
        }

        timeTableDays.add(index, ttd);
    }

    public int getDaysCount() {
        return timeTableDays.size();
    }

    /**
     * Calculates the number of days of the current TimeTable, which are in the future
     * @return Number of days which are in the future
     */
    public int getFutureDaysCount() {
        return getFutureDaysCount(new Date());
    }

    public int getFutureDaysCount(Date currentDate) {
        int futureDays = 0;

        for (TimeTableDay ttd : timeTableDays) {
            if (ttd.isInFuture(currentDate)) {
                futureDays++;
            }
        }

        return futureDays;
    }

    public ArrayList<TimeTableDay> getAllDays() {
        return timeTableDays;
    }

    @SuppressWarnings("unused")
    public int getTotalCancellations(String className) {
        int cancellations = 0;

        for (TimeTableDay day : timeTableDays) {
            cancellations += day.getElementsCount(className);
        }

        return cancellations;
    }

    public TimeTable getTotalDifferences(TimeTable savedTimeTable, String className) {
        Logger.d(TAG, "Getting differences of saved and downloaded timetable!");
        TimeTable differencesTimeTable = new TimeTable();
        Date currentDate = new Date();
        ArrayList<TimeTableDay> savedDays = savedTimeTable.getAllDays();

        for (TimeTableDay ttd : timeTableDays) {
            boolean newDay = true;
            int sixteenHrsInMillisecs = 60 * 60 * 16 * 1000;

            if (currentDate.getTime() > ttd.getDate().getTime() + sixteenHrsInMillisecs) {
                Logger.d(TAG, String.format("Date in the past: %s, ignoring!", ttd.getDateString()));
                continue;
            }

            for (TimeTableDay saved_ttd : savedDays) {
                if (ttd.isSameDay(saved_ttd)) {
                    Logger.d(TAG, String.format("Dates are the same - %s | %s", ttd.getDateString(), saved_ttd.getDateString()));
                    Logger.d(TAG, String.format("%s", ttd.getElements(className).toString()));
                    Logger.d(TAG, String.format("%s", saved_ttd.getElements(className).toString()));
                    differencesTimeTable.addDay(ttd.getDifferences(saved_ttd, className));
                    newDay = false;
                    break;
                }
            }

            if (newDay) {
                int dayDiffs = ttd.getElementsCount(className);

                differencesTimeTable.addDay(ttd.getTTDbyClass(className));
                //TODO Bug - ALL elements are added regardless of the class
                Logger.d(TAG, String.format(Locale.getDefault(),"New Day found - %d cancellations for %s", dayDiffs, className));
            }
        }

        return differencesTimeTable;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (TimeTableDay ttd : timeTableDays) {
            result.append(ttd.toString()).append("\n");
        }

        return result.toString().trim();
    }

    public JSONArray toJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (TimeTableDay ttd : timeTableDays) {
            jsonArray.put(ttd.toJSON());
        }

        return jsonArray;
    }
}
