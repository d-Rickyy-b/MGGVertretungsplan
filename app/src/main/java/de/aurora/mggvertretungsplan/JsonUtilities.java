package de.aurora.mggvertretungsplan;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


class JsonUtilities {

    // Returns a JSON Array of a given ArrayList
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
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
            Log.e("Vertretungsplan_utility", jsonException.getMessage());
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
            Log.e("Vertretungsplan_utility", jsonException.getMessage());
            return new ArrayList<>();
        }

        return resultList;
    }

}       