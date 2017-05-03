package Analyse;

import static Daten.Datenbank.getListeAusgaben;
import static Daten.Datenbank.getLückenloseListeKategorien;
import static Hauptfenster.Hauptfenster.getDatumCellFactory;
import static Hauptfenster.Hauptfenster.getWertCellFactory;
import static java.text.DecimalFormatSymbols.getInstance;
import static java.util.Locale.GERMAN;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import Daten.Ausgabe;
import Daten.Datenbank;
import Daten.Kategorie;
import Daten.Sinnvoll;
import Dialog.AusgabeDialog;
import Dialog.DialogOption;
import Hauptfenster.Hauptfenster;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Quirin
 */
public class AusgabenAnalyse extends VBox {

    private final DecimalFormat formatFürProzent;
    private final DecimalFormat formatFürEuroOhneKomma;
    private double alleAusgabenWerte;
    private final double menubarHöhe;
    private final Hauptfenster hauptfenster;

    private TableView<Ausgabe> tabelleAusgaben;
    private TableView<TabellenObjekt> tabelleKategorie;
    private TableView tabelleSinnvoll;
    private TabPane tabbedPane;
    private HBox hBoxBeideTabellen;
    private VBox hBoxKategorie;
    private HBox hBoxSinnvoll;
    private GridPane gridpane;
    private final Stage stage;
    
    private TextField tfVerwendungszweck;
    private TextField tfWertVon;
    private TextField tfWertBis;
    private TextField tfKommentar;
    private ComboBox<Sinnvoll> cbSinnvoll;
    private ComboBox<Kategorie> cbKategorie;

    private ArrayList<Ausgabe>[] listeKategorie;
    private ArrayList<Ausgabe>[] listeSinnvoll;

