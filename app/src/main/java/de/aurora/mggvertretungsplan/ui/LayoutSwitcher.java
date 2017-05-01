package de.aurora.mggvertretungsplan.ui;

import android.support.annotation.StyleRes;

import de.aurora.mggvertretungsplan.R;

public class LayoutSwitcher {

    @StyleRes
    public static int getTheme(int themeID) {
        switch (themeID) {
            case 0:
                return R.style.AppTheme;
            case 1:
                return R.style.BlueTheme;
            case 2:
                return R.style.PinkTheme;
            default:
                return R.style.AppTheme;
        }
    }
}
