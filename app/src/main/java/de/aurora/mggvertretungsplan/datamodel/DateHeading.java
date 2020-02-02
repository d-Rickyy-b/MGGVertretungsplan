package de.aurora.mggvertretungsplan.datamodel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rico on 26.09.2016.
 */

public class DateHeading {
    @SuppressWarnings("CanBeFinal")
    private final Date date;
    private final Week week;

    public DateHeading(Date date, Week week) {
        this.date = date;
        this.week = week;
    }

    public DateHeading(Date date) {
        this(date, new Week("A"));
    }

    public String getWholeDate() {
        SimpleDateFormat wholeDate = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.getDefault());
        return wholeDate.format(date);
    }

    public Week getWeek() {
        return this.week;
    }

}
