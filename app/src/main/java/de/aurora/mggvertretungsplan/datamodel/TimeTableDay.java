package de.aurora.mggvertretungsplan.datamodel;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import de.aurora.mggvertretungsplan.R;
import de.aurora.mggvertretungsplan.util.Logger;

/**
 * Created by Rico on 19.09.2017.
 */

public class TimeTableDay {
    private static final String TAG = "TimeTableDay";

    private final ArrayList<TimeTableElement> timeTableElements = new ArrayList<>();
    private LocalDate date = LocalDate.now();
    private Week week;

    public TimeTableDay(String date, String week, ArrayList<ArrayList<String>> timeTableDay_List) {
        this.week = new Week(week);
        setDate(date);

        for (ArrayList<String> row : timeTableDay_List) {
            TimeTableElement timeTableElement = new TimeTableElement(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5), row.get(6));
            addElement(timeTableElement);
        }

        mergeConsecutiveCancellations();
    }

    public TimeTableDay(LocalDate date, Week week, ArrayList<TimeTableElement> timeTableElements) {
        this.date = date;
        this.week = week;
        this.timeTableElements.addAll(timeTableElements);
    }

    public TimeTableDay(JSONObject jsonObject) {
        try {
            String date = jsonObject.getString("date");
            setDate(date);

            String mWeek = jsonObject.getString("week");
            this.week = new Week(mWeek);

            JSONArray jsonArray = jsonObject.getJSONArray("elements");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonDay = (JSONObject) jsonArray.get(i);
                TimeTableElement tte = new TimeTableElement(jsonDay);
                addElement(tte);
            }
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    public String getNotificationTitle(Context context) {
        String formatString = context.getString(R.string.notification_title_dateformat);
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(formatString, Locale.getDefault());
        return this.date.format(sdf);
    }

    public String getNotificationTicker(Context context) {
        return context.getString(R.string.notification_ticker);
    }

    public String getNotificationText(Context context) {
        StringBuilder sb = new StringBuilder();

        //'{hr}. Std: {subj} {action}'
        String formatString = "%s. Std: %s %s\n";
        for (TimeTableElement tte : this.timeTableElements) {
            String action = "";
            switch (tte.getType()) {
                case TimeTableElement.SUBSTITUTION:
                    action = context.getString(R.string.cardInfo_representation);
                    break;
                case TimeTableElement.CANCELLATION:
                    action = context.getString(R.string.cardInfo_cancelled);
                    break;
                case TimeTableElement.EMPTY:
                    continue;
            }

            sb.append(String.format(formatString, tte.getHour(), tte.getSubject(), action));
        }

        return sb.toString().trim();
    }

    private void addElement(TimeTableElement tte) {
        // Ignore elements that are already in the list
        for (TimeTableElement t : timeTableElements) {
            if (t.equals(tte)) {
                return;
            }
        }

        int index = 0;
        for (int i = 0; i < timeTableElements.size(); i++) {
            if (tte.getHour_I() < timeTableElements.get(i).getHour_I()) {
                break;
            }
            index++;
        }

        timeTableElements.add(index, tte);
    }

    public boolean isInFuture(LocalDateTime currentDate) {
        LocalDateTime dateTime = LocalDateTime.of(getDate(), LocalTime.of(0, 0, 0));
        return (dateTime.plusHours(16).isAfter(currentDate));
    }

    public boolean isInFuture() {
        return isInFuture(LocalDateTime.now());
    }

    public Week getWeek() {
        return this.week;
    }

    public LocalDate getDate() {
        return date;
    }

    private void setDate(String date) {
        DateTimeFormatter fullDateFormat = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.getDefault());
        int currentYear = LocalDate.now().getYear();

        try {
            if (date.length() == 6) {
                this.date = LocalDate.parse(date + currentYear, fullDateFormat);
            } else {
                this.date = LocalDate.parse(date, fullDateFormat);
            }
        } catch (RuntimeException e) {
            Logger.e(TAG, e.getMessage());
            this.date = LocalDate.now();
        }
    }

    public String getDateString() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());
        return date.format(dateFormat);
    }

    public String getFullDateString() {
        DateTimeFormatter fullDateFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.getDefault());
        return fullDateFormat.format(date);
    }

    public TimeTableDay getTTDbyClass(String className) {
        return new TimeTableDay(this.date, this.week, this.getElements(className));
    }

    public ArrayList<TimeTableElement> getElements(String className) {
        ArrayList<TimeTableElement> elementsOfClass = new ArrayList<>();
        Grade grade = new Grade(className);

        for (TimeTableElement tte : timeTableElements) {
            String elementClassName = tte.getClassName();
            // TODO Remove the "contains" part
            if (grade.matches(elementClassName) || elementClassName.contains(className))
                elementsOfClass.add(tte);
        }

        return elementsOfClass;
    }

    public TimeTableDay filter(String className) {
        return new TimeTableDay(this.date, this.week, getElements(className));
    }

    public ArrayList<ArrayList<String>> getArrayList() {
        ArrayList<ArrayList<String>> elements = new ArrayList<>();

        for (TimeTableElement tte : timeTableElements) {
            elements.add(tte.getElementAsList());
        }
        return elements;
    }

    public ArrayList<ArrayList<String>> getArrayList(String className) {
        ArrayList<ArrayList<String>> elements = new ArrayList<>();

        for (TimeTableElement tte : getElements(className)) {
            elements.add(tte.getElementAsList());
        }

        return elements;
    }

    public int getElementsCount(String className) {
        return getElements(className).size();
    }

    /**
     * Calculates the differences between two TimeTableDays
     *
     * @param ttd       The old/saved Timetable to be compared against
     * @param className The name of the class to search for
     * @return New TimeTableDay containing only the new elements for a certain class
     */
    TimeTableDay getDifferences(TimeTableDay ttd, String className) {
        ArrayList<TimeTableElement> savedElements = ttd.getElements(className);
        ArrayList<TimeTableElement> newElements = this.getElements(className);

        // Remove same elements, which are contained in both lists
        for (int i = 0; i < newElements.size(); i++) {
            TimeTableElement element1 = newElements.get(i);
            for (int j = 0; j < savedElements.size(); j++) {
                TimeTableElement element2 = savedElements.get(j);

                if (element1.equals(element2)) {
                    newElements.remove(i);
                    savedElements.remove(j);
                    i--;
                    break;
                }
            }
        }

        // Remove similar elements, which are contained in both lists
        for (TimeTableElement tte1 : newElements) {
            for (TimeTableElement tte2 : savedElements) {
                if (tte1.getDiffAmount(tte2) == 1) {
                    // This else part catches elements where only one part (hour, subject, etc.) has changed
                    // Without it, every *change* of an existing element would be counted twice
                    savedElements.remove(tte2);
                    break;
                }
            }
        }

        // Set the elements, which are no longer in the TimeTableDay to inactive, so they can be notified as "removed"
        for (TimeTableElement tte : savedElements) {
            tte.setActive(false);
        }

        // savedElements now contains only those elements which are no longer in the TimeTableDay
        // newElements now only contains those elements which are new (not saved yet) or have changed in a single part
        savedElements.addAll(newElements);
        return new TimeTableDay(date, week, savedElements);
    }

    // Checks if this and the given day are at the same date
    public boolean isSameDay(TimeTableDay ttd) {
        return date.isEqual(ttd.getDate());
    }

    // Merges cancellations together (3. & 4. -> 3-4)
    private void mergeConsecutiveCancellations() {
        if (timeTableElements.size() <= 1) {
            return;
        }

        for (int i = 0; i < timeTableElements.size() - 1; i++) {
            TimeTableElement tte = timeTableElements.get(i);

            for (int j = i + 1; j < timeTableElements.size(); j++) {
                TimeTableElement tte2 = timeTableElements.get(j);

                if (tte.getHour().length() <= 2 && tte2.getHour().length() <= 2 &&
                        tte.getHour_I() == (tte2.getHour_I() - 1)) {
                    if (tte.getType() == tte2.getType() &&
                            tte.getClassName().equals(tte2.getClassName()) &&
                            tte.getRoom().equals(tte2.getRoom()) &&
                            tte.getNewRoom().equals(tte2.getNewRoom()) &&
                            tte.getSubject().equals(tte2.getSubject()) &&
                            tte.getNewSubject().equals(tte2.getNewSubject())) {

                        String newTime = String.format("%s-%s", tte.getHour(), tte2.getHour());
                        String newInfo;

                        if (tte.getInfo().equals(tte2.getInfo()))
                            newInfo = tte.getInfo();
                        else if (tte.getInfo().isEmpty() || tte2.getInfo().isEmpty())
                            newInfo = String.format("%s%s", tte.getInfo(), tte2.getInfo());
                        else
                            newInfo = String.format("%s - %s", tte.getInfo(), tte2.getInfo());

                        TimeTableElement replacement = new TimeTableElement(newTime, tte.getClassName(), tte.getSubject(), tte.getNewSubject(), tte.getRoom(), tte.getNewRoom(), newInfo);

                        timeTableElements.remove(tte);
                        timeTableElements.remove(tte2);
                        addElement(replacement);

                        i--;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getFullDateString());

        for (TimeTableElement tte : timeTableElements) {
            result.append("\n").append(tte.toString());
        }

        return result.toString().trim();
    }

    /**
     * Formats the TTD in a beautiful way to share the TT via other apps
     *
     * @return Beautiful formatted string of the TTD's content
     */
    public String toShareString() {
        StringBuilder result = new StringBuilder();
        result.append(getFullDateString());
        result.append(", ");
        result.append(this.week.toString());
        result.append("-Woche\n");

        if (timeTableElements.isEmpty()) {
            //result.append(getString(R.string.card_no_information));
            //TODO Remove hardcoded string
            result.append("Keine Ausfälle!");
            result.append("\n\n");
            return result.toString();
        }

        for (TimeTableElement tte : timeTableElements) {
            result.append(tte.toShareString());
        }
        result.append("\n");

        return result.toString();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("date", getDateString());
        jsonObject.put("week", week.toString());

        JSONArray jsonArray = new JSONArray();

        for (TimeTableElement tte : timeTableElements) {
            jsonArray.put(tte.toJSON());
        }

        jsonObject.put("elements", jsonArray);
        return jsonObject;
    }
}
