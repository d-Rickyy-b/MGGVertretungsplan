package de.aurora.mggvertretungsplan;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadWebPageTask extends AsyncTask<String, String, String> {

    private final AsyncTaskCompleteListener<String> callback;

    public DownloadWebPageTask(AsyncTaskCompleteListener<String> callback) {
        this.callback = callback;
    }

    protected String doInBackground(String... urls) {
        try {
            return downloadURL(urls[0]);
        } catch (IOException e) {
            return "";
        }
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
    protected void onPostExecute(String html) {
        callback.onTaskComplete(html);
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
