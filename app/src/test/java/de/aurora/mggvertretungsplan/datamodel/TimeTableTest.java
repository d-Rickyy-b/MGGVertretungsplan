package de.aurora.mggvertretungsplan.datamodel;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rico on 20.11.2017.
 */
public class TimeTableTest extends TestCase {
    private TimeTable timeTable;
    private static final String WEEK_A = "A";
    private static final String WEEK_B = "B";

    public void setUp() throws Exception {
        super.setUp();

        timeTable = new TimeTable();
    }

    public void testAddDay() {
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();

        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, arrayLists);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getDaysCount());
        TimeTableDay timeTableDay = timeTable.getAllDays().get(0);
        assertEquals(ttd, timeTableDay);

        TimeTableDay ttd2 = new TimeTableDay("01.01.2018", WEEK_A, arrayLists);
        timeTable.addDay(ttd2);
        assertEquals(2, timeTable.getDaysCount());
        TimeTableDay timeTableDay1 = timeTable.getAllDays().get(0);
        TimeTableDay timeTableDay2 = timeTable.getAllDays().get(1);

        assertEquals(ttd, timeTableDay1);
        assertEquals(ttd2, timeTableDay2);
    }

    public void testGetDaysCount() {
        assertEquals(0, timeTable.getDaysCount());

        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, arrayLists);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getDaysCount());

        timeTable.addDay(ttd);
        timeTable.addDay(ttd);
        timeTable.addDay(ttd);

        assertEquals(4, timeTable.getDaysCount());
    }

    public void testGetFutureDaysCount() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        assertEquals(0, timeTable.getFutureDaysCount());

        Calendar cal = Calendar.getInstance();

        // Check if one day in the future is counted towards "future days"
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay(sdf.format(tomorrow), WEEK_A, arrayLists);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getFutureDaysCount());

        // Check if 2 days in the future are counted towards "future days"

        cal.add(Calendar.DATE, 1);
        Date dayAfterTomorrow = cal.getTime();
        TimeTableDay ttd2 = new TimeTableDay(sdf.format(dayAfterTomorrow), WEEK_A, arrayLists);
        timeTable.addDay(ttd2);

        assertEquals(2, timeTable.getFutureDaysCount());

        // Check if prior days are counted towards "future days"
        cal.add(Calendar.DATE, -3);
        Date yesterday = cal.getTime();
        TimeTableDay ttd3 = new TimeTableDay(sdf.format(yesterday), WEEK_A, arrayLists);
        timeTable.addDay(ttd3);

        assertEquals(2, timeTable.getFutureDaysCount());

        // Check if a TimeTableDay on the same date is considered "future", if it is currently 15:59.
        Calendar testCal = Calendar.getInstance(Locale.getDefault());
        testCal.set(Calendar.HOUR_OF_DAY,15);
        testCal.set(Calendar.MINUTE,59);
        testCal.set(Calendar.SECOND,0);
        testCal.set(Calendar.MILLISECOND,0);

        Date today1559 = testCal.getTime();

        testCal.set(Calendar.HOUR_OF_DAY,16);
        testCal.set(Calendar.MINUTE,1);
        Date today1601 = testCal.getTime();

        testCal.set(Calendar.HOUR_OF_DAY,0);
        testCal.set(Calendar.MINUTE,0);
        testCal.set(Calendar.SECOND,0);
        testCal.set(Calendar.MILLISECOND,0);
        Date currentDate = new Date(testCal.getTime().getTime());

        TimeTableDay ttd4 = new TimeTableDay(sdf.format(currentDate), WEEK_A, arrayLists);
        timeTable.addDay(ttd4);

        // The day should count as a future day
        assertEquals(3, timeTable.getFutureDaysCount(today1559));

        // Check if a TimeTableDay on the same date is considered "future", if it is currently >= 16:00.
        // Should be 2 results again, because the latest does not count
        assertEquals(2, timeTable.getFutureDaysCount(today1601));
    }

    public void testGetAllDays() {
        assertEquals(new ArrayList<TimeTableDay>(), timeTable.getAllDays());

        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd = new TimeTableDay("30.12.", WEEK_A, dayList);
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
        TimeTableDay ttd2 = new TimeTableDay("31.12.", WEEK_A, dayList2);
        timeTable.addDay(ttd2);

        rttd.add(ttd2);

        assertEquals(rttd, timeTable.getAllDays());
    }

    public void testGetTotalCancellations() {
        assertEquals(0, timeTable.getTotalCancellations("K1"));

        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd = new TimeTableDay("31.12.", WEEK_A, dayList);
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
        TimeTableDay ttd2 = new TimeTableDay("30.12.", WEEK_A, dayList2);

        timeTable.addDay(ttd2);

        assertEquals(1, timeTable.getTotalCancellations("K2"));
        assertEquals(1, timeTable.getTotalCancellations("K1")); // One cancellation from first day
        assertEquals(3, timeTable.getTotalCancellations("7a"));
        assertEquals(3, timeTable.getTotalCancellations("9c"));
    }

    public void testGetTotalDifferences() {
        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd = new TimeTableDay("31.12.", WEEK_A, dayList);
        timeTable.addDay(ttd);

        ArrayList<ArrayList<String>> dayList2 = new ArrayList<>();
        dayList2.add(new ArrayList<>(Arrays.asList("1", "K2", "D", "---", "H202", "---", "")));
        //dayList2.add(new ArrayList<>(Arrays.asList("1", "K2", "D", "---", "H202", "---", ""))); //Duplicate - should not count
        dayList2.add(new ArrayList<>(Arrays.asList("2", "5b", "E", "---", "H105", "---", "")));
        dayList2.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList2.add(new ArrayList<>(Arrays.asList("4", "7a", "BIO", "---", "S320", "---", ""))); // Getting merged with 3. lesson
        dayList2.add(new ArrayList<>(Arrays.asList("3-4", "9c", "G", "---", "M315", "---", "Test")));
        dayList2.add(new ArrayList<>(Arrays.asList("5", "9c", "D", "---", "M315", "---", "Test")));
        dayList2.add(new ArrayList<>(Arrays.asList("8-9", "9c", "Sp", "---", "M315", "---", "Test")));
        TimeTableDay ttd2 = new TimeTableDay("30.12.", WEEK_A, dayList2);
        timeTable.addDay(ttd2);

        // Second tt
        TimeTable timeTable1 = new TimeTable();
        ArrayList<ArrayList<String>> dayList3 = new ArrayList<>();
        dayList3.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList3.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList3.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList3.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd3 = new TimeTableDay("31.12.", WEEK_A, dayList3);
        timeTable1.addDay(ttd3);

        ArrayList<ArrayList<String>> dayList4 = new ArrayList<>();
        dayList4.add(new ArrayList<>(Arrays.asList("1", "K2", "D", "---", "H202", "---", "")));
        dayList4.add(new ArrayList<>(Arrays.asList("2", "5b", "E", "---", "H105", "---", "")));
        dayList4.add(new ArrayList<>(Arrays.asList("3", "7a", "BIO", "---", "S320", "---", "")));
        dayList4.add(new ArrayList<>(Arrays.asList("4", "7a", "BIO", "---", "S320", "---", ""))); // Getting merged with 3. lesson
        dayList4.add(new ArrayList<>(Arrays.asList("3-4", "9c", "G", "---", "M315", "---", "Test")));
        dayList4.add(new ArrayList<>(Arrays.asList("5", "9c", "D", "---", "M315", "---", "Test")));
        dayList4.add(new ArrayList<>(Arrays.asList("8-9", "9c", "Sp", "---", "M315", "---", "Test")));
        TimeTableDay ttd4 = new TimeTableDay("30.12.", WEEK_A, dayList4);
        timeTable1.addDay(ttd4);

        assertEquals(0, timeTable.getTotalDifferences(timeTable1, "7a").getTotalCancellations("7a"));
        assertEquals(0, timeTable.getTotalDifferences(timeTable1, "K2").getTotalCancellations("K2"));
        assertEquals(0, timeTable.getTotalDifferences(timeTable1, "8f").getTotalCancellations("8f"));

        // The other way around
        assertEquals(0, timeTable1.getTotalDifferences(timeTable, "7a").getTotalCancellations("7a"));
        assertEquals(0, timeTable1.getTotalDifferences(timeTable, "K2").getTotalCancellations("K2"));
        assertEquals(0, timeTable1.getTotalDifferences(timeTable, "8f").getTotalCancellations("8f"));

        // Another check
        assertEquals(timeTable.getTotalDifferences(timeTable1, "7a").getTotalCancellations("7a"), timeTable1.getTotalDifferences(timeTable, "7a").getTotalCancellations("7a"));
        assertEquals(timeTable.getTotalDifferences(timeTable1, "K2").getTotalCancellations("K2"), timeTable1.getTotalDifferences(timeTable, "K2").getTotalCancellations("K2"));
        assertEquals(timeTable.getTotalDifferences(timeTable1, "8f").getTotalCancellations("8f"), timeTable1.getTotalDifferences(timeTable, "8f").getTotalCancellations("8f"));

        TimeTable timeTable2 = new TimeTable();
        timeTable2.addDay(ttd3);

        dayList4.add(new ArrayList<>(Arrays.asList("1-2", "K1", "D", "---", "H001", "---", "Test")));
        ttd4 = new TimeTableDay("30.12.", WEEK_A, dayList4);
        timeTable2.addDay(ttd4);

        assertEquals(0, timeTable.getTotalDifferences(timeTable2, "7a").getTotalCancellations("7a"));
        assertEquals(0, timeTable.getTotalDifferences(timeTable2, "K2").getTotalCancellations("K2"));
        assertEquals(0, timeTable.getTotalDifferences(timeTable2, "8f").getTotalCancellations("8f"));
    }

    public void testToString() {
        assertEquals("", timeTable.toString());

        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("31.12.", WEEK_A, arrayLists);

        timeTable.addDay(ttd);
        assertEquals(ttd.getFullDateString(), timeTable.toString());

        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd2 = new TimeTableDay("31.12.", WEEK_A, dayList);
        timeTable.addDay(ttd2);

        String day1 = ttd.getFullDateString();
        String day2 = ttd.getFullDateString();
        String result = "";

        result += day1 + "\n";
        result += day2 + "\n";
        result += "1 | K1 | Deutsch | --- | H202 | --- | \n";
        result += "2 | 7a | Englisch | --- | H105 | --- | \n";
        result += "3 | 5c | Geschichte | --- | M315 | --- | Test";

        assertEquals(result, timeTable.toString());
    }

}