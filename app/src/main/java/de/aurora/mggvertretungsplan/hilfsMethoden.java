package de.aurora.mggvertretungsplan;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.text.format.DateFormat;


public class hilfsMethoden extends Activity{
	
	int neueLaenge;
	SharedPreferences sp;
	//public static String PREFS_NAME = "MyPreferencesFile";

		public hilfsMethoden() {
			
		}
	
		//Diese Methode durchsucht die Tabellen nach der angegebenen Klasse. Alle gefundenen Elemente werden aufgelistet
		public String[][] stringKuerzen(String str, String klasse){
			
			int[] abstand = haeufigkeit(str, klasse);
			int anzahl = abstand.length, index1=0,index2=0,abstandVar = 0;
			String[][] sArray;
			String zwischenresult = "";
			
			sArray = new String [anzahl][7]; //Neues Stringarray der Länge "anzahl" und der Breite 7 (Da 7 Infos auf der Website)
			
			for(int i=0;i<anzahl;i++){
				abstandVar = abstand[i]; 
					
				if (abstandVar>30){
					zwischenresult = str.substring(abstandVar-30);
				}else{
					zwischenresult = str; //str.substring(0 , str.length());
				}
					
				int endposition = zwischenresult.indexOf("</td></tr>");
				String zwischenresult2 = zwischenresult.substring(0 , endposition+5);
					
				int startposition2 = zwischenresult2.indexOf("<td>");
				String zwischenresult3 = zwischenresult2.substring(startposition2 , zwischenresult2.length());
							
				//hier wird sichergestellt, dass die Tabelle wirklich zuende ist.
				if(zwischenresult3.contains("</tr>")){
					int endposition2 = zwischenresult3.indexOf("</tr>");
					//endresult += " \n" +zwischenresult3.substring(0 , endposition2);
					zwischenresult3 = zwischenresult3.substring(0 , endposition2);
					endposition=endposition2;
				}
				
				zwischenresult3 = zwischenresult3.replace("&auml;", "ä"); //ä einfügen
				zwischenresult3 = zwischenresult3.replace("&ouml;", "ö"); //ö einfügen
				zwischenresult3 = zwischenresult3.replace("&uuml;", "ü"); //ü einfügen
			
				if(zwischenresult3.contains("</td><td>")){ //</td><td></td> </tr></tbody>
					index1 = zwischenresult3.indexOf("<td>")+4;
					index2 = zwischenresult3.indexOf("</td>");
					
					for(int y=0;y<7;y++){
						sArray[i][y] = zwischenresult3.substring(index1,index2);
						index1 = zwischenresult3.indexOf("<td>",index1+1)+4;
						index2 = zwischenresult3.indexOf("</td>",index2+1);
					}
				}
				}		
				sortieren(sArray);
				
				//Zusammenfassen und StundenZusammenfassen
				// TODO Funktioniert nicht ordnugsgemäß
				int laenge = sArray.length;
				//System.out.println(sArray[0][0] + " | " + sArray[0][1] + " | " + sArray[0][2] + " | ");
				
				//if (laenge>=2 && zusammenfassen) {
				if (laenge>=2) {
					//System.out.println(sArray[0][0] + " | " + sArray[0][1] + " | " + sArray[0][2] + " | ");
					//System.out.println("laenge ist >= 2");
					for(int j = 0; j<2;j++){
						int i=1;
						for(i = 1; i<laenge; i++){
							sArray = zusammenfassen(sArray, str, klasse, anzahl,i);
							//sArray = stundenZusammenfassen(sArray,str,klasse,anzahl,i);
							laenge = sArray.length;
							//System.out.println("laenge: " + laenge);
						}
						
						for(i = 1; i<laenge; i++){
							//sArray = zusammenfassen(sArray, str, klasse, anzahl,i);
							sArray = stundenZusammenfassen(sArray,str,klasse,anzahl,i);
							laenge = sArray.length;
							//System.out.println("laenge: " + laenge);
						}
					}
					//System.out.println(sArray[0][0] + " | " + sArray[0][1] + " | " + sArray[0][2] + " | ");
				}
				
				return sArray;
		}	
			
