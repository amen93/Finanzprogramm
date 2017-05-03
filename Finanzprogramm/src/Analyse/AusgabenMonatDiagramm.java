package Analyse;

import org.controlsfx.control.CheckComboBox;
import org.joda.time.DateTime;
import org.joda.time.Months;

import Daten.Ausgabe;
import Daten.Datenbank;
import Daten.Kategorie;
import Hauptfenster.Hauptfenster;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class AusgabenMonatDiagramm {
	
	private final Hauptfenster hauptfenster; 
	private final VBox tabellenBox;
	
	private final CheckComboBox<Kategorie> cbKategorie;
	
	private LineChart<String,Number> lineChart;
	private DateTime ersteAusgabe;
	private ObservableList<String> listeXAchseStrings;
	private final Kategorie kategorieAlle = new Kategorie(-1, "alle");
	final int anzahlMonate;
	
	public AusgabenMonatDiagramm(Hauptfenster hauptfenster) {
		this.hauptfenster = hauptfenster;
		this.tabellenBox = this.hauptfenster.getTabellenBox();
		this.tabellenBox.getChildren().clear();
		this.cbKategorie = new CheckComboBox<>(FXCollections.observableArrayList(Datenbank.getListeKategorien()));
		this.cbKategorie.getItems().add(0, this.kategorieAlle);
		this.cbKategorie.getCheckModel().check(0);
		this.cbKategorie.getCheckModel().getCheckedItems().addListener((ListChangeListener<Kategorie>) c -> AusgabenMonatDiagramm.this.reloadData());
		
        DateTime jetzt = new DateTime(System.currentTimeMillis());
        this.ersteAusgabe = new DateTime(Ausgabe.DATUM_ERSTE_AUSGABE);
        this.ersteAusgabe = this.ersteAusgabe.minusDays(this.ersteAusgabe.getDayOfMonth() - 1);
        this.anzahlMonate = Months.monthsBetween(this.ersteAusgabe, jetzt).getMonths() + 1; //  + 1 für das aktuelle Monat
        AusgabenDiagramDatenEinheit[] listeAlleMonate = new AusgabenDiagramDatenEinheit[this.anzahlMonate];
        DateTime ersteAusgabeKopie = new DateTime(this.ersteAusgabe.getMillis());
        this.listeXAchseStrings = FXCollections.observableArrayList();
        for (int i = 0; i < listeAlleMonate.length; i++) {
        	listeAlleMonate[i] = new AusgabenDiagramDatenEinheit();
        	StringBuilder buffer = new StringBuilder();
        	buffer.append(ersteAusgabeKopie.getMonthOfYear());
        	buffer.append(".");
        	buffer.append(String.valueOf(ersteAusgabeKopie.getYear()).substring(2));
        	this.listeXAchseStrings.add(buffer.toString());
        	ersteAusgabeKopie = ersteAusgabeKopie.plusMonths(1);
		}
        
        final CategoryAxis xAxis = new CategoryAxis(this.listeXAchseStrings);
        final NumberAxis yAxis = new NumberAxis();
        this.lineChart = new LineChart<>(xAxis,yAxis);
        this.lineChart.setTitle("Ausgaben pro Monat");
        this.lineChart.setPrefHeight(this.hauptfenster.getStage().getHeight() / 2);
        
        this.tabellenBox.getChildren().clear();
        this.tabellenBox.getChildren().addAll(this.lineChart, this.cbKategorie);
		this.reloadData();
	}
	
	private AusgabenDiagramDatenEinheit[] createDataList() {
		AusgabenDiagramDatenEinheit[] listeAlleMonate = new AusgabenDiagramDatenEinheit[this.anzahlMonate];
		for (int i = 0; i < listeAlleMonate.length; i++) {
			listeAlleMonate[i] = new AusgabenDiagramDatenEinheit();
		}
		return listeAlleMonate;
	}
	
	private void reloadData() {
    	this.lineChart.getData().clear();
    	ObservableList<Kategorie> list = this.cbKategorie.getCheckModel().getCheckedItems();
    	if(list.isEmpty()) {
    		this.lineChart.getData().clear();
    	} else {
    		for (Kategorie kategorie : this.cbKategorie.getCheckModel().getCheckedItems()) {
				this.lineChart.getData().add(this.berechnetDaten(kategorie));
			}
    	}
	}

	private XYChart.Series berechnetDaten (Kategorie kategorie) {
		ObservableList<Ausgabe> listeAusgaben;
		if(kategorie == this.kategorieAlle) {
			listeAusgaben = Datenbank.getListeAusgaben();
		} else {
			listeAusgaben = Datenbank.getAusgabenByKategorie(kategorie);
		}
        XYChart.Series series = new XYChart.Series();
        AusgabenDiagramDatenEinheit[] listeAlleMonate = this.createDataList(); 
        for (Ausgabe ausgabe : listeAusgaben) {
        	DateTime dateAusgabe = new DateTime(ausgabe.getDatum());
        	int stelleImArray = Months.monthsBetween(this.ersteAusgabe, dateAusgabe).getMonths();
        	listeAlleMonate[stelleImArray].getListe().add(ausgabe);
        	listeAlleMonate[stelleImArray].addWert(ausgabe.getWert());
		}
        
        for (int i = 0; i < listeAlleMonate.length; i++) {
        	final int counter = i;
        	XYChart.Data data = new XYChart.Data(this.listeXAchseStrings.get(i), listeAlleMonate[i].getWertSumme());
        	MouseOverNode pane = new MouseOverNode(Hauptfenster.EUROFORMAT.format(listeAlleMonate[i].getWertSumme()));
        	pane.setOnMouseClicked(handler -> {
        		try {
					this.tabellenBox.getChildren().remove(2);
        		} catch (IndexOutOfBoundsException ex) {
				}
        		TableView<Ausgabe> tabelle = this.hauptfenster.getTabelleAusgaben(FXCollections.observableArrayList(listeAlleMonate[counter].getListe()));
        		tabelle.setPrefHeight(this.hauptfenster.getStage().getHeight() / 2);
    			this.tabellenBox.getChildren().add(2, tabelle);
        	});
        	data.setNode(pane);
        	series.getData().add(data);
        	series.setName(kategorie.getName());
		}
        return series;
	}

}
