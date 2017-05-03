package Daten;

import static Daten.Datenbank.getConnection;
import static Daten.Datenbank.getListeKategorien;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Quirin
 */
public class Kategorie {
    
    public static final int INDEX_FRAGEZEICHEN_KATEGORIE = 0;

    private final int indexInDatenbank;
    private final StringProperty name;

    public Kategorie(int indexDatenbank, String name) {
        this.indexInDatenbank = indexDatenbank;
        this.name = new SimpleStringProperty(name);
    }

    public static boolean kategorieHinzufügen(String name) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO kategorie VALUES(?,?)")) {
            int neuerIndex = Datenbank.getNeuerIndex(Datenbank.KATEGORIE);
            statement.setInt(1, neuerIndex);
            statement.setString(2, name);
            statement.executeUpdate();
            getListeKategorien().add(neuerIndex, new Kategorie(neuerIndex, name));
            return true;
        }
    }

    public boolean kategorieÄndern(String name) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("UPDATE kategorie SET name = ? WHERE id = ?")) {
            statement.setString(1, name);
            statement.setInt(2, this.indexInDatenbank);
            statement.executeUpdate();
            this.name.set(name);
            return true;
        }
    }

    public boolean kategorieLöschen() throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM kategorie WHERE id = ?")) {
            statement.setInt(1, this.indexInDatenbank);
            statement.executeUpdate();
            Datenbank.getListeKategorien().remove(this.indexInDatenbank);
            Kategorie leer = Datenbank.getListeKategorien().get(INDEX_FRAGEZEICHEN_KATEGORIE);
            for (Ausgabe ausgabe : Datenbank.getListeAusgaben()) {
                if(ausgabe.getKategorie() == this) {
                    ausgabe.setKategorie(leer);
                }
            }
            return true;
        }
    }
    
    public int getIndexDatenbank() {
        return this.indexInDatenbank;
    }

    public String getName() {
        return this.name.get();
    }

    public StringProperty nameProperty() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Kategorie other = (Kategorie) obj;
        return this.indexInDatenbank == other.indexInDatenbank;
    }

}
