package de.aurora.mggvertretungsplan.ui;

/**
 * Created by Rico on 26.09.2016.
 */

public class TimeTableCard {
    private String hour;
    private String fach;
    private String neuFach;
    private String typ;
    private String info;
    private String raum;
    private String neuRaum;

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
        this.neuFach = neuFach;
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

