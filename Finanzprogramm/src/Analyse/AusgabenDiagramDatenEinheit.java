package Analyse;

import java.util.ArrayList;

import Daten.Ausgabe;

public class AusgabenDiagramDatenEinheit {
	
	private String name;
	private ArrayList<Ausgabe> liste;
	private double wertSumme;
	
	public AusgabenDiagramDatenEinheit(String name) {
		this.liste = new ArrayList<>();
		this.name = name;
	}
	
	public AusgabenDiagramDatenEinheit() {
		this.liste = new ArrayList<>();
	}
	public String getName() {
		return this.name;
	}
	public ArrayList<Ausgabe> getListe() {
		return this.liste;
	}
	public void addWert(final double wert) {
		this.wertSumme += wert;
	}
	public double getWertSumme() {
		return this.wertSumme;
	}
}