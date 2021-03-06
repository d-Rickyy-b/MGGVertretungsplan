package de.aurora.mggvertretungsplan.ui.intro.slides;

/**
 * Created by Rico on 02.10.2016.
 */

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntroBaseFragment;
import com.github.paolorotolo.appintro.util.TypefaceContainer;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import de.aurora.mggvertretungsplan.R;

@SuppressWarnings("ClassWithTooManyFields")
public class SettingsIntroSlide extends AppIntroBaseFragment {
    private static final String DATA_SWITCH = "de.aurora.mggvertretungsplan.ui.intro.slides.SettingsSlide_switch";
    private static final String DATA_CLASS = "de.aurora.mggvertretungsplan.ui.intro.slides.SettingsSlide_classSpinner";
    private static final String DATA_STUFE = "de.aurora.mggvertretungsplan.ui.intro.slides.SettingsSlide_stufeSpinner";

    private AppCompatSpinner layerSpinner, classSpinner;
    private SwitchCompat notificationsSwitch;
    private SharedPreferences sp;

    private int bgColor, titleColor, descColor;
    private String title, description;
    private TypefaceContainer titleTypeface = null, descTypeface = null;

    private boolean isSwitchChecked = true;

    public static SettingsIntroSlide newInstance(String title, String description, int bgColor, int titleColor, int descColor) {
        SettingsIntroSlide slide = new SettingsIntroSlide();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, description);
        args.putInt(ARG_BG_COLOR, bgColor);
        args.putInt(ARG_TITLE_COLOR, titleColor);
        args.putInt(ARG_DESC_COLOR, descColor);
        slide.setArguments(args);
        return slide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null && !getArguments().isEmpty()) {
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESC);
            bgColor = getArguments().getInt(ARG_BG_COLOR);
            titleColor = getArguments().getInt(ARG_TITLE_COLOR, 0);
            descColor = getArguments().getInt(ARG_DESC_COLOR, 0);

            String argsTitleTypeface = getArguments().getString(ARG_TITLE_TYPEFACE, "");
            String argsDescTypeface = getArguments().getString(ARG_DESC_TYPEFACE, "");

            int argsTitleTypefaceRes = getArguments().getInt(ARG_TITLE_TYPEFACE_RES, 0);
            int argsDescTypefaceRes = getArguments().getInt(ARG_DESC_TYPEFACE_RES, 0);

            if (!argsTitleTypeface.equals(""))
                titleTypeface = new TypefaceContainer(argsTitleTypeface, argsTitleTypefaceRes);
            if (!argsDescTypeface.equals(""))
                descTypeface = new TypefaceContainer(argsDescTypeface, argsDescTypefaceRes);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            isSwitchChecked = savedInstanceState.getBoolean(DATA_SWITCH);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(DATA_CLASS, classSpinner.getSelectedItem().toString());
        outState.putString(DATA_STUFE, layerSpinner.getSelectedItem().toString());
        outState.putBoolean(DATA_SWITCH, notificationsSwitch.isChecked());

        if (titleTypeface != null) {
            outState.putString(ARG_TITLE_TYPEFACE, titleTypeface.getTypeFaceUrl());
            outState.putInt(ARG_TITLE_TYPEFACE_RES, titleTypeface.getTypeFaceResource());
        }
        if (descTypeface != null) {
            outState.putString(ARG_DESC_TYPEFACE, descTypeface.getTypeFaceUrl());
            outState.putInt(ARG_DESC_TYPEFACE_RES, descTypeface.getTypeFaceResource());
        }

        super.onSaveInstanceState(outState);

        saveData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        isSwitchChecked = sp.getBoolean("notification", true);

        View view = inflater.inflate(R.layout.intro_fragment_settings, container, false);
        TextView titleText = view.findViewById(com.github.paolorotolo.appintro.R.id.title);
        TextView descriptionText = view.findViewById(com.github.paolorotolo.appintro.R.id.description);
        LinearLayout mainLayout = view.findViewById(com.github.paolorotolo.appintro.R.id.main);

        titleText.setText(title);

        if (titleColor != 0) {
            titleText.setTextColor(titleColor);
        }

        if (titleTypeface != null)
            titleTypeface.applyTo(titleText);

        descriptionText.setText(description);
        descriptionText.setTextColor(descColor);
        if (descTypeface != null)
            descTypeface.applyTo(descriptionText);

        // ClassLayer:
        layerSpinner = view.findViewById(R.id.slide_class_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity().getApplicationContext(), R.array.ClassLayer_List, R.layout.custom_spinner_item);
        adapter.setDropDownViewResource(R.layout.view_spinner_dropdown_item);
        layerSpinner.setAdapter(adapter);
        layerSpinner.setSelection(getPositionFromString(getResources().getStringArray(R.array.ClassLayer_List), sp.getString("Klassenstufe", "5")));

        layerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            Adapter initializedAdapter = null;

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (initializedAdapter != parentView.getAdapter()) {
                    initializedAdapter = parentView.getAdapter();
                    return;
                }

                String selected = parentView.getItemAtPosition(position).toString();
                if (selected.equals("K1") || selected.equals("K2")) {
                    classSpinner.setActivated(false);
                    classSpinner.setEnabled(false);
                } else {
                    classSpinner.setActivated(true);
                    classSpinner.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // This needs to be overwritten ¯\_(ツ)_/¯
            }
        });

        // Class:
        classSpinner = view.findViewById(R.id.slide_class_spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                getActivity().getApplicationContext(), R.array.Class_List, R.layout.custom_spinner_item);
        adapter2.setDropDownViewResource(R.layout.view_spinner_dropdown_item);
        classSpinner.setAdapter(adapter2);
        classSpinner.setSelection(getPositionFromString(getResources().getStringArray(R.array.Class_List), sp.getString("Klasse", "a")));

        if (layerSpinner.getSelectedItem().toString().equals("K1") || layerSpinner.getSelectedItem().toString().equals("K2")) {
            classSpinner.setActivated(false);
            classSpinner.setEnabled(false);
        }

        notificationsSwitch = view.findViewById(R.id.slide_notifications_switch);
        notificationsSwitch.setTextColor(Color.WHITE);

        notificationsSwitch.setChecked(sp.getBoolean("notification", true));
        mainLayout.setBackgroundColor(bgColor);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        notificationsSwitch.setChecked(isSwitchChecked);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.intro_fragment_settings;
    }

    private int getPositionFromString(String[] classes, String searchedClass) {
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].equals(searchedClass)) {
                return i;
            }
        }

        return -1;
    }

    public void saveData() {
        String fullClassName = "5a", className = "a", classLayer = "5";
        boolean notify;

        // Check if one or both spinners are null, if they are initialize with standard values
        if (layerSpinner != null && classSpinner != null) {
            classLayer = layerSpinner.getSelectedItem().toString();
            className = classSpinner.getSelectedItem().toString();

            if ("K1".equals(classLayer) || "K2".equals(classLayer)) {
                fullClassName = classLayer;
            } else {
                fullClassName = classLayer + className;
            }
        }

        // When notificationsSwitch is null or notificationsSwitch is checked, set notify to true
        notify = ((notificationsSwitch == null) || notificationsSwitch.isChecked());

        // Refresh sp, just in case it got somehow removed
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();

        editor.putBoolean("notification", notify);
        editor.putString("KlasseGesamt", fullClassName);
        editor.putString("Klassenstufe", classLayer);
        editor.putString("Klasse", className);
        editor.apply();
    }
}
