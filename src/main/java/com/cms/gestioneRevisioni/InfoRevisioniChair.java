package com.cms.gestioneRevisioni;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.common.PopupInserimento;
import com.cms.entity.EntityConferenza;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Pagina che mostra lo stato delle revisioni (vista Chair) – Caso d'uso 4.1.7.3.
 */
public class InfoRevisioniChair {

    private final Stage stage;
    private final ControlRevisioni ctrl;
    private final String confId;
    private final EntityConferenza conferenza;
    private final Runnable onReturn;

    public InfoRevisioniChair(Stage stage, ControlRevisioni ctrl, EntityConferenza conferenza, Runnable onReturn) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.conferenza = conferenza;
        this.confId = conferenza.getId();
        this.onReturn = onReturn;
    }

    public void show() {
        Label title = new Label("Stato Revisioni – " + conferenza.getAcronimo());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        TableView<RevisionRow> table = creaTabella();

        // Pulsanti esterni Visualizza, Rimuovi e Aggiungi sulla stessa linea
        Button btnVisualizza = new Button("Visualizza");
        btnVisualizza.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.3),4,0,0,2);");
        btnVisualizza.setOnAction(e -> {
            RevisionRow row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                ctrl.visualizzaRevisione(row.idReale, row.revisore);
            } else {
                new PopupAvviso("Seleziona una revisione").show();
            }
        });

        Button btnRimuovi = new Button("Rimuovi");
        btnRimuovi.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");
        btnRimuovi.setOnAction(e -> {
            RevisionRow row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                ctrl.rimuoviAssegnazione(row.idReale);
                table.getItems().remove(row);
                new PopupAvviso("Assegnazione rimossa correttamente").show();
            } else {
                new PopupAvviso("Seleziona una riga").show();
            }
        });

        Button btnAggiungi = new Button("Aggiungi Nuova Assegnazione");
        btnAggiungi.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3),4,0,0,2);");
        btnAggiungi.setOnAction(e -> ctrl.avviaAggiungiAssegnazione(confId, this::show));

        // Spacer per allineare il pulsante aggiungi a destra
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox azioniBox = new HBox(10, spacer, btnVisualizza, btnRimuovi, btnAggiungi);

        VBox layout = new VBox(15, title, table, azioniBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(null, onReturn);
        header.getBtnBack().setOnAction(e -> onReturn.run());

        VBox root = new VBox(header, layout);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Stato Revisioni – Chair");
        stage.show();
    }

    private TableView<RevisionRow> creaTabella() {
        TableView<RevisionRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(45);
        table.setPrefHeight(5000);
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

        // Nuova colonna ID Revisione
        TableColumn<RevisionRow, String> colIdReale = new TableColumn<>("ID Revisione");
        colIdReale.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().idReale != null ? data.getValue().idReale : ""));
        colIdReale.setMinWidth(80);

        TableColumn<RevisionRow, String> colTitolo = new TableColumn<>("Articolo");
        colTitolo.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().titolo));
        colTitolo.setMinWidth(200);

        TableColumn<RevisionRow, String> colAutore = new TableColumn<>("Autore");
        colAutore.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().autore));

        TableColumn<RevisionRow, String> colRevisore = new TableColumn<>("Revisore");
        colRevisore.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().revisore));

        TableColumn<RevisionRow, String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().completata ? "✓" : "✗"));

        // Nuove colonne Voto ed Expertise
        TableColumn<RevisionRow, String> colVoto = new TableColumn<>("Voto");
        colVoto.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().voto != null && data.getValue().voto > 0 ? String.valueOf(data.getValue().voto) : ""
        ));

        TableColumn<RevisionRow, String> colExpertise = new TableColumn<>("Expertise");
        colExpertise.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().expertise != null && data.getValue().expertise > 0 ? String.valueOf(data.getValue().expertise) : ""
        ));

        table.getColumns().addAll(colTitolo, colAutore, colRevisore, colStato, colVoto, colExpertise);

        List<RevisionRow> dati = ctrl.getStatoRevisioni(confId);
        ObservableList<RevisionRow> dataObs = FXCollections.observableArrayList(dati);
        table.setItems(dataObs);
        return table;
    }

    // DTO semplice per la tabella
    public static class RevisionRow {
        public final String idRevisione;
        public final String idReale;
        public final String titolo;
        public final String autore;
        public final String revisore;
        public final boolean completata;
        public final Integer voto;
        public final Integer expertise;

        public RevisionRow(String idRevisione, String idReale, String titolo, String autore, String revisore, boolean completata, Integer voto, Integer expertise) {
            this.idRevisione = idRevisione;
            this.idReale = idReale;
            this.titolo = titolo;
            this.autore = autore;
            this.revisore = revisore;
            this.completata = completata;
            this.voto = voto;
            this.expertise = expertise;
        }
    }
} 