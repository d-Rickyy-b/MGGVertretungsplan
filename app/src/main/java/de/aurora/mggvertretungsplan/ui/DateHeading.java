package de.aurora.mggvertretungsplan.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rico on 26.09.2016.
 */

public class DateHeading {
    private final Date date;

    public DateHeading(Date date) {
        this.date = date;
    }

    public String getDay_name() {
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.GERMANY);
        return dayOfWeekFormat.format(date);
    }

    public Date getDate() {
        return this.date;
    }

    public String getWholeDate() {
        SimpleDateFormat wholeDate = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.GERMANY);
        return wholeDate.format(date);
    }

    public String getTitle() {
        return "DateHeading";
    }

}
