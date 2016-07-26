package de.aurora.mggvertretungsplan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;


public class hilfsMethoden extends Activity {

    public hilfsMethoden() {
    }

    //Diese Methode durchsucht die Tabellen nach der angegebenen Klasse. Alle gefundenen Elemente werden aufgelistet
    public String[][] stringKuerzen(String str, String klasse) {
        ArrayList<Integer> abstand = haeufigkeit(str, klasse);
        int anzahl = abstand.size(), index1, index2, abstandVar;
        String[][] sArray;
        String zwischenresult;
        sArray = new String[anzahl][7];

        for (int i = 0; i < anzahl; i++) {
            abstandVar = abstand.get(i);

            if (abstandVar > 30) {
                zwischenresult = str.substring(abstandVar - 30);
            } else {
                zwischenresult = str;
            }

            int endposition = zwischenresult.indexOf("</td></tr>");
            zwischenresult = zwischenresult.substring(0, endposition + 5);

            int startposition2 = zwischenresult.indexOf("<td>");
            zwischenresult = zwischenresult.substring(startposition2, zwischenresult.length());

            if (zwischenresult.contains("</tr>")) {
                int endposition2 = zwischenresult.indexOf("</tr>");
                zwischenresult = zwischenresult.substring(0, endposition2);
            }

            zwischenresult = zwischenresult.replace("&auml;", "ä").replace("&ouml;", "ö").replace("&uuml;", "ü");

            if (zwischenresult.contains("</td><td>")) {
                index1 = zwischenresult.indexOf("<td>") + 4;
                index2 = zwischenresult.indexOf("</td>");

                for (int y = 0; y < 7; y++) {
                    sArray[i][y] = zwischenresult.substring(index1, index2);
                    index1 = zwischenresult.indexOf("<td>", index1 + 1) + 4;
                    index2 = zwischenresult.indexOf("</td>", index2 + 1);
                }
            }
        }
        sortieren(sArray);
        int laenge = sArray.length;

        if (laenge >= 2) {
            for (int j = 0; j < 2; j++) {
                for (int i = 1; i < laenge; i++) {
                    sArray = zusammenfassen(sArray, i);
                    laenge = sArray.length;
                }

                for (int i = 1; i < laenge; i++) {
                    sArray = stundenZusammenfassen(sArray, i);
                    laenge = sArray.length;
                }
            }
        }
        return sArray;
    }

    // zählt die Häufigkeit der eingegebenen Klasse im vorhandenen String
    public ArrayList<Integer> haeufigkeit(String quelle, String Ausschnitt) {
        if (quelle == null || Ausschnitt == null) {
            return new ArrayList<>();
        }

        int zahl = 0;
        ArrayList<Integer> laengenArray = new ArrayList<>();

        for (int pos = 0; (pos = quelle.indexOf(Ausschnitt, pos)) != -1; zahl++) {
            if (quelle.substring(pos - 1, pos + 2).equals("BK1") || quelle.substring(pos - 1, pos + 2).equals("BK2")) {
                //zahl--;
                pos++;
            } else {
                laengenArray.add(pos);
            }
            pos += Ausschnitt.length(); //Wenn Klasse ist NICHT BK1 od. BK2, an Stelle hinter der Klasse springen
            }

        return laengenArray;
    }

    // Diese Methode gibt den passenden Namen zu einem bestimmten Datum zurück
    public String getAnyDayByName(int jahr, int monat, int tag) {
        String[] tage = new String[]{"Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"};

        GregorianCalendar gc = new GregorianCalendar();
        gc.set(jahr, monat - 1, tag);
        int tag_index = gc.get(GregorianCalendar.DAY_OF_WEEK);

        return tage[tag_index - 1];
    }

    //doppelte Zeilen in einem Array werden zusammengefasst
    public String[][] zusammenfassen(String[][] inputArray, int stelle) {
        int laenge = inputArray.length;
        int neuLaenge = laenge - 1;

        if (stelle >= laenge || inputArray[stelle][0] == null) {
            return inputArray;
        }

        //wenn Zeile Nr."stelle" genau das gleiche beinhaltet wie Zeile Nr."stelle - 1" dann zusammenfassen
        if (inputArray[stelle][0].equals(inputArray[stelle - 1][0]) && inputArray[stelle][1].equals(inputArray[stelle - 1][1]) &&
                inputArray[stelle][2].equals(inputArray[stelle - 1][2]) && inputArray[stelle][3].equals(inputArray[stelle - 1][3]) &&
                inputArray[stelle][4].equals(inputArray[stelle - 1][4]) && inputArray[stelle][5].equals(inputArray[stelle - 1][5]) &&
                inputArray[stelle][6].equals(inputArray[stelle - 1][6])) {

            String temp[][] = new String[neuLaenge][7];

            //System.arraycopy(inputArray, 0, temp, 0 , stelle);
            //System.arraycopy(inputArray, stelle+1, temp, stelle, neuLaenge - stelle);

            zeileEntfernen(inputArray, stelle, true);

            return temp;
        } else {
            return inputArray;
        }
    }

