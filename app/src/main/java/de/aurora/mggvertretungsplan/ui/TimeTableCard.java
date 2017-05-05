package de.aurora.mggvertretungsplan.ui;

/**
 * Created by Rico on 26.09.2016.
 */

public class TimeTableCard {
    private final String hour;
    private final String fach;
    private final String typ;
    private final String info;
    private final String raum;
    private final String neuRaum;

    public TimeTableCard(String hour, String fach, String neuFach, String raum, String neuRaum, String typ, String info) {
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

    public String getHour() {
        return this.hour;
    }

    public String getTyp() {
        return this.typ;
    }

    public String getTitle() {
        return this.fach;
    }

    public String getInfo() {
        return this.info;
    }

    public String getRaum() {
        return this.raum;
    }

    public String getNeuRaum() {
        return this.neuRaum;
    }
}

