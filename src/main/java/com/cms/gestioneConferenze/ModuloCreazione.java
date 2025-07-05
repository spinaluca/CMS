package com.cms.gestioneConferenze;

import com.cms.gestioneAccount.ControlAccount;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ModuloCreazione {

    private final ControlConferenze ctrl;
    private final ControlAccount ctrlAccount;

    public ModuloCreazione(ControlConferenze ctrl, ControlAccount ctrlAccount) {
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Crea Nuova Conferenza");

        // Titolo e sottotitolo
        Label titleLabel = new Label("Crea Conferenza");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("Inserisci i dati per creare la conferenza");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 24 0;");

        // Campi del form
        Label acronimoLabel = new Label("Acronimo:");
        acronimoLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        TextField acronimoField = new TextField();
        acronimoField.setPromptText("es. ICML, NIPS, ICLR...");
        acronimoField.setPrefWidth(400);

        Label titoloLabel = new Label("Titolo completo:");
        titoloLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        TextField titoloField = new TextField();
        titoloField.setPromptText("Inserisci il titolo completo della conferenza");
        titoloField.setPrefWidth(400);

        Label descrizioneLabel = new Label("Descrizione:");
        descrizioneLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        TextArea descrizioneArea = new TextArea();
        descrizioneArea.setPromptText("Inserisci una descrizione dettagliata della conferenza...");
        descrizioneArea.setPrefWidth(400);
        descrizioneArea.setPrefRowCount(4);
        descrizioneArea.setWrapText(true);

        Label luogoLabel = new Label("Luogo:");
        luogoLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        TextField luogoField = new TextField();
        luogoField.setPromptText("es. Milano, Italia");
        luogoField.setPrefWidth(400);

        // Date
        Label scadenzaSottomissioneLabel = new Label("Scadenza Sottomissione:");
        scadenzaSottomissioneLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        DatePicker scadenzaSottomissione = new DatePicker();
        scadenzaSottomissione.setPromptText("Scadenza Sottomissione");
        scadenzaSottomissione.setPrefWidth(400);

        Label scadenzaRevisioniLabel = new Label("Scadenza Revisioni:");
        scadenzaRevisioniLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        DatePicker scadenzaRevisioni = new DatePicker();
        scadenzaRevisioni.setPromptText("Scadenza Revisioni");
        scadenzaRevisioni.setPrefWidth(400);

        Label dataGraduatoriaLabel = new Label("Pubblicazione Graduatoria:");
        dataGraduatoriaLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        DatePicker dataGraduatoria = new DatePicker();
        dataGraduatoria.setPromptText("Pubblicazione Graduatoria");
        dataGraduatoria.setPrefWidth(400);

        Label scadenzaCameraReadyLabel = new Label("Scadenza Camera-ready:");
        scadenzaCameraReadyLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        DatePicker scadenzaCameraReady = new DatePicker();
        scadenzaCameraReady.setPromptText("Scadenza Camera-ready");
        scadenzaCameraReady.setPrefWidth(400);

        Label scadenzaFeedbackEditoreLabel = new Label("Scadenza Feedback Editore:");
        scadenzaFeedbackEditoreLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        DatePicker scadenzaFeedbackEditore = new DatePicker();
        scadenzaFeedbackEditore.setPromptText("Scadenza Feedback Editore");
        scadenzaFeedbackEditore.setPrefWidth(400);

        Label scadenzaVersioneFinaleLabel = new Label("Scadenza Versione Finale:");
        scadenzaVersioneFinaleLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        DatePicker scadenzaVersioneFinale = new DatePicker();
        scadenzaVersioneFinale.setPromptText("Scadenza Versione Finale");
        scadenzaVersioneFinale.setPrefWidth(400);

        // Parametri numerici
        Label minRevLabel = new Label("Numero Minimo Revisori per Paper:");
        minRevLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        Spinner<Integer> numeroMinimoRevisori = new Spinner<>(1, 10, 3);
        numeroMinimoRevisori.setPrefWidth(400);

        Label nWinLabel = new Label("Numero Massimo Paper Vincitori:");
        nWinLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        Spinner<Integer> numeroVincitori = new Spinner<>(1, 100, 10);
        numeroVincitori.setPrefWidth(400);

        Label valLabel = new Label("Range di Valutazione:");
        valLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        HBox valRange = new HBox(15);
        valRange.setAlignment(Pos.CENTER_LEFT);
        Label minLabel = new Label("Minima:");
        minLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");
        Spinner<Integer> valutazioneMinima = new Spinner<>(-10, 10, 0);
        valutazioneMinima.setPrefWidth(100);
        Label maxLabel = new Label("Massima:");
        maxLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");
        Spinner<Integer> valutazioneMassima = new Spinner<>(-10, 10, 5);
        valutazioneMassima.setPrefWidth(100);
        valRange.getChildren().addAll(minLabel, valutazioneMinima, maxLabel, valutazioneMassima);

        Label distribLabel = new Label("Modalità Distribuzione Paper:");
        distribLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        ChoiceBox<String> modalitaDistribuzione = new ChoiceBox<>();
        modalitaDistribuzione.getItems().addAll("MANUALE", "AUTOMATICA", "BROADCAST");
        modalitaDistribuzione.setValue("MANUALE");
        modalitaDistribuzione.setPrefWidth(400);

        // Stile per tutti i campi input
        String inputStyle = "-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                            "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                            "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;";

        acronimoField.setStyle(inputStyle);
        titoloField.setStyle(inputStyle);
        descrizioneArea.setStyle(inputStyle);
        luogoField.setStyle(inputStyle);
        scadenzaSottomissione.setStyle(inputStyle + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scadenzaRevisioni.setStyle(inputStyle + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        dataGraduatoria.setStyle(inputStyle + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scadenzaCameraReady.setStyle(inputStyle + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scadenzaFeedbackEditore.setStyle(inputStyle + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scadenzaVersioneFinale.setStyle(inputStyle + " -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        numeroMinimoRevisori.setStyle(inputStyle);
        numeroVincitori.setStyle(inputStyle);
        valutazioneMinima.setStyle(inputStyle);
        valutazioneMassima.setStyle(inputStyle);
        modalitaDistribuzione.setStyle(inputStyle);

        // Messaggio di errore
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px;");

        // Pulsanti
        Button createButton = new Button("Crea Conferenza");
        createButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                             "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                             "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                             "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");

        Button backButton = new Button("Annulla");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                           "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                           "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(107, 114, 128, 0.3), 4, 0, 0, 2);");

        HBox buttonContainer = new HBox(12, createButton, backButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // Layout principale
        VBox formContainer = new VBox(16,
            titleLabel, subtitleLabel,
            acronimoLabel, acronimoField,
            titoloLabel, titoloField,
            descrizioneLabel, descrizioneArea,
            luogoLabel, luogoField,
            scadenzaSottomissioneLabel, scadenzaSottomissione,
            scadenzaRevisioniLabel, scadenzaRevisioni,
            dataGraduatoriaLabel, dataGraduatoria,
            scadenzaCameraReadyLabel, scadenzaCameraReady,
            scadenzaFeedbackEditoreLabel, scadenzaFeedbackEditore,
            scadenzaVersioneFinaleLabel, scadenzaVersioneFinale,
            minRevLabel, numeroMinimoRevisori,
            nWinLabel, numeroVincitori,
            valLabel, valRange,
            distribLabel, modalitaDistribuzione,
            errorLabel,
            buttonContainer
        );
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                              "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                              "-fx-padding: 32; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 2); " +
                              "-fx-max-width: 500;");

        VBox layout = new VBox(formContainer);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8fafc; -fx-padding: 40;");

        // Validazione e gestione eventi
        createButton.setOnAction(e -> {
            String acronimo = acronimoField.getText().trim();
            String titolo = titoloField.getText().trim();
            String descrizione = descrizioneArea.getText().trim();
            String luogo = luogoField.getText().trim();
            LocalDate sottom = scadenzaSottomissione.getValue();
            LocalDate rev = scadenzaRevisioni.getValue();
            LocalDate grad = dataGraduatoria.getValue();
            LocalDate cam = scadenzaCameraReady.getValue();
            LocalDate feed = scadenzaFeedbackEditore.getValue();
            LocalDate ver = scadenzaVersioneFinale.getValue();
            int minRev = numeroMinimoRevisori.getValue();
            int nWin = numeroVincitori.getValue();
            int valMin = valutazioneMinima.getValue();
            int valMax = valutazioneMassima.getValue();
            String distrib = modalitaDistribuzione.getValue();

            if (acronimo.isEmpty() || titolo.isEmpty() || descrizione.isEmpty() || luogo.isEmpty() ||
                sottom == null || rev == null || grad == null || cam == null || feed == null || ver == null) {
                errorLabel.setText("Tutti i campi sono obbligatori.");
                return;
            }
            if (sottom.isAfter(rev) || rev.isAfter(grad) || grad.isAfter(cam) || cam.isAfter(feed) || feed.isAfter(ver)) {
                errorLabel.setText("Le date devono essere in ordine cronologico.");
                return;
            }
            if (valMin >= valMax) {
                errorLabel.setText("La valutazione minima deve essere inferiore alla massima.");
                return;
            }
            
            errorLabel.setText("");
            Map<String, String> data = new HashMap<>();
            data.put("acronimo", acronimo);
            data.put("titolo", titolo);
            data.put("descrizione", descrizione);
            data.put("luogo", luogo);
            data.put("scadenzaSottomissione", sottom.toString());
            data.put("scadenzaRevisioni", rev.toString());
            data.put("dataGraduatoria", grad.toString());
            data.put("scadenzaCameraReady", cam.toString());
            data.put("scadenzaFeedbackEditore", feed.toString());
            data.put("scadenzaVersioneFinale", ver.toString());
            data.put("numeroMinimoRevisori", String.valueOf(minRev));
            data.put("valutazioneMinima", String.valueOf(valMin));
            data.put("valutazioneMassima", String.valueOf(valMax));
            data.put("numeroVincitori", String.valueOf(nWin));
            data.put("modalitaDistribuzione", distrib);
            
            ctrl.creaConferenza(data, ctrlAccount.getUtenteCorrente());
            stage.close();
            ctrlAccount.apriHomepageChair();
        });

        backButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(layout, 1050, 750);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
