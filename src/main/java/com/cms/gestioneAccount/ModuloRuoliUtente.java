package com.cms.gestioneAccount;

import com.cms.entity.EntityUtente;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

public class ModuloRuoliUtente {
    private final Stage stage;
    private final ControlAccount ctrl;
    private final Runnable onCancel;

    public ModuloRuoliUtente(Stage stage, ControlAccount ctrl, Runnable onCancel) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.onCancel = onCancel;
    }

    public void show() {
        EntityUtente utente = ctrl.getUtenteCorrente();

        // Title and subtitle
        Label titleLabel = new Label("Gestione Ruoli Utente");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("Configura i tuoi ruoli nel sistema");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 24 0;");

        // Instructions label
        Label instructionsLabel = new Label("Seleziona i ruoli che ricopri:");
        instructionsLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");

        // Checkboxes
        VBox checkboxContainer = new VBox(12);
        checkboxContainer.setAlignment(Pos.CENTER_LEFT);

        CheckBox chairCheck = new CheckBox("🏛 Chair - Gestisci conferenze e revisioni");
        CheckBox autoreCheck = new CheckBox("✍ Autore - Sottometti articoli e contributi");
        CheckBox revisoreCheck = new CheckBox("📋 Revisore - Valuta articoli sottomessi");
        CheckBox editorCheck = new CheckBox("📝 Editor - Gestisci pubblicazioni");

        String checkboxStyle = "-fx-text-fill: #1e293b; -fx-font-size: 14px;";
        chairCheck.setStyle(checkboxStyle);
        autoreCheck.setStyle(checkboxStyle);
        revisoreCheck.setStyle(checkboxStyle);
        editorCheck.setStyle(checkboxStyle);

        checkboxContainer.getChildren().addAll(chairCheck, autoreCheck, revisoreCheck, editorCheck);

        // Competence area section
        VBox competenceContainer = new VBox(8);
        competenceContainer.setAlignment(Pos.CENTER_LEFT);
        competenceContainer.setVisible(false);
        competenceContainer.setManaged(false);

        Label areaLabel = new Label("Aree di competenza per revisioni:");
        areaLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");

        TextArea areeTextArea = new TextArea();
        areeTextArea.setPromptText("Es. Intelligenza Artificiale, Machine Learning, Database, Reti Neurali...");
        areeTextArea.setWrapText(true);
        areeTextArea.setPrefHeight(80);
        areeTextArea.setPrefWidth(585);
        areeTextArea.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-text-fill: #1e293b; " +
            "-fx-border-color: #cbd5e1; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 0; " +
            "-fx-font-size: 14px; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );

        competenceContainer.getChildren().addAll(areaLabel, areeTextArea);

        // Load current roles
        String ruolo = utente.getRuolo();
        if (ruolo.length() == 4) {
            chairCheck.setSelected(ruolo.charAt(0) == 'C');
            autoreCheck.setSelected(ruolo.charAt(1) == 'A');
            revisoreCheck.setSelected(ruolo.charAt(2) == 'R');
            editorCheck.setSelected(ruolo.charAt(3) == 'E');
        }

        boolean isRevisore = revisoreCheck.isSelected();
        competenceContainer.setVisible(isRevisore);
        competenceContainer.setManaged(isRevisore);
        if (isRevisore && utente.getAree() != null) {
            areeTextArea.setText(utente.getAree());
        }

        revisoreCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            competenceContainer.setVisible(newVal);
            competenceContainer.setManaged(newVal);
            if (newVal && utente.getAree() != null) {
                areeTextArea.setText(utente.getAree());
            } else if (!newVal) {
                areeTextArea.clear();
            }
        });

        // Buttons
        Button saveButton = new Button("Salva Modifiche");
        saveButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                           "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                           "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");

        Button cancelButton = new Button("Annulla");
        cancelButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                             "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                             "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                             "-fx-effect: dropshadow(gaussian, rgba(107, 114, 128, 0.3), 4, 0, 0, 2);");

        HBox buttonContainer = new HBox(12, saveButton, cancelButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // Form container
        VBox formContainer = new VBox(16,
                titleLabel, subtitleLabel,
                instructionsLabel, checkboxContainer,
                competenceContainer,
                buttonContainer
        );
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-padding: 32; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 2); " +
                "-fx-max-width: 400;");

        VBox layout = new VBox(formContainer);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8fafc; -fx-padding: 40;");

        // Button actions
        saveButton.setOnAction(e -> {
            StringBuilder ruoloBuilder = new StringBuilder();
            ruoloBuilder.append(chairCheck.isSelected() ? "C" : "-");
            ruoloBuilder.append(autoreCheck.isSelected() ? "A" : "-");
            ruoloBuilder.append(revisoreCheck.isSelected() ? "R" : "-");
            ruoloBuilder.append(editorCheck.isSelected() ? "E" : "-");
            String nuovoRuolo = ruoloBuilder.toString();

            String aree = revisoreCheck.isSelected() ? areeTextArea.getText().trim() : null;

            ctrl.richiestaModificaRuolo(nuovoRuolo, aree);
            ctrl.apriHomepageGenerale();
        });

        cancelButton.setOnAction(e -> onCancel.run());

        Scene scene = new Scene(layout, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Gestione Ruoli Utente");
        stage.show();
    }
}