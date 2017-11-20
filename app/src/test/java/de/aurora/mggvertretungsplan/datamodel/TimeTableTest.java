package de.aurora.mggvertretungsplan.datamodel;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created by Rico on 20.11.2017.
 */
public class TimeTableTest extends TestCase {
    TimeTable timeTable;

    public void setUp() throws Exception {
        super.setUp();

        timeTable = new TimeTable();
    }

    public void testAddDay() throws Exception {
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("01.01.2018", arrayLists);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getDaysCount());
        TimeTableDay timeTableDay = timeTable.getAllDays().get(0);
        assertEquals(ttd, timeTableDay);

        TimeTableDay ttd2 = new TimeTableDay("01.01.2018", arrayLists);
        timeTable.addDay(ttd2);
        assertEquals(2, timeTable.getDaysCount());
        TimeTableDay timeTableDay1 = timeTable.getAllDays().get(0);
        TimeTableDay timeTableDay2 = timeTable.getAllDays().get(1);

        assertEquals(ttd, timeTableDay1);
        assertEquals(ttd2, timeTableDay2);
    }

    public void testGetDaysCount() throws Exception {
        assertEquals(0, timeTable.getDaysCount());

        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("01.01.2018", arrayLists);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getDaysCount());

        timeTable.addDay(ttd);
        timeTable.addDay(ttd);
        timeTable.addDay(ttd);

        assertEquals(4, timeTable.getDaysCount());
    }

    public void testGetFutureDaysCount() throws Exception {
        assertEquals(0, timeTable.getFutureDaysCount());

        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("31.12.", arrayLists);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getFutureDaysCount());

        TimeTableDay ttd2 = new TimeTableDay("30.12.", arrayLists);
        timeTable.addDay(ttd2);

        TimeTableDay ttd3 = new TimeTableDay("01.01.", arrayLists);
        timeTable.addDay(ttd3);

        assertEquals(2, timeTable.getFutureDaysCount());
    }

    public void testGetAllDays() throws Exception {
        assertTrue(false);
    }

    public void testGetTotalCancellations() throws Exception {
        assertTrue(false);
    }

    public void testGetTotalDifferences() throws Exception {
        assertTrue(false);
    }

    public void testToString() throws Exception {
        assertTrue(false);
    }

}