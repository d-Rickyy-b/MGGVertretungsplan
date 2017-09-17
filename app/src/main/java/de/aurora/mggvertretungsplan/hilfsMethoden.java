package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


class hilfsMethoden {

    static ArrayList<ArrayList<String>> datenAufbereiten(ArrayList<ArrayList<String>> tabelle, String className){
        tabelle = getRightClass(tabelle, className);
        tabelle = deleteDoubles(tabelle);
        tabelle = removeBlanks(tabelle);
        sort(tabelle);
        tabelle = stundenZusammenfassen(tabelle);

        return tabelle;
    }

    // Diese Methode gibt den passenden Namen zu einem bestimmten Datum zurück
    static String getDayOfWeek(int jahr, int monat, int tag) {
        String[] DAYS = new String[]{"Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"};

        GregorianCalendar gc = new GregorianCalendar();
        gc.set(jahr, monat - 1, tag);
        int tag_index = gc.get(GregorianCalendar.DAY_OF_WEEK);

        return DAYS[tag_index - 1];
    }


    private static ArrayList<ArrayList<String>> stundenZusammenfassen(ArrayList<ArrayList<String>> inputList) {
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

    static String getFormattedDate(long currentTimeInMillis) {
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(currentTimeInMillis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "HH:mm";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        if (now.get(Calendar.DATE) == currentTime.get(Calendar.DATE)) {
            return "Heute " + DateFormat.format(timeFormatString, currentTime);
        } else if ((now.get(Calendar.DATE) - currentTime.get(Calendar.DATE)) == 1) {
            return "Gestern " + DateFormat.format(timeFormatString, currentTime);
        } else if (now.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, currentTime).toString();
        } else
            return "am " + DateFormat.format("dd.MM.yyyy HH:mm", currentTime).toString();
    }

    private static void sort(ArrayList<ArrayList<String>> inputlist) {
        Collections.sort(inputlist, new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                int value1, value2;
                String strValue1 = o1.get(0);
                String strValue2 = o2.get(0);

                if (strValue1.contains("-")) {
                    String[] parts = strValue1.split("-"); //TODO wenn leerzeichen drin, dann hier auch notwendig
                    value1 = Integer.valueOf(parts[0]);
                } else {
                    value1 = Integer.valueOf(strValue1);
                }

                if (strValue2.contains("-")) {
                    String[] parts = strValue2.split("-");
                    value2 = Integer.valueOf(parts[0]);
                } else {
                    value2 = Integer.valueOf(strValue2);
                }

                if (value1 < value2) {
                    return -1;
                } else if (value1 == value2) {
                    return 0;
                } else { //if (value1 > value2){
                    return 1;
                }
            }
        });
    }

    static boolean listsEqual(ArrayList<ArrayList<String>> listOne, ArrayList<ArrayList<String>> listTwo) {
        if (listOne == null && listTwo == null) {
            return true;
        } else if ((listOne == null || listTwo == null)
                || (listOne.isEmpty() && !listTwo.isEmpty())
                || (!listOne.isEmpty() && listTwo.isEmpty())
                || (listOne.size() != listTwo.size())) {
            return false;
        } else return listOne.isEmpty() && listTwo.isEmpty();
    }

    static int getDifferencesCount(ArrayList<ArrayList<String>> listOne, ArrayList<ArrayList<String>> listTwo) {
        if (listOne.size() < listTwo.size()) {
            ArrayList<ArrayList<String>> tempList = new ArrayList<>(listOne);
            listOne = listTwo;
            listTwo = tempList;
        }

        int counter = 0;

        for (int i = 0; i < listOne.size(); i++) {
            boolean rowsEqual = false;

            for (int j = 0; j < listTwo.size(); j++) {
                if (listOne.get(i).equals(listTwo.get(j))) {
                    rowsEqual = true;
                    break;
                }
            }

            if (!rowsEqual) {
                counter++;
            }
        }

        return counter;
    }

    static CancellationDays parseTimetable(String website_html, String className){
        ArrayList<ArrayList<String>> tableOne, tableTwo;

        website_html = website_html.replace("&auml;", "ä").replace("&ouml;", "ö").replace("&uuml;", "ü");
        Document doc = Jsoup.parse(website_html);

        Elements dates = doc.select("h2.tabber_title");

        ArrayList<String> datesList = new ArrayList<>();

        for (Element date : dates) {
            datesList.add(date.text()); // The parse the dates on the website
        }

        try {
            tableOne = extractTable(doc, 0);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            tableOne = new ArrayList<>();
            tableOne.add(new ArrayList<>(Arrays.asList("", "", "", "", "", "", "")));
        }

        try {
            tableTwo = extractTable(doc, 1);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            tableTwo = new ArrayList<>();
            tableTwo.add(new ArrayList<>(Arrays.asList("", "", "", "", "", "", "")));
        }

        tableOne = datenAufbereiten(tableOne, className);
        tableTwo = datenAufbereiten(tableTwo, className);

        return new CancellationDays(tableOne, tableTwo, datesList);
    }

    // Extracts the two tables from the html code
    static ArrayList<ArrayList<String>> extractTable(Document doc, int index) {
        Element table = doc.select("table").get(index);
        Iterator<Element> rowIterator = table.select("tr").iterator();

        ArrayList<ArrayList<String>> tableArrayList = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Iterator<Element> colIterator = rowIterator.next().select("td").iterator();
            ArrayList<String> tableRow = new ArrayList<>();
            while (colIterator.hasNext()) {
                tableRow.add(colIterator.next().text());
            }
            //TODO was, wenn size kleiner als benoetigte Elemente (7)?
            //Sollte nie vorkommen, da im HTML immer 7 Elemente sind
            if (tableRow.size() > 0)
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
            } catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
        }

        return classList;
    }

    // Remove double lines
    private static ArrayList<ArrayList<String>> deleteDoubles(ArrayList<ArrayList<String>> inputList) {
        Set<ArrayList<String>> set = new LinkedHashSet<>();
        set.addAll(inputList);
        inputList.clear();
        inputList.addAll(set);
        return inputList;
    }

    // Remove blank lines
    private static ArrayList<ArrayList<String>> removeBlanks(ArrayList<ArrayList<String>> inputList) {
        for (ArrayList<String> row : inputList) {
            row.set(0, row.get(0).replace(" - ", "-"));
        }
        return inputList;
    }

    // Returns a JSON Array of a given ArrayList
    static JSONArray getJSONArray(ArrayList<ArrayList<String>> inputlist) {
        return new JSONArray(inputlist);
    }

    // Returns an ArrayList of a given JSON Array as String
    static ArrayList<ArrayList<String>> getArrayList(String jsonArraytext) {
        try {
            if (jsonArraytext == null || jsonArraytext.equals(""))
                return new ArrayList<>();

            JSONArray jsonArray = new JSONArray(jsonArraytext);
            return getArrayList(jsonArray);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Returns an ArrayList of a given JSON Array
    private static ArrayList<ArrayList<String>> getArrayList(JSONArray jsonArray) {
        ArrayList<ArrayList<String>> resultList = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                ArrayList<String> row = new ArrayList<>();
                JSONArray jsonArrayRow = jsonArray.getJSONArray(i);

                for (int j = 0; j < jsonArrayRow.length(); j++) {
                    row.add((String) jsonArrayRow.get(j));
                }
                resultList.add(row);
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return new ArrayList<>();
        }

        return resultList;
    }

    // Returns if a certain line is a cancellation
    private static boolean isCancellation(String substituteSubject, String substituteRoom) {
        return substituteSubject.equals("---") && substituteRoom.equals("---");
    }

    static String getType(String substituteSubject, String substituteRoom) {
        if (isCancellation(substituteSubject, substituteRoom))
            return "Entfall";
        else
            return "Vertretung";
    }


    //gibt den Namen der Abkuerzung eines Faches zurueck
    @SuppressLint("DefaultLocale")
    static String abkuerzung(String abk) {

        if (abk == null || abk.equals("")) {
            return "Kein Fach";
        } else {
            abk = abk.toUpperCase();
            switch (abk) {
                case "D":
                    return "Deutsch";
                case "PH":
                    return "Physik";
                case "CH":
                case "4CH1":
                    return "Chemie";
                case "L":
                    return "Latein";
                case "S":
                    return "Spanisch";
                case "E":
                    return "Englisch";
                case "INF":
                    return "Informatik";
                case "LIT":
                    return "Literatur";
                case "EVR":
                    return "ev. Religion";
                case "KAR":
                    return "kath. Religion";
                case "ETH":
                    return "Ethik";
                case "MA":
                    return "Mathe";
                case "EK":
                    return "Erdkunde";
                case "BIO":
                    return "Biologie";
                case "MU":
                    return "Musik";
                case "SP":
                    return "Sport";
                case "SW":
                    return "Sport weibl.";
                case "SM":
                    return "Sport männl.";
                case "G":
                    return "Geschichte";
                case "F":
                    return "Französisch";
                case "NWT":
                    return "Naturwissenschaft u. Technik";
                case "GK":
                    return "Gemeinschaftskunde";
                case "SF":
                    return "Seminarkurs";
                case "NP":
                    return "Naturphänomene";
                case "WI":
                    return "Wirtschaft";
                case "METH":
                    return "METH";
                case "BK":
                    return "Bildende Kunst";
                case "LRS":
                    return "LRS";
                case "PSY":
                    return "Psychologie";
                case "PHIL":
                    return "Philosophie";
                default:
                    return abk;
            }
        }
    }
}       