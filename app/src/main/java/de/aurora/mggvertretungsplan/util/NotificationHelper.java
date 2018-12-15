package de.aurora.mggvertretungsplan.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import de.aurora.mggvertretungsplan.MainActivity;
import de.aurora.mggvertretungsplan.R;

public class NotificationHelper extends ContextWrapper {
    private static final String TAG = "NotificationHelper";
    private static final String defaultChannelName = "default";
    private static final String newsChannelName = "news";
    private NotificationManager notificationManager;
    private int notificationCounter = 0;


    public NotificationHelper(Context context) {
        super(context);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            Log.d(TAG, "Creating Notification Channels");
            createNotificationChannel(defaultChannelName, "Stundenplanänderungen", "Benachrichtigungen über Änderungen des Vertretungsplans");
            createNotificationChannel(newsChannelName, "Neuigkeiten", "Nachrichten der MGG Webseite");
        }
    }

    private void createNotificationChannel(String id, CharSequence name, String description) {
        // Notification channels have been implemented in API 26
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            this.notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String ticker, String titel, String text, String channel, PendingIntent pIntent) {
        int color;
        if (Build.VERSION.SDK_INT >= 23)
            color = getResources().getColor(R.color.colorAccent, getTheme());
        else
            //noinspection deprecation
            color = getResources().getColor(R.color.colorAccent);

        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this, channel)
                .setContentTitle(titel)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setTicker(ticker)
                .setColor(color)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setChannelId(channel);

        return notification_builder.build();
    }

    public void notifyNews(String ticker, String titel, String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = buildNotification(ticker, titel, text, newsChannelName, pIntent);
        notificationManager.notify(notificationCounter, notification);
        notificationCounter++;
    }

    public void notifyChanges(String ticker, String titel, String text) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = buildNotification(ticker, titel, text, defaultChannelName, pIntent);
        notificationManager.notify(notificationCounter, notification);
        notificationCounter++;
    }
}
