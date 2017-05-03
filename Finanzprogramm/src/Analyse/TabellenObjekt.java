package Analyse;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Quirin
 */
public class TabellenObjekt {
    
    private final StringProperty ersteSpalte;
    private final StringProperty zweiteSpalte;
    private final StringProperty dritteSpalte;

    public TabellenObjekt(String ersteSpalte, String zweiteSpalte, String dritteSpalte) {
        this.ersteSpalte = new SimpleStringProperty(ersteSpalte);
        this.zweiteSpalte = new SimpleStringProperty(zweiteSpalte);
        this.dritteSpalte = new SimpleStringProperty(dritteSpalte);
    }

    public StringProperty ersteSpalteProperty() {
        return ersteSpalte;
    }

    public StringProperty zweiteSpalteProperty() {
        return zweiteSpalte;
    }

    public StringProperty dritteSpalteProperty() {
        return dritteSpalte;
    }
    
    
}
