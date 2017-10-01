package de.aurora.mggvertretungsplan.parsing;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 29.09.2017.
 */

public class LimaCityParser implements WebsiteParser {

    private static final String timeTable_url = "http://rickyy.lima-city.de/html/vertretungsplan.html";

    @Override
    public TimeTable parse(String website_html, String className) {
        WebsiteParser mggParser = new MGGParser();
        return mggParser.parse(website_html, className);
    }

    @Override
    public String getTimeTable_url() {
        return timeTable_url;
    }
}
