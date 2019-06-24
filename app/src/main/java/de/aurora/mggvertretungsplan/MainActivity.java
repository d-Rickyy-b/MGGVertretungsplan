package de.aurora.mggvertretungsplan;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.parsing.BaseParser;
import de.aurora.mggvertretungsplan.parsing.BaseParser.ParsingCompleteListener;
import de.aurora.mggvertretungsplan.parsing.MGGParser;
import de.aurora.mggvertretungsplan.parsing.ParsingTask;
import de.aurora.mggvertretungsplan.services.ServiceScheduler;
import de.aurora.mggvertretungsplan.ui.CardsAdapter;
import de.aurora.mggvertretungsplan.ui.EmptyAdapter;
import de.aurora.mggvertretungsplan.ui.intro.IntroActivity;
import de.aurora.mggvertretungsplan.ui.theming.ThemeManager;
import de.aurora.mggvertretungsplan.util.Logger;
import de.aurora.mggvertretungsplan.util.StorageUtilities;

import static de.aurora.mggvertretungsplan.networking.ConnectionManager.isConnectionActive;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ParsingCompleteListener {
    private static final String TAG = "MainActivity";
    private static final String NEED_RELOAD = "need_reload";

    private SharedPreferences sp;
    private Toolbar toolbar;
    private String class_name;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView recyclerView;
    private CardsAdapter cAdapter;
    private BaseParser websiteParser;
    private Context context;
    private int themeID = 0;
    private boolean need_reload = true;
    //TODO this ^ leads to double download of timetable! Without it, the view won't be updated

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        this.sp = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.themeID = this.sp.getInt("Theme", 0);
        setTheme(ThemeManager.getTheme(themeID));

        this.websiteParser = new MGGParser();

        // If application is called for the first time, intro slides will show up
        if (sp.getBoolean("firstStart", true)) {
            Intent intent = new Intent(this.context, IntroActivity.class);
            startActivity(intent);

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }

        class_name = sp.getString("KlasseGesamt", "5a");

        setContentView(R.layout.layout_main);
        mSwipeLayout = findViewById(R.id.swipe_refresh_layout);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        cAdapter = new CardsAdapter(this.context);

        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3);

        String toolbarTitle = getString(R.string.toolbarTitle_WithClass, class_name);
        toolbar.setAlpha(1);
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        if (savedInstanceState != null) {
            need_reload = savedInstanceState.getBoolean(NEED_RELOAD);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        displaySavedData();

        if (need_reload) {
            mSwipeLayout.setRefreshing(true);
            downloadTimeTable();
        }

        // Remove all notifications after opening the app
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancelAll();

        ServiceScheduler serviceScheduler = new ServiceScheduler();
        serviceScheduler.schedule(this.context);
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
    @Override
    public void onRefresh() {
        downloadTimeTable();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForThemeChange();

        toolbar = findViewById(R.id.toolbar);
        class_name = sp.getString("KlasseGesamt", "5a");

        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass, class_name);
        toolbar.setTitle(toolbarTitle_WithClass);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NEED_RELOAD, false);
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
                Intent preferenceIntent = new Intent(this.context, SettingsActivity.class);
                startActivity(preferenceIntent);
                break;
            case R.id.action_website:
                String[] urls = websiteParser.getTimeTableURLs();

                if (urls.length < 1) {
                    Toast.makeText(this.context, R.string.no_url_to_open, Toast.LENGTH_LONG).show();
                    break;
                }

                String url = urls[0];

                launchCustomTabsIntent(color, url);
                break;
            case R.id.action_feedback:
                launchCustomTabsIntent(color, getString(R.string.feedback_url));
                break;
            case R.id.action_share:
                // Share
                String shareText = "Vertretungsplan " + class_name;
                String input = StorageUtilities.readFile(this);
                try {
                    JSONArray json = new JSONArray(input);
                    // For every Week:
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject dateData = json.getJSONObject(i);
                        String Date = dateData.getString("date");
                        String Week = dateData.getString("week");
                        shareText = shareText + "\n" + Date + " Woche " + Week;
                        JSONArray dateElements = dateData.getJSONArray("elements");
                        // For every Element of the Week:
                        for (int j = 0; j < dateElements.length(); j++){
                            JSONObject actChange = dateElements.getJSONObject(j);
                            String actClass = actChange.getString("class_name");
                            String actHour = actChange.getString("hour");
                            String actSubj = actChange.getString("subject");
                            String act_newSubj = actChange.getString("newSubject");
                            String actRoom = actChange.getString("room");
                            String act_newRoom = actChange.getString("newRoom");
                            String actInfo = actChange.getString("info");
                            //Only if own Class:
                            if (actClass.equals(class_name)) {
                                shareText = shareText + "\n >" + actHour + ". Stunde: ";
                                if (!actSubj.equals(act_newSubj)) {
                                    if (!act_newSubj.equals("---")) {
                                        shareText = shareText + " " + actSubj + " -> " + act_newSubj;
                                    } else {
                                        shareText = shareText + " entfällt";
                                    }
                                    ;
                                }
                                ;

                                if (!actRoom.equals(act_newRoom)) {
                                    if (!act_newRoom.equals("---")) {
                                        shareText = shareText + " " + actRoom + " -> " + act_newRoom;
                                    }
                                    ;
                                }
                                ;
                                if (!actInfo.equals("")) {
                                    shareText = shareText + " " + actInfo;
                                }
                                ;


                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Vertretungsplan des Markgrafen-Gymnasiums");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Teilen via..."));
                break;

            case R.id.action_info:
                Spanned informationText;
                if (Build.VERSION.SDK_INT >= 24) {
                    informationText = Html.fromHtml("Programmiert von Rico Jambor<br><br>Bei Fehlern entweder eine Email an:<br><b>rico.jambor@gmail.com</b><br><br>Oder per Telegram an:<br><center><b>@d_Rickyy_b</b></center>", Html.FROM_HTML_MODE_LEGACY);
                } else {
                    //noinspection deprecation
                    informationText = Html.fromHtml("Programmiert von Rico Jambor<br><br>Bei Fehlern entweder eine Email an:<br><b>rico.jambor@gmail.com</b><br><br>Oder per Telegram an:<br><center><b>@d_Rickyy_b</b></center>");
                }

                Drawable icon = ContextCompat.getDrawable(this.context, R.drawable.ic_info_outline_black).mutate();


                int theme_res_id;
                if (sp.getInt("Theme", 0) == 5) {
                    // Invert colors for dark theme
                    try {
                        icon.setColorFilter(new ColorMatrixColorFilter(new float[]{
                                -1, 0, 0, 0, 255, // red = 255 - red
                                0, -1, 0, 0, 255, // green = 255 - green
                                0, 0, -1, 0, 255, // blue = 255 - blue
                                0, 0, 0, 1, 0     // alpha = alpha
                        }));
                    } catch (Exception e) {
                        Logger.e(TAG, e.getMessage());
                    }

                    theme_res_id = R.style.AlertDialogThemeDark;
                } else {
                    theme_res_id = R.style.AlertDialogTheme;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this, theme_res_id);
                builder
                        .setIcon(icon)
                        .setTitle("MGG Vertretungsplan v" + BuildConfig.VERSION_NAME)
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

    /**
     * Takes a color and an url as string to open a Chrome custom tab
     *
     * @param color The color for the custom chrome intent
     * @param url   The url to be called as string
     */
    private void launchCustomTabsIntent(int color, String url) {
        CustomTabsIntent.Builder customTabsIntentBuilder = new CustomTabsIntent.Builder();
        customTabsIntentBuilder.setToolbarColor(color);
        customTabsIntentBuilder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = customTabsIntentBuilder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    /**
     * Display the saved time table data
     */
    private void displaySavedData() {
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, "Fetch saved data from disk");
                final TimeTable timeTable;
                String data = StorageUtilities.readFile(MainActivity.this);

                if (data.isEmpty()) {
                    timeTable = new TimeTable();
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        timeTable = new TimeTable(jsonArray);
                    } catch (JSONException e) {
                        Logger.e(TAG, e.getMessage());
                        return;
                    }
                }

                handler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                displayData(timeTable);
                            }
                        }
                );
            }
        }, "dataLoader").start();

    }

    /**
     * Downloads the current time table to the local storage, if there is an active connection
     */
    private void downloadTimeTable() {
        if (isConnectionActive(this.context)) {
            class_name = sp.getString("KlasseGesamt", "5a");

            try {
                ParsingTask parsingTask = new ParsingTask(this, websiteParser);
                parsingTask.startParsing();
            } catch (Exception e) {
                mSwipeLayout.setRefreshing(false);
                Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(this.context, R.string.toast_noInternetConnection, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a given TimeTable object in the main view
     *
     * @param timeTable A TimeTable element to be displayed on screen
     */
    private void displayData(TimeTable timeTable) {
        Logger.d(TAG, "Display data on screen");
        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass, class_name);
        toolbar.setTitle(toolbarTitle_WithClass);

        if (timeTable.getDaysCount() == 0 || (!sp.getBoolean("displayPastDays", true) && timeTable.getFutureDaysCount() == 0)) {
            // If there are no days in the timetable or if the option "displayPastDays" is not set and the futureDaysCount equals zero
            // (If there is a day which date is today but the current time is < 16h, that day will be shown anyway)
            // Display a *shrug*, when there is no data to be displayed
            recyclerView.setAdapter(new EmptyAdapter(getString(R.string.no_data_to_display)));
        } else {
            recyclerView.setAdapter(cAdapter);
            cAdapter.clearItems();
            cAdapter.addDays(timeTable);
            cAdapter.notifyDataSetChanged();
            Logger.d(TAG, "Update View");
        }
    }

    private void saveData(final TimeTable timeTable) {
        Logger.d(TAG, "Saving data.json to disk");
        try {
            StorageUtilities.writeToFile(this.context, timeTable.toJSON().toString());
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    // Gets called, when website was downloaded and parsed by the parser
    @Override
    public void onParsingComplete(TimeTable timeTable) {
        Logger.d(TAG, "Parsing complete!");
        mSwipeLayout.setRefreshing(false);

        if (timeTable == null || timeTable.getDaysCount() == 0) {
            recyclerView.setAdapter(new EmptyAdapter(getString(R.string.no_data_to_display)));
            Toast.makeText(this.context, R.string.downloadException, Toast.LENGTH_SHORT).show();
            return;
        }

        displayData(timeTable);
        saveData(timeTable);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            checkForThemeChange();

            String newClassName = sp.getString("KlasseGesamt", "5a");
            if (!class_name.equals(newClassName)) {
                displaySavedData();
            }
        }
    }

    private void checkForThemeChange() {
        int newThemeID = sp.getInt("Theme", 0);
        if (themeID != newThemeID) {
            recreate();
        }
    }

    /*
     * Code by Rico Jambor
     */
}
