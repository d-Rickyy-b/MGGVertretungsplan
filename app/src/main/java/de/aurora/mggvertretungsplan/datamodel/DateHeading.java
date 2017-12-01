package de.aurora.mggvertretungsplan.datamodel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rico on 26.09.2016.
 */

public class DateHeading {
    @SuppressWarnings("CanBeFinal")
    private Date date;
    private String week;

    public DateHeading(Date date, String week) {
        this.date = date;
        this.week = week;
    }

    public DateHeading(Date date) {
        this(date, "A");
    }

    public String getWholeDate() {
        SimpleDateFormat wholeDate = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.GERMANY);
        return wholeDate.format(date);
    }

    public String getWeek() {
        return this.week;
    }

}
