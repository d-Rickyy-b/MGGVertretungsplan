package de.aurora.mggvertretungsplan.datamodel;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import de.aurora.mggvertretungsplan.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by Rico on 20.11.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class TimeTableDayTest {
    private static final String WEEK_A = "A";
    private static final String WEEK_B = "B";
    private ArrayList<ArrayList<String>> testList;

    @Mock
    private Context context;

    @Before
    public void setUp() {
        testList = new ArrayList<>();
    }

    @Test
    public void getNotificationTitle() {
        when(context.getString(R.string.notification_title_dateformat)).thenReturn("EEEE, dd.MM.");
        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, testList);
        assertEquals("Montag, 01.01.", ttd.getNotificationTitle(context));

        TimeTableDay ttd1 = new TimeTableDay("01.01.2019", WEEK_A, testList);
        assertEquals("Dienstag, 01.01.", ttd1.getNotificationTitle(context));

        TimeTableDay ttd2 = new TimeTableDay("19.07.2019", WEEK_A, testList);
        assertEquals("Freitag, 19.07.", ttd2.getNotificationTitle(context));
    }

    @Test
    public void getNotificationTicker() {
        //TimeTableElement tte = new TimeTableElement(hour, class_name, subject, newSubject, room, newRoom, info);
        when(context.getString(R.string.notification_ticker)).thenReturn("Neue Stundenplanänderung");
        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, testList);
        assertEquals("Neue Stundenplanänderung", ttd.getNotificationTicker(context));
    }

    @Test
    public void getNotificationText() {
        when(context.getString(R.string.cardInfo_cancelled)).thenReturn("entfällt");
        when(context.getString(R.string.cardInfo_representation)).thenReturn("Vertretung");

        String className = "K1";
        testList.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        testList.add(new ArrayList<>(Arrays.asList("3", className, "G", "D", "H202", "H202", "")));
        testList.add(new ArrayList<>(Arrays.asList("5-6", className, "E", "---", "H105", "---", "Teacher on holiday!")));

        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, testList);

        String expectedText = "1. Std: Bildende Kunst entfällt\n" +
                              "3. Std: Geschichte Vertretung\n" +
                              "5-6. Std: Englisch entfällt";

        assertEquals(expectedText, ttd.getNotificationText(context));
    }

    @Test
    public void getDate() throws Exception {
        DateTimeFormatter fullDateFormat = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.getDefault());
        LocalDate date = LocalDate.parse("01.01.2018", fullDateFormat);

        TimeTableDay ttd1 = new TimeTableDay("01.01.2018", WEEK_A, testList);
        TimeTableDay ttd2 = new TimeTableDay("1.01.2018", WEEK_A, testList);
        TimeTableDay ttd3 = new TimeTableDay("01.1.2018", WEEK_A, testList);
        TimeTableDay ttd4 = new TimeTableDay("1.1.2018", WEEK_A, testList);

        assertEquals(date, ttd1.getDate());
        assertEquals(date, ttd2.getDate());
        assertEquals(date, ttd3.getDate());
        assertEquals(date, ttd4.getDate());
    }

    @Test
    public void getDateString() {
        int currentYear = LocalDateTime.now().getYear();
        LocalDateTime beginningOfYear = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);

        TimeTableDay ttd = new TimeTableDay("01.01." + currentYear, WEEK_A, testList);

        //SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        assertEquals(beginningOfYear.format(format), ttd.getDateString());

        TimeTableDay ttd2 = new TimeTableDay("01.01.", WEEK_A, testList);

        assertEquals(beginningOfYear.format(format), ttd2.getDateString());
    }

    @Test
    public void getFullDateString() throws Exception {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());
        LocalDate date = LocalDate.parse("01.01.2018", dateFormat);

        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, testList);

        DateTimeFormatter fullDateFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.getDefault());

        assertEquals(date.format(fullDateFormat), ttd.getFullDateString());
    }

    @Test
    public void getWeek() {
        ArrayList<ArrayList<String>> day1 = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, day1);

        assertEquals(WEEK_A, ttd.getWeek().toString());

        TimeTableDay ttd1 = new TimeTableDay("01.01.2018", WEEK_B, day1);
        assertEquals(WEEK_B, ttd1.getWeek().toString());
    }

    @Test
    public void getElements() {
        ArrayList<ArrayList<String>> day1 = new ArrayList<>();
        String className = "K1";
        String className2 = "5a";

        day1.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));

        day1.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day1.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day1.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));

        day1.add(new ArrayList<>(Arrays.asList("8", className, "D", "E", "S020", "H208", "")));
        day1.add(new ArrayList<>(Arrays.asList("9", className, "D", "---", "S020", "---", "")));

        day1.add(new ArrayList<>(Arrays.asList("1", className2, "D", "E", "H105", "H205", "")));

        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, day1);

        TimeTableElement tte = new TimeTableElement("1", className2, "Deutsch", "Englisch", "H105", "H205", "");
        ArrayList<TimeTableElement> testList = new ArrayList<>();
        testList.add(tte);

        // Comparing two strings, because I didn't implement an equals method yet.
        assertEquals(testList.toString(), ttd.getElements(className2).toString());
    }

    @Test
    public void getArrayList() {
        ArrayList<ArrayList<String>> day1 = new ArrayList<>();
        String className = "K1";
        String className2 = "5a";

        day1.add(new ArrayList<>(Arrays.asList("1", className2, "Deutsch", "Englisch", "H105", "H205", "")));
        day1.add(new ArrayList<>(Arrays.asList("1", className, "Bildende Kunst", "---", "BK3", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("2", className, "Deutsch", "Deutsch", "H208", "H210", "Raumänderung")));
        day1.add(new ArrayList<>(Arrays.asList("3-4", className, "Geschichte", "---", "H202", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("3-4", className, "Geschichte", "---", "H105", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("3", className, "Englisch", "Biologie", "S121", "S320", "")));
        day1.add(new ArrayList<>(Arrays.asList("5-6", className, "Informatik", "---", "S020", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("8", className, "Deutsch", "Englisch", "S020", "H208", "")));
        day1.add(new ArrayList<>(Arrays.asList("9", className, "Deutsch", "---", "S020", "---", "")));


        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, day1);

        assertEquals(day1, ttd.getArrayList());
    }

    @Test
    public void getArrayListClass() {
        ArrayList<ArrayList<String>> day1 = new ArrayList<>();
        String className = "K1";
        String className2 = "5a";

        day1.add(new ArrayList<>(Arrays.asList("1", className, "Bildende Kunst", "---", "BK3", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("2", className, "Deutsch", "Deutsch", "H208", "H210", "Raumänderung")));
        day1.add(new ArrayList<>(Arrays.asList("3-4", className, "Geschichte", "---", "H202", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("3-4", className, "Geschichte", "---", "H105", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("3", className, "Englisch", "Biologie", "S121", "S320", "")));
        day1.add(new ArrayList<>(Arrays.asList("5-6", className, "Informatik", "---", "S020", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("8", className, "Deutsch", "Englisch", "S020", "H208", "")));
        day1.add(new ArrayList<>(Arrays.asList("9", className, "Deutsch", "---", "S020", "---", "")));

        ArrayList<ArrayList<String>> day1copy = new ArrayList<>(day1);

        day1.add(new ArrayList<>(Arrays.asList("1", className2, "D", "E", "H105", "H205", "")));

        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, day1);

        assertEquals(day1copy, ttd.getArrayList(className));
    }

    @Test
    public void getElementsCount() {
        String className = "K1";
        String className2 = "6b";

        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, testList);

        assertEquals(0, ttd.getElementsCount("5a"));
        assertEquals(0, ttd.getElementsCount("abc"));

        ArrayList<ArrayList<String>> day1List = new ArrayList<>();

        day1List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day1List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day1List.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));

        day1List.add(new ArrayList<>(Arrays.asList("3-4", className2, "D", "---", "H202", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("5-6", className2, "E", "---", "H105", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("1", className2, "4BK", "---", "BK3", "---", "")));

        TimeTableDay day1 = new TimeTableDay("01.01.2018", WEEK_A, day1List);

        assertEquals(6, day1.getElementsCount(className));
        assertEquals(3, day1.getElementsCount(className2));
    }

    @Test
    public void getDifferences() {
        ArrayList<ArrayList<String>> day1List = new ArrayList<>();
        ArrayList<ArrayList<String>> day2List = new ArrayList<>();
        ArrayList<ArrayList<String>> day3List = new ArrayList<>();
        ArrayList<ArrayList<String>> day4List = new ArrayList<>();
        ArrayList<ArrayList<String>> day5List = new ArrayList<>();
        ArrayList<ArrayList<String>> day6List = new ArrayList<>();
        ArrayList<ArrayList<String>> day7List = new ArrayList<>();
        String className = "K1";

        // Basic data
        day1List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day1List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day1List.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));
        TimeTableDay day1 = new TimeTableDay("01.01.2018", WEEK_A, day1List);

        // Only first line has changed | G -> D
        day2List.add(new ArrayList<>(Arrays.asList("3-4", className, "D", "---", "H202", "---", "")));
        day2List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day2List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day2List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day2List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day2List.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));
        TimeTableDay day2 = new TimeTableDay("01.01.2018", WEEK_A, day2List);

        // Add one line
        day3List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day3List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day3List.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("4", className, "PSY", "---", "H005", "---", "")));
        TimeTableDay day3 = new TimeTableDay("01.01.2018", WEEK_A, day3List);

        // Remove one line
        day4List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day4List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day4List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day4List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day4List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        TimeTableDay day4 = new TimeTableDay("01.01.2018", WEEK_A, day4List);

        // Remove one and add another line
        day5List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day5List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day5List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day5List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day5List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day5List.add(new ArrayList<>(Arrays.asList("8", className, "BIO", "---", "S121", "S320", "")));
        TimeTableDay day5 = new TimeTableDay("01.01.2018", WEEK_A, day5List);

        // Remove one and two another lines which get merged to one
        day6List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day6List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day6List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day6List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day6List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day6List.add(new ArrayList<>(Arrays.asList("8", className, "BIO", "---", "S121", "S320", "")));
        day6List.add(new ArrayList<>(Arrays.asList("9", className, "BIO", "---", "S121", "S320", "")));
        TimeTableDay day6 = new TimeTableDay("01.01.2018", WEEK_A, day6List);

        // Remove one and three another lines
        day7List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day7List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day7List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day7List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day7List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day7List.add(new ArrayList<>(Arrays.asList("8", className, "BIO", "---", "S121", "S320", "")));
        day7List.add(new ArrayList<>(Arrays.asList("4", className, "M", "M", "S121", "H308", "")));
        day7List.add(new ArrayList<>(Arrays.asList("8-9", className, "GK", "S", "S121", "H308", "")));
        TimeTableDay day7 = new TimeTableDay("01.01.2018", WEEK_A, day7List);


        assertEquals(0, day1.getDifferences(day1, className).getElementsCount(className));
        assertEquals(1, day1.getDifferences(day2, className).getElementsCount(className));
        assertEquals(1, day1.getDifferences(day3, className).getElementsCount(className));
        assertEquals(1, day1.getDifferences(day4, className).getElementsCount(className));
        assertEquals(2, day1.getDifferences(day5, className).getElementsCount(className));
        assertEquals(2, day1.getDifferences(day6, className).getElementsCount(className));
        assertEquals(4, day1.getDifferences(day7, className).getElementsCount(className));
    }

    @Test
    public void isSameDay() {
        TimeTableDay ttd = new TimeTableDay("01.01.2018", WEEK_A, testList);
        TimeTableDay ttd2 = new TimeTableDay("01.01.2018", WEEK_A, testList);
        TimeTableDay ttd3 = new TimeTableDay("02.01.2018", WEEK_A, testList);

        assertTrue(ttd.isSameDay(ttd2));
        assertFalse(ttd.isSameDay(ttd3));
    }

    @Test
    public void mergeConsecutiveCancellations() {
        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        String className = "K1";

        // Basic data
        dayList.add(new ArrayList<>(Arrays.asList("3", className, "G", "---", "H202", "---", ""))); // Gets merged with the one after -> 1
        dayList.add(new ArrayList<>(Arrays.asList("4", className, "G", "---", "H202", "---", ""))); // Gets merged with the one before -> 1
        dayList.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", ""))); // -> 2
        dayList.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung"))); // -> 3
        dayList.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", ""))); // -> 4
        dayList.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", ""))); // -> 5
        TimeTableDay day1 = new TimeTableDay("01.01.2018", WEEK_A, dayList);

        assertEquals(5, day1.getElements(className).size());
        dayList.clear();

        dayList.add(new ArrayList<>(Arrays.asList("3", className, "G", "---", "H202", "---", ""))); // Gets merged with the one after -> 1
        dayList.add(new ArrayList<>(Arrays.asList("4", className, "G", "---", "H202", "---", ""))); // Gets merged with the one before -> 1
        dayList.add(new ArrayList<>(Arrays.asList("4", className, "G", "---", "H202", "---", ""))); // Gets removed because duplicate -> removed
        dayList.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", ""))); // -> 2
        dayList.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung"))); // -> 3
        dayList.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", ""))); // -> 4
        dayList.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", ""))); // -> 5
        TimeTableDay day2 = new TimeTableDay("01.01.2018", WEEK_A, dayList);

        assertEquals(5, day2.getElements(className).size());
        dayList.clear();

        dayList.add(new ArrayList<>(Arrays.asList("3", className, "G", "---", "H202", "---", ""))); // Gets merged with the one after -> 1
        dayList.add(new ArrayList<>(Arrays.asList("4", className, "G", "---", "H202", "---", ""))); // Gets merged with the one before -> 1
        dayList.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "D", "BK3", "H105", "statt 02.01"))); // Gets merged with the one after -> 2
        dayList.add(new ArrayList<>(Arrays.asList("2", className, "4BK", "D", "BK3", "H105", "statt 02.01"))); // Gets merged with the one before -> 2
        dayList.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", ""))); // -> 3
        dayList.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", ""))); // -> 4
        TimeTableDay day3 = new TimeTableDay("01.01.2018", WEEK_A, dayList);

        assertEquals(4, day3.getElements(className).size());
        assertEquals("statt 02.01", day3.getElements(className).get(0).getInfo());
        dayList.clear();
    }

    @Test
    public void testToString() {
        ArrayList<ArrayList<String>> dayList = new ArrayList<>();
        dayList.add(new ArrayList<>(Arrays.asList("1", "K1", "D", "---", "H202", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("2", "7a", "E", "---", "H105", "---", "")));
        dayList.add(new ArrayList<>(Arrays.asList("3", "5c", "G", "---", "M315", "---", "Test")));
        TimeTableDay ttd = new TimeTableDay("31.12.", WEEK_A, dayList);

        String result = "";

        String day = ttd.getFullDateString();

        result += day + "\n";
        result += "1 | K1 | Deutsch | --- | H202 | --- | \n";
        result += "2 | 7a | Englisch | --- | H105 | --- | \n";
        result += "3 | 5c | Geschichte | --- | M315 | --- | Test";
        assertEquals(result, ttd.toString());
    }

}