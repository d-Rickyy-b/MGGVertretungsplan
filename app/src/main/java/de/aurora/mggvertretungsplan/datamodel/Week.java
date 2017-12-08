package de.aurora.mggvertretungsplan.datamodel;

/**
 * Created by Rico on 08.12.2017.
 */

public class Week {
    public static final int WEEK_A = 0;
    public static final int WEEK_B = 1;
    private int week = WEEK_A;

    public Week(String week) {
        switch (week) {
            case "B":
                this.week = WEEK_B;
                break;
            case "A":
            default:
                this.week = WEEK_A;
                break;
        }
    }

    public String getWeek() {
        return (week == WEEK_A) ? "A": "B";
    }

    @Override
    public String toString() {
        return getWeek();
    }
}
