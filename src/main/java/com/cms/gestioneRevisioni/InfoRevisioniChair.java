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

    public InfoRevisioniChair(Stage stage, ControlRevisioni ctrl, EntityConferenza conferenza) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.conferenza = conferenza;
        this.confId = conferenza.getId();
    }

    public void show() {
        Label title = new Label("Stato Revisioni – " + conferenza.getAcronimo());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        TableView<RevisionRow> table = creaTabella();

        Button btnAggiungi = new Button("Aggiungi nuova assegnazione");
        btnAggiungi.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");
        btnAggiungi.setOnAction(e -> ctrl.avviaAggiungiAssegnazione(confId)); // stub

        VBox layout = new VBox(15, title, table, btnAggiungi);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(null, () -> {});
        header.getBtnBack().setOnAction(e -> stage.close());

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

        TableColumn<RevisionRow, String> colTitolo = new TableColumn<>("Articolo");
        colTitolo.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().titolo));
        colTitolo.setMinWidth(200);

        TableColumn<RevisionRow, String> colAutore = new TableColumn<>("Autore");
        colAutore.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().autore));

        TableColumn<RevisionRow, String> colRevisore = new TableColumn<>("Revisore");
        colRevisore.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().revisore));

        TableColumn<RevisionRow, String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().completata ? "✔️" : "❌"));

        TableColumn<RevisionRow, Void> colView = new TableColumn<>("Visualizza");
        colView.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Visualizza");
            {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;" +
                        "-fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: 600; -fx-font-size: 12px;");
                btn.setOnAction(e -> {
                    RevisionRow row = getTableView().getItems().get(getIndex());
                    ctrl.visualizzaRevisioneChair(row.idRevisione)
                            .ifPresentOrElse(ok -> {},
                                    () -> new PopupAvviso("Revisione non disponibile").show());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        TableColumn<RevisionRow, Void> colRemove = new TableColumn<>("Rimuovi");
        colRemove.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Rimuovi");
            {
                btn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-border-color: transparent;" +
                        "-fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: 600; -fx-font-size: 12px;");
                btn.setOnAction(e -> {
                    RevisionRow row = getTableView().getItems().get(getIndex());
                    ctrl.rimuoviAssegnazione(row.idRevisione);
                    getTableView().getItems().remove(row);
                    new PopupAvviso("Assegnazione rimossa correttamente").show();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(colTitolo, colAutore, colRevisore, colStato, colView, colRemove);

        List<RevisionRow> dati = ctrl.getStatoRevisioni(confId);
        ObservableList<RevisionRow> dataObs = FXCollections.observableArrayList(dati);
        table.setItems(dataObs);
        return table;
    }

    // DTO semplice per la tabella
    public static class RevisionRow {
        public final String idRevisione;
        public final String titolo;
        public final String autore;
        public final String revisore;
        public final boolean completata;

        public RevisionRow(String idRevisione, String titolo, String autore, String revisore, boolean completata) {
            this.idRevisione = idRevisione;
            this.titolo = titolo;
            this.autore = autore;
            this.revisore = revisore;
            this.completata = completata;
        }
    }
} 