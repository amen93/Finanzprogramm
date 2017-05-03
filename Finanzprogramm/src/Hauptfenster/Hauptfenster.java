package Hauptfenster;

import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import Analyse.AusgabenAnalyse;
import Analyse.AusgabenKategorieDiagramm;
import Analyse.AusgabenMonatDiagramm;
import Daten.Ausgabe;
import Daten.Datenbank;
import Daten.Fixkosten;
import Daten.Kategorie;
import Daten.Sinnvoll;
import Dialog.AusgabeDialog;
import Dialog.DialogOption;
import Dialog.FixkostenDialog;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * @author Quirin Ertl
 */
public class Hauptfenster extends Application {

    public static void main(String args[]) throws InterruptedException {
        Hauptfenster.launch(args);
    }

    public static final SimpleDateFormat DATUMFORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final DecimalFormat EUROFORMAT = new DecimalFormat("###,###.00");

    static {
        EUROFORMAT.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.GERMAN));
        EUROFORMAT.setPositiveSuffix(" €");
    }
    
    public static final int breite = 1050, höhe = 700;
    private TableView akutelleTabelle;
    private Thread thread;
    private Scene scene;
    private Stage stage;
    private VBox rootbox;
    private VBox tabellenBox;
    private MenuItem miUnfertigKategorie;
    private MenuItem miUnfertigSinnvoll;
    private MenuBar menuBar;
    private Menu menuHinzufügen;
    
    @Override
    public void start(Stage stage) throws Exception {
    	new AutoCompletePopup<>();
        this.stage = stage;
        while(new LoginDialog().showAndWait().get() == false){
        }

        this.thread = new Thread(() -> {
            try {
                new Datenbank(new File("C:\\Users\\Quirin\\Documents\\Finanzprogramm\\Finanzdatenbank.db"));
            } catch (SQLException ex) {
                fehlermeldung(ex);
            }
        });
        this.thread.start();
        stage.setScene(this.zeichneHauptfenster(stage));

        stage.show();
        
        this.unfertigZählen();
        this.fixkostenPrüfen();
    }
    
    public void fixkostenPrüfen() {
        ArrayList<Fixkosten> listeAnfallenderFixkosten = new ArrayList<>();
        for (Fixkosten fixkosten : Datenbank.getListeFixkosten()) {
            if (System.currentTimeMillis() > fixkosten.getDatum()) {
                listeAnfallenderFixkosten.add(fixkosten);
            }
        }
        if (listeAnfallenderFixkosten.size() > 0) {
        	MenuItem miFixkostenHinzufügen = new MenuItem("Angefallene Fixkosten (" + listeAnfallenderFixkosten.size() + ")");
        	if(this.menuHinzufügen.getItems().size() == 4) {
        		this.menuHinzufügen.getItems().set(3, miFixkostenHinzufügen);
        	} else {
        		this.menuHinzufügen.getItems().add(miFixkostenHinzufügen);        		
        	}
        	miFixkostenHinzufügen.setOnAction(event -> {
        		for(Fixkosten fixkosten : listeAnfallenderFixkosten) {
        			if(new AusgabeDialog(this, fixkosten, DialogOption.FIXKOSTEN).showAndWait().get()) {
        				try {
							fixkosten.fixkostenAktualisieren();
						} catch (SQLException e) {
							fehlermeldung(e);
						}
        			}
        		}
        	});
        } else {
        	if(this.menuHinzufügen.getItems().size() == 4) {
        		this.menuHinzufügen.getItems().remove(3);
        	}
        }
    }

    private Scene zeichneHauptfenster(Stage stage) {
        stage.setTitle("Finanzprogramm");
        stage.getIcons().add(new Image(this.getClass().getResource("/Icons/Programm-Icon-Klein.png").toString()));
        this.rootbox = new VBox();
        this.tabellenBox = new VBox();
        this.scene = new Scene(this.rootbox, breite, höhe);
        this.menuHinzufügen = new Menu("Hinzufügen");
        this.rootbox.getChildren().addAll(this.zeichneMenübar(), this.tabellenBox);
        
        try {
            this.thread.join();
        } catch (InterruptedException ex) {
            fehlermeldung(ex);
        }
        this.zeichneTabelleAusgaben(Datenbank.getListeAusgaben());
        return this.scene;
    }

    private void zeichneTabelleKategorie(ObservableList<Kategorie> listeKategorien) {
    	this.tabellenBox.getChildren().clear();
        TableView<Kategorie> tabelleKategorie = new TableView<>();
        tabelleKategorie.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelleKategorie.setPrefHeight(höhe);
        this.scene.heightProperty().addListener(e -> {
            tabelleKategorie.setPrefHeight(this.scene.getHeight());
        });
        tabelleKategorie.setRowFactory(tv -> {
            return this.tabelleKategorieMouseKlicks(tabelleKategorie);
        });

        TableColumn nameSpalte = new TableColumn("Name");
        nameSpalte.setCellValueFactory(new PropertyValueFactory<>("name"));

        tabelleKategorie.setItems(FXCollections.observableArrayList(listeKategorien));
        tabelleKategorie.getColumns().add(nameSpalte);
        tabelleKategorie.autosize();
        this.akutelleTabelle = tabelleKategorie;
        this.tabellenBox.getChildren().add(tabelleKategorie);
    }

    private void zeichneTabelleAusgaben(ObservableList<Ausgabe> listeAusgaben) {
    	this.tabellenBox.getChildren().clear();
    	TableView<Ausgabe> tabelleAusgaben = this.getTabelleAusgaben(listeAusgaben);
        this.akutelleTabelle = tabelleAusgaben;
        this.tabellenBox.getChildren().add(tabelleAusgaben);
    }
    
    public TableView<Ausgabe> getTabelleAusgaben(ObservableList<Ausgabe> listeAusgaben) {
        TableView<Ausgabe> tabelleAusgaben = new TableView<>();
        tabelleAusgaben.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelleAusgaben.setPrefHeight(höhe);
        this.scene.heightProperty().addListener(e -> {
            tabelleAusgaben.setPrefHeight(this.scene.getHeight());
        });
        tabelleAusgaben.setRowFactory(tv -> {
            TableRow<Ausgabe> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    new AusgabeDialog(this, row.getItem(), DialogOption.ÄNDERN).showAndWait();
                    tabelleAusgaben.refresh();
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

        verwendungszweckSpalte.setCellValueFactory(new PropertyValueFactory<>("verwendungszweck"));
        wertSpalte.setCellValueFactory(new PropertyValueFactory<>("wert"));
        wertSpalte.setCellFactory(getWertCellFactory());
        amSpalte.setCellValueFactory(new PropertyValueFactory<>("datum"));
        amSpalte.setCellFactory(getDatumCellFactory());
        sinnvollSpalte.setCellValueFactory(new PropertyValueFactory<>("sinnvoll"));
        kategorieSpalte.setCellValueFactory(new PropertyValueFactory<>("kategorie"));
        kommentarSpalte.setCellValueFactory(new PropertyValueFactory<>("kommentar"));

        tabelleAusgaben.setItems(listeAusgaben);
        tabelleAusgaben.getColumns().addAll(verwendungszweckSpalte, wertSpalte, amSpalte, sinnvollSpalte, kategorieSpalte, kommentarSpalte);
        tabelleAusgaben.autosize();
		return tabelleAusgaben;
    }

    private void zeichneTabelleFixkosten(ObservableList<Fixkosten> listeFixkosten) {
    	this.tabellenBox.getChildren().clear();
        TableView<Fixkosten> tabelleFixkosten = new TableView<>();
        tabelleFixkosten.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelleFixkosten.setPrefHeight(höhe);
        this.scene.heightProperty().addListener(e -> {
            tabelleFixkosten.setPrefHeight(this.scene.getHeight());
        });
        tabelleFixkosten.setRowFactory(tv -> {
            TableRow<Fixkosten> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    new FixkostenDialog(this, row.getItem()).showAndWait();
                    tabelleFixkosten.refresh();
                }
            });
            return row;
        });
        TableColumn<Fixkosten, String> verwendungszweckSpalte = new TableColumn("Verwendungszweck");
        TableColumn wertSpalte = new TableColumn("Wert");
        TableColumn sinnvollSpalte = new TableColumn("Sinnvoll");
        TableColumn kategorieSpalte = new TableColumn("Kategorie");
        TableColumn terminSpalte = new TableColumn("Termin");
        TableColumn kommentarSpalte = new TableColumn("Kommentar");

        verwendungszweckSpalte.setCellValueFactory(new PropertyValueFactory<>("verwendungszweck"));
        wertSpalte.setCellValueFactory(new PropertyValueFactory<>("wert"));
        wertSpalte.setCellFactory(getWertCellFactory());
        sinnvollSpalte.setCellValueFactory(new PropertyValueFactory<>("sinnvoll"));
        kategorieSpalte.setCellValueFactory(new PropertyValueFactory<>("kategorie"));
        terminSpalte.setCellValueFactory(new PropertyValueFactory<>("datum"));
        terminSpalte.setCellFactory(getDatumCellFactory());
        kommentarSpalte.setCellValueFactory(new PropertyValueFactory<>("kommentar"));

        tabelleFixkosten.setItems(FXCollections.observableArrayList(listeFixkosten));
        tabelleFixkosten.getColumns().addAll(verwendungszweckSpalte, wertSpalte, sinnvollSpalte, terminSpalte, kategorieSpalte, kommentarSpalte);
        tabelleFixkosten.autosize();
        this.akutelleTabelle = tabelleFixkosten;
        this.tabellenBox.getChildren().add(tabelleFixkosten);
    }

    private MenuBar zeichneMenübar() {

        // Menü Tabellen
        Menu menuTabellen = new Menu("Tabellen");
        MenuItem miTabelleAusgaben = new MenuItem("Ausgaben");
        miTabelleAusgaben.setOnAction(e -> {
            this.zeichneTabelleAusgaben(Datenbank.getListeAusgaben());
        });
        MenuItem miTabelleFixkosten = new MenuItem("Fixkosten");
        miTabelleFixkosten.setOnAction(e -> {
            this.zeichneTabelleFixkosten(Datenbank.getListeFixkosten());
        });
        MenuItem miTabelleKategorie = new MenuItem("Kategorie");
        miTabelleKategorie.setOnAction(e -> {
            this.zeichneTabelleKategorie(Datenbank.getListeKategorien());
        });
        menuTabellen.getItems().addAll(miTabelleAusgaben, miTabelleFixkosten, miTabelleKategorie);

        // Menü Hinzufügen
        this.menuHinzufügen = new Menu("Hinzufügen");
        MenuItem miHinzufügenAusgaben = new MenuItem("Ausgabe");
        miHinzufügenAusgaben.setStyle("-fx-text-fill: red");
        miHinzufügenAusgaben.setOnAction(e -> {
            AusgabeDialog dialog = new AusgabeDialog(this, null, DialogOption.HINZUFÜGEN);
            dialog.showAndWait();
        });
        MenuItem miHinzufügenFixkosten = new MenuItem("Fixkosten");
        miHinzufügenFixkosten.setOnAction(e -> {
            FixkostenDialog dialog = new FixkostenDialog(this, null);
            dialog.showAndWait();
        });
        MenuItem miHinzufügenKategorie = new MenuItem("Kategorie");
        miHinzufügenKategorie.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.initOwner(this.getStage());
            dialog.setTitle("Neue Kategorie");
            dialog.setHeaderText("Neue Kategorie");
            dialog.setContentText("Name der neue Kategorie:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    Kategorie.kategorieHinzufügen(result.get());
                } catch (SQLException ex) {
                    fehlermeldung(ex);
                }
            }
        });
        this.menuHinzufügen.getItems().addAll(miHinzufügenAusgaben, miHinzufügenFixkosten, miHinzufügenKategorie);

        // Menü Unfertig
        Menu menuUnfertig = new Menu("Unfertige Ausgaben");
        this.miUnfertigKategorie = new MenuItem("Sinnvoll undefiniert ()");
        this.miUnfertigKategorie.setOnAction(e -> {
            ObservableList<Ausgabe> liste = FXCollections.observableArrayList();
            Kategorie fragezeichen = Datenbank.getListeKategorien().get(Kategorie.INDEX_FRAGEZEICHEN_KATEGORIE);
            for (Ausgabe ausgabe : Datenbank.getListeAusgaben()) {
                if(fragezeichen == ausgabe.getKategorie()) {
                    liste.add(ausgabe);
                }
            }
            this.zeichneTabelleAusgaben(liste);
        });
        this.miUnfertigSinnvoll = new MenuItem("Kategorie undefiniert ()");
        this.miUnfertigSinnvoll.setOnAction(e -> {
            ObservableList<Ausgabe> liste = FXCollections.observableArrayList();
            for (Ausgabe ausgabe : Datenbank.getListeAusgaben()) {
                if(Sinnvoll.LEER == ausgabe.getSinnvoll()) {
                    liste.add(ausgabe);
                }
            }
            this.zeichneTabelleAusgaben(liste);
        });
        menuUnfertig.getItems().addAll(this.miUnfertigKategorie, this.miUnfertigSinnvoll);

        // Menü Analyse
        Menu menuAnalyse = new Menu("Analyse");
        MenuItem miAnalyseAusgaben = new MenuItem("Ausgaben");
        miAnalyseAusgaben.setStyle("-fx-text-fill: red");
        miAnalyseAusgaben.setOnAction(e -> {
        	this.tabellenBox.getChildren().clear();
        	this.tabellenBox.getChildren().add(new AusgabenAnalyse(this, this.menuBar.getHeight()));
        });
        MenuItem miAusgabenDiagramm = new MenuItem("Ausgaben pro Monat");
        miAusgabenDiagramm.setOnAction(e -> {
        	new AusgabenMonatDiagramm(this);
        });
        MenuItem miKategorieDiagramm = new MenuItem("Ausgaben pro Kategorie");
        miKategorieDiagramm.setOnAction(e -> {
        	new AusgabenKategorieDiagramm(this);
        });
        
        menuAnalyse.getItems().addAll(miAnalyseAusgaben, miAusgabenDiagramm, miKategorieDiagramm);

        // Menüleiste
        this.menuBar = new MenuBar(menuTabellen, this.menuHinzufügen, menuUnfertig, menuAnalyse);

        return this.menuBar;
    }  
    
    private TableRow<Kategorie> tabelleKategorieMouseKlicks(TableView<Kategorie> tabelleKategorie) {
        TableRow<Kategorie> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && (!row.isEmpty())) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Kategorie-Name ändern");
                dialog.setHeaderText("Name der Kategorie " + tabelleKategorie.getSelectionModel().getSelectedItem().getName() + " ändern?");
                dialog.setContentText("Neuer Name der Kategorie:");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        row.getItem().kategorieÄndern(result.get());
                    } catch (SQLException ex) {
                        fehlermeldung(ex);
                    }
                }
            }
        });
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem removeMenuItem = new MenuItem("Löschen");
        removeMenuItem.setOnAction((ActionEvent event) -> {
            try {
                Kategorie kategorie = tabelleKategorie.getSelectionModel().getSelectedItem();
                Alert alert = new Alert(Alert.AlertType.WARNING, "Die Kategorie " + kategorie.getName() + " wirklich löschen?", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == ButtonType.YES) {
                        kategorie.kategorieLöschen();
                        this.unfertigZählen();
                    }
                }
            } catch (SQLException ex) {
                fehlermeldung(ex);
            }
        });
        contextMenu.getItems().add(removeMenuItem);
        // Set context menu on row, but use a binding to make it only show for non-empty rows:
        row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                .then((ContextMenu) null)
                .otherwise(contextMenu)
        );
        return row;
    }

    public static Callback<TableColumn<Ausgabe, Double>, TableCell<Ausgabe, Double>> getWertCellFactory() {
        return (TableColumn<Ausgabe, Double> param) -> {
            TableCell<Ausgabe, Double> cell = new TableCell<Ausgabe, Double>() {

                @Override
                public void updateItem(Double item, boolean empty) {
                    if (item != null) {
                        this.setText(EUROFORMAT.format(item));
                    }
                }
            };
            return cell;
        };
    }

    public static Callback<TableColumn<Ausgabe, Long>, TableCell<Ausgabe, Long>> getDatumCellFactory() {
        return (TableColumn<Ausgabe, Long> param) -> {
            TableCell<Ausgabe, Long> cell = new TableCell<Ausgabe, Long>() {

                @Override
                public void updateItem(Long item, boolean empty) {
                    if (item != null) {
                        this.setText(DATUMFORMAT.format(item));
                    }
                }
            };
            return cell;
        };
    }
    
	/**
	 * Vielleicht einfach in den AusgabneDialog -> closeListener oder so ODER
	 *      -> in die Ausgabe-Klasse
	 */
    public void unfertigZählen() {
        int counterKategorie = 0, counterSinnvoll = 0;
        Kategorie fragezeichen = Datenbank.getListeKategorien().get(Kategorie.INDEX_FRAGEZEICHEN_KATEGORIE);
        Sinnvoll leer = Sinnvoll.LEER;
        for (Ausgabe ausgabe : Datenbank.getListeAusgaben()) {
            if (fragezeichen == ausgabe.getKategorie()) {
                counterKategorie++;
            }
            if (leer == ausgabe.getSinnvoll()) {
                counterSinnvoll++;
            }
        }
        this.miUnfertigSinnvoll.setText("Sinnvoll undefiniert (" + counterSinnvoll +")");
        this.miUnfertigKategorie.setText("Kategorie undefiniert (" + counterKategorie +")");
    }

    public static void fehlermeldung(Object ex) {
        new Alert(Alert.AlertType.ERROR, ex.toString(), ButtonType.OK).showAndWait();
    }
    
    public Stage getStage() {
		return this.stage;
	}
    
    public VBox getTabellenBox() {
		return this.tabellenBox;
	}
}
