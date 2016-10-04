package de.aurora.mggvertretungsplan.ui.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.aurora.mggvertretungsplan.R;

/**
 * Created by Rico on 29.09.2016.
 */

public class ClassPickerPreference extends Preference {

    public ClassPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClassPickerPreference(Context context) {
        super(context);
    }

    public ClassPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return li.inflate(R.layout.class_picker_preference, parent, false);
    }
}