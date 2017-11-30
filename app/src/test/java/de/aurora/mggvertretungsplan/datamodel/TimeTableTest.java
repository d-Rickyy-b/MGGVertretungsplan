package de.aurora.mggvertretungsplan.datamodel;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

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
        assertEquals(new ArrayList<TimeTableDay>(), timeTable.getAllDays());

        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd = new TimeTableDay("30.12.", dayList);
        timeTable.addDay(ttd);

        ArrayList<TimeTableDay> rttd = new ArrayList<>();
        rttd.add(ttd);

        assertEquals(rttd, timeTable.getAllDays());

        ArrayList<ArrayList<String>> dayList2 = new ArrayList<>();
        dayList2.add(new ArrayList<>(Arrays.asList("1", "K2", "D", "---", "H202", "---", "")));
        //dayList2.add(new ArrayList<>(Arrays.asList("1", "K2", "D", "---", "H202", "---", ""))); //Duplicate - should not count
        dayList2.add(new ArrayList<>(Arrays.asList("2", "5b", "E", "---", "H105", "---", "")));
        dayList2.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList2.add(new ArrayList<>(Arrays.asList("3-4", "9c", "G", "---", "M315", "---", "Test")));
        dayList2.add(new ArrayList<>(Arrays.asList("4", "7a", "BIO", "---", "S320", "---", ""))); // Getting merged with 3. lesson
        dayList2.add(new ArrayList<>(Arrays.asList("5", "9c", "D", "---", "M315", "---", "Test")));
        dayList2.add(new ArrayList<>(Arrays.asList("8-9", "9c", "Sp", "---", "M315", "---", "Test")));
        TimeTableDay ttd2 = new TimeTableDay("31.12.", dayList2);
        timeTable.addDay(ttd2);

        rttd.add(ttd2);

        assertEquals(rttd, timeTable.getAllDays());
    }

    public void testGetTotalCancellations() throws Exception {
        assertEquals(0, timeTable.getTotalCancellations("K1"));

        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd = new TimeTableDay("31.12.", dayList);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getTotalCancellations("K1"));
        assertEquals(2, timeTable.getTotalCancellations("7a"));

        ArrayList<ArrayList<String>> dayList2 = new ArrayList<>();
        dayList2.add(new ArrayList<>(Arrays.asList("1", "K2", "D", "---", "H202", "---", "")));
        //dayList2.add(new ArrayList<>(Arrays.asList("1", "K2", "D", "---", "H202", "---", ""))); //Duplicate - should not count
        dayList2.add(new ArrayList<>(Arrays.asList("2", "5b", "E", "---", "H105", "---", "")));
        dayList2.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList2.add(new ArrayList<>(Arrays.asList("4", "7a", "BIO", "---", "S320", "---", ""))); // Getting merged with 3. lesson
        dayList2.add(new ArrayList<>(Arrays.asList("3-4", "9c", "G", "---", "M315", "---", "Test")));
        dayList2.add(new ArrayList<>(Arrays.asList("5", "9c", "D", "---", "M315", "---", "Test")));
        dayList2.add(new ArrayList<>(Arrays.asList("8-9", "9c", "Sp", "---", "M315", "---", "Test")));
        TimeTableDay ttd2 = new TimeTableDay("30.12.", dayList2);

        timeTable.addDay(ttd2);

        assertEquals(1, timeTable.getTotalCancellations("K2"));
        assertEquals(1, timeTable.getTotalCancellations("K1")); // One cancellation from first day
        assertEquals(3, timeTable.getTotalCancellations("7a"));
        assertEquals(3, timeTable.getTotalCancellations("9c"));
    }

    public void testGetTotalDifferences() throws Exception {
    }

    public void testToString() throws Exception {
        assertEquals("", timeTable.toString());

        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("31.12.", arrayLists);

        timeTable.addDay(ttd);
        assertEquals(ttd.getFullDateString() + "\n", timeTable.toString());

        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd2 = new TimeTableDay("31.12.", dayList);
        timeTable.addDay(ttd2);

        String day1 = ttd.getFullDateString();
        String day2 = ttd.getFullDateString();
        String result = "";

        result += day1 + "\n";
        result += day2 + "\n";
        result += "1 | K1 | Deutsch | --- | H202 | --- | \n";
        result += "2 | 7a | Englisch | --- | H105 | --- | \n";
        result += "3 | 5c | Geschichte | --- | M315 | --- | Test\n";

        assertEquals(result, timeTable.toString());
    }

}