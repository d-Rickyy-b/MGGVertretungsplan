package de.aurora.mggvertretungsplan.parsing;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import de.aurora.mggvertretungsplan.AsyncTaskCompleteListener;
import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;

/**
 * Created by Rico on 01.10.2017.
 */

// This parser is not made for production, it's a parser made for creating mocks - hence the bad coding

@SuppressWarnings("unused")
public class MockParser implements WebsiteParser {

    private final static String timeTable_url = "http://example.com";

    public void startDownload(AsyncTaskCompleteListener<ArrayList<String>> callback) {
        callback.onTaskComplete(new ArrayList<String>());
    }

    @Override
    public TimeTable parse(ArrayList<String> websites, String className) {
        TimeTable timeTable1 = new TimeTable("K1");
        TimeTable timeTable2 = new TimeTable("K1");
        String date1 = "02.10.";
        String date2 = "02.10.";

        ArrayList<ArrayList<String>> day1 = new ArrayList<>();
        day1.add(new ArrayList<>(Arrays.asList("1", "K1", "4BK", "---", "BK3", "---", "")));
        day1.add(new ArrayList<>(Arrays.asList("2", "K1", "D", "D", "H208", "H210", "Raumänderung")));
        day1.add(new ArrayList<>(Arrays.asList("3", "K1", "E", "BIO", "S121", "S320", "")));
        day1.add(new ArrayList<>(Arrays.asList("5 - 6", "K1", "INF", "---", "S020", "---", "")));

        day1.add(new ArrayList<>(Arrays.asList("8", "K1", "D", "E", "S020", "H208", "")));
        day1.add(new ArrayList<>(Arrays.asList("9", "K1", "D", "---", "S020", "---", ""))); // Row will be removed

        ArrayList<ArrayList<String>> day2 = new ArrayList<>();
        day2.add(new ArrayList<>(Arrays.asList("2", "K1", "4BK", "---", "BK3", "---", ""))); // 1 -> 2
        day2.add(new ArrayList<>(Arrays.asList("2", "K1", "D", "D", "H208", "H209", "Raumänderung"))); // H208 -> H209
        day2.add(new ArrayList<>(Arrays.asList("3", "K1", "D", "BIO", "S121", "S320", ""))); // E -> D
        day2.add(new ArrayList<>(Arrays.asList("5 - 6", "K1", "INF", "D", "S020", "H001", ""))); //2 vars changed
        day2.add(new ArrayList<>(Arrays.asList("1", "K1", "EVR", "---", "M315", "---", ""))); //New cancellation

        day2.add(new ArrayList<>(Arrays.asList("8", "K1", "D", "E", "S020", "H208", ""))); // Row unchanged

        // Nr. of cancellations prev: 10
        // Nr. of cancellations should be : 1 + 3 + 2 + 1 = 7

        TimeTableDay ttd1 = new TimeTableDay(date1, day1);
        TimeTableDay ttd2 = new TimeTableDay(date2, day2);

        timeTable1.addDay(ttd1);
        timeTable2.addDay(ttd2);

        int diffs = timeTable1.getTotalDifferences(timeTable2, "K1");
        Log.d("MockParser", String.format("Diffs: %s", diffs));

        return new TimeTable(className);
    }

    @Override
    public String getTimeTable_url() {
        return timeTable_url;
    }
}
