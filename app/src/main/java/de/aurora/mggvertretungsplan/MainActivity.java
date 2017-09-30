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
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;
import de.aurora.mggvertretungsplan.parsing.MGGParser;
import de.aurora.mggvertretungsplan.parsing.WebsiteParser;
import de.aurora.mggvertretungsplan.ui.CardsAdapter;
import de.aurora.mggvertretungsplan.ui.intro.IntroActivity;
import de.aurora.mggvertretungsplan.ui.theming.ThemeManager;

public class MainActivity extends AppCompatActivity implements AsyncTaskCompleteListener<String>, SwipeRefreshLayout.OnRefreshListener {
    private SharedPreferences sp;
    private Toolbar toolbar;
    private String class_name;
    private SwipeRefreshLayout mSwipeLayout;
    private CardsAdapter cAdapter;
    private WebsiteParser websiteParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        int themeID = sp.getInt("Theme", 0);
        setTheme(ThemeManager.getTheme(themeID));
        super.onCreate(savedInstanceState);
        websiteParser = new MGGParser();

        // If application is called for the first time, intro slides will show up
        if (sp.getBoolean("firstStart", true)) {
            Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
            startActivity(intent);

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }

        class_name = sp.getString("KlasseGesamt", "5a");

        setContentView(R.layout.layout_main);
        mSwipeLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3);

        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setAlpha(1);
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));
        toolbar.showOverflowMenu();

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        cAdapter = new CardsAdapter(this);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        displaySavedData();
        downloadTimeTable();
        serviceHandler();
    }

    // Checks which hardware key was pressed
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
        downloadTimeTable();
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar = findViewById(R.id.toolbar);
        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass);

        class_name = sp.getString("KlasseGesamt", "5a");
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            recreate();
        }
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
                websiteIntent.launchUrl(this, Uri.parse(websiteParser.getTimeTable_url()));
                break;
            case R.id.action_feedback:
                CustomTabsIntent.Builder chromeTabsFeedbackBuilder = new CustomTabsIntent.Builder();
                chromeTabsFeedbackBuilder.setToolbarColor(color);
                chromeTabsFeedbackBuilder.setShowTitle(true);
                CustomTabsIntent feedbackIntent = chromeTabsFeedbackBuilder.build();
                feedbackIntent.launchUrl(this, Uri.parse(getString(R.string.feedback_url)));
                break;
            case R.id.action_info:
                Spanned informationText;
                if (Build.VERSION.SDK_INT >= 24) {
                    informationText = Html.fromHtml("Programmiert von Rico Jambor<br><br>Bei Fehlern entweder eine Email an:<br><b>rico.jambor@gmail.com</b><br><br>Oder per Telegram an:<br><center><b>@d_Rickyy_b</b></center>", Html.FROM_HTML_MODE_LEGACY);
                } else {
                    //noinspection deprecation
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

    // Method to display the saved data
    private void displaySavedData() {
        TimeTable timeTable = new TimeTable();

        int count = sp.getInt("TT_Changes_Count", timeTable.getCount());

        for (int i = 0; i < count; i++) {
            ArrayList<ArrayList<String>> table;
            table = JsonUtilities.getArrayList(sp.getString("table" + i, ""));
            String date = sp.getString("Date" + i, "01.01.");

            if (table != null) {
                TimeTableDay day = new TimeTableDay(date, table);
                timeTable.addDay(day);
            }
        }

        displayData(timeTable);
    }

    private void serviceHandler() {
        Intent intentsOpen = new Intent(this, BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intentsOpen, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (sp.getBoolean("notification", true)) {
            String interval_s = sp.getString("AbrufIntervall", "1800000");
            long interval = Long.valueOf(interval_s);

            Date date = new Date(System.currentTimeMillis() + interval);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);

            Log.d("MainActivity", String.format("Scheduling BackgroundService - Interval: %s - Next start: %s", interval_s, dateFormat.format(date)));
            startAlarmManager(alarmManager, pendingIntent, interval);
        } else {
            Log.d("MainActivity", "Cancelling BackgroundService");
            endAlarmManager(alarmManager, pendingIntent);
        }
    }

    private void startAlarmManager(AlarmManager alarmManager, PendingIntent pendingIntent, long interval) {
        long firstStart = System.currentTimeMillis() + interval;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstStart, interval, pendingIntent);
    }

    private void endAlarmManager(AlarmManager alarmManager, PendingIntent pendingIntent) {
        alarmManager.cancel(pendingIntent);
    }

    private boolean isConnectionActive() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Get saved class, Check for connection, start downloading the timetable
    private void downloadTimeTable() {
        if (isConnectionActive()) {
            mSwipeLayout.setRefreshing(true);
            class_name = sp.getString("KlasseGesamt", "5a");

            try {
                new DownloadWebPageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, websiteParser.getTimeTable_url());
            } catch (Exception e) {
                mSwipeLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), R.string.toast_noInternetConnection, Toast.LENGTH_SHORT).show();
        }
    }

    // Creates the view of the Android App
    private void displayData(TimeTable timeTable) {
        Log.d("MainActivity", "Display data on screen");
        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass);
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));

        cAdapter.clearItems();
        cAdapter.addDays(timeTable);
        cAdapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
    }

    // Gets called, when website was downloaded
    public void onTaskComplete(String website_html) {
        Log.d("MainActivity", "Async DownloadTask complete!");
        if (website_html.equals("")) {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), R.string.downloadException, Toast.LENGTH_SHORT).show();
            return;
        }

        TimeTable timeTable = websiteParser.parse(website_html, class_name);
        if (timeTable == null)
            return;

        displayData(timeTable);

        try {
            SharedPreferences.Editor editor = sp.edit();

            int i = 0;
            for (TimeTableDay ttd : timeTable.getAllDays()) {
                editor.putString("Date" + i, ttd.getDateString());
                editor.putString("table" + i, JsonUtilities.getJSONArray(ttd.getArrayList()).toString());
                i++;
            }

            editor.putInt("TT_Changes_Count", timeTable.getCount());

            editor.apply();
        } catch (NullPointerException npe) {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_errorOccurred, Toast.LENGTH_LONG).show();
            Log.d("MainActivity", "NullPointerException - Day or table not present.");
        }
    }

   /*
     * Code by Rico Jambor
    */
}
