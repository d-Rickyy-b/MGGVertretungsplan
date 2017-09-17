package de.aurora.mggvertretungsplan;

import java.util.ArrayList;

/**
 * Created by Rico on 16.09.2017.
 */

class CancellationDays {
    private ArrayList<ArrayList<String>> tableOne, tableTwo;
    private ArrayList<String> datesList;

    CancellationDays(ArrayList<ArrayList<String>> tableOne, ArrayList<ArrayList<String>> tableTwo, ArrayList<String> datesList) {
        this.tableOne = tableOne;
        this.tableTwo = tableTwo;
        this.datesList = datesList;
    }

    ArrayList<ArrayList<String>> getFirstDay() {
        return tableOne;
    }

    ArrayList<ArrayList<String>> getSecondDay() {
        return tableTwo;
    }

    private String getDate(int day) {
        if (day < datesList.size()) {
            return datesList.get(day);
        } else {
            return "01.01.";
        }
    }

    String getFirstDate() {
        return getDate(0);
    }

    String getSecondDate() {
        return getDate(1);
    }
}
