package de.aurora.mggvertretungsplan.parsing;

import android.os.AsyncTask;

import java.util.ArrayList;

import de.aurora.mggvertretungsplan.AsyncTaskCompleteListener;
import de.aurora.mggvertretungsplan.DownloadWebPageTask;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 28.11.2017.
 */

public abstract class BaseParser implements AsyncTaskCompleteListener<ArrayList<String>> {

    // Downloads the websites specified in urls and calls the onTaskComplete method of the parser
    void downloadWebsite(String... urls) {
        new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urls);
    }

    public abstract void startParsing(ParsingCompleteListener callback);

    public abstract TimeTable parse(ArrayList<String> websites);

    public abstract void onTaskComplete(ArrayList<String> websites);

    public abstract String getTimeTable_url();
}
