package de.aurora.mggvertretungsplan.parsing;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.parsing.BaseParser.ParsingCompleteListener;

/**
 * Created by Rico on 04.12.2017.
 */

public class ParsingTask extends AsyncTask<String, Void, TimeTable> {
    private static final String TAG = "ParsingTask";
    private final ParsingCompleteListener callback;
    private final BaseParser parser;

    public ParsingTask(ParsingCompleteListener callback, BaseParser parser) {
        this.callback = callback;
        this.parser = parser;
    }

    public void startParsing() {
        Log.d(TAG, "Start ParsingTask");
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, parser.getTimeTableURLs());
    }

    private String downloadURL(String urlString) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            Log.d(TAG, String.format("Downloading webpage: %s", url.toString()));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            // int responseCode = conn.getResponseCode();
            is = conn.getInputStream();

            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    protected TimeTable doInBackground(String... urls) {
        Thread.currentThread().setName("ParsingTask");

        // Download all the html websites as Strings
        ArrayList<String> websites = new ArrayList<>();

        for (String url : urls) {
            try {
                websites.add(downloadURL(url));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        // Parse the TimeTable
        //noinspection UnnecessaryLocalVariable
        TimeTable timeTable = this.parser.parse(websites);
        return timeTable;
    }

    @Override
    protected void onPostExecute(TimeTable timeTable) {
        // TODO move to background task
        callback.onParsingComplete(timeTable);
    }

}
