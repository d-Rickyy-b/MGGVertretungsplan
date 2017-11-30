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
    ParsingCompleteListener callback;

    BaseParser(ParsingCompleteListener callback) {
        this.callback = callback;
    }

    // Downloads the websites specified in urls and calls the onTaskComplete method
    // of the parser, as soon as downloading has finished
    void downloadWebsite(String... urls) {
        new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urls);
    }

    // Place to initialize variables.
    // Must call the downloadWebsite method.
    // Standard behaviour: downloads the website found under timeTable_url
    public void startParsing() {
        downloadWebsite(getTimeTable_url());
    }

    // Method where the actual magic of parsing the string into a timeTable object happens.
    // This method needs to be overritten.
    abstract TimeTable parse(ArrayList<String> websites);

    // Calls the parse method to parse the website(s) into a timeTable object
    public void onTaskComplete(ArrayList<String> websites) {
        TimeTable timeTable = parse(websites);
        callback.onParsingComplete(timeTable);
    }

    // Must return the url, which will be opened in the UI after tapping on the
    // "Website" button to reach the website in a Chrome Tab
    public abstract String getTimeTable_url();
}
