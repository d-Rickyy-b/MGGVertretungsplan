package de.aurora.mggvertretungsplan.datamodel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Created by Rico on 26.09.2016.
 */

public class DateHeading {
    @SuppressWarnings("CanBeFinal")
    private final LocalDate date;
    private final Week week;

    public DateHeading(LocalDate date, Week week) {
        this.date = date;
        this.week = week;
    }

    public DateHeading(LocalDate date) {
        this(date, new Week("A"));
    }

    public String getWholeDate() {
        DateTimeFormatter wholeDate = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.getDefault());
        return wholeDate.format(date);
    }

    public Week getWeek() {
        return this.week;
    }

}
