package com.cms.gestioneEditings;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.gestioneAccount.ControlAccount;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    
    // Helper method to add a detail row with label and value
    public void show() {
        EntityConferenza conf = ctrl.getConferenza(confId)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + confId));

        Label lbl = new Label("[" + conf.getAcronimo() + "] " + conf.getTitolo());
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");

        // INFO CONFERENZA LEFT PANEL
        VBox left = new VBox(8,
                new Label("Luogo: " + conf.getLuogo()),
                new Label("Distribuzione Revisioni: " + conf.getModalitaDistribuzione()),
                new Label("Scadenza Sottomissione: " + conf.getScadenzaSottomissione()),
                new Label("Scadenza Revisioni: " + conf.getScadenzaRevisioni()),
                new Label("Data Pubblicazione Graduatoria: " + conf.getDataGraduatoria()),
                new Label("Scadenza Camera-ready: " + conf.getScadenzaCameraReady()),
                new Label("Scadenza Feedback Editore: " + conf.getScadenzaFeedbackEditore()),
                new Label("Scadenza Versione Finale: " + conf.getScadenzaVersioneFinale()),
                new Label("Numero Minimo Revisori: " + conf.getNumeroMinimoRevisori()),
                new Label("Valutazione Min: " + conf.getValutazioneMinima() +
                        " | Max: " + conf.getValutazioneMassima()),
                new Label("Numero Vincitori: " + conf.getNumeroVincitori()),
                new Label("Editor: " + conf.getEditor()
                        .map(email -> email + " | " + ctrlAccount.getDatiUtente(email)
                                .map(u -> u.getNome() + " " + u.getCognome())
                                .orElse(""))
                        .orElse("<nessuno>"))
        );
        left.setPrefWidth(800);
        left.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;" +
                "-fx-padding: 0;");

        // DESCRIZIONE SCROLL
        Label descrLabel = new Label(conf.getDescrizione());
        descrLabel.setWrapText(true);
        descrLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");

        ScrollPane descrScroll = new ScrollPane(descrLabel);
        descrScroll.setFitToWidth(true);
        descrScroll.setPrefViewportHeight(5000);
        descrScroll.setStyle("-fx-background-color: transparent;" +
                "-fx-focus-color: transparent;" +
                "-fx-faint-focus-color: transparent;" +
                "-fx-background-insets: 0;" +
                "-fx-padding: 0;" +
                "-fx-border-width: 0;");
        descrScroll.lookupAll(".viewport").forEach(node ->
                node.setStyle("-fx-background-color: transparent;")
        );

        VBox right = new VBox(8, new Label("Descrizione:"), descrScroll);
        right.setPrefWidth(1000);
        right.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;" +
                "-fx-padding: 0;");

        HBox infoPanel = new HBox(20, left, right);
        infoPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");
        infoPanel.setPrefWidth(1000);
        infoPanel.setMaxHeight(490);

        HBox infoSection = new HBox(20, infoPanel);
        infoSection.setPadding(new Insets(10));

        // TABELLA CAMERA-READY
        Label tableLabel = new Label("Versioni Camera-ready:");
        TableView<EntityArticolo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(45);
        table.setPrefHeight(5000);
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");

        TableColumn<EntityArticolo, String> colTitolo = new TableColumn<>("Titolo");
        colTitolo.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitolo()));
        colTitolo.setMinWidth(200);

        TableColumn<EntityArticolo, String> colAut = new TableColumn<>("Autore");
        colAut.setCellValueFactory(data -> {
            String email = data.getValue().getAutoreId();
            String label = ctrlAccount.getDatiUtente(email)
                    .map(u -> email + " | " + u.getNome() + " " + u.getCognome())
                    .orElse(email);
            return new ReadOnlyStringWrapper(label);
        });

        TableColumn<EntityArticolo, String> colFeed = new TableColumn<>("Feedback");
        colFeed.setCellValueFactory(data -> new ReadOnlyStringWrapper(ctrl.hasFeedback(data.getValue().getId()) ? "✔️" : "❌"));
        colFeed.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colTitolo, colAut, colFeed);

        List<EntityArticolo> articoli = ctrl.getCameraReadyArticoli(confId).stream()
                .sorted(Comparator.comparing(EntityArticolo::getTitolo))
                .collect(Collectors.toList());
        table.getItems().addAll(articoli);

        // BUTTONS
        Button btnVisualizza = new Button("Visualizza Versione Camera-ready");
        btnVisualizza.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3),4,0,0,2);");

        Button btnFeedback = new Button("Invia Feedback");
        btnFeedback.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");

        btnVisualizza.setOnAction(e -> {
            EntityArticolo sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.visualizzaVersioneCameraready(sel.getId());
            } else {
                new PopupAvviso("Seleziona una versione camera-ready").show();
            }
        });

        btnFeedback.setOnAction(e -> {
            EntityArticolo sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.inviaFeedback(confId, sel.getId());
                show(); // refresh
            } else {
                new PopupAvviso("Seleziona una versione camera-ready").show();
            }
        });

        HBox buttonsBox = new HBox(10, btnVisualizza, btnFeedback);

        VBox tableBox = new VBox(8, tableLabel, table, buttonsBox);
        tableBox.setPrefWidth(1000);
        tableBox.setPadding(new Insets(10));

        VBox layout = new VBox(lbl, infoSection, tableBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrlAccount, this::show);
        header.getBtnBack().setOnAction(e -> ctrlAccount.apriHomepageEditor());

        VBox root = new VBox(header, layout);
        root.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(root, 1050, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dettagli Conferenza - Editor");
        stage.show();
    }
}