		/**zählt die Häufigkeit der eingegebenen Klasse im vorhandenen Dokument*/
		public int[] haeufigkeit(String Quelle, String Ausschnitt){
			if(Quelle==null||Ausschnitt==null){ //wenn einer der übergabeparameter null ist wird sofort abgebrochen! (leeres Array zurückgeben)
				return new int[0]; }
				
			int zahl = 0;
			int[] laengenArray = new int[0];
			
			for(int schalter = 0; schalter < 2 ;schalter++){
				//System.out.println("Schalter = " + schalter);
				laengenArray = new int[zahl];
				zahl=0;
				
				for(int pos=0; (pos=Quelle.indexOf(Ausschnitt, pos)) !=-1;zahl++){
					if (Quelle.substring(pos-1, pos+2).equals("BK1") || Quelle.substring(pos-1, pos+2).equals("BK2")){ //Wenn BK1 oder BK2 gefunden wird, überspringen! ...
						zahl--;		//... gefundene Klasse "entfernen" ...
						pos+=1;		//... einen Schritt weitergehen um BK1 od. BK2 nicht noch einmal zu finden
						
					}else if(schalter == 1){
							laengenArray[zahl]=pos;
						}
						pos += Ausschnitt.length();		//Wenn Klasse ist NICHT BK1 od. BK2, an Stelle hinter der Klasse springen
				}
			}
			return laengenArray;
		}
		
		/**prüft Abstand zwischen den Klassen in der HTML-Datei */
		public int abstand(String str, int s,String klasse){
				int zahl = haeufigkeit(str,klasse).length;	//haeufigkeit(str,klasse); 	//hier wird die Häufigkeit der Klasse abgerufen (wie oft fällt was aus)
				int[] laenge = new int[zahl]; 				//integerarray erstellen
				int i=0;
				laenge[0]=0;
				
				for(int pos=0; (pos=str.indexOf(klasse, pos)) !=-1;i++){
					pos += klasse.length();
					laenge[i]= pos;
				}
				int abstandVar = laenge[s];
			
			return abstandVar;
		}
		
		/**gibt den Namen zum heutigen Tag zurück*/
		public String getCurrentDayByName(){
			GregorianCalendar gc = new GregorianCalendar();
	    	int tag_index = gc.get(GregorianCalendar.DAY_OF_WEEK);
	    		
	    	String[] tage = new String[] { "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag" };
	    	
	    	return tage[tag_index-1];	
		}
		
		/**Diese Methode gibt den passenden Namen zu einem bestimmten Datum zurück*/
		public String getAnyDayByName(int jahr, int monat, int tag){
			GregorianCalendar gc = new GregorianCalendar();
			gc.set(jahr, monat-1, tag); //Monat-1 weil die Monate in Android von 0-11 gehen
	    	int tag_index =  gc.get(GregorianCalendar.DAY_OF_WEEK);
	    		
	    	String[] tage = new String[] { "Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag" };
	    	
	    	return tage[tag_index-1];// -1 weil ein Array bei 0 beginnt	
		}
		
		/**Diese Methode wandelt ein gegebenes Array in einen String um, es werden auch bestimmte Zeichen rausgekürzt*/
		public String arrayToString(String tabelle, String klasse){
			int[] abstand = haeufigkeit(tabelle, klasse);
			int anzahl = abstand.length;
			//int x = getx1(tabelle, klasse);
			
			if (anzahl == 0){ //falls die Klasse 0 mal in der HTML Datei vorhanden ist, wird ausgegeben "Es fällt nichts aus!"
				return "Es fällt nichts aus!";
			} else{
				String[][] sArray = stringKuerzen(tabelle,klasse);
				String tempStr = "", endStr = "";
				for(int i=0;i<sArray.length;i++){
					tempStr = "| ";
		    		for(int y=0;y<7;y++){	//aus 6 -> 7 gemacht da 7 items (Stunde,Klasse,Fach,Raum,Raum,Bemerkung)
		    			tempStr += sArray[i][y] + " | ";
						
		    		}
		    		tempStr = tempStr.replace("|  |","| --- |");
		    		endStr += tempStr + " \n";
				}
		    	return endStr;
		    }
		}
		
