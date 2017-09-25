package de.aurora.mggvertretungsplan.datamodel;

import android.annotation.SuppressLint;
import android.graphics.Color;

/**
 * Created by Rico on 26.09.2016.
 */

public class TimeTableElement {
    public static final int EMPTY = -1;
    public static final int CANCELLATION = 0;
    public static final int SUBSTITUTION = 1;

    private final String hour;
    private final String subject;
    private final String newSubject;
    private final String room;
    private final String newRoom;
    private final int type;
    private final String info;

    public TimeTableElement() {
        hour = "";
        subject = "";
        newSubject = "";
        room = "";
        newRoom = "";
        type = EMPTY;
        info = "";
    }

    TimeTableElement(String hour, String subject, String newSubject, String room, String newRoom, String info) {
        this.hour = hour.replace(" - ", "-");
        this.subject = getFullSubject(subject);
        this.newSubject = getFullSubject(newSubject);
        this.room = room;
        this.newRoom = newRoom;
        this.type = calcType();

        if (!info.isEmpty()) {
            info = info.substring(0, 1).toUpperCase() + info.substring(1);

            if (!subject.equals(newSubject) && !newSubject.equals("---") && !newSubject.equals("")) {
                info = String.format("%s - %s", this.newSubject, info);
            }
        } else if (type == CANCELLATION) {
            info = "Entfällt";
        } else if (type == SUBSTITUTION) {
            if (!subject.equals(newSubject) && !newSubject.equals("---") && !newSubject.isEmpty()) {
                info = String.format("%s - %s", this.newSubject, "Vertretung");
            } else
                info = "Vertretung";
        } else {
            info = this.newSubject;
        }

        this.info = info;
    }

    int getHour_I() {
        int hour_i;
        try {
            hour_i = Integer.valueOf(getHour().substring(0, 1));
        } catch (NumberFormatException nfe) {
            hour_i = 1;
        }
        return hour_i;
    }

    public String getHour() {
        return hour;
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
        return info;
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

    boolean equals(TimeTableElement tte) {
        String hour = tte.getHour();
        String subject = tte.getSubject();
        String newSubject = tte.getNewSubject();
        String room = tte.getRoom();
        String newRoom = tte.getNewRoom();
        int type = tte.getType();
        String info = tte.getInfo();

        return hour.equals(this.hour) &&
                subject.equals(this.subject) &&
                newSubject.equals(this.newSubject) &&
                room.equals(this.room) &&
                newRoom.equals(this.newRoom) &&
                type == this.type &&
                info.equals(this.info);
    }

    private int calcType() {
        if (newSubject.equals("---") && newRoom.equals("---"))
            return CANCELLATION;
        else
            return SUBSTITUTION;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s", hour, subject, newSubject, room, newRoom, info);
    }

    // Returns the full name of a subject abbreviation
    @SuppressLint("DefaultLocale")
    private static String getFullSubject(String abbr) {

        String pattern = "[0-9]+([a-zA-Z]+)[0-9]+";
        if (abbr.matches(pattern)) {
            abbr = abbr.replaceAll(pattern, "$1");
        }

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
                case "EVR":
                    return "ev. Religion";
                case "KAR":
                    return "kath. Religion";
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
                    return abbr;
            }
        }
    }

//    public String getHash() {
//        return String.format("%s%s%s%s%s%s", getHour(), getSubject(), getType(), getInfo(), getRoom(), getNewRoom());
//    }

}

