package de.aurora.mggvertretungsplan.datamodel;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Rico on 26.09.2016.
 */

public class TimeTableElement {
    public static final int EMPTY = -1;
    public static final int CANCELLATION = 0;
    public static final int SUBSTITUTION = 1;

    private final String hour;
    private final String class_name;
    private final String subject;
    private final String newSubject;
    private final String room;
    private final String newRoom;
    private final int type;
    private final String info;

    public TimeTableElement() {
        hour = "";
        class_name = "";
        subject = "";
        newSubject = "";
        room = "";
        newRoom = "";
        type = EMPTY;
        info = "";
    }

    TimeTableElement(String hour, String class_name, String subject, String newSubject, String room, String newRoom, String info) {
        this.hour = hour.replace(" - ", "-");
        this.class_name = class_name;
        this.subject = getFullSubject(subject);
        this.newSubject = getFullSubject(newSubject);
        this.room = room;
        this.newRoom = newRoom;
        this.type = calcType();
        this.info = info.trim();
    }

    // Returns the full name of a subject abbreviation
    @SuppressLint("DefaultLocale")
    private static String getFullSubject(String subj) {
        String pattern = "[0-9]+([a-zA-Z]+)[0-9]*";
        String abbr;

        if (subj.matches(pattern)) {
            abbr = subj.replaceAll(pattern, "$1");
        } else
            abbr = subj;

        if (abbr == null || abbr.equals("")) {
            return "Kein Fach";
        } else {
            switch (abbr.toUpperCase()) {
                case "D":
                    return "Deutsch";
                case "PH":
                    return "Physik";
                case "CH":
                    return "Chemie";
                case "L":
                    return "Latein";
                case "S":
                    return "Spanisch";
                case "E":
                    return "Englisch";
                case "INF":
                    return "Informatik";
                case "LIT":
                    return "Literatur";
                case "EV":
                case "EVR":
                    return "Ev. Religion";
                case "RK":
                case "KAR":
                    return "Kath. Religion";
                case "ET":
                case "ETH":
                    return "Ethik";
                case "M":
                case "MA":
                    return "Mathe";
                case "EK":
                    return "Erdkunde";
                case "BIO":
                    return "Biologie";
                case "MU":
                    return "Musik";
                case "SP":
                    return "Sport";
                case "SW":
                    return "Sport weibl.";
                case "SM":
                    return "Sport männl.";
                case "G":
                    return "Geschichte";
                case "F":
                    return "Französisch";
                case "NWT":
                    return "Naturwissenschaft u. Technik";
                case "GK":
                    return "Gemeinschaftskunde";
                case "SF":
                    return "Seminarkurs";
                case "NP":
                    return "Naturphänomene";
                case "WI":
                    return "Wirtschaft";
                case "METH":
                    return "METH";
                case "BK":
                    return "Bildende Kunst";
                case "LRS":
                    return "LRS";
                case "PSY":
                    return "Psychologie";
                case "PHIL":
                    return "Philosophie";
                default:
                    return subj;
            }
        }
    }

    // Method to get the hour as integer value.
    // Needed, because 'hour' can have values such as "1-2" which can't be converted to an integer
    int getHour_I() {
        String hour = getHour();
        try {
            if (hour.matches("^([0-9]{1,2})$"))
                return Integer.parseInt(hour);
            else
                return Integer.parseInt(hour.replaceAll("^([0-9]{1,2}).*", "$1"));
        } catch (NumberFormatException nfe) {
            Log.e("TimeTableElement", String.format("Hour doesn't match pattern: %s", hour));
            return 12;
        }
    }

    public String getHour() {
        return hour;
    }

    public String getClass_name() {
        return this.class_name;
    }

    public String getSubject() {
        return subject;
    }

    public String getNewSubject() {
        return newSubject;
    }

    public String getRoom() {
        return room;
    }

    public String getNewRoom() {
        return newRoom;
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return this.info;
    }

    public int getColor() {
        switch (type) {
            case CANCELLATION:
                return Color.parseColor("#FF6961");
            case SUBSTITUTION:
                return Color.parseColor("#779ECB");
            default:
                return Color.parseColor("#F5F5F5");
        }
    }

    public String getInfoForDisplay() {
        String info_e;
        if (!info.isEmpty() && !" ".equals(info)) {
            info_e = info.substring(0, 1).toUpperCase() + info.substring(1);

            if (!subject.equals(newSubject) && !newSubject.equals("---") && !newSubject.isEmpty()) {
                info_e = String.format("%s - %s", this.newSubject, info);
            }
        } else if (type == CANCELLATION) {
            info_e = "Entfällt";
        } else if (type == SUBSTITUTION) {
            if (!subject.equals(newSubject) && !newSubject.equals("---") && !newSubject.isEmpty()) {
                info_e = String.format("%s - %s", this.newSubject, "Vertretung");
            } else
                info_e = "Vertretung";
        } else {
            info_e = this.newSubject;
        }

        return info_e;
    }

    boolean equals(TimeTableElement tte) {
        return getDiffAmount(tte) == 0;
    }

    // Returns the number of differences between two elements
    int getDiffAmount(TimeTableElement tte) {
        int diffs = 0;
        ArrayList<String> list1 = getElementAsList();
        ArrayList<String> list2 = tte.getElementAsList();

        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i)))
                diffs++;
        }

        return diffs;
    }

    // Returns the TTE object as ArrayList
    ArrayList<String> getElementAsList() {
        return new ArrayList<>(Arrays.asList(hour, class_name, subject, newSubject, room, newRoom, info));
    }

    private int calcType() {
        if (newSubject.equals("---") && newRoom.equals("---"))
            return CANCELLATION;
        else
            return SUBSTITUTION;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s | %s", hour, class_name, subject, newSubject, room, newRoom, info);
    }

}

