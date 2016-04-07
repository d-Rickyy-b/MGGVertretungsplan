package de.aurora.mggvertretungsplan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Arrays;
import java.util.Date;


public class VertretungsplanService extends Service implements AsyncTaskCompleteListener<String> {

    SharedPreferences sp;
    private String klasse, ersteTabelle_saved, zweiteTabelle_saved;

    public VertretungsplanService() {

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Date d = new Date();
        CharSequence s = DateFormat.format("H:mm:ss", d.getTime());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateData();
        //TODO Vibration zu Permissions hinzufügen
        stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    public void updateData() {
        Log.v("VertretungsplanService", "UpdateData");
        if (aktiveVerbindung()) {
            sp = PreferenceManager.getDefaultSharedPreferences(this);

            klasse = sp.getString("KlasseGesamt", "5a");
            ersteTabelle_saved = sp.getString("ersteTabelle", "");
            zweiteTabelle_saved = sp.getString("zweiteTabelle", "");

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.Url1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notification(String ticker, String titel, String text) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        Log.i("VertretungsplanService", "Notification!");
        @SuppressWarnings("deprecation")
        Notification n = new Notification.Builder(getApplicationContext())
                .setContentTitle(titel)
                .setContentText(text)
                .setTicker(ticker)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .getNotification();
        //TODO statt .getNotification() -> .build() ... Android Version 14 (IceCreamSandwich) -> Version 16 (JellyBean)
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, n);
    }

    //Überprüft Internetverbindung (true = vorhandene Verbindung, false = keine Verbindung)
    public boolean aktiveVerbindung() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.icon_inverted : R.drawable.app_logo_material;
    }


    public void onTaskComplete(String html) {
        Log.v("VertretungsplanService", "onTaskComplete");
        String zwischenString, beideTabellen, ersteTabelle, zweiteTabelle;
        hilfsMethoden hm = new hilfsMethoden();

        int htmlLaenge = html.length();

        final String startPunkt = "___-1\"></a><h2 class=\"tabber_title\">",
                stoppPunkt = "</table><div style=\"clear:both;\"></div></div><div style=\"clear:both;\"></div>",    // geändert am 02.02.14 von </tbody></table><div style=\"clear:both;\"></div></div><div style || </table><div style=\"clear:both;\"></div>
                trennPunkt = "___-2\"></a><h2 class=\"tabber_title\">";


        try {
            zwischenString = html.substring(html.indexOf(startPunkt), htmlLaenge);
            beideTabellen = zwischenString.substring(0, zwischenString.indexOf(stoppPunkt));

            ersteTabelle = beideTabellen.substring(0, beideTabellen.indexOf(trennPunkt));
            zweiteTabelle = beideTabellen.substring(beideTabellen.indexOf(trennPunkt), beideTabellen.length());

            int[] abstand1 = hm.haeufigkeit(ersteTabelle, klasse);
            int[] abstand2 = hm.haeufigkeit(zweiteTabelle, klasse);

            int anzahl1 = abstand1.length;
            int anzahl2 = abstand2.length;

            boolean faelltEtwasAus = false;
            int anzahlAusfaelle = 0;

            if (anzahl1 != 0) {
                String[][] sArray1 = hm.stringKuerzen(ersteTabelle, klasse);
                String[][] sArray1_1 = hm.stringKuerzen(ersteTabelle_saved, klasse);


                if (!Arrays.deepEquals(sArray1, sArray1_1)) {
                    faelltEtwasAus = true;
                    anzahlAusfaelle += sArray1.length;
                }
            }

            //Wenn anzahl2 nicht 0 ist, dann gespeicherte Tabelle mit aktueller vergleichen.
            if (anzahl2 != 0) {

                String[][] sArray2 = hm.stringKuerzen(zweiteTabelle, klasse);
                String[][] sArray2_1 = hm.stringKuerzen(zweiteTabelle_saved, klasse);

                if (!Arrays.deepEquals(sArray2, sArray2_1)) {
                    faelltEtwasAus = true;
                    anzahlAusfaelle += sArray2.length;
                }
            }


            if (faelltEtwasAus) {
                if (anzahlAusfaelle > 1) {
                    notification("Stundenplan Änderung!", "MGG Vertretungsplan", anzahlAusfaelle + " Änderungen!"); //Push mit der Nachricht "Es fällt etwas aus!"
                } else if (anzahlAusfaelle == 1) {
                    notification("Stundenplan Änderung!", "MGG Vertretungsplan", anzahlAusfaelle + " Änderung!"); //Push mit der Nachricht "Es fällt etwas aus!"
                } else {
                    Log.v("VertretungsplanService", "Fehler!");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}