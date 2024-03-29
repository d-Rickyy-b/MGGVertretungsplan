package de.aurora.mggvertretungsplan.datamodel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
            if (ttd.getDate().isBefore(timeTableDays.get(i).getDate())) {
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
     *
     * @return Number of days which are in the future
     */
    public int getFutureDaysCount() {
        return getFutureDaysCount(LocalDateTime.now());
    }

    public int getFutureDaysCount(LocalDateTime currentDate) {
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
        LocalDateTime currentDateTime = LocalDateTime.now();
        return getTotalDifferencesForSpecificTime(savedTimeTable, className, currentDateTime);
    }

    public TimeTable getTotalDifferencesForSpecificTime(TimeTable savedTimeTable, String className, LocalDateTime givenDateTime) {
        Logger.d(TAG, "Getting differences of saved and downloaded timetable!");
        TimeTable differencesTimeTable = new TimeTable();
        ArrayList<TimeTableDay> savedDays = savedTimeTable.getAllDays();

        for (TimeTableDay ttd : timeTableDays) {
            boolean newDay = true;
            LocalDateTime ttdDateTimeAt16 = LocalDateTime.of(ttd.getDate(), LocalTime.of(16, 0, 0));

            if (givenDateTime.isAfter(ttdDateTimeAt16)) {
                Logger.d(TAG, String.format("Date in the past: %s, ignoring!", ttd.getDateString()));
                continue;
            }

            for (TimeTableDay saved_ttd : savedDays) {
                if (ttd.isSameDay(saved_ttd)) {
                    Logger.d(TAG, String.format("Dates are the same - %s | %s", ttd.getDateString(), saved_ttd.getDateString()));
                    Logger.d(TAG, String.format("New TTD   : %s", ttd.getElements(className).toString()));
                    Logger.d(TAG, String.format("Stored TTD: %s", saved_ttd.getElements(className).toString()));
                    differencesTimeTable.addDay(ttd.getDifferences(saved_ttd, className));
                    newDay = false;
                    break;
                }
            }

            if (newDay) {
                int dayDiffs = ttd.getElementsCount(className);

                differencesTimeTable.addDay(ttd.getTTDbyClass(className));
                //TODO Bug - ALL elements are added regardless of the class
                Logger.d(TAG, String.format(Locale.getDefault(), "New Day found - %d cancellations for %s", dayDiffs, className));
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

    /**
     * Filters the timetable so that only entries of a certain class will stay. All other classes
     * will be removed.
     *
     * @param className The name of the class, which should be filtered on.
     * @return TimeTable object
     */
    public TimeTable filter(String className) {
        final TimeTable tt = new TimeTable();
        for (TimeTableDay ttd : timeTableDays) {
            tt.addDay(ttd.filter(className));
        }

        return tt;
    }

    public JSONArray toJSON() throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (TimeTableDay ttd : timeTableDays) {
            jsonArray.put(ttd.toJSON());
        }

        return jsonArray;
    }
}
