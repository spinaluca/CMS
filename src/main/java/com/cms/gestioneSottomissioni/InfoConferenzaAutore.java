package com.cms.gestioneSottomissioni;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.layout.Priority;
import java.util.Map;

public class InfoConferenzaAutore {
    private final Stage stage;
    private final ControlSottomissioni ctrl;
    private final ControlAccount ctrl2;
    private final String idConferenza;
    private final boolean isIscritto;
    private Label stato;

    public InfoConferenzaAutore(Stage stage, ControlSottomissioni ctrl, ControlAccount ctrl2, String idConferenza, boolean isIscritto) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
        this.idConferenza = idConferenza;
        this.isIscritto = isIscritto;
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenza(idConferenza)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + idConferenza));

        // === BLOCCO SUPERIORE UGUALE A InfoConferenzaChair ===
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
                        .map(email -> email + " | " + ctrl.getNomeCompleto(email).orElse(""))
                        .orElse("<nessuno>"))
        );
        left.setPrefWidth(800);
        left.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;" +
                "-fx-padding: 0;");

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

        VBox right = new VBox(8, new Label("Descrizione:"), descr);
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

        Label lbl = new Label("[" + conf.getAcronimo() + "] " + conf.getTitolo());
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");

        VBox layout = new VBox(0, lbl, infoSection);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        // === BLOCCO INFERIORE: A SINISTRA info articolo, A DESTRA tabella revisioni ===
        if (isIscritto) {
            String idArticolo = ctrl.getArticoloId(idConferenza, ctrl2.getUtenteCorrente().getEmail());
            EntityArticolo art = ctrl.getDatiArticolo(idArticolo);
            Label titoloArt = new Label("Titolo: " + (art.getTitolo() != null ? art.getTitolo() : "<non inserito>"));
            stato = new Label("Stato: " + (art.getStato() != null ? art.getStato() : "<non assegnato>"));
            Label posizione = new Label("Posizione: " + (art.getPosizione() != null ? art.getPosizione() : "<nessuna>"));
            Label punteggio = new Label("Punteggio: " + (art.getPunteggio() != null ? art.getPunteggio() : "0"));

            Label paroleChiaveLabel = new Label(art.getParoleChiave() != null ? art.getParoleChiave() : "<non inserite>");
            paroleChiaveLabel.setWrapText(true);

            ScrollPane paroleChiave = new ScrollPane(paroleChiaveLabel);
            paroleChiave.setFitToWidth(true);
            paroleChiave.setPrefHeight(75);
            paroleChiave.setStyle("-fx-background-color: transparent;-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;-fx-background-insets: 0;-fx-padding: 0;-fx-border-width: 0;");

            // Label sopra il riquadro dati articolo
            Label datiArticoloLabel = new Label("Dati articolo:");
            // === BLOCCO DATI ARTICOLO ===
            VBox articoloBox = new VBox(8,
                titoloArt, stato, posizione, punteggio, new Label("Parole Chiave:"), paroleChiave
            );
            articoloBox.setPrefWidth(400);
            articoloBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                    "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");

            // === TABELLA DELLE REVISIONI ===
            TableView<String[]> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<String[], String> colRevisore = new TableColumn<>("Revisore");
            colRevisore.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()[0]));
            colRevisore.setMinWidth(200);

            TableColumn<String[], String> colVoto = new TableColumn<>("Voto");
            colVoto.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()[1]));

            TableColumn<String[], String> colExpertise = new TableColumn<>("Expertise");
            colExpertise.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()[2]));

            table.getColumns().setAll(colRevisore, colVoto, colExpertise);

            // Popolamento tabella: uso ctrl.getRevisioniArticoloById(art.getId())
            Map<String, String> revisioni = ctrl.getRevisioniArticoloById(art.getId());
            if (revisioni != null) {
                for (Map.Entry<String, String> entry : revisioni.entrySet()) {
                    String descrizioneRev = entry.getValue(); // "Revisore: X - Voto: Y - Expertise: Z"
                    String revisore = "<non assegnato>";
                    String voto = "-";
                    String expertise = "-";
                    int revIdx = descrizioneRev.indexOf("Revisore: ");
                    int votoIdx = descrizioneRev.indexOf("- Voto: ");
                    int expIdx = descrizioneRev.indexOf("- Expertise: ");
                    if (revIdx != -1 && votoIdx != -1 && expIdx != -1) {
                        revisore = ctrl.getNomeCompleto(descrizioneRev.substring(revIdx + 10, votoIdx).trim()).orElse("<non assegnato>");
                        voto = descrizioneRev.substring(votoIdx + 8, expIdx).trim();
                        expertise = descrizioneRev.substring(expIdx + 13).trim();
                    }
                    table.getItems().add(new String[]{revisore, voto, expertise});
                }
            }

            table.setFixedCellSize(45);
            table.setPrefHeight(5000);
            table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

            // Label sopra la tabella revisioni
            Label revisioniLabel = new Label("Revisioni ricevute:");
            VBox revisoriBox = new VBox(8, table);
            revisoriBox.setPrefWidth(600);
            revisoriBox.setStyle("");

            // HBox con le due colonne
            HBox listsBox = new HBox(15,
                new VBox(4, datiArticoloLabel, articoloBox),
                new VBox(4, revisioniLabel, revisoriBox)
            );
            listsBox.setPadding(new Insets(10));
            layout.getChildren().add(listsBox);

            // Bottoni
            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            
            // Pulsanti di invio
            Button btnSottomettiArticolo = createButton("Sottometti Articolo", "#f59e0b");
            btnSottomettiArticolo.setOnAction(e -> {
                if (ctrl.sottomettiArticolo(art.getId())) {
                    show(); // Ricarica la pagina per aggiornare i dati
                }
            });
            
            Button btnInviaCameraReady = createButton("Invia Camera-ready", "#10b981");
            btnInviaCameraReady.setOnAction(e -> {
                ctrl.inviaCameraready(art.getId());
                show(); // Ricarica la pagina per aggiornare i dati
            });
            
            Button btnInviaVersioneFinale = createButton("Invia Versione Finale", "#2563eb");
            btnInviaVersioneFinale.setOnAction(e -> {
                ctrl.inviaVersioneFinale(art.getId());
                show(); // Ricarica la pagina per aggiornare i dati
            });
            
            HBox bottoniInvio = new HBox(10,
                spacer1,
                btnSottomettiArticolo,
                btnInviaCameraReady,
                btnInviaVersioneFinale
            );
            bottoniInvio.setPadding(new Insets(0, 0, 0, 0));

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            
            // Pulsanti di visualizzazione
            Button btnVisualizzaFeedbackEditor = createButton("Visualizza Feedback Editor", "#8b5cf6");
            btnVisualizzaFeedbackEditor.setOnAction(e -> {
                ctrl.visualizzaFeedback(art.getId());
            });
            
            Button btnVisualizzaArticolo = createButton("Visualizza Articolo", "#f59e0b");
            btnVisualizzaArticolo.setOnAction(e -> {
                ctrl.visualizzaArticolo(art.getId());
            });
            
            Button btnVisualizzaCameraReady = createButton("Visualizza Camera-ready", "#10b981");
            btnVisualizzaCameraReady.setOnAction(e -> {
                ctrl.visualizzaCameraready(art.getId());
            });
            
            Button btnVisualizzaVersioneFinale = createButton("Visualizza Versione Finale", "#2563eb");
            btnVisualizzaVersioneFinale.setOnAction(e -> {
                ctrl.visualizzaVersioneFinale(art.getId());
            });
            
            HBox bottoniVisual = new HBox(10,
                spacer2,
                btnVisualizzaFeedbackEditor,
                btnVisualizzaArticolo,
                btnVisualizzaCameraReady,
                btnVisualizzaVersioneFinale
            );
            bottoniVisual.setPadding(new Insets(0, 0, 0, 0));

            VBox boxBottoni = new VBox(8, bottoniInvio, bottoniVisual);
            boxBottoni.setPadding(new Insets(0, 10, 10, 10));
            layout.getChildren().add(boxBottoni);
        }

        HeaderBar header = new HeaderBar(ctrl.getAccountController(), this::show);
        header.getBtnBack().setOnAction(e -> ctrl.apriHomepageAutore());

        VBox root = new VBox(header, layout);
        root.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dettagli Conferenza Autore");
        stage.show();
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;");
        return button;
    }
}