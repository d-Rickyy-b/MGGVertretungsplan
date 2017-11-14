package de.aurora.mggvertretungsplan;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DownloadWebPageTask extends AsyncTask<String, Void, ArrayList<String>> {

    private final AsyncTaskCompleteListener<ArrayList<String>> callback;

    public DownloadWebPageTask(AsyncTaskCompleteListener<ArrayList<String>> callback) {
        this.callback = callback;
    }

    @Override
    protected ArrayList<String> doInBackground(String... urls) {
        ArrayList<String> websites = new ArrayList<>();

        for (String url : urls) {
            try {
                websites.add(downloadURL(url));
            } catch (IOException e) {
                Log.e("DownloadWebPageTask", e.getMessage());
            }
        }

        return websites;
    }

    private String downloadURL(String urlString) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            // int responseCode = conn.getResponseCode();
            is = conn.getInputStream();

            return readStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    protected void onPostExecute(ArrayList<String> websites) {
        callback.onTaskComplete(websites);
    }

    private String readStream(InputStream is) throws IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        return sb.toString();
    }
}
