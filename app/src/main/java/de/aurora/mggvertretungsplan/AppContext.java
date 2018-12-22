package de.aurora.mggvertretungsplan;

import android.app.Application;
import android.content.Context;

public class AppContext extends Application {
    public static volatile Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
    }
}
