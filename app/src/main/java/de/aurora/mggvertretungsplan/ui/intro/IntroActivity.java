package de.aurora.mggvertretungsplan.ui.intro;

/**
 * Created by Rico on 02.10.2016.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.ui.intro.slides.SettingsIntroSlide;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int image = R.drawable.app_logo_material;
        int backgroundColor = ContextCompat.getColor(this, R.color.introBackground);
        int titleColor = ContextCompat.getColor(this, R.color.Weiss);
        int descrColor = titleColor;

        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_slide_title), getString(R.string.welcome_slide_descr), image, backgroundColor));
        addSlide(SettingsIntroSlide.newInstance(getString(R.string.settings_slide_title), getString(R.string.settings_slide_descr), backgroundColor, titleColor, descrColor));

        setGoBackLock(true);
        showSkipButton(false);
        setDoneText(getString(R.string.action_done));

        setFadeAnimation();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SettingsIntroSlide slide = (SettingsIntroSlide) getSlides().get(1);
        slide.saveData();
        finish();
    }
}