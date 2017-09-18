package de.aurora.mggvertretungsplan.ui.settings;

import android.content.Context;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.aurora.mggvertretungsplan.R;

/**
 * Created by Rico on 29.09.2016.
 */

class PreferenceClassPicker extends Preference {

    public PreferenceClassPicker(Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return li.inflate(R.layout.class_picker_preference, parent, false);
    }
}
