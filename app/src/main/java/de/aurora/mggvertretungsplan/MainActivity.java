package de.aurora.mggvertretungsplan;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONException;

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
        cAdapter = new CardsAdapter(this);

        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3);

        String toolbarTitle = getString(R.string.toolbarTitle_WithClass);
        toolbar.setAlpha(1);
        toolbar.setTitle(String.format(toolbarTitle, class_name));
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

        long thirtyMinsInMillis = 30 * 60 * 1000;
        ServiceScheduler serviceScheduler = new ServiceScheduler();
        serviceScheduler.setAlarmManager(this, thirtyMinsInMillis);
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

        toolbar = findViewById(R.id.toolbar);
        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass);

        class_name = sp.getString("KlasseGesamt", "5a");
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));
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
                    Toast.makeText(this, R.string.no_url_to_open, Toast.LENGTH_LONG).show();
                    break;
                }

                String url = urls[0];

                launchCustomTabsIntent(color, url);
                break;
            case R.id.action_feedback:
                launchCustomTabsIntent(color, getString(R.string.feedback_url));
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
        CustomTabsIntent.Builder chromeTabsFeedbackBuilder = new CustomTabsIntent.Builder();
        chromeTabsFeedbackBuilder.setToolbarColor(color);
        chromeTabsFeedbackBuilder.setShowTitle(true);
        CustomTabsIntent feedbackIntent = chromeTabsFeedbackBuilder.build();
        feedbackIntent.launchUrl(this, Uri.parse(url));
    }

    /**
     * Display the saved time table data
     */
    private void displaySavedData() {
        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Fetch saved data from disk");
                final TimeTable timeTable;
                String data = StorageUtilities.readFile(MainActivity.this);

                if (data.isEmpty()) {
                    timeTable = new TimeTable();
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        timeTable = new TimeTable(jsonArray);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
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
        Log.d(TAG, "Display data on screen");
        String toolbarTitle_WithClass = getString(R.string.toolbarTitle_WithClass);
        toolbar.setTitle(String.format(toolbarTitle_WithClass, class_name));

        if (timeTable.getDaysCount() == 0 || (sp.getBoolean("", true) && timeTable.getFutureDaysCount() == 0)) {
            recyclerView.setAdapter(new EmptyAdapter(getString(R.string.no_data_to_display)));
        } else {
            recyclerView.setAdapter(cAdapter);
            cAdapter.clearItems();
            cAdapter.addDays(timeTable);
            cAdapter.notifyDataSetChanged();
            Log.d(TAG, "Notify changes");
        }
    }

    private void saveData(final TimeTable timeTable) {
        Log.d(TAG, "Saving data.json to disk");
        try {
            StorageUtilities.writeToFile(this, timeTable.toJSON().toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // Gets called, when website was downloaded and parsed by the parser
    @Override
    public void onParsingComplete(TimeTable timeTable) {
        Log.d(TAG, "Parsing complete!");
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
            int newThemeID = sp.getInt("Theme", 0);
            if (themeID != newThemeID) {
                recreate();
            }

            String newClassName = sp.getString("KlasseGesamt", "5a");
            if (!class_name.equals(newClassName)) {
                displaySavedData();
            }
        }
    }

   /*
     * Code by Rico Jambor
    */
}
