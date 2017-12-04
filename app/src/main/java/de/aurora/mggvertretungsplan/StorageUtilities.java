package de.aurora.mggvertretungsplan;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Rico on 02.12.2017.
 */

public class StorageUtilities {
    private static final String FILENAME = "data.json";

    static String readFile(Context context) {
        Log.d("StorageUtilities", "Reading file!");
        String content = "";
        try {
            FileInputStream fileInputStream = context.openFileInput(FILENAME);

            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                fileInputStream.close();
                content = stringBuilder.toString();
            }
        } catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
            return "";
        }

        Log.d("StorageUtilities", "Finished reading");
        return content;
    }

    static void writeToFile(final Context context, final String data) {
        // Save data to disk in background thread

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("StorageUtilities", "Writing to file");
                    FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(data.getBytes());
                    fos.close();
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }
        }).start();

        Log.d("StorageUtilities", "Write finished");
    }
}
