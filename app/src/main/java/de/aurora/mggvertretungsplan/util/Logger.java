package de.aurora.mggvertretungsplan.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import de.aurora.mggvertretungsplan.AppContext;

public class Logger {
    private static final String TAG = "Logger";
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static volatile Logger instance = null;
    private OutputStreamWriter streamWriter;
    private File logFile;
    DateTimeFormatter fullDateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS", Locale.getDefault());

    public static Logger getInstance() {
        Logger localInstance = instance;
        if (localInstance == null) {
            synchronized (Logger.class) {
                localInstance = instance;
                if (localInstance == null) {
                    localInstance = new Logger();
                    instance = localInstance;
                }
            }
        }

        return localInstance;
    }

    public Logger() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        try {
            File sdCard = AppContext.applicationContext.getExternalFilesDir(null);

            if (sdCard == null) {
                Log.e(TAG, "external storage path is null");
                return;
            }
            File dir = new File(sdCard.getAbsolutePath() + "/logs");
            Log.v(TAG, dir.toString());
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            logFile = new File(dir, dateFormat.format(LocalDateTime.now()) + ".txt");
            Log.v(TAG, logFile.toString());
            boolean res = logFile.createNewFile();

            FileOutputStream stream = new FileOutputStream(logFile);
            streamWriter = new OutputStreamWriter(stream);
            streamWriter.write("-----Start log " + dateFormat.format(LocalDateTime.now()) + "-----\n");
            streamWriter.flush();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void sendLogs() {

    }

    public static void deleteLogs() {
        File sdCard = AppContext.applicationContext.getExternalFilesDir(null);

        if (sdCard == null) {
            return;
        }

        File dir = new File(sdCard.getAbsolutePath() + "/logs");
        deleteRecursively(dir);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                deleteRecursively(child);
            }
        } else {
            file.delete();
        }
    }

    public static void d(final String tag, final String message) {
        Log.d(tag, message);
        writeLog("DEBUG", tag, message);
    }

    public static void v(final String tag, final String message) {
        Log.v(tag, message);
        writeLog("VERBOSE", tag, message);
    }

    public static void i(final String tag, final String message) {
        Log.i(tag, message);
        writeLog("INFO", tag, message);
    }

    public static void w(final String tag, final String message) {
        Log.w(tag, message);
        writeLog("WARN", tag, message);
    }

    public static void e(final String tag, final String message) {
        Log.e(tag, message);
        writeLog("ERROR", tag, message);
    }

    public static void wtf(final String tag, final String message) {
        Log.wtf(tag, message);
        writeLog("WTF", tag, message);
    }

    private void writeLog(String data) {
        try {
            this.streamWriter.write(data);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static void writeLog(final String level, final String tag, final String message) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppContext.applicationContext);
        boolean logToFile = sp.getBoolean("logToFile", false);

        if (!logToFile)
            return;

        Logger logger = getInstance();
        String timestamp = logger.fullDateFormat.format(LocalDateTime.now());
        logger.writeLog(String.format("%s - %s/%s - %s\n", timestamp, level, tag, message));
    }
}