    public AusgabenAnalyse(final Hauptfenster hauptfenster, final double menubarHöhe) {
        super(15);
        this.hauptfenster = hauptfenster;
        this.stage = hauptfenster.getStage();
        this.menubarHöhe = menubarHöhe;
        this.formatFürProzent = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.GERMAN));
        this.formatFürProzent.setDecimalFormatSymbols(getInstance(GERMAN));
        this.formatFürEuroOhneKomma = new DecimalFormat("#,###.## €", DecimalFormatSymbols.getInstance(Locale.GERMAN));
        this.hBoxKategorie = new VBox();
        this.hBoxSinnvoll = new HBox();
        this.hBoxBeideTabellen = new HBox(15);
        this.hBoxBeideTabellen.getChildren().addAll(this.hBoxKategorie, this.hBoxSinnvoll);
        this.gridpane = new GridPane();
        this.tabbedPane = new TabPane();
        Tab analyseTab = new Tab("Analyse");
        Tab sucheTab = new Tab("Suche");
        analyseTab.setContent(this.hBoxBeideTabellen);
        sucheTab.setContent(this.gridpane);
        this.tabbedPane.getTabs().addAll(analyseTab, sucheTab);
        this.getChildren().add(this.tabbedPane);
        this.stage.getScene().heightProperty().addListener(listener -> {
        	if(this.tabelleAusgaben != null) {
        		this.tabelleAusgaben.setPrefHeight(this.stage.getScene().getHeight() - menubarHöhe - this.getSpacing() - this.tabelleKategorie.getHeight());
        	}
        });
        this.initTables();
        this.tabelleKategorie();
        this.initSucheTab();
        new Thread(() -> {
        	int counter = 500;
        	while(counter > 0) {
        		this.tabelleAusgaben.setPrefHeight(this.stage.getScene().getHeight() - this.menubarHöhe - this.getSpacing() - this.tabelleKategorie.getHeight());
        		try {
					Thread.sleep(7);
					counter -= 10;
				} catch (InterruptedException e) {
				}
        	}
        }).start();
    }
    
    
    private void initSucheTab() {
    	Button bSuchen = new Button("Suchen");
    	bSuchen.setOnAction(action-> {
    		this.zeichneTabelleAusgaben(this.getListeAusgabenAusSuche());
    	});
        this.tfVerwendungszweck = new TextField();
        this.tfWertVon = new TextField();
        this.tfWertBis = new TextField();
        this.tfKommentar = new TextField();
        this.cbSinnvoll = new ComboBox<>(Sinnvoll.getList());
        this.cbSinnvoll.getItems().add(0, null);
        this.cbKategorie = new ComboBox<>(Datenbank.getListeKategorien());
        this.cbKategorie.getItems().add(0, null);
        this.gridpane.add(new Label("Verwendungszweck"), 0, 0);
        this.gridpane.add(new Label("Wert"), 0, 1);
        this.gridpane.add(new Label(" - "), 2, 1);
        this.gridpane.add(new Label("Sinnvoll"), 0, 2);
        this.gridpane.add(new Label("Kategorie"), 0, 3);
        this.gridpane.add(new Label("Kommentar"), 0, 4);
        
        this.gridpane.setVgap(5);
        this.gridpane.setHgap(5);
        
        this.gridpane.add(this.tfVerwendungszweck, 1, 0);
        this.gridpane.add(this.tfWertVon, 1, 1);
        this.gridpane.add(this.tfWertBis, 3, 1);
        this.gridpane.add(this.cbSinnvoll, 1, 2);
        this.gridpane.add(this.cbKategorie, 1, 3);
        this.gridpane.add(this.tfKommentar, 1, 4);
        this.gridpane.add(bSuchen, 0, 5);

    }

    private void initTables() {
    	
    	// KATEGORIE 
        this.tabelleKategorie = new TableView();
        this.tabelleKategorie.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tabelleKategorie.setPrefWidth(Hauptfenster.breite / 3);
        this.tabelleKategorie.setPrefHeight(Hauptfenster.höhe / 3);
        this.tabelleKategorie.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TableColumn spalteLeerKategorie = new TableColumn("");
        TableColumn spalteAnzahlKategorie = new TableColumn("Anzahl | %");
        TableColumn spalteEuroKategorie = new TableColumn("€ | %");
        spalteLeerKategorie.setCellValueFactory(new PropertyValueFactory<>("ersteSpalte"));
        spalteAnzahlKategorie.setCellValueFactory(new PropertyValueFactory<>("zweiteSpalte"));
        spalteEuroKategorie.setCellValueFactory(new PropertyValueFactory<>("dritteSpalte"));
        
        spalteLeerKategorie.setSortable(false);
        spalteAnzahlKategorie.setSortable(false);
        spalteEuroKategorie.setSortable(false);

        this.tabelleKategorie.getColumns().addAll(spalteLeerKategorie, spalteAnzahlKategorie, spalteEuroKategorie);

        this.tabelleKategorie.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            ObservableList<Integer> markierteZeilen = this.tabelleKategorie.getSelectionModel().getSelectedIndices();
            if (markierteZeilen.size() != 0) {
                ArrayList<Ausgabe>[] liste = new ArrayList[markierteZeilen.size()];
                if (markierteZeilen.get(0) == 0) {
                	liste = null;
                } else {
                	int counter = 0;
                	for (Integer integer : markierteZeilen) {
						liste[counter] = this.listeKategorie[integer];
						counter++;
					}
                }
                this.ausgabenTabelle(liste);
                this.tabelleSinnvoll(liste);
            }
        });
        this.hBoxKategorie.getChildren().add(this.tabelleKategorie);
        
        // SINNVOLL
        this.tabelleSinnvoll = new TableView();
        this.tabelleSinnvoll.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tabelleSinnvoll.setPrefWidth(Hauptfenster.breite / 3);
        this.tabelleSinnvoll.setPrefHeight(Hauptfenster.höhe / 3);
        TableColumn spalteLeerSinnvoll = new TableColumn("");
        TableColumn spalteAnzahlSinnvoll = new TableColumn("%");
        TableColumn spalteEuroSinnvoll = new TableColumn("Anzahl");
        spalteLeerSinnvoll.setCellValueFactory(new PropertyValueFactory<>("ersteSpalte"));
        spalteAnzahlSinnvoll.setCellValueFactory(new PropertyValueFactory<>("zweiteSpalte"));
        spalteEuroSinnvoll.setCellValueFactory(new PropertyValueFactory<>("dritteSpalte"));
        
        spalteLeerSinnvoll.setSortable(false);
        spalteAnzahlSinnvoll.setSortable(false);
        spalteEuroSinnvoll.setSortable(false);
        
        this.tabelleSinnvoll.getColumns().addAll(spalteLeerSinnvoll, spalteAnzahlSinnvoll, spalteEuroSinnvoll);

        this.tabelleSinnvoll.getSelectionModel().selectedItemProperty().addListener( (obs, oldSelection, newSelection) -> {
            ObservableList<Integer> markierteZeilen = this.tabelleSinnvoll.getSelectionModel().getSelectedIndices();
            if (markierteZeilen.get(0) != 0) {
                ArrayList<Ausgabe>[] liste = new ArrayList[markierteZeilen.size()];
                for (int i = 0; i < markierteZeilen.size(); i++) {
                    liste[i] = this.listeSinnvoll[markierteZeilen.get(i)];
                }
                this.ausgabenTabelle(liste);
            }
        });
        this.hBoxSinnvoll.getChildren().add(this.tabelleSinnvoll);
        
        
        // AUSGABEN
        
        this.tabelleAusgaben = new TableView<>();
        this.tabelleAusgaben.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.tabelleAusgaben.setPrefHeight(this.stage.getScene().getHeight() - this.menubarHöhe - this.getSpacing() - this.tabelleKategorie.getHeight());
        this.tabelleAusgaben.setRowFactory(tv -> {
            TableRow<Ausgabe> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    new AusgabeDialog(this.hauptfenster, row.getItem(), DialogOption.ÄNDERN).showAndWait();
                }
            });
            return row;
        });
        TableColumn verwendungszweckSpalte = new TableColumn("Verwendungszweck");
        TableColumn wertSpalte = new TableColumn("Wert");
        TableColumn amSpalte = new TableColumn("Am");
        TableColumn sinnvollSpalte = new TableColumn("Sinnvoll");
        TableColumn kategorieSpalte = new TableColumn("Kategorie");
        TableColumn kommentarSpalte = new TableColumn("Kommentar");
        
        sinnvollSpalte.setSortable(false);
        kategorieSpalte.setSortable(false);
        kommentarSpalte.setSortable(false);
        
        verwendungszweckSpalte.setCellValueFactory(new PropertyValueFactory<>("verwendungszweck"));
        wertSpalte.setCellValueFactory(new PropertyValueFactory<>("wert"));
        wertSpalte.setCellFactory(getWertCellFactory());
        amSpalte.setCellValueFactory(new PropertyValueFactory<>("datum"));
        amSpalte.setCellFactory(getDatumCellFactory());
        sinnvollSpalte.setCellValueFactory(new PropertyValueFactory<>("sinnvoll"));
        kategorieSpalte.setCellValueFactory(new PropertyValueFactory<>("kategorie"));
        kommentarSpalte.setCellValueFactory(new PropertyValueFactory<>("kommentar"));
        
        this.tabelleAusgaben.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.tabelleAusgaben.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Ausgabe>) c -> {
			ObservableList<Ausgabe> liste;
			if(c.getList().size() == 0) {
				liste = AusgabenAnalyse.this.tabelleAusgaben.getItems();
			} else {
				liste = (ObservableList<Ausgabe>) c.getList();
			}
			double wert = 0;
			for (Ausgabe ausgabe : liste) {
				wert += ausgabe.getWert();
			}
		    AusgabenAnalyse.this.tabelleAusgaben.setTooltip(new Tooltip(Hauptfenster.EUROFORMAT.format(wert)));
		});
        this.tabelleAusgaben.getColumns().addAll(verwendungszweckSpalte, wertSpalte, amSpalte, sinnvollSpalte, kategorieSpalte, kommentarSpalte);
        this.getChildren().add(this.tabelleAusgaben);
    }
    
    private void ausgabenTabelle(ArrayList<Ausgabe>[] liste) {
        final ObservableList<Ausgabe> listeAusgaben;
        if (liste == null) {
            listeAusgaben = getListeAusgaben();
        } else {
            listeAusgaben = FXCollections.observableArrayList();
            for (ArrayList<Ausgabe> liste1 : liste) {
                for (Ausgabe liste11 : liste1) {
                    listeAusgaben.add(liste11);
                }
            }
        }
        this.zeichneTabelleAusgaben(listeAusgaben);
    }

    private void tabelleKategorie() {
        ObservableList<TabellenObjekt> inhaltKategorie = this.datenKategorie();
        this.tabelleKategorie.setItems(inhaltKategorie);
        this.tabelleKategorie.refresh();
    }

    private void tabelleSinnvoll(ArrayList<Ausgabe>[] listeDurchKategorieAusgewählteAusgaben) {
        ObservableList<TabellenObjekt> inhaltKategorie = this.datenSinnvoll(listeDurchKategorieAusgewählteAusgaben);
        this.tabelleSinnvoll.setItems(inhaltKategorie);
        this.tabelleSinnvoll.refresh();
    }
    
    private void zeichneTabelleAusgaben(ObservableList<Ausgabe> listeAusgaben) {
        this.tabelleAusgaben.setItems(listeAusgaben);
        this.tabelleAusgaben.refresh();
        double wert = 0;
		for (Ausgabe ausgabe : listeAusgaben) {
			wert += ausgabe.getWert();
		}
	    AusgabenAnalyse.this.tabelleAusgaben.setTooltip(new Tooltip(Hauptfenster.EUROFORMAT.format(wert)));
    }

    private ObservableList<TabellenObjekt> datenKategorie() {
        this.listeKategorie = this.getKategorieArrayListArray();

        ObservableList<TabellenObjekt> inhaltKategorie = FXCollections.observableArrayList();
        int anzahlKategorieGesamt = 0;
        for (ArrayList<Ausgabe> liste : this.listeKategorie) {
            anzahlKategorieGesamt += liste.size();
        }
        inhaltKategorie.add(new TabellenObjekt(
                "alle",
                getListeAusgaben().size() + "  | 100 %",
                this.formatFürEuroOhneKomma.format((int) this.alleAusgabenWerte) + " | 100 %"));
        for (int i = 1; i < this.listeKategorie.length; i++) {
            double werte = this.getAlleWerteInEuro(this.listeKategorie[i]);
            inhaltKategorie.add(new TabellenObjekt(
                    getLückenloseListeKategorien().get(i).getName(),
                    this.listeKategorie[i].size() + " | " + this.formatFürProzent.format((this.listeKategorie[i].size() * 100 / anzahlKategorieGesamt)) + " %", // Anzahl | %
                    this.formatFürEuroOhneKomma.format((int) werte) + " | " + this.formatFürProzent.format((int) (werte * 100 / this.alleAusgabenWerte)) + " %")); // € | %
        }
        return inhaltKategorie;
    }

    private double getAlleWerteInEuro(ArrayList<Ausgabe> listeAusgaben) {
        double wert = 0;
        for (Ausgabe a : listeAusgaben) {
            wert += a.getWert();
        }
        return wert;
    }

    private ObservableList<TabellenObjekt> datenSinnvoll(ArrayList<Ausgabe>[] listeDurchKategorieAusgewählteAusgaben) {
        this.listeSinnvoll = this.getSinnvollArrayListArray(listeDurchKategorieAusgewählteAusgaben);
        ObservableList<TabellenObjekt> inhaltSinnvoll = FXCollections.observableArrayList();
        int anzahlSinnvollGesamt = 0;
        double alleWerteInKategorie;
        if (this.tabelleKategorie.getSelectionModel().getSelectedIndex() != 0) {
            alleWerteInKategorie = this.getAlleWerteInEuro(this.listeKategorie[this.tabelleKategorie.getSelectionModel().getSelectedIndex()]);
        } else {
            alleWerteInKategorie = this.alleAusgabenWerte;
        }
        for (ArrayList<Ausgabe> liste : this.listeSinnvoll) {
            anzahlSinnvollGesamt += liste.size();
        }
        for (int i = 0; i < this.listeSinnvoll.length; i++) {
            double werte = this.getAlleWerteInEuro(this.listeSinnvoll[i]);
            String spalteZwei = this.listeSinnvoll[i].isEmpty() ? "0 | 0 %" : this.listeSinnvoll[i].size() + " | " + this.formatFürProzent.format((this.listeSinnvoll[i].size() * 100 / anzahlSinnvollGesamt)) + " %";
            inhaltSinnvoll.add(new TabellenObjekt(
                    Sinnvoll.values()[i].toString(),
                    spalteZwei,
                    this.formatFürEuroOhneKomma.format((int) werte) + " | " + this.formatFürProzent.format((int) (werte * 100 / alleWerteInKategorie)) + " %")); // € | %
        }
        return inhaltSinnvoll;
    }

    private ArrayList<Ausgabe>[] getKategorieArrayListArray() {
        this.alleAusgabenWerte = 0;
        ArrayList<Ausgabe>[] liste = new ArrayList[getLückenloseListeKategorien().size()];
        for (Kategorie kategorie : getLückenloseListeKategorien()) {
            liste[kategorie.getIndexDatenbank()] = new ArrayList<>();
        }

        for (Ausgabe ausgabe : getListeAusgaben()) {
            liste[ausgabe.getKategorie().getIndexDatenbank()].add(ausgabe);
            this.alleAusgabenWerte += ausgabe.getWert();
        }
        return liste;
    }

    private ArrayList<Ausgabe>[] getSinnvollArrayListArray(ArrayList<Ausgabe>[] listeDurchKategorieAusgewählteAusgaben) {
        ArrayList<Ausgabe>[] liste = new ArrayList[7];
        for (int i = 0; i < liste.length; i++) {
            liste[i] = new ArrayList<>();
        }
        ArrayList<Ausgabe> ausgabenListe;
        if (listeDurchKategorieAusgewählteAusgaben == null) {
        	ausgabenListe = new ArrayList<>(getListeAusgaben());
        } else {
        	ausgabenListe = new ArrayList<>();
        	for (ArrayList<Ausgabe> arrayList : listeDurchKategorieAusgewählteAusgaben) {
				ausgabenListe.addAll(arrayList);
			}
        }
        
        

        for (Ausgabe ausgabe : ausgabenListe) {
            switch (ausgabe.getSinnvoll()) {
                case LEER: {
                    liste[0].add(ausgabe);
                    break;
                }
                case PFLICHTAUSGABE: {
                    liste[1].add(ausgabe);
                    break;
                }
                case GAR_NICHT: {
                    liste[2].add(ausgabe);
                    break;
                }
                case NICHT_SO: {
                    liste[3].add(ausgabe);
                    break;
                }
                case WEDER_NOCH: {
                    liste[4].add(ausgabe);
                    break;
                }
                case SINNVOLL: {
                    liste[5].add(ausgabe);
                    break;
                }
                case WORTH: {
                    liste[6].add(ausgabe);
                    break;
                }
                default: {
                    Hauptfenster.fehlermeldung("Programmierfehler in AusgabenAnalyse -> getSinnvollArrayListArray -> getSinnvoll lieferte " + ausgabe.getSinnvoll());
                    break;
                }
            }
        }
        return liste;
    }

    private ObservableList<Ausgabe> getListeAusgabenAusSuche() {
        String verwendungszweck = this.tfVerwendungszweck.getText().isEmpty() ? null : this.tfVerwendungszweck.getText();
        Double wertVon;
        Double wertBis;
        try {
            wertVon = this.tfWertVon.getText().isEmpty() ? null : new Double(this.tfWertVon.getText().replace(",", "."));
            wertBis = this.tfWertBis.getText().isEmpty() ? null : new Double(this.tfWertBis.getText().replace(",", "."));
        } catch (NumberFormatException ex) {
            Hauptfenster.fehlermeldung("Falsche Eingabe bei den Feldern für Wert!\n" + ex.getMessage());
            return null;
        }
        Sinnvoll sinnvoll = this.cbSinnvoll.getSelectionModel().getSelectedItem();
        Kategorie kateogrie = this.cbKategorie.getSelectionModel().getSelectedItem();
        String kommentar = this.tfKommentar.getText().isEmpty() ? null : this.tfKommentar.getText();

        final int ANZAHL_EINGABEFELDER = 6;
        boolean[] suchKriterien = new boolean[ANZAHL_EINGABEFELDER]; // 2 Spalten
        // alle die NICHT null sind, also in denen eine Benutzereingabe ist, sind jetzt true!
        suchKriterien[0] = verwendungszweck != null;
        suchKriterien[1] = wertVon != null;
        suchKriterien[2] = wertBis != null;
        suchKriterien[3] = sinnvoll != null;
        suchKriterien[4] = kateogrie != null;
        suchKriterien[5] = kommentar != null;

        ObservableList<Ausgabe> listeAusgaben = Datenbank.getListeAusgaben();
        boolean alleNull = true;
        for (int i = 0; i < ANZAHL_EINGABEFELDER; i++) {
            if (suchKriterien[i] == true) {
                alleNull = false;
                break;
            }
        }
        if (alleNull == true) {
            return listeAusgaben;
        }

        ObservableList<Ausgabe> listeAusgabenSuche = FXCollections.observableArrayList();
        for (Ausgabe ausgabe : listeAusgaben) {
            boolean kriterienGefunden[] = new boolean[ANZAHL_EINGABEFELDER];
            if (verwendungszweck != null && ausgabe.getVerwendungszweck().toLowerCase().contains(verwendungszweck.toLowerCase())) {
                kriterienGefunden[0] = true;
            }
            if (wertVon != null && ausgabe.getWert() >= wertVon) {
                kriterienGefunden[1] = true;
            }
            if (wertBis != null && ausgabe.getWert() <= wertBis) {
                kriterienGefunden[2] = true;
            }
            if (sinnvoll != null && ausgabe.getSinnvoll() == sinnvoll) {
                kriterienGefunden[3] = true;
            }
            if (kateogrie != null && ausgabe.getKategorie() == kateogrie) {
                kriterienGefunden[4] = true;
            }
            if (kommentar != null && ausgabe.getKommentar().toLowerCase().contains(kommentar.toLowerCase())) {
                kriterienGefunden[5] = true;
            }
            boolean eintragHinzufügen = true;
            for (int i = 0; i < ANZAHL_EINGABEFELDER; i++) {
                if (suchKriterien[i] == true && kriterienGefunden[i] == false) {
                    eintragHinzufügen = false;
                    break;
                }
            }
            if (eintragHinzufügen == true) {
                listeAusgabenSuche.add(ausgabe);
            }
        }
        return listeAusgabenSuche;
    }

}
