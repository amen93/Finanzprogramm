package Daten;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Quirin
 */
public enum Sinnvoll {
    
    LEER(""),
    PFLICHTAUSGABE("Pflichtausgabe"),
    GAR_NICHT("gar nicht"),
    NICHT_SO("nicht so"),
    WEDER_NOCH("weder noch"),
    SINNVOLL("sinnvoll"),
    WORTH("worth!!");

    private final String name;

    private Sinnvoll(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
    public static ObservableList<Sinnvoll> getList() {
        ObservableList<Sinnvoll> liste = FXCollections.observableArrayList();
        liste.addAll(LEER, PFLICHTAUSGABE, GAR_NICHT, NICHT_SO, WEDER_NOCH, SINNVOLL, WORTH);
        return liste;
    }
}
