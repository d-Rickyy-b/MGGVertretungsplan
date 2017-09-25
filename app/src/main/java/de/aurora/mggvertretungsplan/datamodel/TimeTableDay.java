package de.aurora.mggvertretungsplan.datamodel;

import android.util.Log;

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
    private final ArrayList<TimeTableElement> timeTableElements = new ArrayList<>();
    private ArrayList<ArrayList<String>> timeTableDay_List = new ArrayList<>();
    private Date date = new Date();

    public TimeTableDay(String date, ArrayList<ArrayList<String>> timeTableDay_List) {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        int currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);

        this.timeTableDay_List = timeTableDay_List;

        try {
            if (date.length() == 6)
                this.date = fullDateFormat.parse(date + currentYear);
            else {
                this.date = fullDateFormat.parse(date);
            }
        } catch (ParseException e) {
            Log.e("Vertretungsplan_TTD", e.getMessage());
            this.date = new Date();
        }

        for (ArrayList<String> zeile : timeTableDay_List) {
            TimeTableElement timeTableElement = new TimeTableElement(zeile.get(0), hilfsMethoden.abkuerzung(zeile.get(2)), hilfsMethoden.abkuerzung(zeile.get(3)), zeile.get(4), zeile.get(5), zeile.get(6));
            addElement(timeTableElement);
        }

    }

    private void addElement(TimeTableElement tte) {
        int index = 0;
        for (int i = 0; i < timeTableElements.size(); i++) {
            if (tte.getHour_I() < timeTableElements.get(i).getHour_I()) {
                break;
            }
            index++;
        }

        timeTableElements.add(index, tte);
    }

    // Returns the number of unique items in one list compared to another list.
    // A single list should only contain unique items
    private static int getUniques(ArrayList<TimeTableElement> e1, ArrayList<TimeTableElement> e2) {
        // TODO This method has a bug with getting uniques when changes to an existing date have been made
        int uniques = 0;

        for (TimeTableElement element : e1) {
            boolean isUnique = true;

            for (TimeTableElement element_o : e2) {
                if (element.equals(element_o)) {
                    isUnique = false;
                    break;
                }
            }

            if (isUnique) {
                uniques += 1;
            }
        }
        return uniques;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        return dateFormat.format(date);
    }

    public String getFullDateString() {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.GERMANY);
        return fullDateFormat.format(date);
    }

    public ArrayList<TimeTableElement> getElements() {
        return timeTableElements;
    }

    public ArrayList<ArrayList<String>> getArrayList() {
        //TODO implement another way instead of saving the list
        return timeTableDay_List;
    }

    public int getCancellations() {
        return timeTableElements.size();
    }

    // Returns the number of differences between two lists
    public int getDifferences(TimeTableDay ttd) {
        int diffs = 0;
        if (getCancellations() >= ttd.getCancellations())
            diffs += getUniques(timeTableElements, ttd.getElements());
        else
            diffs += getUniques(ttd.getElements(), timeTableElements);

        return diffs;
    }

    public boolean isSameDay(TimeTableDay ttd) {
        return date.getTime() == ttd.getDate().getTime();
    }

    public String toString() {
        String result = "";
        result += getFullDateString() + "\n";
        for (TimeTableElement tte : timeTableElements) {
            result += tte.toString() + "\n";
        }

        return result;
    }
}
