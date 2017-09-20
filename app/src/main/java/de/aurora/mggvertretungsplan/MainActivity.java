package de.aurora.mggvertretungsplan;

import android.app.AlarmManager;
import android.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import de.aurora.mggvertretungsplan.datamodel.DateHeading;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;
import de.aurora.mggvertretungsplan.datamodel.TimeTableElement;
import de.aurora.mggvertretungsplan.ui.CardsAdapter;
import de.aurora.mggvertretungsplan.ui.theming.ThemeManager;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String>, SwipeRefreshLayout.OnRefreshListener {

    private final ArrayList<DateHeading> headingsList = new ArrayList<>();
    private ArrayList<TimeTableElement> dayOneList = new ArrayList<>();
    private ArrayList<TimeTableElement> dayTwoList = new ArrayList<>();
    private SharedPreferences sp;
    private Toolbar toolbar;
    private String class_name, toolbarTitle_WithClass;
    private int currentYear;
    private SwipeRefreshLayout mSwipeLayout;
    private CardsAdapter cAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        int themeID = sp.getInt("Theme", 0);
        setTheme(ThemeManager.getTheme(themeID));
        super.onCreate(savedInstanceState);

        //TODO wieder entfernen, sobald die Funktion wieder geht
        sp.edit().putBoolean("AktTagAnzeigen", true).apply(); //true!!!

        class_name = sp.getString("KlasseGesamt", "5a");

        // If application is called for the first time, intro slides will show up
        if (sp.getBoolean("firstStart", true)) {
            Intent intent = new Intent(getApplicationContext(), de.aurora.mggvertretungsplan.ui.intro.IntroActivity.class);
            startActivity(intent);

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }

        setContentView(R.layout.layout_main);
        mSwipeLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3);

        toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setAlpha(1);
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));
        toolbar.showOverflowMenu();

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        cAdapter = new CardsAdapter(dayOneList, dayTwoList, headingsList, this);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        displaySavedData();
        updateData();
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


    // When swiped to refresh
    public void onRefresh() {
        updateData();
    }


    // Method to display the saved data
    private void displaySavedData() {
        String firstDate = sp.getString("firstDate", "01.01." + currentYear);
        String secondDate = sp.getString("secondDate", "01.01." + currentYear);

        ArrayList<ArrayList<String>> tableOne, tableTwo;

        tableOne = hilfsMethoden.getArrayList(sp.getString("tableOne", ""));
        tableTwo = hilfsMethoden.getArrayList(sp.getString("tableTwo", ""));

        TimeTable timeTable = new TimeTable();
        TimeTableDay ttd = new TimeTableDay(firstDate, tableOne);
        TimeTableDay ttd2 = new TimeTableDay(secondDate, tableTwo);
        timeTable.addTimeTableDay(ttd);
        timeTable.addTimeTableDay(ttd2);

        sp.edit().putBoolean("AktTagAnzeigen", true).apply();
        displayData(timeTable, sp.getBoolean("AktTagAnzeigen", true));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Checks which item of the toolbar menu was selected
    public boolean onOptionsItemSelected(MenuItem item) {
        TypedValue typedValue = new TypedValue();
        Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        @ColorInt int color = typedValue.data;

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent preferenceIntent = new Intent(getApplicationContext(), SettingsActivity.class);
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

    //TODO Refactor!
    private void serviceProvider() {
        if (sp.getBoolean("notification", true)) {
            AlarmManager(Long.valueOf(sp.getString("AbrufIntervall", "1800000")));
            Log.v("VertretungsplanService", "serviceProvider, Interval: " + sp.getString("AbrufIntervall", "1800000"));
        } else {
            AlarmManagerBeenden();
        }
    }

    //TODO Refactor
    //AlarmManager Starten! -> Hintergrund Prozess
    private void AlarmManager(long interval) {
        long firstStart = System.currentTimeMillis() + interval;

        Intent intentsOpen = new Intent(this, VertretungsplanService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intentsOpen, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
    }

    //TODO Refactor
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
            class_name = sp.getString("KlasseGesamt", "5a");
            sp = PreferenceManager.getDefaultSharedPreferences(this);

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.vertretungsplan_url));
            } catch (Exception e) {
                mSwipeLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), getString(R.string.toast_noInternetConnection), Toast.LENGTH_SHORT).show();
        }
    }

    // Creates the view of the Android App
    private void displayData(TimeTable timeTable, boolean aktTagAnzeigen) {
        Log.v("Vertretungsplan", "Anzeigen");
        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass);
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));
        headingsList.clear();
        dayOneList.clear();
        dayTwoList.clear();

        Date date1, currentDate;
        date1 = timeTable.getDay(0).getDate();
        currentDate = new Date();

        int sixteenHours = 60 * 60 * 16;
        long secondsDiff = (currentDate.getTime() - date1.getTime()) / 1000;

        // Day 1
        // Displays the current day only when the setting is active
        // OR when it's not set, but it's before 16:00
        if (aktTagAnzeigen || ((secondsDiff > 0) && (secondsDiff < sixteenHours))) {
            headingsList.add(new DateHeading(timeTable.getDay(0).getDate()));
            dayOneList = timeTable.getDay(0).getElements();
        }

        // Day 2
        headingsList.add(new DateHeading(timeTable.getDay(1).getDate()));
        dayTwoList = timeTable.getDay(1).getElements();

        cAdapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
    }

    // Wird aufgerufen wenn die Website heruntergeladen wurde
    public void onTaskComplete(String website_html) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        TimeTable timeTable = hilfsMethoden.parseTimetable(website_html, class_name);
        displayData(timeTable, sp.getBoolean("AktTagAnzeigen", true));

        // TODO NullPointerException
        String firstDate = timeTable.getDay(0).getDateString();
        String secondDate = timeTable.getDay(1).getDateString();

        ArrayList<ArrayList<String>> tableOne = timeTable.getDay(0).getArrayList();
        ArrayList<ArrayList<String>> tableTwo = timeTable.getDay(1).getArrayList();

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("firstDate", firstDate);
        editor.putString("secondDate", secondDate);
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
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));
        updateData();
        serviceProvider();
    }

   /*
     * Code by Rico Jambor
	 */
}
