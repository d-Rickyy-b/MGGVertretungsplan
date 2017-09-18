package de.aurora.mggvertretungsplan.ui;

/**
 * Created by Rico on 26.09.2016.
 */

public class TimeTableElement {
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

    String getHour() {
        return this.hour;
    }

    String getType() {
        return this.type;
    }

    String getTitle() {
        return this.subject;
    }

    String getInfo() {
        return this.info;
    }

    String getRoom() {
        return this.room;
    }

    String getNewRoom() {
        return this.newRoom;
    }
}

