package com.cms.gestioneConferenze;

import com.cms.common.PopupInserimento;
import com.cms.common.HeaderBar;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.stream.Collectors;

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

        // INFO CONFERENZA
        VBox left = new VBox(6,
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
        left.setPrefWidth(400);

        // DESCRIZIONE
        Label descrizione = new Label(conf.getDescrizione());
        descrizione.setWrapText(true);
        ScrollPane descr = new ScrollPane(descrizione);
        descr.setFitToWidth(true); // fa sì che il testo si adatti alla larghezza
        descr.setPrefViewportHeight(100); // altezza massima visibile
        descr.setPrefWidth(400); // larghezza massima visibile
        descr.setStyle(
                "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-border-width: 0;" +
                        "-fx-border-color: transparent;"
        );

        VBox right = new VBox(6, new Label("Descrizione:"), descr);
        right.setPrefWidth(480);

        HBox infoSection = new HBox(20, left, right);
        infoSection.setPadding(new Insets(10));

        // ARTICOLI
        Label articoliLbl = new Label("Articoli:");
        TableView<EntityArticolo> tableArticoli = new TableView<>();
        tableArticoli.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EntityArticolo, Integer> colPos = new TableColumn<>("Posizione");
        colPos.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPosizione()));

        TableColumn<EntityArticolo, String> colTit = new TableColumn<>("Titolo");
        colTit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitolo()));
        colTit.setMinWidth(200);

        TableColumn<EntityArticolo, String> colAut = new TableColumn<>("Autore");
        colAut.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAutoreId()));

        TableColumn<EntityArticolo, Integer> colRev = new TableColumn<>("Revisioni");
        colRev.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNumRevisioni()));

        TableColumn<EntityArticolo, Double> colScore = new TableColumn<>("Punteggio");
        colScore.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPunteggio()));

        TableColumn<EntityArticolo, String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStato()));
        colStato.setStyle("-fx-alignment: CENTER-RIGHT;");

        tableArticoli.getColumns().addAll(colPos, colTit, colAut, colRev, colScore, colStato);
        tableArticoli.getItems().addAll(ctrl.getArticoliConferenza(confId).stream()
                .sorted(Comparator.comparingInt(EntityArticolo::getPosizione))
                .collect(Collectors.toList()));

        VBox articoliBox = new VBox(5, articoliLbl, tableArticoli);

        // REVISORI
        Label revisoriLbl = new Label("Revisori:");
        ListView<String> lvRev = new ListView<>();
        ctrl.getRevisoriConStato(confId).forEach((email, stato) -> {
            String nome = ctrl.getNomeCompleto(email).orElse("");
            lvRev.getItems().add(email + (nome.isEmpty() ? "" : " | " + nome) + " [" + stato + "]");
        });

        Button bInv = new Button("Invita Revisore");
        bInv.setOnAction(e -> new PopupInserimento()
                .promptEmail("revisore")
                .ifPresent(email -> {
                    ctrl.invitaRevisore(email, confId);
                    show();
                }));

        Button bRem = new Button("Rimuovi Revisore");
        bRem.setOnAction(e -> {
            String sel = lvRev.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.rimuoviRevisore(sel.split(" ")[0], confId); // usa solo l'email
                show();
            }
        });

        HBox revButtons = new HBox(10, bInv, bRem);
        VBox revisoriBox = new VBox(5, revisoriLbl, lvRev, revButtons);
        articoliBox.setPrefWidth(700); // più spazio per la tabella
        revisoriBox.setPrefWidth(350); // meno per i revisori


        // BOTTONE EDITOR + NAV
        Button bEd = new Button("Aggiungi Editor");
        bEd.setDisable(conf.getEditor().isPresent());
        bEd.setOnAction(e -> new PopupInserimento()
                .promptEmail("editor")
                .ifPresent(email -> {
                    ctrl.aggiungiEditor(email, confId);
                    show();
                }));

        Button btnBack = new Button("Indietro");
        btnBack.setOnAction(e -> new HomepageChair(stage, ctrl, ctrl2).show());

        HBox bottomButtons = new HBox(10, btnBack, bEd);

        HBox listsBox = new HBox(15, articoliBox, revisoriBox);
        listsBox.setPadding(new Insets(10));

        VBox layout = new VBox(10, lbl, infoSection, listsBox, bottomButtons);
        layout.setPadding(new Insets(10));

        HeaderBar header = new HeaderBar(ctrl2, this::show);
        header.getBtnBack().setOnAction(e -> ctrl2.apriHomepageChair());

        VBox root = new VBox(header, layout);

        stage.setScene(new Scene(root, 1100, 720));
        stage.setTitle("Dettagli Conferenza");
        stage.show();
    }
}