package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String>, SwipeRefreshLayout.OnRefreshListener {

    private String klasse;
    private int jahr;
    public AdView adView;
    SharedPreferences sp;
    private SwipeRefreshLayout mSwipeLayout;
    Toolbar toolbar;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setAlpha(1);
        toolbar.setTitle("Vertretungsplan");
        toolbar.showOverflowMenu();

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        createAdBanner();
        gespeicherteDatenAnzeigen();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                return true;
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (toolbar.isOverflowMenuShowing()) {
                    toolbar.dismissPopupMenus();
                } else {
                    toolbar.showOverflowMenu();
                }
                return false;
        }
        return true;
    }


    public void onRefresh() {
        initialisierung();
    }


    public void serviceProvider() {
        if (sp.getBoolean("notification", true)) {
            AlarmManager(Integer.valueOf(sp.getString("AbrufIntervall", "1800000")));
        } else {
            AlarmManagerBeenden();
        }
    }

    public void gespeicherteDatenAnzeigen() {
        TextView aktualisiertAmTextView = (TextView) findViewById(R.id.listText);
        String AbrufDatum = sp.getString("AbrufDatum", "Noch nie aktualisiert!");
        aktualisiertAmTextView.setText("Zuletzt aktualisiert am: " + AbrufDatum);

        String[][] ersteTabelleArr, zweiteTabelleArr;
        String erDatum, zwDatum;

        GregorianCalendar calender = new GregorianCalendar();
        jahr = calender.get(GregorianCalendar.YEAR);

        erDatum = sp.getString("erstesDatum", "01.01.2015");
        zwDatum = sp.getString("zweitesDatum", "01.01.2015");
        klasse = sp.getString("KlasseGesamt", "5a");
        setTitle("Vertretungsplan (" + klasse + ")");

        ersteTabelleArr = stringToArray(sp.getString("ersteTabelle", ""));
        zweiteTabelleArr = stringToArray(sp.getString("zweiteTabelle", ""));

        anzeigen(ersteTabelleArr, zweiteTabelleArr, erDatum, zwDatum, sp.getBoolean("AktTagAnzeigen", true));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Werbebanner hinzufügen
    public void createAdBanner() {
        adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB")
                .build();
        adView.loadAd(adRequest);
    }

    //Aktion die ausgeführt werden soll, wenn action bar item geklickt wurde (auch aktualisieren)
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent preferenceIntent = new Intent(getApplicationContext(), de.aurora.mggvertretungsplan.PreferenceWithHeaders.class);
                startActivity(preferenceIntent);
                break;
            case R.id.action_webview:
                Intent webViewIntent = new Intent(getApplicationContext(), de.aurora.mggvertretungsplan.webView_Activity.class);
                startActivity(webViewIntent);
                break;
            case R.id.action_info:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setIcon(R.drawable.ic_menu_info_details)
                        .setTitle("MGG Vertretungsplan v" + getString(R.string.version))
                        .setMessage("Programmiert von Rico Jambor");

                AlertDialog dialog = builder.create();
                dialog.show();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //AlarmManager Starten! -> Hintergrund Prozess
    public void AlarmManager(int intervall) {
        long interval = (long) intervall;
        long firstStart = System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS * 30;

        Intent intentsOpen = new Intent(this, VertretungsplanService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intentsOpen, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
    }

    //beendet AlarmManager
    public void AlarmManagerBeenden() {
        Intent intentsOpen = new Intent(this, VertretungsplanService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intentsOpen, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public boolean aktiveVerbindung() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork.isConnected();
    }

    // überprüfen ob Klasse ausgewählt, ob Internetverbinding besteht, gibt Befehl zum Runterladen
    private void initialisierung() {
        if (aktiveVerbindung()) {
            createAdBanner();
            mSwipeLayout.setRefreshing(true);
            klasse = sp.getString("KlasseGesamt", "5a");
            sp = PreferenceManager.getDefaultSharedPreferences(this);

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.Url1));
            } catch (Exception e) {
                mSwipeLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), "Keine Internetverbindung!", Toast.LENGTH_SHORT).show();
        }
    }

    // Macht aus dem String ein Array
    private String[][] stringToArray(String inputString) {
        hilfsMethoden hm = new hilfsMethoden();
        return hm.stringKuerzen(inputString, klasse);
    }

    //Initialisieren einer ArrayList, Hinzufügen von Items, Adapter an ListView binden
    private void anzeigen(String[][] ersteTabelleArr, String[][] zweiteTabelleArr, String erstesDatum, String zweitesDatum, boolean aktTagAnzeigen) {
        hilfsMethoden hm = new hilfsMethoden();
        ListView listView = (ListView) findViewById(R.id.listView);

        ArrayList<Vertretungen> list = new ArrayList<>();

        long unixTime = System.currentTimeMillis() / 1000L;
        String AbrufDatum = new SimpleDateFormat("HH").format((unixTime + 3600) * 1000L).toString();
        String tagAkt = (String) DateFormat.format("dd", new Date());
        String monatAkt = (String) DateFormat.format("MM", new Date());

        int monat1 = Integer.valueOf(erstesDatum.substring(3, 5)), monat2 = Integer.valueOf(zweitesDatum.substring(3, 5));
        int tag1 = Integer.valueOf(erstesDatum.substring(0, 2)), tag2 = Integer.valueOf(zweitesDatum.substring(0, 2));

        String erstesDatumName = hm.getAnyDayByName(jahr, Integer.valueOf(erstesDatum.substring(3, 5)), Integer.valueOf(erstesDatum.substring(0, 2)));
        String zweitesDatumName = hm.getAnyDayByName(jahr, Integer.valueOf(zweitesDatum.substring(3, 5)), Integer.valueOf(zweitesDatum.substring(0, 2)));

        //Wenn der erste Tag vor dem zweiten kommt im gleichen Monat (normalfall), oder wenn der erste Monat vor dem zweiten kommt
        if ((tag1 < tag2 && monat1 == monat2) || (tag1 > tag2 && monat1 < monat2)) {
            //Wenn Tag angezeigt werden soll, dann anzeigen
            //ODER Wenn Tag nicht angezeigt werden soll, aber es noch vor 16 Uhr ist, dann anzeigen
            //ODER Wenn der Tag1 kleiner als der aktuelle ist, aber der aktuelle Monat kleiner als der monat1 ist
            if (aktTagAnzeigen || (!aktTagAnzeigen && Integer.valueOf(AbrufDatum) < 16 && tag1 == Integer.valueOf(tagAkt) && monat1 == Integer.valueOf(monatAkt)) ||
                    (tag1 > Integer.valueOf(tagAkt) && monat1 == Integer.valueOf(monatAkt)) ||
                    (tag1 < Integer.valueOf(tagAkt) && monat1 > Integer.valueOf(monatAkt))) {

                //Tag 1
                list.add(new Vertretungen("", "", "", "", "", erstesDatum, erstesDatumName));
                for (int i = 0; i < ersteTabelleArr.length; i++) {
                    list.add(new Vertretungen(ersteTabelleArr[i][0] + ". Stunde", hm.abkuerzung(ersteTabelleArr[i][2]), ersteTabelleArr[i][4], ersteTabelleArr[i][5], ersteTabelleArr[i][6], "", ""));
                }
                list.add(new Vertretungen("", "", "", "", "", "", ""));
            }

            //Tag 2
            list.add(new Vertretungen("", "", "", "", "", zweitesDatum, zweitesDatumName));
            for (int i = 0; i < zweiteTabelleArr.length; i++) {
                list.add(new Vertretungen(zweiteTabelleArr[i][0] + ". Stunde", hm.abkuerzung(zweiteTabelleArr[i][2]), zweiteTabelleArr[i][4], zweiteTabelleArr[i][5], zweiteTabelleArr[i][6], "", ""));
            }

            //wenn der erste Tag Nach dem 2. kommt (im selben Monat), oder wenn der erste Tag vor dem zweiten kommt, aber der erste Monat nach dem zweiten
        } else if ((tag1 > tag2 && monat1 == monat2) || (tag1 < tag2 && monat1 > monat2)) {
            if (aktTagAnzeigen ||
                    (Integer.valueOf(AbrufDatum) < 16 && tag2 == Integer.valueOf(tagAkt) && monat2 == Integer.valueOf(monatAkt)) ||
                    (tag2 > Integer.valueOf(tagAkt) && monat2 == Integer.valueOf(monatAkt)) ||
                    (tag2 < Integer.valueOf(tagAkt) && monat2 > Integer.valueOf(monatAkt))
                    ) {

                list.add(new Vertretungen("", "", "", "", "", zweitesDatum, zweitesDatumName));
                for (int i = 0; i < zweiteTabelleArr.length; i++) {
                    list.add(new Vertretungen(zweiteTabelleArr[i][0] + ". Stunde", hm.abkuerzung(zweiteTabelleArr[i][2]), zweiteTabelleArr[i][4], zweiteTabelleArr[i][5], zweiteTabelleArr[i][6], "", ""));
                }

                list.add(new Vertretungen("", "", "", "", "", "", ""));
            }

            //Tag 2
            list.add(new Vertretungen("", "", "", "", "", erstesDatum, erstesDatumName));
            for (int i = 0; i < ersteTabelleArr.length; i++) {
                list.add(new Vertretungen(ersteTabelleArr[i][0] + ". Stunde", hm.abkuerzung(ersteTabelleArr[i][2]), ersteTabelleArr[i][4], ersteTabelleArr[i][5], ersteTabelleArr[i][6], "", ""));
            }

        }
        vertretungsplanArrayAdapter adapter = new vertretungsplanArrayAdapter(MainActivity.this, R.id.listView, list);
        listView.setAdapter(adapter);
        mSwipeLayout.setRefreshing(false);
    }

    // Wird aufgerufen wenn die Website heruntergeladen wurde
    public void onTaskComplete(String html) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        String beideTabellen, ersteTabelle, zweiteTabelle, erstesDatum, zweitesDatum;
        String[][] ersteTabelleArr, zweiteTabelleArr;

        TextView aktualisiertAmTextView = (TextView) findViewById(R.id.listText);

        final String startPunkt = "___-1\"></a><h2 class=\"tabber_title\">",
                stoppPunkt = "</table><div style=\"clear:both;\"></div></div><div style=\"clear:both;\"></div>",
                trennPunkt = "___-2\"></a><h2 class=\"tabber_title\">";

        try {
            /** Beschneiden der Tabellen, Speichern in Strings, Auslesen des Datums aus html **/
            beideTabellen = html.substring(html.indexOf(startPunkt), html.indexOf(stoppPunkt));
            ersteTabelle = beideTabellen.substring(0, beideTabellen.indexOf(trennPunkt));
            zweiteTabelle = beideTabellen.substring(beideTabellen.indexOf(trennPunkt), beideTabellen.length());

            erstesDatum = ersteTabelle.substring(36, 42);
            zweitesDatum = zweiteTabelle.substring(36, 42);


            hilfsMethoden hm = new hilfsMethoden();
            String AbrufDatum = hm.getFormattedDate(System.currentTimeMillis());

            aktualisiertAmTextView.setText("Zuletzt aktualisiert" + AbrufDatum);

            ersteTabelleArr = stringToArray(ersteTabelle);
            zweiteTabelleArr = stringToArray(zweiteTabelle);

            setTitle("Vertretungsplan (" + klasse + ")");
            anzeigen(ersteTabelleArr, zweiteTabelleArr, erstesDatum, zweitesDatum, sp.getBoolean("AktTagAnzeigen", true));

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("erstesDatum", erstesDatum);
            editor.putString("zweitesDatum", zweitesDatum);
            editor.putString("AbrufDatum", AbrufDatum);
            editor.putString("ersteTabelle", ersteTabelle);
            editor.putString("zweiteTabelle", zweiteTabelle);
            editor.commit();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Ein interner Fehler ist aufgetreten!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        initialisierung();
        serviceProvider();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

	/*
     * Code by Rico Jambor
	 */
}
