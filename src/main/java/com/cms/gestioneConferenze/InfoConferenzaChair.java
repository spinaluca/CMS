package com.cms.gestioneConferenze;

import com.cms.common.PopupInserimento;
import com.cms.common.HeaderBar;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import javafx.animation.FadeTransition;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import com.cms.utils.DownloadUtil;
import com.cms.common.PopupErrore;
import com.cms.common.PopupAvviso;
import com.cms.gestioneRevisioni.RevisioneArticolo;
import com.cms.gestioneRevisioni.ControlRevisioni;
import com.cms.common.BoundaryDBMS;
import com.cms.gestioneRevisioni.InfoRevisioniChair;


public class InfoConferenzaChair {
    private final Stage stage;
    private final ControlConferenze ctrl;
    private final ControlAccount ctrl2;
    private final String confId;

    public InfoConferenzaChair(Stage stage, ControlConferenze ctrl, ControlAccount ctrl2, String confId) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
        this.confId = confId;
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenza(confId)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + confId));
        Label lbl = new Label("[" + conf.getAcronimo() + "] " + conf.getTitolo());
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");

        // INFO CONFERENZA
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
                        .map(email -> email + " | " + ctrl2.getDatiUtente(email)
                                .map(u -> u.getNome() + " " + u.getCognome())
                                .orElse(""))
                        .orElse("<nessuno>"))
        );
        left.setPrefWidth(800);
        left.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;" +
                "-fx-padding: 0;");

        // DESCRIZIONE
        Label descrizione = new Label(conf.getDescrizione());
        descrizione.setWrapText(true);
        descrizione.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");

        ScrollPane descr = new ScrollPane(descrizione);
        descr.setFitToWidth(true);
        descr.setPrefViewportHeight(5000);
        descr.setStyle("-fx-background-color: transparent;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 0;" +
                "-fx-border-width: 0;");
        descr.lookupAll(".viewport").forEach(node ->
                node.setStyle("-fx-background-color: transparent;")
        );

        // PULSANTE AGGIUNGI EDITOR
        Button bEd = new Button("Aggiungi Editor");
        bEd.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.3),4,0,0,2);");

        bEd.setOnAction(e -> new PopupInserimento()
                .promptEmail("editor")
                .ifPresent(email -> {
                    ctrl.aggiungiEditor(email, confId);
                    show();
                }));

        VBox right;
        if (conf.getEditor().isPresent()) {
            right = new VBox(8, new Label("Descrizione:"), descr);
        } else {
            right = new VBox(8, new Label("Descrizione:"), descr, bEd);
        }
        right.setPrefWidth(1000);
        right.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;" +
                "-fx-padding: 0;");

        // PANNELLO UNIFICATO CON INFO E DESCRIZIONE
        HBox infoPanel = new HBox(20, left, right);
        infoPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");
        infoPanel.setPrefWidth(1000);
        infoPanel.setMaxHeight(490);

        HBox infoSection = new HBox(20, infoPanel);
        infoSection.setPadding(new Insets(10));

        // ARTICOLI
        Label articoliLbl = new Label("Articoli:");
        TableView<EntityArticolo> tableArticoli = new TableView<>();
        tableArticoli.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        TableColumn<EntityArticolo, Integer> colPos = new TableColumn<>("Pos.");
        colPos.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPosizione()));

        TableColumn<EntityArticolo, String> colTit = new TableColumn<>("Titolo");
        colTit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitolo()));
        colTit.setMinWidth(200);

        TableColumn<EntityArticolo, String> colAut = new TableColumn<>("Autore");
        colAut.setCellValueFactory(data -> new ReadOnlyStringWrapper(ctrl2.getNomeCompleto(data.getValue().getAutoreId())
                .orElse(data.getValue().getAutoreId())));

        TableColumn<EntityArticolo, Integer> colRev = new TableColumn<>("Revisioni");
        colRev.setCellValueFactory(data -> {
            int numRev = ctrl.getNumRevisioni(data.getValue().getId());
            return new ReadOnlyObjectWrapper<>(numRev);
        });

        TableColumn<EntityArticolo, Double> colScore = new TableColumn<>("Punteggio");
        colScore.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPunteggio()));

        TableColumn<EntityArticolo, String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStato()));

        tableArticoli.getColumns().addAll(colPos, colTit, colAut, colRev, colScore, colStato);
        tableArticoli.getItems().addAll(ctrl.getArticoliConferenza(confId).stream()
                .sorted(Comparator.comparingInt(a -> a.getPosizione() != null ? a.getPosizione() : 0))
                .collect(Collectors.toList()));

        // Stile della tabella come in HomepageChair
        tableArticoli.setFixedCellSize(45);
        tableArticoli.setPrefHeight(5000);
        tableArticoli.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                      "-fx-border-width: 1; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");
        tableArticoli.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // PULSANTI ARTICOLI
        Button btnVisualizzaUltimaVersione = new Button("Visualizza Ultima Versione");
        btnVisualizzaUltimaVersione.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3),4,0,0,2);");

        Button btnRevisiona = new Button("Revisiona");
        btnRevisiona.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.3),4,0,0,2);");

        // Nuovo pulsante Visualizza Stato Revisioni
        Button btnStatoRevisioni = new Button("Visualizza Stato Revisioni");
        btnStatoRevisioni.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3),4,0,0,2);");
        btnStatoRevisioni.setOnAction(e -> {
            BoundaryDBMS db = new BoundaryDBMS();
            ControlRevisioni ctrlRev = new ControlRevisioni(db);
            EntityConferenza conferenza = ctrl.getConferenza(confId)
                    .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + confId));
            new InfoRevisioniChair(stage, ctrlRev, conferenza, this::show).show();
        });

        // Spacer per allineare il pulsante rosso a destra
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Per ora senza azioni definite
        btnVisualizzaUltimaVersione.setOnAction(e -> {
            EntityArticolo row = tableArticoli.getSelectionModel().getSelectedItem();
            if (row != null) {
                ctrl.visualizzaUltimaVersione(row.getId());
            } else {
                new PopupAvviso("Seleziona una revisione").show();
            }
        });

        btnRevisiona.setOnAction(e -> {
            EntityArticolo row = tableArticoli.getSelectionModel().getSelectedItem();
            if (row != null) {
                new RevisioneArticolo(stage, new ControlRevisioni(new BoundaryDBMS()), ctrl2, row.getId(), confId ,true).show();
            } else {
                new PopupAvviso("Seleziona un articolo").show();
            }
        });

        HBox articoliButtons = new HBox(10, btnVisualizzaUltimaVersione, btnRevisiona, spacer, btnStatoRevisioni);

        VBox articoliBox = new VBox(8, articoliLbl, tableArticoli, articoliButtons);
        articoliBox.setPrefWidth(700);
        articoliBox.setStyle(left.getStyle());

        // REVISORI
        Label revisoriLbl = new Label("Revisori:");
        ListView<String> lvRev = new ListView<>();
        ctrl.getRevisoriConStato(confId).forEach((email, stato) -> {
            String nome = ctrl2.getDatiUtente(email)
                    .map(u -> u.getNome() + " " + u.getCognome())
                    .orElse("");
            lvRev.getItems().add(email + (nome.isEmpty() ? "" : " | " + nome) + " [" + stato + "]");
        });

        Button bInv = new Button("Invita Revisore");
        bInv.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");

        Button bRem = new Button("Rimuovi Revisore");
        bRem.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.3),4,0,0,2);");

        bInv.setOnAction(e -> new PopupInserimento()
                .promptEmail("revisore")
                .ifPresent(email -> {
                    ctrl.invitaRevisore(email, confId);
                    show();
                }));

        bRem.setOnAction(e -> {
            String sel = lvRev.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.rimuoviRevisore(sel.split(" ")[0], confId);
                show();
            }
        });

        HBox revButtons = new HBox(10, bInv, bRem);
        // Stile del ListView come la tabella
        lvRev.setFixedCellSize(45);
        lvRev.setPrefHeight(5000);
        lvRev.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                      "-fx-border-width: 1; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

        VBox revisoriBox = new VBox(8, revisoriLbl, lvRev, revButtons);
        revisoriBox.setPrefWidth(350);
        revisoriBox.setStyle(left.getStyle());

        HBox listsBox = new HBox(15, articoliBox, revisoriBox);
        listsBox.setPadding(new Insets(10));

        // BOTTOM BUTTONS
        VBox layout = new VBox(lbl, infoSection, listsBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrl2, this::show);
        header.getBtnBack().setOnAction(e -> ctrl2.apriHomepageChair());

        VBox root = new VBox(header, layout);
        root.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(root, 1050, 850);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dettagli Conferenza");
        stage.show();
    }
}