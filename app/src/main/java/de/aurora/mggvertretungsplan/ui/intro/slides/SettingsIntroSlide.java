package de.aurora.mggvertretungsplan.ui.intro.slides;

/**
 * Created by Rico on 02.10.2016.
 */

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntroBaseFragment;
import com.github.paolorotolo.appintro.CustomFontCache;

import de.aurora.mggvertretungsplan.R;

public class SettingsIntroSlide extends AppIntroBaseFragment {
    private static final String DATA_SWITCH = "de.aurora.mggvertretungsplan.ui.intro.slides.InputDemoSlide_switch";
    private static final String DATA_CLASS = "de.aurora.mggvertretungsplan.ui.intro.slides.InputDemoSlide_classSpinner";
    private static final String DATA_STUFE = "de.aurora.mggvertretungsplan.ui.intro.slides.InputDemoSlide_stufeSpinner";

    private AppCompatSpinner stufeSpinner, classSpinner;
    private SwitchCompat notificationsSwitch;
    private SharedPreferences sp;

    private boolean isSwitchChecked = true;
    private String title, titleTypeface, description, descTypeface;
    private int drawable, bgColor, titleColor, descColor;


    public static SettingsIntroSlide newInstance(CharSequence title, CharSequence description, int bgColor, int titleColor, int descColor) {
        SettingsIntroSlide slide = new SettingsIntroSlide();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title.toString());
        args.putString(ARG_DESC, description.toString());
        args.putInt(ARG_BG_COLOR, bgColor);
        args.putInt(ARG_TITLE_COLOR, titleColor);
        args.putInt(ARG_DESC_COLOR, descColor);
        slide.setArguments(args);
        return slide;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            isSwitchChecked = savedInstanceState.getBoolean(DATA_SWITCH);
            title = savedInstanceState.getString(ARG_TITLE);
            titleTypeface = savedInstanceState.getString(ARG_TITLE_TYPEFACE);
            description = savedInstanceState.getString(ARG_DESC);
            descTypeface = savedInstanceState.getString(ARG_DESC_TYPEFACE);
            bgColor = savedInstanceState.getInt(ARG_BG_COLOR);
            titleColor = savedInstanceState.getInt(ARG_TITLE_COLOR);
            descColor = savedInstanceState.getInt(ARG_DESC_COLOR);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_DRAWABLE, drawable);
        outState.putString(ARG_TITLE, title);
        outState.putString(ARG_DESC, description);
        outState.putInt(ARG_BG_COLOR, bgColor);
        outState.putInt(ARG_TITLE_COLOR, titleColor);
        outState.putInt(ARG_DESC_COLOR, descColor);
        outState.putString(DATA_CLASS, classSpinner.getSelectedItem().toString());
        outState.putString(DATA_STUFE, stufeSpinner.getSelectedItem().toString());
        outState.putBoolean(DATA_SWITCH, notificationsSwitch.isChecked());
        super.onSaveInstanceState(outState);

        saveData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        isSwitchChecked = sp.getBoolean("notification", true);

        View v = inflater.inflate(R.layout.intro_fragment_settings, container, false);
        TextView t = v.findViewById(com.github.paolorotolo.appintro.R.id.title);
        TextView d = v.findViewById(com.github.paolorotolo.appintro.R.id.description);
        LinearLayout mainLayout = v.findViewById(com.github.paolorotolo.appintro.R.id.main);

        t.setText(title);
        t.setTextColor(titleColor);

        if (titleTypeface != null && titleTypeface.equals("")) {
            if (CustomFontCache.get(titleTypeface, getContext()) != null) {
                t.setTypeface(CustomFontCache.get(titleTypeface, getContext()));
            }
        }

        d.setText(description);
        d.setTextColor(descColor);

        if (descTypeface != null && descTypeface.equals("")) {
            if (CustomFontCache.get(descTypeface, getContext()) != null) {
                d.setTypeface(CustomFontCache.get(descTypeface, getContext()));
            }
        }

        // ClassLayer:
        stufeSpinner = v.findViewById(R.id.slide_class_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity().getApplicationContext(), R.array.ClassLayer_List, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.view_spinner_dropdown_item);
        stufeSpinner.setAdapter(adapter);
        stufeSpinner.setSelection(getPositionFromString(getResources().getStringArray(R.array.ClassLayer_List), sp.getString("Klassenstufe", "5")));

        stufeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        classSpinner = v.findViewById(R.id.slide_class_spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                getActivity().getApplicationContext(), R.array.Class_List, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(R.layout.view_spinner_dropdown_item);
        classSpinner.setAdapter(adapter2);
        classSpinner.setSelection(getPositionFromString(getResources().getStringArray(R.array.Class_List), sp.getString("Klasse", "a")));

        if (stufeSpinner.getSelectedItem().toString().equals("K1") || stufeSpinner.getSelectedItem().toString().equals("K2")) {
            classSpinner.setActivated(false);
            classSpinner.setEnabled(false);
        }

        notificationsSwitch = v.findViewById(R.id.slide_notifications_switch);
        notificationsSwitch.setTextColor(Color.WHITE);

        notificationsSwitch.setChecked(sp.getBoolean("notification", true));
        mainLayout.setBackgroundColor(bgColor);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (getArguments() != null && !getArguments().isEmpty()) {
            drawable = getArguments().getInt(ARG_DRAWABLE);
            title = getArguments().getString(ARG_TITLE);
            titleTypeface = getArguments().containsKey(ARG_TITLE_TYPEFACE) ?
                    getArguments().getString(ARG_TITLE_TYPEFACE) : "";
            description = getArguments().getString(ARG_DESC);
            descTypeface = getArguments().containsKey(ARG_DESC_TYPEFACE) ?
                    getArguments().getString(ARG_DESC_TYPEFACE) : "";
            bgColor = getArguments().getInt(ARG_BG_COLOR);
            titleColor = getArguments().containsKey(ARG_TITLE_COLOR) ?
                    getArguments().getInt(ARG_TITLE_COLOR) : 0;
            descColor = getArguments().containsKey(ARG_DESC_COLOR) ?
                    getArguments().getInt(ARG_DESC_COLOR) : 0;
        }
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

    private int getPositionFromString(String[] klassen, String gesuchteKlasse) {
        for (int i = 0; i < klassen.length; i++) {
            if (klassen[i].equals(gesuchteKlasse)) {
                return i;
            }
        }

        return -1;
    }

    public void saveData() {
        String klasseGesamt, klasse, stufe;

        if (stufeSpinner.getSelectedItem().toString().equals("K1") || stufeSpinner.getSelectedItem().toString().equals("K2")) {
            klasseGesamt = stufeSpinner.getSelectedItem().toString();
        } else {
            klasseGesamt = stufeSpinner.getSelectedItem().toString() + classSpinner.getSelectedItem().toString();
        }

        stufe = stufeSpinner.getSelectedItem().toString();
        klasse = classSpinner.getSelectedItem().toString();

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("notification", notificationsSwitch.isChecked());
        editor.putString("KlasseGesamt", klasseGesamt);
        editor.putString("Klassenstufe", stufe);
        editor.putString("Klasse", klasse);
        editor.apply();
    }
}
