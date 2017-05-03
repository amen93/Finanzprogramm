package Daten;

import static Daten.Datenbank.getConnection;
import static Daten.Datenbank.getListeAusgaben;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author Quirin
 */
public class Ausgabe {
	
	public static long DATUM_ERSTE_AUSGABE;

    protected final SimpleIntegerProperty indexInDatenbank;
    protected final SimpleStringProperty verwendungszweck;
    protected final SimpleStringProperty kommentar;
    protected Sinnvoll sinnvoll;
    protected Kategorie kategorie;
    protected final SimpleDoubleProperty wert;
    protected final SimpleLongProperty datum;

    public Ausgabe(int indexInDatenbank, String verwendungszweck, double wert, Sinnvoll sinnvoll, String kommentar, long datumAusgegeben, Kategorie kategorie) {
        this.indexInDatenbank = new SimpleIntegerProperty(indexInDatenbank);
        this.verwendungszweck = new SimpleStringProperty(verwendungszweck);
        this.wert = new SimpleDoubleProperty(wert);
        this.sinnvoll = sinnvoll;
        this.kommentar = new SimpleStringProperty(kommentar);
        this.datum = new SimpleLongProperty(datumAusgegeben);
        this.kategorie = kategorie;
    }

    public static boolean ausgabeHinzufügen(String verwendungszweck, double wert, Sinnvoll sinnvoll, String kommentar, long datum, Kategorie kategorie) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO ausgaben VALUES(?,?,?,?,?,?,?)")) {
            int neuerIndex = Datenbank.getNeuerIndex(Datenbank.AUSGABEN);
            statement.setInt(1, neuerIndex);
            statement.setString(2, verwendungszweck);
            statement.setDouble(3, wert);
            statement.setInt(4, sinnvoll.ordinal());
            statement.setString(5, kommentar);
            statement.setLong(6, datum);
            statement.setInt(7, kategorie.getIndexDatenbank());
            statement.executeUpdate();

            getListeAusgaben().add(0, new Ausgabe(neuerIndex, verwendungszweck, wert, sinnvoll, kommentar, datum, kategorie));
            return true;
        }
    }

    public boolean ausgabeÄndern(String verwendungszweck, double wert, Sinnvoll sinnvoll, String kommentar, long datum, Kategorie kategorie) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("UPDATE ausgaben SET verwendungszweck = ?, wert = ?, sinnvoll = ?, kommentar = ?, datum = ?, kategorie = ? WHERE id = ?")) {
            statement.setString(1, verwendungszweck);
            statement.setDouble(2, wert);
            statement.setInt(3, sinnvoll.ordinal());
            statement.setString(4, kommentar);
            statement.setLong(5, datum);
            statement.setInt(6, kategorie.getIndexDatenbank());
            statement.setInt(7, this.indexInDatenbank.get());
            statement.executeUpdate();
            this.verwendungszweck.set(verwendungszweck);
            this.wert.set(wert);
            this.datum.set(datum);
            this.sinnvoll = sinnvoll;
            this.kategorie = kategorie;
            this.kommentar.set(kommentar);
        }
        return true;
    }

    public boolean ausgabeLöschen() throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM ausgaben WHERE id = ?")) {
            statement.setInt(1, this.indexInDatenbank.get());
            statement.executeUpdate();
            getListeAusgaben().remove(this);
            return true;
        }
    }
    
    @Override
    public String toString() {
    	return this.verwendungszweck.get() + ", " + this.wert.get() + "€";
    }

    public int getIndexInDatenbank() {
        return this.indexInDatenbank.get();
    }

    public String getVerwendungszweck() {
        return this.verwendungszweck.get();
    }

    public double getWert() {
        return this.wert.get();
    }

    public String getKommentar() {
        return this.kommentar.get();
    }

    public long getDatum() {
        return this.datum.get();
    }

    public Sinnvoll getSinnvoll() {
        return this.sinnvoll;
    }

    public Kategorie getKategorie() {
        return this.kategorie;
    }

    public void setSinnvoll(Sinnvoll sinnvoll) {
        this.sinnvoll = sinnvoll;
    }

    public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }
    

    // PROPERTIES
    public SimpleIntegerProperty indexInDatenbankProperty() {
        return this.indexInDatenbank;
    }

    public SimpleStringProperty verwendungszweckProperty() {
        return this.verwendungszweck;
    }

    public SimpleDoubleProperty wertProperty() {
        return this.wert;
    }

    public SimpleLongProperty datumProperty() {
        return this.datum;
    }

    public SimpleStringProperty kommentarProperty() {
        return this.kommentar;
    }

}
