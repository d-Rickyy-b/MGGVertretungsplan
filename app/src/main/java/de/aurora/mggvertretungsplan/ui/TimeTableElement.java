package de.aurora.mggvertretungsplan.ui;

/**
 * Created by Rico on 26.09.2016.
 */

public class TimeTableElement {
    private final String hour;
    private final String fach;
    private final String typ;
    private final String info;
    private final String raum;
    private final String neuRaum;

    public TimeTableElement(String hour, String fach, String neuFach, String raum, String neuRaum, String typ, String info) {
        if (info.length() > 0) {
            info = info.substring(0, 1).toUpperCase() + info.substring(1);

            if (!fach.equals(neuFach) && !neuFach.equals("---") && !neuFach.equals("")) {
                info = String.format("%s - %s", neuFach, info);
            }
        } else if (typ.equals("Entfall")) {
            info = "Entfällt";
        } else if (typ.equals("Vertretung")) {
            info = "Vertretung";
        } else {
            info = neuFach;
        }

        //TODO wenn Entfall, dann neuRaum ausblenden?!

        this.hour = hour;
        this.fach = fach;
        this.raum = raum;
        this.neuRaum = neuRaum;
        this.typ = typ;
        this.info = info;
    }

    String getHour() {
        return this.hour;
    }

    String getTyp() {
        return this.typ;
    }

    String getTitle() {
        return this.fach;
    }

    String getInfo() {
        return this.info;
    }

    String getRaum() {
        return this.raum;
    }

    String getNeuRaum() {
        return this.neuRaum;
    }
}

