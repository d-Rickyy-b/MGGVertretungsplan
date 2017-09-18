package de.aurora.mggvertretungsplan.ui.intro;

/**
 * Created by Rico on 02.10.2016.
 */

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.util.TypedValue;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.ui.intro.slides.SettingsIntroSlide;
import de.aurora.mggvertretungsplan.ui.theming.ThemeManager;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeID = PreferenceManager.getDefaultSharedPreferences(this).getInt("Theme", 0);
        setTheme(ThemeManager.getTheme(themeID));
        super.onCreate(savedInstanceState);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        @ColorInt int backgroundColor = typedValue.data;

        int image = R.drawable.app_logo_material;
        int titleColor = Color.WHITE;

        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_welcome_title), getString(R.string.slide_welcome_descr), image, backgroundColor));
        addSlide(SettingsIntroSlide.newInstance(getString(R.string.slide_settings_title), getString(R.string.slide_settings_descr), backgroundColor, titleColor, titleColor));

        setGoBackLock(true);
        showSkipButton(false);
        setDoneText(getString(R.string.slide_action_done));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SettingsIntroSlide slide = (SettingsIntroSlide) getSlides().get(1);
        slide.saveData();
        finish();
    }
}