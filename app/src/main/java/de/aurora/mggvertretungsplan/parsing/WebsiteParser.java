package de.aurora.mggvertretungsplan.parsing;

import de.aurora.mggvertretungsplan.AsyncTaskCompleteListener;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 22.09.2017.
 */

public interface WebsiteParser {

    void startDownload(AsyncTaskCompleteListener<String> callback);

    TimeTable parse(String website_html, String className);

    String getTimeTable_url();
}
