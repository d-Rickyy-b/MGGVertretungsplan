package de.aurora.mggvertretungsplan.datamodel;

import junit.framework.TestCase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());

        assertEquals(0, timeTable.getFutureDaysCount());

        // Check if one day in the future is counted towards "future days"
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0, 0).plusDays(1);
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay(tomorrow.format(sdf), WEEK_A, arrayLists);
        timeTable.addDay(ttd);

        assertEquals(1, timeTable.getFutureDaysCount());

        // Check if 2 days in the future are counted towards "future days"
        LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);
        TimeTableDay ttd2 = new TimeTableDay(dayAfterTomorrow.format(sdf), WEEK_A, arrayLists);
        timeTable.addDay(ttd2);

        assertEquals(2, timeTable.getFutureDaysCount());

        // Check if prior days are counted towards "future days"
        LocalDateTime yesterday = LocalDateTime.now().minusDays(3);
        TimeTableDay ttd3 = new TimeTableDay(sdf.format(yesterday), WEEK_A, arrayLists);
        timeTable.addDay(ttd3);

        // If the first assertion is wrong, we got a date parsing issue!
        LocalDateTime ttd3DateTime = LocalDateTime.of(ttd3.getDate(), LocalTime.of(0,0,0));
        assertTrue(now.isAfter(ttd3DateTime));
        assertEquals(2, timeTable.getFutureDaysCount());
        LocalDate today = LocalDate.now();

        // Check if a TimeTableDay on the same date is considered "future", if it is currently 15:59.
        LocalDateTime today1559 = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 15,59,0);
        LocalDateTime today1601 = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 16,1,0);
        LocalDateTime currentDate = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 0,0, 0);

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