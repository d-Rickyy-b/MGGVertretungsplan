package de.aurora.mggvertretungsplan;

import android.app.AlertDialog;
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
import de.aurora.mggvertretungsplan.ui.CardsAdapter;
import de.aurora.mggvertretungsplan.ui.EmptyAdapter;
import de.aurora.mggvertretungsplan.ui.intro.IntroActivity;
import de.aurora.mggvertretungsplan.ui.theming.ThemeManager;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ParsingCompleteListener {
    private static final String TAG = "MainActivity";
    private SharedPreferences sp;
    private Toolbar toolbar;
    private String class_name;
    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView recyclerView;
    private CardsAdapter cAdapter;
    private BaseParser websiteParser;
    private int themeID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        themeID = sp.getInt("Theme", 0);
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

        toolbar = findViewById(R.id.toolbar);
        toolbar.setAlpha(1);
        toolbar.setTitle(String.format(getString(R.string.toolbarTitle_WithClass), class_name));
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= 21) {
            toolbar.setElevation(25);
        }

        cAdapter = new CardsAdapter(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        displaySavedData();
        downloadTimeTable();

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
        Log.d(TAG, String.format("onActivityResult: requestCode %s, resultCode %s", requestCode, resultCode));
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
                String[] urls = websiteParser.getTimeTableURLs();

                if (urls.length < 1) {
                    Toast.makeText(this, R.string.no_url_to_open, Toast.LENGTH_LONG).show();
                    break;
                }

                String url = urls[0];

                CustomTabsIntent.Builder chromeTabsBuilder = new CustomTabsIntent.Builder();
                chromeTabsBuilder.setToolbarColor(color);
                chromeTabsBuilder.setShowTitle(true);
                CustomTabsIntent websiteIntent = chromeTabsBuilder.build();
                websiteIntent.launchUrl(this, Uri.parse(url));
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

    // Method to display the saved data
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

    private boolean isConnectionActive() {
        try {
            final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

            return null != activeNetwork && activeNetwork.isConnected();
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    // Get saved class, Check for connection, start downloading the timetable
    private void downloadTimeTable() {
        if (isConnectionActive()) {
            class_name = sp.getString("KlasseGesamt", "5a");

            try {
                ParsingTask parsingTask = new ParsingTask(this, websiteParser);
                parsingTask.startParsing();
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
    public void displayData(TimeTable timeTable) {
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
            Toast.makeText(getApplicationContext(), R.string.downloadException, Toast.LENGTH_SHORT).show();
            return;
        }

        displayData(timeTable);
        saveData(timeTable);
    }

   /*
     * Code by Rico Jambor
    */
}
