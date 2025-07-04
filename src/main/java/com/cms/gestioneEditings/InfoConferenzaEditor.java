package com.cms.gestioneEditings;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InfoConferenzaEditor {
    private final Stage stage;
    private final ControlEditings ctrl;
    private final ControlAccount ctrlAccount;
    private final String confId;

    public InfoConferenzaEditor(Stage stage, ControlEditings ctrl, ControlAccount ctrlAccount, String confId) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
        this.confId = confId;
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenza(confId)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + confId));

        Label lbl = new Label("[" + conf.getAcronimo() + "] " + conf.getTitolo());

        // INFO CONFERENZA
        VBox left = new VBox(6,
                new Label("Luogo: " + conf.getLuogo()),
                new Label("Distribuzione: " + conf.getModalitaDistribuzione()),
                new Label("Scadenza Feedback Editore: " + conf.getScadenzaFeedbackEditore())
        );
        left.setPrefWidth(300);

        Label descrLabel = new Label(conf.getDescrizione());
        descrLabel.setWrapText(true);
        ScrollPane descrScroll = new ScrollPane(descrLabel);
        descrScroll.setFitToWidth(true);
        descrScroll.setPrefHeight(120);
        descrScroll.setPrefWidth(400);
        descrScroll.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-border-width: 0; -fx-border-color: transparent;");

        VBox right = new VBox(6, new Label("Descrizione:"), descrScroll);
        right.setPrefWidth(420);

        HBox infoSection = new HBox(20, left, right);
        infoSection.setPadding(new Insets(10));

        // TAB ARTICOLI CAMERA-READY
        TableView<EntityArticolo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EntityArticolo, String> colTitolo = new TableColumn<>("Titolo");
        colTitolo.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitolo()));
        colTitolo.setMinWidth(200);

        TableColumn<EntityArticolo, String> colAut = new TableColumn<>("Autore");
        colAut.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAutoreId()));

        TableColumn<EntityArticolo, String> colFeed = new TableColumn<>("Feedback");
        colFeed.setCellValueFactory(data -> new ReadOnlyStringWrapper(ctrl.hasFeedback(data.getValue().getId()) ? "✔️" : "❌"));
        colFeed.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colTitolo, colAut, colFeed);

        List<EntityArticolo> articoli = ctrl.getCameraReadyArticoli(confId).stream()
                .sorted(Comparator.comparing(EntityArticolo::getTitolo))
                .collect(Collectors.toList());
        table.getItems().addAll(articoli);

        Button btnVisualizza = new Button("Visualizza");
        btnVisualizza.setOnAction(e -> {
            EntityArticolo sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.visualizzaVersioneCameraready(sel.getId());
            } else {
                new PopupAvviso("Seleziona una versione camera-ready").show();
            }
        });

        Button btnFeedback = new Button("Invia Feedback");
        btnFeedback.setOnAction(e -> {
            EntityArticolo sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.inviaFeedback(confId, sel.getId());
                show(); // refresh view
            } else {
                new PopupAvviso("Seleziona una versione camera-ready").show();
            }
        });

        HBox buttonsBox = new HBox(10, btnVisualizza, btnFeedback);

        VBox tableBox = new VBox(8, new Label("Versioni Camera-ready:"), table, buttonsBox);

        Button btnBack = new Button("Indietro");
        btnBack.setOnAction(e -> new HomepageEditor(stage, ctrl, ctrlAccount).show());

        VBox layout = new VBox(10, lbl, infoSection, tableBox, btnBack);
        layout.setPadding(new Insets(10));

        HeaderBar header = new HeaderBar(ctrlAccount, this::show);
        header.getBtnBack().setOnAction(e -> ctrlAccount.apriHomepageEditor());

        VBox root = new VBox(header, layout);

        stage.setScene(new Scene(root, 900, 650));
        stage.setTitle("Dettagli Conferenza - Editor");
        stage.show();
    }
} 