package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


public class hilfsMethoden {

    public hilfsMethoden() {
    }

    // Diese Methode gibt den passenden Namen zu einem bestimmten Datum zurück
    public static String getAnyDayByName(int jahr, int monat, int tag) {
        String[] tage = new String[]{"Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"};

        GregorianCalendar gc = new GregorianCalendar();
        gc.set(jahr, monat - 1, tag);
        int tag_index = gc.get(GregorianCalendar.DAY_OF_WEEK);

        return tage[tag_index - 1];
    }


    public static ArrayList<ArrayList<String>> stundenZusammenfassen(ArrayList<ArrayList<String>> inputList) {
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

    public static String getFormattedDate(long currentTimeInMillis) {
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

    public static void sortieren(ArrayList<ArrayList<String>> inputlist) {
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

    public static boolean listsEqual(ArrayList<ArrayList<String>> listOne, ArrayList<ArrayList<String>> listTwo) {
        if (listOne == null && listTwo == null) {
            return true;
        } else if ((listOne == null || listTwo == null)
                || (listOne.isEmpty() && !listTwo.isEmpty())
                || (!listOne.isEmpty() && listTwo.isEmpty())
                || (listOne.size() != listTwo.size())) {
            return false;
        } else if (listOne.isEmpty() && listTwo.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static int getDifferencesCount(ArrayList<ArrayList<String>> listOne, ArrayList<ArrayList<String>> listTwo) {
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

    public static ArrayList<ArrayList<String>> removeBlanks(ArrayList<ArrayList<String>> inputList) {
        //TODO vielleicht NICHT ersetzen... sah ganz gut mit leerzeichen aus
        for (ArrayList<String> row : inputList) {
            row.set(0, row.get(0).replace(" - ", "-"));
        }
        return inputList;
    }

    //Doppelte Zeilen werden gelöscht
    public static ArrayList<ArrayList<String>> deleteDoubles(ArrayList<ArrayList<String>> inputList) {
        Set<ArrayList<String>> set = new LinkedHashSet<>();
        set.addAll(inputList);
        inputList.clear();
        inputList.addAll(set);
        return inputList;
    }

    //Hiermit werden nur die richtigen Klassen rausgesucht
    public static ArrayList<ArrayList<String>> getRightClass(ArrayList<ArrayList<String>> inputList, String klasse) {
        ArrayList<ArrayList<String>> classList = new ArrayList<>();

        for (ArrayList<String> element : inputList) {
            if (element.get(1).contains(klasse)) {
                classList.add(element);
            }
        }

        return classList;
    }

    public static ArrayList<ArrayList<String>> extractTable(Document doc, int index) {
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

    public static JSONArray getJSONArray(ArrayList<ArrayList<String>> inputlist) {
        return new JSONArray(inputlist);
    }

    public static ArrayList<ArrayList<String>> getArrayList(String jsonArraytext) {
        try {
            if (jsonArraytext == "" || jsonArraytext == null)
                return new ArrayList<>();

            JSONArray jsonArray = new JSONArray(jsonArraytext);
            return getArrayList(jsonArray);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static ArrayList<ArrayList<String>> getArrayList(JSONArray jsonArray) {
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

    public static boolean isEntfall(String fachVertretung, String raumVertretung) {
        return fachVertretung.equals("---") && raumVertretung.equals("---");
    }

    public static String getType(String fachVertretung, String raumVertretung) {
        if (isEntfall(fachVertretung, raumVertretung))
            return "Entfall";
        else
            return "Vertretung";
    }


    //gibt den Namen der Abkuerzung eines Faches zurueck
    @SuppressLint("DefaultLocale")
    public static String abkuerzung(String abk) {

        if (abk == null || abk.equals("")) {
            return "Kein Fach";
        } else {
            abk = abk.toUpperCase();
            if (abk.equals("D")) {
                return "Deutsch";
            } else if (abk.equals("PH")) {
                return "Physik";
            } else if (abk.equals("CH") || abk.equals("4CH1")) {
                return "Chemie";
            } else if (abk.equals("L")) {
                return "Latein";
            } else if (abk.equals("S")) {
                return "Spanisch";
            } else if (abk.equals("E")) {
                return "Englisch";
            } else if (abk.equals("INF")) {
                return "Informatik";
            } else if (abk.equals("LIT")) {
                return "Literatur";
            } else if (abk.equals("EVR")) {
                return "ev. Religion";
            } else if (abk.equals("KAR")) {
                return "kath. Religion";
            } else if (abk.equals("ETH")) {
                return "Ethik";
            } else if (abk.equals("MA")) {
                return "Mathe";
            } else if (abk.equals("EK")) {
                return "Erdkunde";
            } else if (abk.equals("BIO")) {
                return "Biologie";
            } else if (abk.equals("MU")) {
                return "Musik";
            } else if (abk.equals("SP")) {
                return "Sport";
            } else if (abk.equals("SW")) {
                return "Sport weibl.";
            } else if (abk.equals("SM")) {
                return "Sport männl.";
            } else if (abk.equals("G")) {
                return "Geschichte";
            } else if (abk.equals("F")) {
                return "Französisch";
            } else if (abk.equals("NWT")) {
                return "Naturwissenschaft u. Technik";
            } else if (abk.equals("GK")) {
                return "Gemeinschaftskunde";
            } else if (abk.equals("SF")) {
                return "Seminarkurs";
            } else if (abk.equals("NP")) {
                return "Naturphänomene";
            } else if (abk.equals("WI")) {
                return "Wirtschaft";
            } else if (abk.equals("METH")) {
                return "METH";
            } else if (abk.equals("BK")) {
                return "Bildende Kunst";
            } else if (abk.equals("LRS")) {
                return "LRS";
            } else if (abk.equals("PSY")) {
                return "Psychologie";
            } else if (abk.equals("PHIL")) {
                return "Philosophie";
            } else {
                return abk;
            }
        }
    }
}       