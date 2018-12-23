package de.aurora.mggvertretungsplan.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Rico on 02.12.2017.
 */

public class StorageUtilities {
    private static final String TAG = "StorageUtilities";

    private static final String FILENAME = "data.json";

    public static String readFile(Context context) {
        Logger.d(TAG, "Reading file!");
        String content = "";
        try {
            FileInputStream fileInputStream = context.openFileInput(FILENAME);

            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                // The assignment here is needed to get the line as a String
                //noinspection NestedAssignment
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                fileInputStream.close();
                content = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            writeToFile(context, "");
        } catch (IOException e) {
            Logger.e(TAG, "File read failed: " + e.toString());
            return "";
        }

        Logger.d(TAG, "Finished reading");
        return content;
    }

    public static void writeToFile(final Context context, final String data) {
        // Save data to disk in background thread

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.d(TAG, "Writing to file");
                    FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(data.getBytes());
                    fos.close();
                    Logger.d(TAG, "Write finished");
                } catch (IOException e) {
                    Logger.e(TAG, "File write failed: " + e.toString());
                }
            }
        }, "dataSaver").start();
    }
}
