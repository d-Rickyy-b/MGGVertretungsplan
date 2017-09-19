package de.aurora.mggvertretungsplan.datamodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.aurora.mggvertretungsplan.hilfsMethoden;

/**
 * Created by Rico on 19.09.2017.
 */

public class TimeTableDay {
    private ArrayList<TimeTableElement> timeTableElements = new ArrayList<>();
    private Date date = new Date();
    private int currentYear;

    public TimeTableDay(Date date) {
        this.date = date;
    }

    public TimeTableDay(String date, ArrayList<ArrayList<String>> timeTableDay_List) {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);

        try {
            this.date = fullDateFormat.parse(date + currentYear);
        } catch (ParseException e) {
            e.printStackTrace();
            this.date = new Date();
        }

        for (ArrayList<String> zeile : timeTableDay_List) {
            TimeTableElement timeTableElement = new TimeTableElement(zeile.get(0), hilfsMethoden.abkuerzung(zeile.get(2)), hilfsMethoden.abkuerzung(zeile.get(3)), zeile.get(4), zeile.get(5), hilfsMethoden.getType(zeile.get(3), zeile.get(5)), zeile.get(6));
            this.addElement(timeTableElement);
        }

    }

    public void addElement(TimeTableElement tte) {
        this.timeTableElements.add(tte);
    }

    public Date getDate() {
        return this.date;
    }

    public String getDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        return dateFormat.format(this.date);
    }

    public String getFullDateString() {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.GERMANY);
        return fullDateFormat.format(this.date);
    }

    public ArrayList<TimeTableElement> getElements() {
        return this.timeTableElements;
    }

    public int getCancellations() {
        return this.timeTableElements.size();
    }

    public ArrayList<ArrayList<String>> getArrayList() {
        ArrayList<ArrayList<String>> aL = new ArrayList<>();

        for (TimeTableElement tte : timeTableElements) {
            //TODO this doesn't work yet!
            ArrayList<String> stringList = new ArrayList<>();

            stringList.add(tte.getHour());
            stringList.add(tte.getSubject());
            stringList.add(tte.getRoom());
            stringList.add(tte.getNewRoom());
            stringList.add(tte.getType());
            stringList.add(tte.getInfo());
        }


        return new ArrayList<ArrayList<String>>();
    }
}
