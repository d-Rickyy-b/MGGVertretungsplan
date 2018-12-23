package de.aurora.mggvertretungsplan.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionManager {
    private final static String TAG = "ConnectionManager";

    public static boolean isConnectionActive(Context context) {
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null == conMgr) {
            return false;
        }

        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return null != activeNetwork && activeNetwork.isConnected();
    }

}
