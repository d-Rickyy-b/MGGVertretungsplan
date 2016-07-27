package de.aurora.mggvertretungsplan;

class Vertretungen {
	private String stunde, raum, fach, neuRaum,bemerkung,tag,datum;
	
	public Vertretungen(String stunde, String fach, String raum, String neuRaum, String bemerkung,String tag, String datum) {
		this.stunde = stunde;
		this.raum = raum;
		this.fach = fach;
		this.neuRaum = neuRaum;
		this.bemerkung = bemerkung;
		this.tag = tag;
		this.datum = datum;
	}

// --Commented out by Inspection START (26.07.2016 22:30):
//	public void setstunde(String stunde) {
//        this.stunde= stunde;
//    }
// --Commented out by Inspection STOP (26.07.2016 22:30)
    public String getStunde() {
        return stunde;
    }
// --Commented out by Inspection START (26.07.2016 22:30):
//	public void setRaum(String raum) {
//        this.raum= raum;
//    }
// --Commented out by Inspection STOP (26.07.2016 22:30)
    public String getRaum() {
        return raum;
    }
// --Commented out by Inspection START (26.07.2016 22:31):
//    public void setFach(String fach) {
//        this.fach= fach;
//    }
// --Commented out by Inspection STOP (26.07.2016 22:31)
    public String getFach() {
        return fach;
    }
// --Commented out by Inspection START (26.07.2016 22:31):
//    public void setNeuRaum(String neuRaum) {
//        this.neuRaum= neuRaum;
//    }
// --Commented out by Inspection STOP (26.07.2016 22:31)
    public String getNeuRaum() {
        return neuRaum;
    }
// --Commented out by Inspection START (26.07.2016 22:31):
//    public void setBemerkung(String bemerkung) {
//        this.bemerkung= bemerkung;
//    }
// --Commented out by Inspection STOP (26.07.2016 22:31)
    public String getBemerkung() {
        return bemerkung;
    }
// --Commented out by Inspection START (26.07.2016 22:31):
//    public void setTag(String tag){
//    	this.tag = tag;
//    }
// --Commented out by Inspection STOP (26.07.2016 22:31)
    public String getTag(){
    	return tag;
    }
// --Commented out by Inspection START (26.07.2016 22:31):
//    public void setDatum(String datum){
//    	this.datum = datum;
//    }
// --Commented out by Inspection STOP (26.07.2016 22:31)
    public String getDatum(){
    	return datum;
    }
	
	
}
