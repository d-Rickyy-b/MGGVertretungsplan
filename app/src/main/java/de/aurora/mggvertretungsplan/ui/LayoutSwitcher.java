package de.aurora.mggvertretungsplan.ui;

import android.support.annotation.StyleRes;

import de.aurora.mggvertretungsplan.R;

public class LayoutSwitcher {
    private static final int bluePrimary = 0xFF64B5F6;
    private static final int pinkPrimary = 0xFFD81B60;
    private static final int stdPrimary = 0xFF757575;

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

    public static int getID(int color){
        switch (color) {
            case stdPrimary:
                return 0;
            case bluePrimary:
                return 1;
            case pinkPrimary:
                return 2;
            default:
                return 0;
        }
    }
}
