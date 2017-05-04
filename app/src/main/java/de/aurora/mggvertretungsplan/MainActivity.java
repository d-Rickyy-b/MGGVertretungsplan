package de.aurora.mggvertretungsplan;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.aurora.mggvertretungsplan.ui.CardsAdapter;
import de.aurora.mggvertretungsplan.ui.DateHeading;
import de.aurora.mggvertretungsplan.ui.LayoutSwitcher;
import de.aurora.mggvertretungsplan.ui.TimeTableCard;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String>, SwipeRefreshLayout.OnRefreshListener {

    private SharedPreferences sp;
    private Toolbar toolbar;
    private int clickcount = 0;
    private String klasse;
    private int jahr;
    private SwipeRefreshLayout mSwipeLayout;
    private ArrayList<TimeTableCard> dayOneList = new ArrayList<>();
    private ArrayList<TimeTableCard> dayTwoList = new ArrayList<>();
    private ArrayList<DateHeading> headingsList = new ArrayList<>();
    private CardsAdapter cAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        int themeID = sp.getInt("Theme", 0);
        setTheme(LayoutSwitcher.getTheme(themeID));
        super.onCreate(savedInstanceState);

        //TODO wieder entfernen, sobald die Funktion wieder geht
        sp.edit().putBoolean("AktTagAnzeigen", true).apply(); //true!!!

        if (sp.getBoolean("firstStart", true)) {
            //Wenn erster Start
            Intent intent = new Intent(getApplicationContext(), de.aurora.mggvertretungsplan.ui.intro.IntroActivity.class);
            startActivity(intent);

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }

        setContentView(R.layout.activity_main);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setAlpha(1);
        toolbar.setTitle("Vertretungsplan");
        toolbar.showOverflowMenu();

//        toolbar.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                clickcount = clickcount + 1;
//                if (clickcount % 10 == 0) {
//                    Toast.makeText(getApplicationContext(), "Notification gesendet!", Toast.LENGTH_SHORT).show();
//
//                    notification("Ticker", "Titel", "Text");
//                    notification("Stundenplan Änderung!", "MGG Vertretungsplan", "3 Änderungen!");
//                    Intent intent = new Intent(getApplicationContext(), de.aurora.mggvertretungsplan.ui.intro.IntroActivity.class);
//                    startActivity(intent);
//                }
//            }
//        });

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        jahr = new GregorianCalendar().get(GregorianCalendar.YEAR);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        cAdapter = new CardsAdapter(dayOneList, dayTwoList, headingsList, this);
        recyclerView.setHasFixedSize(true);

        //Neuer LayoutManager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        gespeicherteDatenAnzeigen();
        updateData();
    }


    //added for testing purposes
    private void notification(String ticker, String titel, String text) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        Log.i("VertretungsplanService", "Notification!");
        @SuppressWarnings("deprecation")
        android.support.v7.app.NotificationCompat.Builder notification = (android.support.v7.app.NotificationCompat.Builder) new android.support.v7.app.NotificationCompat.Builder(this)
                .setContentTitle(titel)
                .setContentText(text)
                .setTicker(ticker)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        //.setVibrate(new long[]{0,300,200,300})
        //.setLights(Color.WHITE, 1000, 5000)

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification.build());
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
        updateData();
    }


    private void gespeicherteDatenAnzeigen() {
        String AbrufDatum = sp.getString("AbrufDatum", ": Noch nie aktualisiert!");

        jahr = new GregorianCalendar().get(GregorianCalendar.YEAR);
        klasse = sp.getString("KlasseGesamt", "5a");
        setTitle(String.format("Vertretungsplan (%s)", klasse));

        String erDatum = sp.getString("erstesDatum", "01.01." + jahr);
        String zwDatum = sp.getString("zweitesDatum", "01.01." + jahr);

        ArrayList<ArrayList<String>> tableOne, tableTwo;

        tableOne = hilfsMethoden.getArrayList(sp.getString("ersteTabelle", ""));
        tableTwo = hilfsMethoden.getArrayList(sp.getString("zweiteTabelle", ""));

        sp.edit().putBoolean("AktTagAnzeigen", true).apply();
        anzeigen(tableOne, tableTwo, erDatum, zwDatum, sp.getBoolean("AktTagAnzeigen", true));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Aktion die ausgeführt werden soll, wenn action bar item geklickt wurde
    public boolean onOptionsItemSelected(MenuItem item) {
        TypedValue typedValue = new TypedValue();
        Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        @ColorInt int color = typedValue.data;

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent preferenceIntent = new Intent(getApplicationContext(), Settings.class);
                startActivityForResult(preferenceIntent, 0);
                break;
            case R.id.action_webview:
                CustomTabsIntent.Builder chromeTabsBuilder = new CustomTabsIntent.Builder();
                chromeTabsBuilder.setToolbarColor(color);
                chromeTabsBuilder.setShowTitle(true);
                CustomTabsIntent websiteIntent = chromeTabsBuilder.build();
                websiteIntent.launchUrl(this, Uri.parse(getString(R.string.vertretungsplan_url)));
                break;
            case R.id.action_feedback:
                CustomTabsIntent.Builder chromeTabsFeedbackBuilder = new CustomTabsIntent.Builder();
                chromeTabsFeedbackBuilder.setToolbarColor(color);
                chromeTabsFeedbackBuilder.setShowTitle(true);
                CustomTabsIntent feedbackIntent = chromeTabsFeedbackBuilder.build();
                feedbackIntent.launchUrl(this, Uri.parse(getString(R.string.feedback_url)));
                break;
            case R.id.action_info:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                builder
                        .setIcon(R.drawable.ic_info_outline_black)
                        .setTitle("MGG Vertretungsplan v" + getString(R.string.version))
                        .setMessage(Html.fromHtml("Programmiert von Rico Jambor<br><br>Bei Fehlern entweder eine Email an:<br><b>rico.jambor@gmail.com</b><br><br>Oder per Telegram an:<br><center><b>@d_Rickyy_b</b></center>"))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void serviceProvider() {
        if (sp.getBoolean("notification", true)) {
            AlarmManager(Long.valueOf(sp.getString("AbrufIntervall", "1800000")));
            Log.v("VertretungsplanService", "serviceProvider, Interval: " + sp.getString("AbrufIntervall", "1800000"));
        } else {
            AlarmManagerBeenden();
        }
    }

    //AlarmManager Starten! -> Hintergrund Prozess
    private void AlarmManager(long interval) {
        long firstStart = System.currentTimeMillis() + interval;

        Intent intentsOpen = new Intent(this, VertretungsplanService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intentsOpen, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
    }

    //beendet AlarmManager
    private void AlarmManagerBeenden() {
        Intent intentsOpen = new Intent(this, VertretungsplanService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intentsOpen, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private boolean aktiveVerbindung() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    // überprüfen ob Klasse ausgewählt, ob Internetverbinding besteht, gibt Befehl zum Runterladen
    private void updateData() {
        if (aktiveVerbindung()) {
            mSwipeLayout.setRefreshing(true);
            klasse = sp.getString("KlasseGesamt", "5a");
            sp = PreferenceManager.getDefaultSharedPreferences(this);

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.vertretungsplan_url));
            } catch (Exception e) {
                mSwipeLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), "Keine Internetverbindung!", Toast.LENGTH_SHORT).show();
        }
    }


    //Initialisieren einer ArrayList, Hinzufügen von Items, Adapter an ListView binden
    private void anzeigen(ArrayList<ArrayList<String>> ersterTag, ArrayList<ArrayList<String>> zweiterTag, String erstesDatum, String zweitesDatum, boolean aktTagAnzeigen) {
        headingsList.clear();
        dayOneList.clear();
        dayTwoList.clear();

        long unixTime = System.currentTimeMillis() / 1000L;
        String currentTimeInHours = new SimpleDateFormat("HH", new Locale("de")).format((unixTime + 3600) * 1000L);
        String tagAkt = (String) DateFormat.format("dd", new Date());
        String monatAkt = (String) DateFormat.format("MM", new Date());
        int monat1, monat2, tag1, tag2;

        try {
            monat1 = Integer.valueOf(erstesDatum.substring(3, 5));
            monat2 = Integer.valueOf(zweitesDatum.substring(3, 5));
            tag1 = Integer.valueOf(erstesDatum.substring(0, 2));
            tag2 = Integer.valueOf(zweitesDatum.substring(0, 2));
        } catch (Exception e) {
            e.printStackTrace();
            monat1 = monat2 = 1;
            tag1 = tag2 = 1;
        }

        Log.v("Vertretungsplan", erstesDatum + jahr + " | " + zweitesDatum + jahr);

        //Wenn der erste Tag vor dem zweiten kommt im gleichen Monat (normalfall), oder wenn der erste Monat vor dem zweiten kommt
        if ((tag1 > tag2 && monat1 == monat2) || (tag1 < tag2 && monat1 > monat2)) {
            ArrayList<ArrayList<String>> tempList = new ArrayList<>(ersterTag);
            ersterTag = zweiterTag;
            zweiterTag = tempList;

            int tmpTag = tag1;
            tag1 = tag2;
            tag2 = tmpTag;

            int tmpMonat = monat1;
            monat1 = monat2;
            monat2 = tmpMonat;

        }

        String erstesDatumName = hilfsMethoden.getAnyDayByName(jahr, monat1, tag1);
        String zweitesDatumName = hilfsMethoden.getAnyDayByName(jahr, monat2, tag2);

        Log.v("MainActivity", erstesDatumName + " | " + zweitesDatumName);

        headingsList.add(new DateHeading(erstesDatumName, tag1 + "." + monat1 + "." + jahr));
        headingsList.add(new DateHeading(zweitesDatumName, tag2 + "." + monat2 + "." + jahr));

        //Wenn Tag angezeigt werden soll, dann anzeigen
        //ODER Wenn Tag nicht angezeigt werden soll, aber es noch vor 16 Uhr ist, dann anzeigen
        //ODER Wenn der Tag1 kleiner als der aktuelle ist, aber der aktuelle Monat kleiner als der monat1 ist
        if (aktTagAnzeigen ||
                (Integer.valueOf(currentTimeInHours) < 16 && tag1 == Integer.valueOf(tagAkt) && monat1 == Integer.valueOf(monatAkt)) ||
                (tag1 > Integer.valueOf(tagAkt) && monat1 == Integer.valueOf(monatAkt)) ||
                (tag1 < Integer.valueOf(tagAkt) && monat1 > Integer.valueOf(monatAkt))) {

            //Tag 1
            for (ArrayList<String> zeile : ersterTag) {
                if (zeile.size() == 7) {
                    TimeTableCard timeTableCard = new TimeTableCard(zeile.get(0), hilfsMethoden.abkuerzung(zeile.get(2)), hilfsMethoden.abkuerzung(zeile.get(3)), zeile.get(4), zeile.get(5), hilfsMethoden.getType(zeile.get(3), zeile.get(5)), zeile.get(6));
                    dayOneList.add(timeTableCard);
                }
            }
        } else {
            //Tag 1 aus Headings löschen
            headingsList.remove(0);
        }

        //Tag 2
        for (ArrayList<String> zeile : zweiterTag) {
            TimeTableCard timeTableCard = new TimeTableCard(zeile.get(0), hilfsMethoden.abkuerzung(zeile.get(2)), hilfsMethoden.abkuerzung(zeile.get(3)), zeile.get(4), zeile.get(5), hilfsMethoden.getType(zeile.get(3), zeile.get(5)), zeile.get(6));
            dayTwoList.add(timeTableCard);
        }

        cAdapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
    }

    // Wird aufgerufen wenn die Website heruntergeladen wurde
    public void onTaskComplete(String html) {
        setTitle("Vertretungsplan (" + klasse + ")");
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        String erstesDatum, zweitesDatum;
        ArrayList<ArrayList<String>> tableOne, tableTwo;

        //Umlaute entfernen
        html = html.replace("&auml;", "ä").replace("&ouml;", "ö").replace("&uuml;", "ü");

        Document doc = Jsoup.parse(html);
        Elements dates = doc.select("h2.tabber_title");

        ArrayList<String> datesList = new ArrayList<>();
        for (Element date : dates) {
            datesList.add(date.text()); //Datum 1 und 2
        }

        try {
            erstesDatum = datesList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            erstesDatum = "";
        }

        try {
            zweitesDatum = datesList.get(1);
        } catch (Exception e) {
            e.printStackTrace();
            zweitesDatum = "";
        }

        try {
            tableOne = hilfsMethoden.extractTable(doc, 0);

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            tableOne = new ArrayList<>();
        }

        try {
            tableTwo = hilfsMethoden.extractTable(doc, 1);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            tableTwo = new ArrayList<>();
        }

        tableOne = hilfsMethoden.datenAufbereiten(tableOne, klasse);
        tableTwo = hilfsMethoden.datenAufbereiten(tableTwo, klasse);

        String AbrufDatum = hilfsMethoden.getFormattedDate(System.currentTimeMillis());

        //TODO wenn ein Datum = "", dann "Keine Informationen" anzeigen
        ArrayList<ArrayList<String>> tableOne_saved = hilfsMethoden.getArrayList(sp.getString("ersteTabelle", ""));
        ArrayList<ArrayList<String>> tableTwo_saved = hilfsMethoden.getArrayList(sp.getString("zweiteTabelle", ""));

        int count1 = hilfsMethoden.getDifferencesCount(tableOne, tableOne_saved);
        int count2 = hilfsMethoden.getDifferencesCount(tableTwo, tableTwo_saved);


//        if ((count1 + count2) > 0) {
            Log.v("Vertretungsplan", "Anzeigen");
            anzeigen(tableOne, tableTwo, erstesDatum, zweitesDatum, sp.getBoolean("AktTagAnzeigen", true));
//        } else {
//            Log.v("Vertretungsplan", "Nicht anzeigen");
//            mSwipeLayout.setRefreshing(false);
//        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("erstesDatum", erstesDatum);
        editor.putString("zweitesDatum", zweitesDatum);
        editor.putString("AbrufDatum", AbrufDatum);
        editor.putString("ersteTabelle", hilfsMethoden.getJSONArray(tableOne).toString());
        editor.putString("zweiteTabelle", hilfsMethoden.getJSONArray(tableTwo).toString());
        editor.putBoolean("AktTagAnzeigen", true);
        editor.apply();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            recreate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
        serviceProvider();
    }

   /*
     * Code by Rico Jambor
	 */
}