		/**formt 3 int Werte in einen DatumsString (mit 2-Stelligen Daten tt.mm.jj)*/
		public String betterDate(int tag, int monat, int jahr){
			String tagStr,monatStr;//jahrStr;
		
			if (tag<10) {
				tagStr = "0"+tag;
			} else{
				tagStr = String.valueOf(tag);
			}
			
			if (monat<10) {
				monatStr="0"+monat;
			}else{
				monatStr=String.valueOf(monat);
			}
			
			//jahrStr = String.valueOf(jahr);
					
			//return tagStr+"."+monatStr+"."+jahrStr;
			return tagStr+"."+monatStr+"."+jahr;
						
		}

		/**doppelte Zeilen in einem Array werden zusammengefasst*/
		public String[][] zusammenfassen(String[][] inputArray, String tabelle, String klasse, int anzahl, int stelle){
			int laenge = inputArray.length, neuAnzahl = laenge; //neuAnzahl = anzahl;
			
			//prüfen ob die Stelle, die man prüfen möchte, länger ist als das vorhandene Array
			if(stelle >= laenge){
				//System.out.println("stelle > laenge " + stelle);
				//System.out.println("Stelle: " + stelle);
				return inputArray;
			}
			
			//prüfen ob das Array an der Stelle einen Wert enthaelt
			if(inputArray[stelle][0] == null){
				//System.out.println("inp.Array = null");
				return inputArray;
			}
			
				//wenn Zeile Nr."stelle" genau das gleiche beinhaltet wie Zeile Nr."stelle - 1" dann zusammenfassen
				if (inputArray[stelle][0].equals(inputArray[stelle-1][0]) && inputArray[stelle][1].equals(inputArray[stelle-1][1]) && 
					inputArray[stelle][2].equals(inputArray[stelle-1][2]) && inputArray[stelle][3].equals(inputArray[stelle-1][3]) && 
					inputArray[stelle][4].equals(inputArray[stelle-1][4]) && inputArray[stelle][5].equals(inputArray[stelle-1][5]) &&
					inputArray[stelle][6].equals(inputArray[stelle-1][6])) {
						
						neuAnzahl-=1;
						String temp[][] = new String[neuAnzahl][7]; //neues Array mit einer Zeile weniger
						
						//Füllt neues Array bis zur Stelle
						for(int j = 0; j<stelle;j++){
							for(int r = 0; r < 7; r++){
								temp[j][r] = inputArray[j][r];
							}
						}
						
						//Füllt neues Array, lässt "Stelle" raus
						for(int j = stelle+1; j<neuAnzahl+1;j++){
							for(int r = 0; r < 7; r++){
								temp[j-1][r] = inputArray[j][r];
							}
						}
						
					return temp;
				}else{
				return inputArray;
				}	
		}
		
