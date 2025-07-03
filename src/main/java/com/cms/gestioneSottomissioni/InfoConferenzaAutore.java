package com.cms.gestioneSottomissioni;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;

public class InfoConferenzaAutore {
    private final Stage stage;
    private final ControlSottomissioni ctrl;
    private final String idConferenza;
    private final boolean isIscritto;
    private Label stato;

    public InfoConferenzaAutore(Stage stage, ControlSottomissioni ctrl, String idConferenza, boolean isIscritto) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.idConferenza = idConferenza;
        this.isIscritto = isIscritto;
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenza(idConferenza)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + idConferenza));

        double width = isIscritto ? 760 : 760;
        double height = isIscritto ? 675 : 380;
        double leftWidth = 300;
        double rightWidth = 400;

        Label lbl = new Label("[" + conf.getAcronimo() + "] " + conf.getTitolo());

        VBox infoConf = new VBox(6,
                new Label("Luogo: " + conf.getLuogo()),
                new Label("Distribuzione: " + conf.getModalitaDistribuzione()),
                new Label("Scadenza Sottomissione: " + conf.getScadenzaSottomissione()),
                new Label("Scadenza Revisioni: " + conf.getScadenzaRevisioni()),
                new Label("Data Pubblicazione Graduatoria: " + conf.getDataGraduatoria()),
                new Label("Scadenza Invio Versione Camera-ready: " + conf.getScadenzaCameraReady()),
                new Label("Scadenza Invio Feedback Editore: " + conf.getScadenzaFeedbackEditore()),
                new Label("Scadenza Invio Versione Finale: " + conf.getScadenzaVersioneFinale()),
                new Label("Numero Minimo Revisori: " + conf.getNumeroMinimoRevisori()),
                new Label("Valutazione Minima: " + conf.getValutazioneMinima() +
                        " | Massima: " + conf.getValutazioneMassima()),
                new Label("Numero Vincitori: " + conf.getNumeroVincitori()),
                new Label("Editor: " + conf.getEditor()
                        .map(email -> email + ctrl.getNomeCompleto(email).map(n -> " | " + n).orElse(""))
                        .orElse("<nessuno>"))
        );
        infoConf.setPrefWidth(leftWidth);

        VBox left = infoConf;
        if (isIscritto) {
            EntityArticolo art = ctrl.getDatiArticolo(idConferenza);

            Label titoloArt = new Label("Titolo Articolo: " + (art.getTitolo() != null ? art.getTitolo() : "<non inserito>"));

            Label paroleChiaveLabel = new Label(art.getParoleChiave() != null ? art.getParoleChiave() : "<non inserite>");
            paroleChiaveLabel.setWrapText(true);
            ScrollPane paroleChiave = new ScrollPane(paroleChiaveLabel);
            paroleChiave.setFitToWidth(true);
            paroleChiave.setPrefHeight(75);
            paroleChiave.setPrefWidth(195);
            paroleChiave.setStyle(
                    "-fx-focus-color: transparent;" +
                            "-fx-faint-focus-color: transparent;" +
                            "-fx-background-insets: 0;" +
                            "-fx-padding: 0;" +
                            "-fx-border-width: 0;" +
                            "-fx-border-color: transparent;"
            );

            HBox paroleChiaveBox = new HBox(5, new Label("Parole Chiave:"), paroleChiave);

            stato = new Label("Stato: " + (art.getStato() != null ? art.getStato() : "<non assegnato>"));
            Label posizione = new Label("Posizione graduatoria: " + (art.getPosizione() != null ? art.getPosizione() : "<nessuna>"));
            Label punteggio = new Label("Punteggio: " + (art.getPunteggio() != null ? art.getPunteggio() : "0"));

            VBox articoloBox = new VBox(6,
                    titoloArt,
                    stato,
                    posizione,
                    punteggio,
                    paroleChiaveBox
            );

            VBox fullLeft = new VBox(20, infoConf, articoloBox);
            fullLeft.setPrefWidth(leftWidth);
            left = fullLeft;
        }

        Label descrizione = new Label(conf.getDescrizione());
        descrizione.setWrapText(true);
        ScrollPane descr = new ScrollPane(descrizione);
        descr.setFitToWidth(true);
        descr.setPrefHeight(245);
        descr.setPrefWidth(rightWidth);
        descr.setStyle(
                "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-border-width: 0;" +
                        "-fx-border-color: transparent;"
        );

        VBox right = new VBox(6, new Label("Descrizione:"), descr);
        right.setPrefWidth(rightWidth);

        VBox layoutRight = new VBox(20, right);

        if (isIscritto) {
            Label revisioniLbl = new Label("Revisioni:");
            ListView<String> listRevisioni = new ListView<>();
            ctrl.getRevisioniArticolo(idConferenza).forEach((idRev, desc) ->
                    listRevisioni.getItems().add(desc + " [" + idRev + "]")
            );
            listRevisioni.setPrefHeight(120);

            Button btnViewRev = new Button("Visualizza Revisione");
            btnViewRev.setOnAction(e -> {
                String sel = listRevisioni.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    String idRev = sel.substring(sel.lastIndexOf("[")+1, sel.lastIndexOf("]"));
                    ctrl.visualizzaRevisione(idRev);
                }
            });

            VBox revisoriBox = new VBox(5, revisioniLbl, listRevisioni, btnViewRev);
            revisoriBox.setPrefWidth(rightWidth);
            layoutRight.getChildren().add(revisoriBox);
        }

        HBox infoSection = new HBox(20, left, layoutRight);
        infoSection.setPadding(new Insets(10));

        VBox layout = new VBox(10, lbl, infoSection);
        layout.setPadding(new Insets(10));
        AtomicInteger statoInt = new AtomicInteger();

        if (isIscritto) {
            Button btnSottometti = new Button("Sottometti Articolo");
            Button btnCameraReady = new Button("Invia Versione Camera-ready");
            Button btnFinal = new Button("Invia Versione Finale");

            Button btnVisualizza = new Button("Visualizza Articolo");
            Button btnVisualCamera = new Button("Visualizza Versione Camera-ready");
            Button btnVisualFinal = new Button("Visualizza Versione Finale");
            Button btnFeedback = new Button("Visualizza Feedback Editor");

            // azioni
            btnSottometti.setOnAction(e -> {
                boolean successo = ctrl.sottomettiArticolo(idConferenza);
                if (successo) stato.setText("Stato: Sottomesso");
            });
            btnCameraReady.setOnAction(e -> ctrl.inviaCameraready(idConferenza));
            btnFinal.setOnAction(e -> ctrl.inviaVersioneFinale(idConferenza));

            btnVisualizza.setOnAction(e -> ctrl.visualizzaArticolo(idConferenza));
            btnVisualCamera.setOnAction(e -> ctrl.visualizzaCameraready(idConferenza));
            btnVisualFinal.setOnAction(e -> ctrl.visualizzaVersioneFinale(idConferenza));
            btnFeedback.setOnAction(e -> ctrl.visualizzaFeedback(idConferenza));

            HBox bottoniInvio = new HBox(10, btnSottometti, btnCameraReady, btnFinal);
            bottoniInvio.setPadding(new Insets(10));

            HBox bottoniVisual = new HBox(10, btnVisualizza, btnVisualCamera, btnVisualFinal, btnFeedback);
            bottoniVisual.setPadding(new Insets(10));

            VBox boxBottoni = new VBox(0, bottoniInvio, bottoniVisual);
            layout.getChildren().add(boxBottoni);
        }


        HeaderBar header = new HeaderBar(ctrl.getAccountController(), this::show);
        header.getBtnBack().setOnAction(e -> ctrl.apriHomepageAutore());
        VBox root = new VBox(header, layout);

        stage.setScene(new Scene(root, width, height));
        stage.setTitle("Dettagli Conferenza Autore");
        stage.show();
    }
}