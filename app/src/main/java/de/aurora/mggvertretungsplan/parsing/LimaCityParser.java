package de.aurora.mggvertretungsplan.parsing;

import java.util.ArrayList;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 29.09.2017.
 */

@SuppressWarnings("unused")
public class LimaCityParser extends BaseParser {
    private static final String timeTable_url = "http://rickyy.lima-city.de/html/vertretungsplan.html";

    @Override
    public TimeTable parse(ArrayList<String> websites) {
        //TODO Fix this any time in the future
        return null;
    }

    @Override
    public String[] getTimeTableURLs() {
        return new String[0];
    }
}
