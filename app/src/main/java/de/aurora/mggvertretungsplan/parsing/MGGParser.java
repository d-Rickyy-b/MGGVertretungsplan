package de.aurora.mggvertretungsplan.parsing;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;

/**
 * Created by Rico on 22.09.2017.
 */

public class MGGParser implements WebsiteParser {

    public MGGParser() {

    }

    // Returns a nicely reworked ArrayList of the cancellations
    private static ArrayList<ArrayList<String>> prepareData(ArrayList<ArrayList<String>> tabelle, String className) {
        tabelle = getRightClass(tabelle, className);
        tabelle = deleteDoubles(tabelle);
        tabelle = mergeCancellations(tabelle);

        return tabelle;
    }

    // Extracts the two tables from the html code
    private static ArrayList<ArrayList<String>> extractTable(Document doc, int index) {
        Element table = doc.select("table").get(index);
        Iterator<Element> rowIterator = table.select("tr").iterator();

        //TODO check if these selectors are present. Otherwise stop parsing and throw error!

        ArrayList<ArrayList<String>> tableArrayList = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Iterator<Element> colIterator = rowIterator.next().select("td").iterator();
            ArrayList<String> tableRow = new ArrayList<>();
            while (colIterator.hasNext()) {
                tableRow.add(colIterator.next().text());
            }
            //TODO was, wenn size kleiner als benoetigte Elemente (7)?
            //Sollte nie vorkommen, da im HTML immer 7 Elemente sind
            if (!tableRow.isEmpty())
                tableArrayList.add(tableRow);
        }

        return tableArrayList;
    }

    // Remove all lines which don't contain the right class
    private static ArrayList<ArrayList<String>> getRightClass(ArrayList<ArrayList<String>> inputList, String className) {
        ArrayList<ArrayList<String>> classList = new ArrayList<>();

        for (ArrayList<String> element : inputList) {
            try {
                if (element.get(1).contains(className)) {
                    classList.add(element);
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e("Vertretungsplan_parser", e.getMessage());
            }
        }

        return classList;
    }

    // Remove double lines
    private static ArrayList<ArrayList<String>> deleteDoubles(ArrayList<ArrayList<String>> inputList) {
        Set<ArrayList<String>> set = new LinkedHashSet<>(inputList);
        inputList.clear();
        inputList.addAll(set);
        return inputList;
    }

    // Merges cancellations together (3. & 4. -> 3-4)
    private static ArrayList<ArrayList<String>> mergeCancellations(ArrayList<ArrayList<String>> inputList) {
        if (inputList.size() <= 1) {
            return inputList;
        }

        for (int i = 1; i < inputList.size(); i++) {
            boolean identical = true;
            ArrayList<String> zeile = inputList.get(i);

            if (!zeile.get(0).equals(inputList.get(i - 1).get(0))) {
                for (int j = 1; j < zeile.size(); j++) {
                    if (!zeile.get(j).equals(inputList.get(i - 1).get(j))) {
                        identical = false;
                        break;
                    }
                }

                if (identical) {
                    String neuStunde;
                    //TODO prüfen ob Stunde > als 2 Zeichen | Ansonsten passiert folgendes: 5-6-8 (bei 5-6 und 8)
                    neuStunde = String.format("%s-%s", inputList.get(i - 1).get(0), inputList.get(i).get(0));
                    inputList.get(i - 1).set(0, neuStunde);
                    inputList.remove(i);
                    i--;
                }
            }
        }

        return inputList;
    }

    @Override
    public TimeTable parse(String website_html, String className) {
        ArrayList<String> datesList = new ArrayList<>();
        TimeTable timeTable = new TimeTable();

        website_html = website_html.replace("&auml;", "ä").replace("&ouml;", "ö").replace("&uuml;", "ü");
        Document doc = Jsoup.parse(website_html);

        Elements dates = doc.select("h2.tabber_title");

        for (Element date : dates) {
            datesList.add(date.text()); // The parse the dates on the website
        }

        for (int i = 0; i < dates.size(); i++) {
            try {
                ArrayList<ArrayList<String>> table = extractTable(doc, i);
                table = prepareData(table, className);

                if (table != null) {
                    TimeTableDay day = new TimeTableDay(datesList.get(i), table);
                    timeTable.addDay(day);
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e("MGGparser", "parse(): There is probably no content to excract!");
                Log.e("MGGparser", e.getMessage());
            }
        }

        return timeTable;
    }

}
