package de.aurora.mggvertretungsplan.datamodel;

import java.util.ArrayList;

/**
 * Created by Rico on 19.09.2017.
 */

public class TimeTable {
    private final ArrayList<TimeTableDay> timeTableDays = new ArrayList<>();

    public TimeTable() {

    }

    // Adds a day right at the right place via insertionsort
    public void addTimeTableDay(TimeTableDay ttd) {
        int index = 0;
        for (int i = 0; i < timeTableDays.size(); i++) {
            if (ttd.getDate().before(timeTableDays.get(i).getDate())) {
                break;
            }
            index++;
        }

        this.timeTableDays.add(index, ttd);
    }

    public TimeTableDay getDay(int index) {
        if (index <= (timeTableDays.size() - 1)) {
            return timeTableDays.get(index);
        }

        return null;
        //TODO error
    }

    public ArrayList<TimeTableDay> getAllDays() {
        return this.timeTableDays;
    }

    public int getTotalCancellations() {
        int cancellations = 0;

        for (TimeTableDay day : timeTableDays) {
            cancellations += day.getCancellations();
        }

        return cancellations;
    }
}
