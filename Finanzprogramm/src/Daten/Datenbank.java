package Daten;

import static Hauptfenster.Hauptfenster.fehlermeldung;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Quirin Ertl
 */
public class Datenbank {

    private static Connection connection;
    private static ObservableList<Ausgabe> listeAusgaben;
    private static ObservableList<Fixkosten> listeFixkosten;
    private static ObservableList<Kategorie> listeKategorien;

    public Datenbank(File datenbank) throws SQLException {
        try {
            this.datenbankVerbindungHerstellen(datenbank);
            this.datenAuslesen();
        } catch (ClassNotFoundException ex) {
            fehlermeldung(ex);
            System.exit(0);
        }
    }

    private void datenAuslesen() throws SQLException {
        this.kategorieInitialisieren();
        this.ausgabenListeInitialisieren();
        this.fixkostenListeInitialisieren();
    }

    public static Connection getConnection() {
        return connection;
    }

    private void datenbankVerbindungHerstellen(File datenbank) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + datenbank.getPath());
    }

    public static ObservableList<Fixkosten> getListeFixkosten() {
        return listeFixkosten;
    }

    public static ObservableList<Ausgabe> getListeAusgaben() {
        return listeAusgaben;
    }

    public static ObservableList<Kategorie> getListeKategorien() {
        return listeKategorien;
    }
    
    public static ArrayList<Kategorie> getLückenloseListeKategorien() {
    	ArrayList<Kategorie> liste = new ArrayList<>();
    	for (Kategorie kategorie : listeKategorien) {
			if(kategorie != null) {
				liste.add(kategorie);
			}
		}
    	return liste;
    }
    
    public static ObservableList<Ausgabe> getAusgabenByKategorie(Kategorie kategorie) {
    	if(kategorie == null) {
    		return FXCollections.observableArrayList();
    	}
    	ObservableList<Ausgabe> liste = FXCollections.observableArrayList();
    	for (Ausgabe ausgabe : listeAusgaben) {
			if(kategorie.equals(ausgabe.getKategorie())) {
				liste.add(ausgabe);
			}
		}
    	return liste;
    }
    
    private void kategorieInitialisieren() throws SQLException {
        listeKategorien = FXCollections.observableArrayList();
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT * FROM kategorie")) {
            while (rs.next()) {
                listeKategorien.add(rs.getInt(1), new Kategorie(rs.getInt(1), rs.getString(2)));
            }
        }
    }

    private void ausgabenListeInitialisieren() throws SQLException {
        listeAusgaben = FXCollections.observableArrayList();
        boolean ersteAusgabe = true;
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT * FROM ausgaben ORDER BY datum DESC")) {
            while (rs.next()) {
            	if(ersteAusgabe) {
            		Ausgabe.DATUM_ERSTE_AUSGABE = rs.getLong(6);
            	}
                listeAusgaben.add(new Ausgabe(rs.getInt(1), rs.getString(2), rs.getDouble(3), Sinnvoll.values()[rs.getInt(4)], rs.getString(5), rs.getLong(6), listeKategorien.get(rs.getInt(7))));
            }
        }
    }

    private void fixkostenListeInitialisieren() throws SQLException {
        listeFixkosten = FXCollections.observableArrayList();
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT * FROM fixkosten")) {
            while (rs.next()) {
                listeFixkosten.add(new Fixkosten(rs.getInt(1), rs.getString(2), rs.getDouble(3), Sinnvoll.values()[rs.getInt(4)], rs.getString(5), rs.getLong(6), listeKategorien.get(rs.getInt(7))));
            }
        }
    }

    public static int getNeuerIndex(String tabellenName) {
        try (Statement statement = connection.createStatement()) {
            return statement.executeQuery("SELECT MAX(ID) +1 FROM " + tabellenName).getInt(1);
        } catch (SQLException ex) {
            fehlermeldung("Datenbankfehler beim Erhöhen des Indizies -> Index -> getNeuerIndex()\n" + ex);
            return 0;
        }
    }
    
    public static final String AUSGABEN = "AUSGABEN";
    public static final String FIXKOSTEN = "FIXKOSTEN";
    public static final String KATEGORIE = "KATEGORIE";

}
