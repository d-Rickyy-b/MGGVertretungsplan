package de.aurora.mggvertretungsplan.parsing;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;

/**
 * Created by Rico on 22.09.2017.
 */

public class MGGParser extends BaseParser {
    private static final String TAG = "MGGParser";
    private static final String timeTable_url = "https://www.mgg.karlsruhe.de/stupla/stupla.php";
    private static final String timeTable_url_2 = "https://www.mgg.karlsruhe.de/stupla/stuplamorgen.php";

    public MGGParser() {
    }

    // Extracts the two tables from the html code
    private static ArrayList<ArrayList<String>> extractTable(Document doc) {
        Element table = doc.select("table.mon_list").get(0);
        Iterator<Element> rowIterator = table.select("tr").iterator();

        //TODO check if these selectors are present. Otherwise stop parsing and throw error!

        ArrayList<ArrayList<String>> tableArrayList = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Iterator<Element> colIterator = rowIterator.next().select("td").iterator();
            ArrayList<String> tableRow = new ArrayList<>();
            while (colIterator.hasNext()) {
                tableRow.add(colIterator.next().text());
            }

            if (!tableRow.isEmpty()) {
                Collections.swap(tableRow, 3, 4);
                tableArrayList.add(tableRow);
            }
        }

        return tableArrayList;
    }

    // Remove double lines
    private static ArrayList<ArrayList<String>> deleteDoubles(ArrayList<ArrayList<String>> inputList) {
        Set<ArrayList<String>> set = new LinkedHashSet<>(inputList);
        inputList.clear();
        inputList.addAll(set);
        return inputList;
    }

    @Override
    public String[] getTimeTableURLs() {
        return new String[]{timeTable_url, timeTable_url_2};
    }

    private TimeTableDay parseDay(String website, int index) {
        ArrayList<String> datesList = new ArrayList<>();

        website = website.replace("&nbsp;", "");
        Document doc = Jsoup.parse(website);

        Elements dates = doc.select("div.mon_title");

        for (Element date : dates) {
            datesList.add(date.text()); // parse the dates on the website
        }

        TimeTableDay day = null;
        try {
            ArrayList<ArrayList<String>> table = extractTable(doc);
            table = deleteDoubles(table);

            if (index >= datesList.size()) {
                return day;
            }

            String date_header = datesList.get(index);

            // Compile pattern to find Strings like the following:
            // 08.12.2017 Freitag, Woche A
            // Which are displayed as date string at the top of the website
            Pattern pattern = Pattern.compile("(?<date>[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{4})( [a-zA-Z]+, Woche (?<week>[AB]))?");
            Matcher matcher = pattern.matcher(date_header);

            String date_text = "01.01.", week_text = "A";

            if (matcher.matches()) {
                try {
                    date_text = matcher.group(1);
                    week_text = matcher.group(3);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            if (table != null) {
                day = new TimeTableDay(date_text, week_text, table);
            } else
                day = new TimeTableDay(date_text, week_text, new ArrayList<ArrayList<String>>());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "parseDay(): There is probably no content to extract!");
            Log.e(TAG, e.getMessage());
        }

        return day;
    }

    @Override
    public TimeTable parse(ArrayList<String> websites) {
        TimeTable timeTable = new TimeTable();
        int index = 0;

        for (String website : websites) {
            try {
                TimeTableDay day = parseDay(website, index);

                if (null == day) {
                    index++;
                    continue;
                }

                timeTable.addDay(day);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            index++;
        }

        return timeTable;
    }

}