		/**wenn 2 Zeilen gleich sind (Zeile 1: 5. Stunde; Zeile 2: 6. Stunde), diese zusammenfassen*/
		public String[][] stundenZusammenfassen(String[][] inputArray, String tabelle, String klasse, int anzahl, int stelle){
			int laenge = inputArray.length, neuAnzahl = laenge; //neuAnzahl = anzahl;
			
			//prüfen ob die Stelle, die man prüfen möchte, länger ist als das vorhandene Array
			if(stelle >= laenge){
				//System.out.println("stelle > länge");
				return inputArray;
			}
			
			//prüfen ob das Array an der Stelle einen Wert enthält
			if(inputArray[stelle][0] == null){
				//System.out.println("inp.Array = null");
				return inputArray;
			}
				//wenn Zeile Nr."stelle" nicht genau das gleiche beinhaltet wie Zeile Nr."stelle - 1" dann zusammenfassen
			if (!inputArray[stelle][0].equals(inputArray[stelle-1][0]) && inputArray[stelle][1].equals(inputArray[stelle-1][1]) && 
				inputArray[stelle][2].equals(inputArray[stelle-1][2]) && inputArray[stelle][3].equals(inputArray[stelle-1][3]) && 
				inputArray[stelle][4].equals(inputArray[stelle-1][4]) && inputArray[stelle][5].equals(inputArray[stelle-1][5]) &&
				inputArray[stelle][6].equals(inputArray[stelle-1][6])) {
					/**Die Zeilen sollen in allem gleich sein, außer an der ersten Stelle**/
				neuAnzahl-=1;
				String temp[][] = new String[neuAnzahl][9]; //neues Array mit einer Zeile weniger
				
				//wenn beide stellen nicht länger als 2, dann zusammenfassen
				if (inputArray[stelle][1].length()<=2 && inputArray[stelle-1][1].length()<=2){
				// TODO Normaler ablauf ... zusammenfassen
					temp = zeileEntfernen(inputArray,neuAnzahl,stelle,false);
					
				} else if((inputArray[stelle][1].length()<=2 && inputArray[stelle-1][1].length()>2)){
					//TODO Zeile stelle löschen
					temp = zeileEntfernen(inputArray,neuAnzahl,stelle,true);
					
				} else if(inputArray[stelle-1][1].length()<=2 && inputArray[stelle][1].length()>2){
					// TODO Zeile stelle-1 löschen
					temp = zeileEntfernen(inputArray,neuAnzahl,stelle-1,true);
				}
					
				return temp;
			}else{
				return inputArray;
			}
		}
		
		/** Entfernt Zeile "stelle" aus dem Array **/
		public String[][] zeileEntfernen(String [][]inputArray,int neuLaenge,int stelle,boolean nurEntfernen){
			String temp[][] = new String[neuLaenge][9]; //neues Array mit einer Zeile weniger
			
			System.out.println("Stelle: " + stelle);
			System.out.println("laenge: " + inputArray.length);
			System.out.println("NeuLaenge: " + neuLaenge);
			System.out.println("nurEntfernen: " + nurEntfernen);
			
			
			
			
			//For-Schleife 1
			for(int j = 0; j<stelle;j++){
				for(int r = 0; r < 7; r++){
					temp[j][r] = inputArray[j][r];
				}
			}
			//Ende For-Schleife 1
			
			//For-Schleife 2
			for(int j = stelle+1; j<neuLaenge+1;j++){
				for(int r = 0; r < 7; r++){
					temp[j-1][r] = inputArray[j][r];
				}
			}//Ende For-Schleife 2
			
			if(!nurEntfernen){
				System.out.println("inputArray: " + inputArray[stelle][0]);
				temp[stelle-1][0]= temp[stelle-1][0] + " - " + inputArray[stelle][0];
			} 
			
			
			
			
			return temp;
		}
		
