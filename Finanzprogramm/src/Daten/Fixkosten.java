package Daten;

import static Daten.Datenbank.getConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * @author Quirin
 */
public class Fixkosten extends Ausgabe {

    public Fixkosten(int indexInDatenbank, String verwendungszweck, double wert, Sinnvoll sinnvoll, String kommentar, long datum, Kategorie kategorie) {
        super(indexInDatenbank, verwendungszweck, wert, sinnvoll, kommentar, datum, kategorie);
    }

    public static boolean fixkostenHinzufügen(String verwendungszweck, double wert, Sinnvoll sinnvoll, String kommentar, long datum, Kategorie kategorie) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO fixkosten VALUES(?,?,?,?,?,?,?,?,?)")) {
            int neuerIndex = Datenbank.getNeuerIndex(Datenbank.FIXKOSTEN);
            statement.setInt(1, neuerIndex);
            statement.setString(2, verwendungszweck);
            statement.setDouble(3, wert);
            statement.setInt(4, sinnvoll.ordinal());
            statement.setString(5, kommentar);
            statement.setLong(6, datum);
            statement.setInt(7, kategorie.getIndexDatenbank());
            statement.executeUpdate();
            Datenbank.getListeFixkosten().add(0, new Fixkosten(neuerIndex, verwendungszweck, wert, sinnvoll, kommentar, datum, kategorie));
            return true;
        }
    }

    public boolean fixkostenÄndern(String verwendungszweck, double wert, Sinnvoll sinnvoll, String kommentar, long datum, Kategorie kategorie) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("UPDATE fixkosten SET verwendungszweck = ?, wert = ?, sinnvoll = ?, kommentar = ?, termin = ?, kategorie = ? WHERE id = ?")) {
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
            return true;
        }
    }

    public boolean fixkostenAktualisieren() throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("UPDATE fixkosten SET termin = ? WHERE id = ?")) {
        	long neuerTermin = this.rechneNächstenTermin();
            statement.setLong(1, neuerTermin);
            statement.setInt(2, this.indexInDatenbank.get());
            statement.executeUpdate();
            this.datum.set(neuerTermin);
            return true;
        }
    }

    private long rechneNächstenTermin() {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(this.datum.get());
    	LocalDateTime termin = LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
    	termin = termin.plusMonths(1);
    	return termin.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public boolean fixkostenLöschen() throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM fixkosten WHERE id = ?")) {
            statement.setInt(1, this.indexInDatenbank.get());
            statement.executeUpdate();
            Datenbank.getListeFixkosten().remove(this);
            return true;
        }
    }

}
