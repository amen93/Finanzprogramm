package Dialog;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import Daten.Datenbank;
import Daten.Fixkosten;
import Daten.Kategorie;
import Daten.Sinnvoll;
import Hauptfenster.Hauptfenster;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * @author Quirin
 */
public class FixkostenDialog extends Dialog<Boolean> {

    private final Fixkosten fixkosten;

    private final TextField tfVerwendungszweck;
    private final TextField tfWert;
    private final DatePicker datepickerNächsterTermin;
    private final ComboBox<Sinnvoll> cbSinnvoll;
    private final ComboBox<Kategorie> cbKategorie;
    private final TextField tfKommentar;
    private final Button bLöschen;

    private ButtonType bTypeÄndernHinzufügen;

    public FixkostenDialog(Hauptfenster hauptfenster, Fixkosten fixkosten) {
        super();
        this.initOwner(hauptfenster.getStage());
        this.fixkosten = fixkosten;
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 40, 20, 20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        this.tfVerwendungszweck = new TextField();
        this.tfWert = new TextField();
        this.datepickerNächsterTermin = new DatePicker(LocalDate.now());
        this.cbSinnvoll = new ComboBox<>(Sinnvoll.getList());
        this.cbKategorie = new ComboBox<>(Datenbank.getListeKategorien());
        this.tfKommentar = new TextField();
        this.bLöschen = new Button("Löschen");
        
        gridPane.add(new Label("Verwendungszweck"), 0, 0);
        gridPane.add(new Label("Wert"), 0, 1);
        gridPane.add(new Label("Nächster Termin"), 0, 2);
        gridPane.add(new Label("Tag im Monat"), 0, 3);
        gridPane.add(new Label("Sinnvoll"), 0, 4);
        gridPane.add(new Label("Kategorie"), 0, 5);
        gridPane.add(new Label("Kommentar"), 0, 6);
        if(fixkosten != null) {
        	gridPane.add(this.bLöschen, 0, 7);
        }
        
        gridPane.add(this.tfVerwendungszweck, 1, 0);
        gridPane.add(this.tfWert, 1, 1);
        gridPane.add(this.datepickerNächsterTermin, 1, 2);
        gridPane.add(this.cbSinnvoll, 1, 3);
        gridPane.add(this.cbKategorie, 1, 4);
        gridPane.add(this.tfKommentar, 1, 5);
        
        this.uiBefüllen();
        
        this.getDialogPane().getButtonTypes().addAll(this.bTypeÄndernHinzufügen, ButtonType.CANCEL);
        this.getDialogPane().setContent(gridPane);
        
        this.bLöschen.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Die Fixkosten für " + fixkosten.getVerwendungszweck() + " wirklich löschen?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == ButtonType.YES) {
                    try {
                        fixkosten.fixkostenLöschen();
                        this.close();
                    } catch (SQLException ex) {
                        Hauptfenster.fehlermeldung(ex);
                    }
                }
            }
        });

        this.setResultConverter(dialogButton -> {
            if (dialogButton == this.bTypeÄndernHinzufügen) {
            	try {
	                if (fixkosten == null) {
	                    Fixkosten.fixkostenHinzufügen(this.tfVerwendungszweck.getText(), Double.valueOf(this.tfWert.getText().replace(',', '.')), this.cbSinnvoll.getValue(), this.tfKommentar.getText(), Date.from(Instant.from(this.datepickerNächsterTermin.getValue().atStartOfDay(ZoneId.systemDefault()))).getTime(), this.cbKategorie.getValue());
	                } else {
	                	fixkosten.fixkostenÄndern(this.tfVerwendungszweck.getText(), Double.valueOf(this.tfWert.getText().replace(',', '.')), this.cbSinnvoll.getValue(), this.tfKommentar.getText(), Date.from(Instant.from(this.datepickerNächsterTermin.getValue().atStartOfDay(ZoneId.systemDefault()))).getTime(), this.cbKategorie.getValue());
	                }
                } catch (SQLException ex) {
                	Hauptfenster.fehlermeldung(ex);
                } finally {
					hauptfenster.fixkostenPrüfen();
				}
            }
            return false;
        });
    }

    private void uiBefüllen() {
        if (this.fixkosten == null) {
            this.setTitle("Fixkosten hinzufügen");
            this.bTypeÄndernHinzufügen = new ButtonType("Hinzufügen", ButtonBar.ButtonData.OK_DONE);
        } else {
            this.setTitle("Fixkosten bearbeiten");
            this.bTypeÄndernHinzufügen = new ButtonType("Ändern", ButtonBar.ButtonData.OK_DONE);
            this.tfVerwendungszweck.setText(this.fixkosten.getVerwendungszweck());
            this.tfWert.setText(String.valueOf(this.fixkosten.getWert()).replace(".", ","));
            this.datepickerNächsterTermin.setValue(new Date(this.fixkosten.getDatum()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            this.cbSinnvoll.setValue(this.fixkosten.getSinnvoll());
            this.cbKategorie.setValue(this.fixkosten.getKategorie());
            this.tfKommentar.setText(this.fixkosten.getKommentar());
        }
    }

}
