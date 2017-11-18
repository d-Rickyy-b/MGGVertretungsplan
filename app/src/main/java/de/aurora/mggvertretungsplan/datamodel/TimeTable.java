package de.aurora.mggvertretungsplan.datamodel;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Rico on 19.09.2017.
 */

public class TimeTable {
    private final ArrayList<TimeTableDay> timeTableDays = new ArrayList<>();
    private final String className;

    public TimeTable(String className) {
        this.className = className;
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

    // Returns the number of days, which date is in the future
    public int getFutureDaysCount() {
        int futureDays = 0;

        for (TimeTableDay ttd : timeTableDays) {
            int sixteenHrsInMillis = 16 * 60 * 60 * 1000;
            if (ttd.getDate().getTime() + sixteenHrsInMillis >= (new Date()).getTime()) {
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

    public int getTotalDifferences(TimeTable savedTimeTable, String className) {
        int differences = 0;
        Date currentDate = new Date();
        ArrayList<TimeTableDay> savedDays = savedTimeTable.getAllDays();

        for (TimeTableDay ttd : timeTableDays) {
            boolean newDay = true;
            int sixteenHrsInMillisecs = 60 * 60 * 16 * 1000;

            if (currentDate.getTime() > ttd.getDate().getTime() + sixteenHrsInMillisecs) {
                Log.d("TimeTable", String.format("Date in the past: %s, ignoring!", ttd.getDateString()));
                continue;
            }

            for (TimeTableDay saved_ttd : savedDays) {
                if (ttd.isSameDay(saved_ttd)) {
                    Log.d("TimeTable", String.format("Dates are the same - %s | %s", ttd.getDateString(), saved_ttd.getDateString()));
                    Log.d("TimeTable", String.format("%s", ttd.getElements(className).toString()));
                    Log.d("TimeTable", String.format("%s", saved_ttd.getElements(className).toString()));
                    differences += ttd.getDifferences(saved_ttd, className);
                    newDay = false;
                    break;
                }
            }

            if (newDay) {
                int dayDiffs = ttd.getElementsCount(className);
                differences += dayDiffs;
                Log.d("TimeTable", String.format("New Day found - %d cancellations for %s", dayDiffs, className));
            }
        }

        return differences;
    }

    @Override
    public String toString() {
        String result = "";

        for (TimeTableDay ttd : timeTableDays) {
            result += ttd.toString();
        }

        return result;
    }
}
