package de.aurora.mggvertretungsplan;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


class DownloadWebPageTask extends AsyncTask<String, String, String> {

    private final AsyncTaskCompleteListener<String> callback;

    public DownloadWebPageTask(AsyncTaskCompleteListener<String> callback) {
        this.callback = callback;
    }

    protected String doInBackground(String... urls) {
        String response = "";
        for (String url_string : urls) {
            try {
                URL url = new URL(url_string);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    response = readStream(in);
                } catch (IOException ioException) {
                    Log.e("DownloadWebPageTask", ioException.getMessage());
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception exception) {
                Log.e("DownloadWebPageTask", exception.getMessage());
            }
        }
        return response;
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
        is.close();
        return sb.toString();
    }
}
