package com.cms.gestioneRevisioni;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityArticolo;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoConferenzaRevisore {
    private final Stage stage;
    private final ControlRevisioni ctrl;
    private final ControlAccount ctrl2;
    private final String confId;

    public InfoConferenzaRevisore(Stage stage, ControlRevisioni ctrl, ControlAccount ctrl2, String confId) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
        this.confId = confId;
    }

    private String getEmailRevisore() {
        return ctrl2.getUtenteCorrente().getEmail();
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenzaRevisore(confId, getEmailRevisore())
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata"));

        Label lbl = new Label("[" + conf.getAcronimo() + "] " + conf.getTitolo());
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");

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
                new Label("Valutazione Min: " + conf.getValutazioneMinima() + " | Max: " + conf.getValutazioneMassima()),
                new Label("Numero Vincitori: " + conf.getNumeroVincitori())
        );
        left.setPrefWidth(800);

        Label descrizione = new Label(conf.getDescrizione());
        descrizione.setWrapText(true);
        descrizione.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");
        descrizione.setMaxHeight(5000);
        ScrollPane descr = new ScrollPane(descrizione);
        descr.setFitToWidth(true);
        descr.setStyle("-fx-background-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0;");
        descr.lookupAll(".viewport").forEach(node -> node.setStyle("-fx-background-color: transparent;"));
        descr.setMaxHeight(5000);

        VBox right = new VBox(8, new Label("Descrizione:"), descr);
        right.setPrefWidth(1000);
        right.setMaxHeight(5000);
        VBox.setVgrow(descr, Priority.ALWAYS);

        HBox infoPanel = new HBox(20, left, right);
        infoPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");
        infoPanel.setPrefWidth(1000);
        infoPanel.setMaxHeight(1000);

        HBox infoSection = new HBox(20, infoPanel);
        infoSection.setPadding(new Insets(10));

        Label articoliLbl = new Label("Articoli da revisionare/revisionati:");
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> colTit = new TableColumn<>("Titolo");
        colTit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()[0]));
        colTit.setMinWidth(200);

        TableColumn<String[], String> colAut = new TableColumn<>("Autore");
        colAut.setCellValueFactory(data -> {
            String autoreId = data.getValue()[1];
            String nomeCompleto = ctrl2.getNomeCompleto(autoreId).orElse(autoreId);
            return new ReadOnlyStringWrapper(nomeCompleto);
        });
        colAut.setMinWidth(200);

        TableColumn<String[], String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(data -> {
            String votoStr = data.getValue()[2];
            int voto = 0;
            if (votoStr != null && !votoStr.equalsIgnoreCase("null") && !votoStr.isEmpty()) {
                try { voto = Integer.parseInt(votoStr); } catch (NumberFormatException ignored) {}
            }
            String stato = voto > 0 ? "Revisionato" : "Da revisionare";
            return new ReadOnlyStringWrapper(stato);
        });

        TableColumn<String[], String> colVoto = new TableColumn<>("Voto");
        colVoto.setCellValueFactory(data -> {
            String votoStr = data.getValue()[2];
            return new ReadOnlyStringWrapper(
                    (votoStr == null || votoStr.equalsIgnoreCase("null") || votoStr.equals("0")) ? "" : votoStr
            );
        });

        TableColumn<String[], String> colExp = new TableColumn<>("Expertise");
        colExp.setCellValueFactory(data -> {
            String expStr = data.getValue()[3];
            return new ReadOnlyStringWrapper(
                    (expStr == null || expStr.equalsIgnoreCase("null") || expStr.equals("0")) ? "" : expStr
            );
        });

        table.getColumns().addAll(colTit, colAut, colStato, colVoto, colExp);

        // Mappa ausiliaria con chiave combinata titolo + autore
        Map<String, String> titoloAutoreToIdArt = new HashMap<>();

        List<String> idArticoli = ctrl.getArticoliRevisore(confId, getEmailRevisore());
        for (String idArt : idArticoli) {
            EntityArticolo art = ctrl.getArticoloById(idArt).orElse(null);
            if (art == null) continue;
            String titolo = art.getTitolo();
            String autore = ctrl2.getNomeCompleto(art.getAutoreId()).orElse(art.getAutoreId());
            String votoStr = ctrl.getVotoRevisione(idArt, getEmailRevisore()).map(Object::toString).orElse("");
            String expStr = ctrl.getExpertiseRevisione(idArt, getEmailRevisore()).map(Object::toString).orElse("");
            table.getItems().add(new String[]{titolo, autore, votoStr, expStr});
            titoloAutoreToIdArt.put(titolo + "||" + autore, idArt); // salva mappatura combinata
        }

        table.setFixedCellSize(45);
        table.setPrefHeight(5000);
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

        Button btnAggiungi = new Button("Aggiungi articolo da revisionare");
        btnAggiungi.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");
        btnAggiungi.setOnAction(e -> {
            ctrl.aggiungiArticoloRevisore(confId, getEmailRevisore());
            show();
        });

        Button btnDelega = new Button("Delega sotto-revisore");
        btnDelega.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.3),4,0,0,2);");
        btnDelega.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                String titolo = sel[0];
                ctrl.delegaSottoRevisore(confId, titolo, getEmailRevisore());
                show();
            }
        });

        Button btnRevisiona = new Button("Revisiona");
        btnRevisiona.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.3),4,0,0,2);");
        btnRevisiona.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                String titolo = sel[0];
                String autore = sel[1];
                String idArt = titoloAutoreToIdArt.get(titolo + "||" + autore);
                new RevisioneArticolo(stage, ctrl, ctrl2, idArt, confId, false).show();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox articoliButtons = new HBox(10, spacer, btnDelega, btnAggiungi, btnRevisiona);

        VBox articoliBox = new VBox(8, articoliLbl, table);
        articoliBox.getChildren().add(articoliButtons);
        articoliBox.setPrefWidth(1000);
        articoliBox.setPadding(new Insets(10));

        VBox layout = new VBox(lbl, infoSection, articoliBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrl2, this::show);
        header.getBtnBack().setOnAction(e -> ctrl2.apriHomepageRevisore());

        VBox root = new VBox(header, layout);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dettagli Conferenza – Revisore");
        stage.show();
    }
}