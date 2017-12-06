package de.aurora.mggvertretungsplan.parsing;

import java.util.ArrayList;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 28.11.2017.
 */

public abstract class BaseParser {

    BaseParser() {
    }

    // Method where the actual magic of parsing the string into a timeTable object happens.
    // This method needs to be overritten.
    public abstract TimeTable parse(ArrayList<String> websites);

    // Must return an Array of at least one url, which will be downloaded.
    // URL at pos. 0 will be opened in the UI after tapping on the
    // "Website" button to reach the website in a Chrome Tab
    public abstract String[] getTimeTableURLs();

}
