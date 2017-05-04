package de.aurora.mggvertretungsplan.ui;

/**
 * Created by Rico on 26.09.2016.
 */

public class DateHeading {
    private final String day_name;
    private final String date;

    public DateHeading(String day_name, String date) {
        this.day_name = day_name;
        this.date = date;
    }

    public String getDay_name() {
        return this.day_name;
    }

    public String getDate() {
        return this.date;
    }

    public String getWholeDate() {
        return String.format("%s, %s", this.day_name, this.date);
    }

    public String getTitle() {
        return "DateHeading";
    }

}
