package de.aurora.mggvertretungsplan.datamodel;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Rico on 20.11.2017.
 */
public class TimeTableDayTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getDate() throws Exception {
        ArrayList<ArrayList<String>> testList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 0, 1);

        //SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        //Date date = fullDateFormat.parse("01.01.2018");

        TimeTableDay ttd = new TimeTableDay("01.01.2018", testList);

        //assertEquals(date, ttd.getDate());
        assertEquals(calendar, ttd.getDate());
    }

    @Test
    public void getDateString() throws Exception {
        ArrayList<ArrayList<String>> testList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
        calendar.set(currentYear, 0, 1);

        TimeTableDay ttd = new TimeTableDay("01.01." + currentYear, testList);

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        assertEquals(format.format(calendar.getTime()), ttd.getDateString());

        TimeTableDay ttd2 = new TimeTableDay("01.01.", testList);

        assertEquals(format.format(calendar.getTime()), ttd2.getDateString());
    }

    @Test
    public void getFullDateString() throws Exception {
        ArrayList<ArrayList<String>> testList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, 0, 1);

        TimeTableDay ttd = new TimeTableDay("01.01.2018", testList);

        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd.MM.yyyy");

        assertEquals(format.format(calendar.getTime()), ttd.getFullDateString());
    }

    @Test
    public void getElements() throws Exception {
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

        TimeTableDay ttd = new TimeTableDay("01.01.2018", day1);

        TimeTableElement tte = new TimeTableElement("1", className2, "Deutsch", "Englisch", "H105", "H205", "");
        ArrayList<TimeTableElement> testList = new ArrayList<>();
        testList.add(tte);

        // Comparing two strings, because I didn't implement an equals method yet.
        assertEquals(testList.toString(), ttd.getElements(className2).toString());
    }

    @Test
    public void getArrayList() throws Exception {
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


        TimeTableDay ttd = new TimeTableDay("01.01.2018", day1);

        assertEquals(day1, ttd.getArrayList());
    }

    @Test
    public void getArrayListClass() throws Exception {
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

        ArrayList<ArrayList<String>> day1copy = (ArrayList<ArrayList<String>>) day1.clone();

        day1.add(new ArrayList<>(Arrays.asList("1", className2, "D", "E", "H105", "H205", "")));

        TimeTableDay ttd = new TimeTableDay("01.01.2018", day1);

        assertEquals(day1copy, ttd.getArrayList(className));
    }

    @Test
    public void getElementsCount() throws Exception {
        ArrayList<ArrayList<String>> testList = new ArrayList<>();
        String className = "K1";
        String className2 = "6b";

        TimeTableDay ttd = new TimeTableDay("01.01.2018", testList);

        assertEquals(0, ttd.getElementsCount("5a"));
        assertEquals(0, ttd.getElementsCount("abc"));

        ArrayList<ArrayList<String>> day1List = new ArrayList();

        day1List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day1List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day1List.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));

        day1List.add(new ArrayList<>(Arrays.asList("3-4", className2, "D", "---", "H202", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("5-6", className2, "E", "---", "H105", "---", "")));
        day1List.add(new ArrayList<>(Arrays.asList("1", className2, "4BK", "---", "BK3", "---", "")));

        TimeTableDay day1 = new TimeTableDay("01.01.2018", day1List);

        assertEquals(6, day1.getElementsCount(className));
        assertEquals(3, day1.getElementsCount(className2));
    }

    @Test
    public void getDifferences() throws Exception {
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
        TimeTableDay day1 = new TimeTableDay("01.01.2018", day1List);

        // Only first line has changed | G -> D
        day2List.add(new ArrayList<>(Arrays.asList("3-4", className, "D", "---", "H202", "---", "")));
        day2List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day2List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day2List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day2List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day2List.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));
        TimeTableDay day2 = new TimeTableDay("01.01.2018", day2List);

        // Add one line
        day3List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day3List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day3List.add(new ArrayList<>(Arrays.asList("5 - 6", className, "INF", "---", "S020", "---", "")));
        day3List.add(new ArrayList<>(Arrays.asList("4", className, "PSY", "---", "H005", "---", "")));
        TimeTableDay day3 = new TimeTableDay("01.01.2018", day3List);

        // Remove one line
        day4List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day4List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day4List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day4List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day4List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        TimeTableDay day4 = new TimeTableDay("01.01.2018", day4List);

        // Remove one and add another line
        day5List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day5List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day5List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day5List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day5List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day5List.add(new ArrayList<>(Arrays.asList("8", className, "BIO", "---", "S121", "S320", "")));
        TimeTableDay day5 = new TimeTableDay("01.01.2018", day5List);

        // Remove one and two another lines which get merged to one
        day6List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day6List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day6List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day6List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day6List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day6List.add(new ArrayList<>(Arrays.asList("8", className, "BIO", "---", "S121", "S320", "")));
        day6List.add(new ArrayList<>(Arrays.asList("9", className, "BIO", "---", "S121", "S320", "")));
        TimeTableDay day6 = new TimeTableDay("01.01.2018", day6List);

        // Remove one and three another lines
        day7List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H202", "---", "")));
        day7List.add(new ArrayList<>(Arrays.asList("3-4", className, "G", "---", "H105", "---", "")));
        day7List.add(new ArrayList<>(Arrays.asList("1", className, "4BK", "---", "BK3", "---", "")));
        day7List.add(new ArrayList<>(Arrays.asList("2", className, "D", "D", "H208", "H210", "Raumänderung")));
        day7List.add(new ArrayList<>(Arrays.asList("3", className, "E", "BIO", "S121", "S320", "")));
        day7List.add(new ArrayList<>(Arrays.asList("8", className, "BIO", "---", "S121", "S320", "")));
        day7List.add(new ArrayList<>(Arrays.asList("4", className, "M", "M", "S121", "H308", "")));
        day7List.add(new ArrayList<>(Arrays.asList("8-9", className, "GK", "S", "S121", "H308", "")));
        TimeTableDay day7 = new TimeTableDay("01.01.2018", day7List);


        assertEquals(0, day1.getDifferences(day1, className));
        assertEquals(1, day1.getDifferences(day2, className));
        assertEquals(1, day1.getDifferences(day3, className));
        assertEquals(1, day1.getDifferences(day4, className));
        assertEquals(2, day1.getDifferences(day5, className));
        assertEquals(2, day1.getDifferences(day6, className));
        assertEquals(4, day1.getDifferences(day7, className));

    }

    @Test
    public void isSameDay() throws Exception {
        ArrayList<ArrayList<String>> testList = new ArrayList<>();
        TimeTableDay ttd = new TimeTableDay("01.01.2018", testList);
        TimeTableDay ttd2 = new TimeTableDay("01.01.2018", testList);
        TimeTableDay ttd3 = new TimeTableDay("02.01.2018", testList);

        assertTrue(ttd.isSameDay(ttd2));
        assertFalse(ttd.isSameDay(ttd3));
    }

    @Test
    public void testToString() throws Exception {
    }

}