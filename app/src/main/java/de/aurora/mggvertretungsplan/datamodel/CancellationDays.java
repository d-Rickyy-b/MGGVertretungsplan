package de.aurora.mggvertretungsplan.datamodel;

import java.util.ArrayList;

/**
 * Created by Rico on 16.09.2017.
 */

public class CancellationDays {
    private final ArrayList<ArrayList<String>> tableOne, tableTwo;
    private final ArrayList<String> datesList;

    public CancellationDays(ArrayList<ArrayList<String>> tableOne, ArrayList<ArrayList<String>> tableTwo, ArrayList<String> datesList) {
        this.tableOne = tableOne;
        this.tableTwo = tableTwo;
        this.datesList = datesList;
    }

    public ArrayList<ArrayList<String>> getFirstDay() {
        return tableOne;
    }

    public ArrayList<ArrayList<String>> getSecondDay() {
        return tableTwo;
    }

    private String getDate(int day) {
        if (day < datesList.size()) {
            return datesList.get(day);
        } else {
            return "01.01.";
        }
    }

    public String getFirstDate() {
        return getDate(0);
    }

    public String getSecondDate() {
        return getDate(1);
    }
}
