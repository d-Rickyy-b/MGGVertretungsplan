package de.aurora.mggvertretungsplan.datamodel;

import android.graphics.Color;

/**
 * Created by Rico on 26.09.2016.
 */

public final class TimeTableElement {
    private final String hour;
    private final String subject;
    private final String type;
    private final String info;
    private final String room;
    private final String newRoom;

    public TimeTableElement(String hour, String subject, String newSubject, String room, String newRoom, String type, String info) {
        if (info.length() > 0) {
            info = info.substring(0, 1).toUpperCase() + info.substring(1);

            if (!subject.equals(newSubject) && !newSubject.equals("---") && !newSubject.equals("")) {
                info = String.format("%s - %s", newSubject, info);
            }
        } else if (type.equals("Entfall")) {
            info = "Entfällt";
        } else if (type.equals("Vertretung")) {
            info = "Vertretung";
        } else {
            info = newSubject;
        }

        this.hour = hour;
        this.subject = subject;
        this.room = room;
        this.newRoom = newRoom;
        this.type = type;
        this.info = info;
    }

    public String getHour() {
        return this.hour;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getType() {
        return this.type;
    }

    public String getTitle() {
        return this.subject;
    }

    public String getInfo() {
        return this.info;
    }

    public String getRoom() {
        return this.room;
    }

    public String getNewRoom() {
        return this.newRoom;
    }

    public int getColor() {
        switch (getType()) {
            case "Entfall":
                return Color.parseColor("#FF6961");
            case "Vertretung":
                return Color.parseColor("#779ECB");
            default:
                return Color.parseColor("#F5F5F5");
        }
    }
}

