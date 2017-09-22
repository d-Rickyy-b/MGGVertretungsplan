package de.aurora.mggvertretungsplan;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 22.09.2017.
 */

interface WebsiteParser {

    public TimeTable parse(String website_html, String className);

}
