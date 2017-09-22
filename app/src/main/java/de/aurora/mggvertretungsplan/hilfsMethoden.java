package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;
import de.aurora.mggvertretungsplan.datamodel.TimeTableDay;


public class hilfsMethoden {

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

    // Returns the type of a line in the list
    public static String getType(String substituteSubject, String substituteRoom) {
        if (isCancellation(substituteSubject, substituteRoom))
            return "Entfall";
        else
            return "Vertretung";
    }


    // Returns the full name of a subject
    @SuppressLint("DefaultLocale")
    public static String abkuerzung(String abk) {

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