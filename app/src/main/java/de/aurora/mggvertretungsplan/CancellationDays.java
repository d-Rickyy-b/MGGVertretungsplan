package de.aurora.mggvertretungsplan;

import java.util.ArrayList;

/**
 * Created by Rico on 16.09.2017.
 */

class CancellationDays {
    ArrayList<ArrayList<String>> tableOne, tableTwo;

    CancellationDays(ArrayList<ArrayList<String>> tableOne, ArrayList<ArrayList<String>> tableTwo){
        this.tableOne = tableOne;
        this.tableTwo = tableTwo;
    }

    ArrayList<ArrayList<String>> getFirstDay(){
        return tableOne;
    }

    ArrayList<ArrayList<String>> getSecondDay(){
        return tableTwo;
    }
}
