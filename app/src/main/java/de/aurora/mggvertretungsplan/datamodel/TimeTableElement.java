package de.aurora.mggvertretungsplan.datamodel;

import android.graphics.Color;

/**
 * Created by Rico on 26.09.2016.
 */

public final class TimeTableElement {
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
        this.hour = "";
        this.subject = "";
        this.newSubject = "";
        this.room = "";
        this.newRoom = "";
        this.type = EMPTY;
        this.info = "";
    }

    TimeTableElement(String hour, String subject, String newSubject, String room, String newRoom, String info) {
        this.hour = hour;
        this.subject = subject;
        this.newSubject = newSubject;
        this.room = room;
        this.newRoom = newRoom;
        this.type = calcType();

        if (info.length() > 0) {
            info = info.substring(0, 1).toUpperCase() + info.substring(1);

            if (!subject.equals(newSubject) && !newSubject.equals("---") && !newSubject.equals("")) {
                info = String.format("%s - %s", newSubject, info);
            }
        } else if (type == CANCELLATION) {
            info = "Entfällt";
        } else if (type == SUBSTITUTION) {
            info = "Vertretung";
        } else {
            info = newSubject;
        }

        this.info = info;
    }

    int getHour_I() {
        int hour_i;
        try {
            hour_i = Integer.valueOf(this.getHour().substring(0, 1));
        } catch (NumberFormatException nfe) {
            hour_i = 1;
        }
        return hour_i;
    }

    public String getHour() {
        return this.hour;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getNewSubject() {
        return this.newSubject;
    }

    public String getRoom() {
        return this.room;
    }

    public String getNewRoom() {
        return this.newRoom;
    }

    public int getType() {
        return this.type;
    }

    public String getInfo() {
        return this.info;
    }

    public int getColor() {
        switch (this.type) {
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

//    public String getHash() {
//        return String.format("%s%s%s%s%s%s", getHour(), getSubject(), getType(), getInfo(), getRoom(), getNewRoom());
//    }

}