		/**entfernt NULL einträge | Nicht nötig  ?!?!?!? */
		public String[][] nullEntfernen(String[][] inputArray){
			int x = 0;
			for(int i=0;i<inputArray.length;i++){
				if(inputArray[i][0] == null){
					x++;
				}
			}
			
			String[][] arr = new String[inputArray.length-x][7];
			
			for(int i=0; i<inputArray.length-x;i++){
				for(int r=0;r<7;r++){
				arr[i][r]=inputArray[i][r];
				}
			}
			return arr;
		}
		
		
		/**gibt den Namen der Abkuerzung eines Faches zurueck */
		@SuppressLint("DefaultLocale")
		public String abkuerzung(String abk){
			
			if (abk.equals("")||abk==null){
				return "kein Fach angegeben";
			}else{
				abk = abk.toUpperCase();
				if(abk.equals("D")){return "Deutsch";}
				else if(abk.equals("PH")){return "Physik";}
				else if(abk.equals("CH")||abk.equals("4CH1")){return "Chemie";}
				else if(abk.equals("L")){return "Latein";}
				else if(abk.equals("S")){return "Spanisch";}
				else if(abk.equals("E")){return "Englisch";}
				else if(abk.equals("INF")){return "Informatik";}
				else if(abk.equals("LIT")){return "Literatur";}
				else if(abk.equals("EVR")){return "ev. Religion";}
				else if(abk.equals("KAR")){return "kath. Religion";}
				else if(abk.equals("ETH")){return "Ethik";}
				else if(abk.equals("MA")){return "Mathe";}
				else if(abk.equals("EK")){return "Erdkunde";}
				else if(abk.equals("BIO")){return "Biologie";}
				else if(abk.equals("MU")){return "Musik";}
				else if(abk.equals("SP")){return "Sport";}
				else if(abk.equals("SW")){return "Sport weibl.";}
				else if(abk.equals("SM")){return "Sport männl.";}
				else if(abk.equals("G")){return "Geschichte";}
				else if(abk.equals("F")){return "Französisch";}
				else if(abk.equals("NWT")){return "Naturwissenschaft u. Technik";}
				else if(abk.equals("GK")){return "Gemeinschaftskunde";}
				else if(abk.equals("SF")){return "Seminarkurs";}
				else if(abk.equals("NP")){return "Naturphänomene";}
				else if(abk.equals("WI")){return "Wirtschaft";}
				else if(abk.equals("METH")){return "Meth";}
				else if(abk.equals("BK")){return "Bildende Kunst";}
				else if(abk.equals("LRS")){return "LRS";}
				else if(abk.equals("PSY")){return "Psychologie";}
				else if(abk.equals("PHIL")){return "Philosophie";}
				else{return abk;}
			}	
			
			
		}
		
		/**sortiert ein Array */
		public void sortieren(String[][] data){
			 Arrays.sort(data, new Comparator<String[]>() {
		            @Override
		            public int compare(final String[] entry1, final String[] entry2) {
		                final String string1 = entry1[0];
		                final String string2 = entry2[0];
		                
		                //Versucht die Stunden als Zahl zu speichern und dann zu sortieren, sollte es fehlschlagen wird nach Strings sortiert
		                try {
						
	                	String string1Copy = string1,string2Copy = string2;
		                
		                if (string1Copy.length()==2){string1Copy = string1Copy.substring(0,2);}
		                else if (string1Copy.length()<=5){string1Copy = string1Copy.substring(0,1);}
		                else if (string1Copy.length()>=6){string1Copy = string1Copy.substring(0,2);}
		                
		                if (string2Copy.length()==2){string2Copy = string2Copy.substring(0,2);}
		                else if (string2Copy.length()<=5){string2Copy = string2Copy.substring(0,1);}
		                else if (string2Copy.length()>=6){string2Copy = string2Copy.substring(0,2);}
		                
		                return Integer.valueOf(string1Copy).compareTo(Integer.valueOf(string2Copy));
						} catch (Exception e) {
							return string1.compareTo(string2);
						}
		                
		                
		                //return Integer.valueOf(string1.substring(0,2)).compareTo(Integer.valueOf(string2.substring(0, 1)));
		                //return string1.compareTo(string2);
		                //}
		                
		            }
		        });
		}

		
		public String getFormattedDate(long smsTimeInMilis) {
		    Calendar smsTime = Calendar.getInstance();
		    smsTime.setTimeInMillis(smsTimeInMilis);

		    Calendar now = Calendar.getInstance();

		    final String timeFormatString = "HH:mm";
		    final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
		    final long HOURS = 60 * 60 * 60;
		    if(now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ){
		        return ": Heute " + DateFormat.format(timeFormatString, smsTime);
		    }else if(now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1 ){
		        return ": Gestern " + DateFormat.format(timeFormatString, smsTime);
		    }else if(now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)){
		        return DateFormat.format(dateTimeFormatString, smsTime).toString();
		    }else
		        return " am: " + DateFormat.format("dd.MM.yyyy HH:mm", smsTime).toString();
		}
		

}       