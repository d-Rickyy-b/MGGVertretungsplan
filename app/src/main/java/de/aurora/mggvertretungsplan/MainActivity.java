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
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.aurora.mggvertretungsplan.ui.CardsAdapter;
import de.aurora.mggvertretungsplan.ui.DateHeading;
import de.aurora.mggvertretungsplan.ui.LayoutSwitcher;
import de.aurora.mggvertretungsplan.ui.TimeTableCard;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String>, SwipeRefreshLayout.OnRefreshListener {

    private SharedPreferences sp;
    private Toolbar toolbar;
    private String klasse;
    private int currentYear;
    private SwipeRefreshLayout mSwipeLayout;
    private final ArrayList<TimeTableCard> dayOneList = new ArrayList<>();
    private final ArrayList<TimeTableCard> dayTwoList = new ArrayList<>();
    private final ArrayList<DateHeading> headingsList = new ArrayList<>();
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
            // If application is called for the first time, intro slides will show up
            Intent intent = new Intent(getApplicationContext(), de.aurora.mggvertretungsplan.ui.intro.IntroActivity.class);
            startActivity(intent);

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }

        setContentView(R.layout.activity_main);
        mSwipeLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setAlpha(1);
        toolbar.setTitle("Vertretungsplan");
        toolbar.showOverflowMenu();

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        cAdapter = new CardsAdapter(dayOneList, dayTwoList, headingsList, this);
        recyclerView.setHasFixedSize(true);

        //Neuer LayoutManager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        displaySavedData();
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


    private void displaySavedData() {
        currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
        klasse = sp.getString("KlasseGesamt", "5a");
        setTitle(String.format("Vertretungsplan (%s)", klasse));

        String firstDate = sp.getString("firstDate", "01.01." + currentYear);
        String secondDate = sp.getString("secondDate", "01.01." + currentYear);

        ArrayList<ArrayList<String>> tableOne, tableTwo;

        tableOne = hilfsMethoden.getArrayList(sp.getString("tableOne", ""));
        tableTwo = hilfsMethoden.getArrayList(sp.getString("tableTwo", ""));

        sp.edit().putBoolean("AktTagAnzeigen", true).apply();
        anzeigen(tableOne, tableTwo, firstDate, secondDate, sp.getBoolean("AktTagAnzeigen", true));
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
            case R.id.action_website:
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
                android.text.Spanned informationText;
                if (SDK_INT >= 24) {
                    informationText = Html.fromHtml("Programmiert von Rico Jambor<br><br>Bei Fehlern entweder eine Email an:<br><b>rico.jambor@gmail.com</b><br><br>Oder per Telegram an:<br><center><b>@d_Rickyy_b</b></center>", Html.FROM_HTML_MODE_LEGACY);
                } else {
                    informationText = Html.fromHtml("Programmiert von Rico Jambor<br><br>Bei Fehlern entweder eine Email an:<br><b>rico.jambor@gmail.com</b><br><br>Oder per Telegram an:<br><center><b>@d_Rickyy_b</b></center>");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                builder
                        .setIcon(R.drawable.ic_info_outline_black)
                        .setTitle("MGG Vertretungsplan v" + getString(R.string.version))
                        .setMessage(informationText)
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

    private boolean isConnectionActive() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    // überprüfen ob Klasse ausgewählt, ob Internetverbinding besteht, gibt Befehl zum Runterladen
    private void updateData() {
        if (isConnectionActive()) {
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
    private void anzeigen(ArrayList<ArrayList<String>> ersterTag, ArrayList<ArrayList<String>> zweiterTag, String firstDate, String secondDate, boolean aktTagAnzeigen) {
        headingsList.clear();
        dayOneList.clear();
        dayTwoList.clear();

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        Date date1, date2, currentDate;

        try {
            date1 = fullDateFormat.parse(firstDate);
            date2 = fullDateFormat.parse(secondDate);
            currentDate = new Date();
        } catch (java.text.ParseException e) {
            date1 = date2 = currentDate = new Date();
        }

        // Switch days if the first is later than the second
        if (date1.after(date2)){
            ArrayList<ArrayList<String>> tempList = new ArrayList<>(ersterTag);
            ersterTag = zweiterTag;
            zweiterTag = tempList;

            Date tmpDate = date1;
            date1 = date2;
            date2 = tmpDate;
        }

        headingsList.add(new DateHeading(date1));
        headingsList.add(new DateHeading(date2));

        int sixteenHours = 60 * 60 * 16;
        long secondsDiff = (currentDate.getTime() - date1.getTime()) / 1000;

        // Displays the current day onlw when the setting is active
        // OR when it's not set, but it's before 16:00
        if (aktTagAnzeigen || ((secondsDiff > 0) && (secondsDiff < sixteenHours))) {
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
    public void onTaskComplete(String website_html) {
        setTitle("Vertretungsplan (" + klasse + ")");
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        String firstDate, secondDate;
        ArrayList<ArrayList<String>> tableOne, tableTwo;

        CancellationDays cancellationDays = hilfsMethoden.parseTimetable(website_html, klasse);
        firstDate = cancellationDays.getFirstDate() + currentYear;
        secondDate = cancellationDays.getSecondDate() + currentYear;

        tableOne = cancellationDays.getFirstDay();
        tableTwo = cancellationDays.getSecondDay();

        String AbrufDatum = hilfsMethoden.getFormattedDate(System.currentTimeMillis());

        Log.v("Vertretungsplan", "Anzeigen");
        anzeigen(tableOne, tableTwo, firstDate, secondDate, sp.getBoolean("AktTagAnzeigen", true));

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("firstDate", firstDate);
        editor.putString("secondDate", secondDate);
        editor.putString("AbrufDatum", AbrufDatum);
        editor.putString("tableOne", hilfsMethoden.getJSONArray(tableOne).toString());
        editor.putString("tableTwo", hilfsMethoden.getJSONArray(tableTwo).toString());
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
