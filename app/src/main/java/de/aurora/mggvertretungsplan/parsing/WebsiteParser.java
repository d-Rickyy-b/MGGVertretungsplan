package de.aurora.mggvertretungsplan.parsing;

import java.util.ArrayList;

import de.aurora.mggvertretungsplan.AsyncTaskCompleteListener;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 22.09.2017.
 */

public interface WebsiteParser {

    void startDownload(AsyncTaskCompleteListener<ArrayList<String>> callback);

    TimeTable parse(ArrayList<String> websites, String className);

    String getTimeTable_url();
}
