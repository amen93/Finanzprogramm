package Dialog;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import Daten.Ausgabe;
import Daten.Datenbank;
import Daten.Kategorie;
import Daten.Sinnvoll;
import Hauptfenster.Hauptfenster;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
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
public class AusgabeDialog extends Dialog<Boolean> {

    private Ausgabe ausgabe;

    private final TextField tfVerwendungszweck;
    private final TextField tfWert;
    private final DatePicker datepicker;
    private final ComboBox<Sinnvoll> cbSinnvoll;
    private final ComboBox<Kategorie> cbKategorie;
    private final TextField tfKommentar;
    private final Button bLöschen;
    private final DialogOption option;

    private ButtonType bTypeÄndernHinzufügen;

    public AusgabeDialog(Hauptfenster hauptfenster, Ausgabe ausgabe, DialogOption option) {
        super();
        this.initOwner(hauptfenster.getStage());
        this.ausgabe = ausgabe;
        this.option = option;
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 40, 20, 20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        this.tfVerwendungszweck = new TextField();
        this.tfWert = new TextField();
        this.datepicker = new DatePicker(LocalDate.now());
        this.cbSinnvoll = new ComboBox<>(Sinnvoll.getList());
        this.cbKategorie = new ComboBox<>(Datenbank.getListeKategorien());
        this.tfKommentar = new TextField();
        this.bLöschen = new Button("Löschen");
        this.cbSinnvoll.getSelectionModel().select(Sinnvoll.LEER);
        this.cbKategorie.getSelectionModel().select(Datenbank.getListeKategorien().get(Kategorie.INDEX_FRAGEZEICHEN_KATEGORIE));
        
        gridPane.add(new Label("Verwendungszweck"), 0, 0);
        gridPane.add(new Label("Wert"), 0, 1);
        gridPane.add(new Label("Datum"), 0, 2);
        gridPane.add(new Label("Sinnvoll"), 0, 3);
        gridPane.add(new Label("Kategorie"), 0, 4);
        gridPane.add(new Label("Kommentar"), 0, 5);
        if (this.option == DialogOption.ÄNDERN) {
        	gridPane.add(this.bLöschen, 0, 6);
        }
        
        gridPane.add(this.tfVerwendungszweck, 1, 0);
        gridPane.add(this.tfWert, 1, 1);
        gridPane.add(this.datepicker, 1, 2);
        gridPane.add(this.cbSinnvoll, 1, 3);
        gridPane.add(this.cbKategorie, 1, 4);
        gridPane.add(this.tfKommentar, 1, 5);
        
        AutoCompletionTextFieldBinding<Ausgabe> autoCompletionBinding = new AutoCompletionTextFieldBinding<>(this.tfVerwendungszweck, SuggestionProvider.create(param -> param.getVerwendungszweck(), Datenbank.getListeAusgaben()));
        autoCompletionBinding.setOnAutoCompleted(value -> {
        	AusgabeDialog.this.ausgabe = value.getCompletion();
        	this.felderBefüllen();
        });
        
        this.uiBefüllen();

        this.getDialogPane().getButtonTypes().addAll(this.bTypeÄndernHinzufügen, ButtonType.CANCEL);
        this.getDialogPane().setContent(gridPane);

        this.bLöschen.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Die Ausgabe für " + this.ausgabe.getVerwendungszweck() + " wirklich löschen?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == ButtonType.YES) {
                    try {
                        this.ausgabe.ausgabeLöschen();
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
	                if (option == DialogOption.HINZUFÜGEN || option == DialogOption.FIXKOSTEN) {
	                        return Ausgabe.ausgabeHinzufügen(this.tfVerwendungszweck.getText(), Double.valueOf(this.tfWert.getText().replace(',', '.')), this.cbSinnvoll.getValue(), this.tfKommentar.getText(), Date.from(Instant.from(this.datepicker.getValue().atStartOfDay(ZoneId.systemDefault()))).getTime(), this.cbKategorie.getValue());
	                } else {
	                        return this.ausgabe.ausgabeÄndern(this.tfVerwendungszweck.getText(), Double.valueOf(this.tfWert.getText().replace(',', '.')), this.cbSinnvoll.getValue(), this.tfKommentar.getText(), Date.from(Instant.from(this.datepicker.getValue().atStartOfDay(ZoneId.systemDefault()))).getTime(), this.cbKategorie.getValue());
	                }
            	} catch (SQLException ex) {
            		Hauptfenster.fehlermeldung(ex);
            	}
            	finally {
					hauptfenster.fixkostenPrüfen();
					hauptfenster.unfertigZählen();
				}
            }
            return false;
        });
    }

	private void uiBefüllen() {
		if (this.option == DialogOption.HINZUFÜGEN || this.option == DialogOption.FIXKOSTEN) {
			this.setTitle("Ausgabe hinzufügen");        	
			this.bTypeÄndernHinzufügen = new ButtonType("Hinzufügen", ButtonBar.ButtonData.OK_DONE);
			if (this.option == DialogOption.FIXKOSTEN) {
				this.felderBefüllen();
			}
		} else {
			this.setTitle("Ausgabe bearbeiten");
			this.bTypeÄndernHinzufügen = new ButtonType("Ändern", ButtonBar.ButtonData.OK_DONE);
			this.felderBefüllen();
		}
	}
    
    private void felderBefüllen() {
    	this.tfVerwendungszweck.setText(this.ausgabe.getVerwendungszweck());
        this.tfWert.setText(String.valueOf(this.ausgabe.getWert()).replace(".", ","));
        this.datepicker.setValue(new Date(this.ausgabe.getDatum()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        this.cbSinnvoll.setValue(this.ausgabe.getSinnvoll());
        this.cbKategorie.setValue(this.ausgabe.getKategorie());
        this.tfKommentar.setText(this.ausgabe.getKommentar());
    }
    
}
