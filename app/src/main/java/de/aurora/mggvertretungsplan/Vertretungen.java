package de.aurora.mggvertretungsplan;

public class Vertretungen {
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

	public void setStunde(String stunde) {
        this.stunde= stunde;
    }
    public String getStunde() {
        return stunde;
    }
	public void setRaum(String raum) {
        this.raum= raum;
    }
    public String getRaum() {
        return raum;
    }
    public void setFach(String fach) {
        this.fach= fach;
    }
    public String getFach() {
        return fach;
    }
    public void setNeuRaum(String neuRaum) {
        this.neuRaum= neuRaum;
    }
    public String getNeuRaum() {
        return neuRaum;
    }
    public void setBemerkung(String bemerkung) {
        this.bemerkung= bemerkung;
    }
    public String getBemerkung() {
        return bemerkung;
    }
    public void setTag(String tag){
    	this.tag = tag;
    }
    public String getTag(){
    	return tag;
    }
    public void setDatum(String datum){
    	this.datum = datum;
    }
    public String getDatum(){
    	return datum;
    }
	
	
}
