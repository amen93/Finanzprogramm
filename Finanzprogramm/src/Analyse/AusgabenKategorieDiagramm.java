package Analyse;

import java.util.ArrayList;

import Daten.Ausgabe;
import Daten.Datenbank;
import Daten.Kategorie;
import Hauptfenster.Hauptfenster;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class AusgabenKategorieDiagramm {

	private final VBox tabellenBox;
	
	public AusgabenKategorieDiagramm(Hauptfenster hauptfenster) {
		this.tabellenBox = hauptfenster.getTabellenBox();
		this.tabellenBox.getChildren().clear();
		
		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis();
		final BarChart<String, Number> balkendiagramm = new BarChart<>(xAxis, yAxis);
		balkendiagramm.setTitle("Ausgaben pro Kategorie");

		XYChart.Series serie = new XYChart.Series();
		ArrayList<AusgabenDiagramDatenEinheit> listData = new ArrayList<>();
		for (Kategorie kategorie : Datenbank.getListeKategorien()) {
			if(kategorie != null) {
				listData.add(kategorie.getIndexDatenbank(), new AusgabenDiagramDatenEinheit(kategorie.getName()));
			}
		}

		for (Ausgabe ausgabe : Datenbank.getListeAusgaben()) {
			AusgabenDiagramDatenEinheit einheit = listData.get(ausgabe.getKategorie().getIndexDatenbank());
			einheit.addWert(ausgabe.getWert());
			einheit.getListe().add(ausgabe);
		}
		
		listData.sort((o1, o2) -> {
			if(o1.getWertSumme() < o2.getWertSumme()) {
				return 1;
			}
			if(o1.getWertSumme() > o2.getWertSumme()) {
				return -1;
			}
			return 0;
		});
		for (AusgabenDiagramDatenEinheit einheit : listData) {
        	XYChart.Data data = new XYChart.Data(einheit.getName(), einheit.getWertSumme());
			MouseOverNode pane = new MouseOverNode(Hauptfenster.EUROFORMAT.format(einheit.getWertSumme()));
        	pane.setOnMouseClicked(handler -> {
        		try {
					this.tabellenBox.getChildren().remove(1);
        		} catch (IndexOutOfBoundsException ex) {
				}
        		TableView<Ausgabe> tabelle = hauptfenster.getTabelleAusgaben(FXCollections.observableArrayList(einheit.getListe()));
        		tabelle.setPrefHeight(hauptfenster.getStage().getHeight() / 2);
    			this.tabellenBox.getChildren().add(1, tabelle);
        	});
        	data.setNode(pane);
			serie.getData().add(data);
		}

		balkendiagramm.getData().add(serie);
        balkendiagramm.setLegendVisible(false);
		this.tabellenBox.getChildren().add(balkendiagramm);
	}
}
