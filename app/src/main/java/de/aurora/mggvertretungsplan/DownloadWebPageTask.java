package de.aurora.mggvertretungsplan;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DownloadWebPageTask extends AsyncTask<String, String, String> {

    private AsyncTaskCompleteListener<String> callback;

    public DownloadWebPageTask(AsyncTaskCompleteListener<String> callback) {
        this.callback = callback;
    }

    protected String doInBackground(String... urls) {
        //Hier wird die HTML Datei runtergeladen
        String response = "";

        for (String url : urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    //die Methode doInBackground fuehrt automatisch die Methode onPostExecute aus
    //Diese Methode kuerzt die HTML Datei so, dass nur noch die beiden Tabellen uebrig bleiben. Die Tabellen werden als String gespeichert.
    @Override
    protected void onPostExecute(String html) {
        callback.onTaskComplete(html);
    }
}
