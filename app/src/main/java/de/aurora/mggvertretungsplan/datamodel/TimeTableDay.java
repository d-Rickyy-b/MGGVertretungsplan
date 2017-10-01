package de.aurora.mggvertretungsplan.datamodel;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Rico on 19.09.2017.
 */

public class TimeTableDay {
    private final ArrayList<TimeTableElement> timeTableElements = new ArrayList<>();
    private Date date = new Date();

    public TimeTableDay(String date, ArrayList<ArrayList<String>> timeTableDay_List) {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        int currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);

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
            TimeTableElement timeTableElement = new TimeTableElement(zeile.get(0), zeile.get(1), zeile.get(2), zeile.get(3), zeile.get(4), zeile.get(5), zeile.get(6));
            addElement(timeTableElement);
        }

        mergeConsecutiveCancellations();
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
        ArrayList<ArrayList<String>> elements = new ArrayList<>();

        for (TimeTableElement tte : timeTableElements) {
            elements.add(tte.getElementAsList());
        }
        return elements;
    }

    public int getCancellations() {
        return timeTableElements.size();
    }

    // Returns the number of differences between two lists
    public int getDifferences(TimeTableDay ttd) {
        // TODO Refactor in own method to avoid duplicate code
        int diffs = 0;

        ArrayList<TimeTableElement> savedElements = ttd.getElements();

        for (TimeTableElement tte : getElements()) {
            boolean dayExists = false;
            for (TimeTableElement tte2 : savedElements) {
                if (tte.equals(tte2)) {
                    dayExists = true;
                    break;
                } else if (tte.getDiffAmount(tte2) == 1) {
                    savedElements.remove(tte2);
                    dayExists = false;
                    break;
                }
            }

            if (!dayExists) {
                diffs++;
            }
        }

        for (TimeTableElement tte : savedElements) {
            boolean dayExists = false;
            for (TimeTableElement tte2 : getElements()) {
                if (tte.equals(tte2)) {
                    dayExists = true;
                    break;
                } else if (tte.getDiffAmount(tte2) == 1) {
                    Log.e("TTD", "This shouldn't ever happen");
                    dayExists = false;
                    break;
                }
            }

            if (!dayExists) {
                diffs++;
            }
        }

        return diffs;
    }

    public boolean isSameDay(TimeTableDay ttd) {
        return date.getTime() == ttd.getDate().getTime();
    }

    // Merges cancellations together (3. & 4. -> 3-4)
    private void mergeConsecutiveCancellations() {
        if (getCancellations() <= 1) {
            return;
        }

        for (int i = 1; i < getCancellations(); i++) {
            TimeTableElement tte = timeTableElements.get(i - 1);
            TimeTableElement tte2 = timeTableElements.get(i);

            if (tte.getHour().length() <= 2 && tte2.getHour().length() <= 2 &&
                    tte.getHour_I() == (tte2.getHour_I() - 1)) {
                if (tte.getType() == tte2.getType() &&
                        tte.getRoom().equals(tte2.getRoom()) &&
                        tte.getNewRoom().equals(tte2.getNewRoom()) &&
                        tte.getSubject().equals(tte2.getSubject()) &&
                        tte.getNewSubject().equals(tte2.getNewSubject())) {

                    String newTime = String.format("%s-%s", tte.getHour(), tte2.getHour());
                    String newInfo;

                    if (tte.getInfo().equals(tte2.getInfo()))
                        newInfo = tte.getInfo();
                    else
                        newInfo = String.format("%s - %s", tte.getInfo(), tte2.getInfo());

                    TimeTableElement replacement = new TimeTableElement(newTime, tte.getClass_name(), tte.getSubject(), tte.getNewSubject(), tte.getRoom(), tte.getNewRoom(), newInfo);

                    timeTableElements.remove(tte);
                    timeTableElements.remove(tte2);
                    addElement(replacement);

                    i--;
                }

            }
        }
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