    //wenn 2 Zeilen gleich sind (Zeile 1: 5. Stunde; Zeile 2: 6. Stunde), diese zusammenfassen
    public String[][] stundenZusammenfassen(String[][] inputArray, int stelle) {
        int laenge = inputArray.length;

        //prüfen ob die Stelle, die man prüfen möchte, länger ist als das vorhandene Array
        if (stelle >= laenge) {
            return inputArray;
        }

        //prüfen ob das Array an der Stelle einen Wert enthält
        if (inputArray[stelle][0] == null) {
            return inputArray;
        }
        //wenn Zeile Nr."stelle" nicht genau das gleiche beinhaltet wie Zeile Nr."stelle - 1" dann zusammenfassen
        if (!inputArray[stelle][0].equals(inputArray[stelle - 1][0]) && inputArray[stelle][1].equals(inputArray[stelle - 1][1]) &&
                inputArray[stelle][2].equals(inputArray[stelle - 1][2]) && inputArray[stelle][3].equals(inputArray[stelle - 1][3]) &&
                inputArray[stelle][4].equals(inputArray[stelle - 1][4]) && inputArray[stelle][5].equals(inputArray[stelle - 1][5]) &&
                inputArray[stelle][6].equals(inputArray[stelle - 1][6])) {
            /**Die Zeilen sollen in allem gleich sein, außer an der ersten Stelle**/
            String temp[][] = new String[laenge - 1][9];

            //wenn beide stellen nicht länger als 2, dann zusammenfassen
            if (inputArray[stelle][1].length() <= 2 && inputArray[stelle - 1][1].length() <= 2) {
                temp = zeileEntfernen(inputArray, stelle, false);
            } else if ((inputArray[stelle][1].length() <= 2 && inputArray[stelle - 1][1].length() > 2)) {
                temp = zeileEntfernen(inputArray, stelle, true);

            } else if (inputArray[stelle - 1][1].length() <= 2 && inputArray[stelle][1].length() > 2) {
                temp = zeileEntfernen(inputArray, stelle - 1, true);
            }

            return temp;
        } else {
            return inputArray;
        }
    }

    //Entfernt Zeile "stelle" aus dem Array
    public String[][] zeileEntfernen(String[][] inputArray, int stelle, boolean nurEntfernen) {
        int neuLaenge = inputArray.length - 1;
        String temp[][] = new String[neuLaenge][9];

        System.arraycopy(inputArray, 0, temp, 0 , stelle);
        System.arraycopy(inputArray, stelle+1, temp, stelle, neuLaenge - stelle);

        if (!nurEntfernen) {
            temp[stelle - 1][0] = temp[stelle - 1][0] + " - " + inputArray[stelle][0];
        }

        return temp;
    }

    //gibt den Namen der Abkuerzung eines Faches zurueck
    @SuppressLint("DefaultLocale")
    public String abkuerzung(String abk) {

        if (abk == null || abk.equals("")) {
            return "kein Fach";
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
                return "Meth";
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

    //sortiert ein Array
    public void sortieren(String[][] data) {
        Arrays.sort(data, new Comparator<String[]>() {
            @Override
            public int compare(final String[] entry1, final String[] entry2) {
                final String string1 = entry1[0];
                final String string2 = entry2[0];

                //Versucht die Stunden als Zahl zu speichern und dann zu sortieren, sollte es fehlschlagen wird nach Strings sortiert
                try {
                    String string1Copy = string1, string2Copy = string2;

                    if (string1Copy.length() == 2) {
                        string1Copy = string1Copy.substring(0, 2);
                    } else if (string1Copy.length() <= 5) {
                        string1Copy = string1Copy.substring(0, 1);
                    } else if (string1Copy.length() >= 6) {
                        string1Copy = string1Copy.substring(0, 2);
                    }

                    if (string2Copy.length() == 2) {
                        string2Copy = string2Copy.substring(0, 2);
                    } else if (string2Copy.length() <= 5) {
                        string2Copy = string2Copy.substring(0, 1);
                    } else if (string2Copy.length() >= 6) {
                        string2Copy = string2Copy.substring(0, 2);
                    }

                    return Integer.valueOf(string1Copy).compareTo(Integer.valueOf(string2Copy));
                } catch (Exception e) {
                    return string1.compareTo(string2);
                }
            }
        });
    }


    public String getFormattedDate(long currentTimeInMillis) {
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(currentTimeInMillis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "HH:mm";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        if (now.get(Calendar.DATE) == currentTime.get(Calendar.DATE)) {
            return ": Heute " + DateFormat.format(timeFormatString, currentTime);
        } else if ((now.get(Calendar.DATE) - currentTime.get(Calendar.DATE)) == 1) {
            return ": Gestern " + DateFormat.format(timeFormatString, currentTime);
        } else if (now.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, currentTime).toString();
        } else
            return " am: " + DateFormat.format("dd.MM.yyyy HH:mm", currentTime).toString();
    }
}       