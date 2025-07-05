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

        // Main container with modern styling
        VBox mainContainer = new VBox(20);
        mainContainer.getStyleClass().add("main-container");
        mainContainer.setStyle("-fx-padding: 30");

        // Header section
        VBox headerSection = new VBox(5);
        headerSection.getStyleClass().add("header-section");
        
        Label titleLabel = new Label("👤 Gestione Ruoli Utente");
        titleLabel.getStyleClass().add("page-title");
        
        Label subtitleLabel = new Label("Configura i tuoi ruoli nel sistema");
        subtitleLabel.getStyleClass().add("page-subtitle");
        
        headerSection.getChildren().addAll(titleLabel, subtitleLabel);

        // Roles selection card
        VBox rolesCard = new VBox(15);
        rolesCard.getStyleClass().add("modern-card");
        
        Label instructionsLabel = new Label("Seleziona i ruoli che ricopri:");
        instructionsLabel.getStyleClass().add("section-title");

        // Create checkboxes with modern styling
        VBox checkboxContainer = new VBox(12);
        
        CheckBox chairCheck = new CheckBox("🏛 Chair - Gestisci conferenze e revisioni");
        chairCheck.getStyleClass().add("modern-checkbox");
        
        CheckBox autoreCheck = new CheckBox("✍ Autore - Sottometti articoli e contributi");
        autoreCheck.getStyleClass().add("modern-checkbox");
        
        CheckBox revisoreCheck = new CheckBox("📋 Revisore - Valuta articoli sottomessi");
        revisoreCheck.getStyleClass().add("modern-checkbox");
        
        CheckBox editorCheck = new CheckBox("📝 Editor - Gestisci pubblicazioni");
        editorCheck.getStyleClass().add("modern-checkbox");

        checkboxContainer.getChildren().addAll(chairCheck, autoreCheck, revisoreCheck, editorCheck);
        rolesCard.getChildren().addAll(instructionsLabel, checkboxContainer);

        // Competence areas card (visible only when Revisore is selected)
        VBox competenceCard = new VBox(15);
        competenceCard.getStyleClass().add("modern-card");
        
        Label competenceTitle = new Label("Aree di Competenza");
        competenceTitle.getStyleClass().add("section-title");
        
        Label competenceSubtitle = new Label("Specifica le tue aree di expertise per le revisioni");
        competenceSubtitle.getStyleClass().add("field-description");
        
        VBox textAreaContainer = new VBox(5);
        
        Label areaLabel = new Label("Aree di competenza (separate da virgola):");
        areaLabel.getStyleClass().add("field-label");
        
        TextArea areeTextArea = new TextArea();
        areeTextArea.getStyleClass().add("modern-text-area");
        areeTextArea.setPromptText("Es. Intelligenza Artificiale, Machine Learning, Database, Reti Neurali...");
        areeTextArea.setPrefRowCount(4);
        areeTextArea.setWrapText(true);
        
        textAreaContainer.getChildren().addAll(areaLabel, areeTextArea);
        competenceCard.getChildren().addAll(competenceTitle, competenceSubtitle, textAreaContainer);

        // Set initial state based on existing roles
        String ruolo = utente.getRuolo();
        if (ruolo.length() == 4) {
            chairCheck.setSelected(ruolo.charAt(0) == 'C');
            autoreCheck.setSelected(ruolo.charAt(1) == 'A');
            revisoreCheck.setSelected(ruolo.charAt(2) == 'R');
            editorCheck.setSelected(ruolo.charAt(3) == 'E');
        }

        // Show competence card only if Revisore is selected
        boolean isRevisore = revisoreCheck.isSelected();
        competenceCard.setVisible(isRevisore);
        competenceCard.setManaged(isRevisore);
        if (isRevisore && utente.getAree() != null) {
            areeTextArea.setText(utente.getAree());
        }

        // Add listener to show/hide competence areas
        revisoreCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            competenceCard.setVisible(newVal);
            competenceCard.setManaged(newVal);
            if (newVal && utente.getAree() != null) {
                areeTextArea.setText(utente.getAree());
            } else if (!newVal) {
                areeTextArea.clear();
            }
        });

        // Action buttons
        HBox buttonsContainer = new HBox(12);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.setAlignment(Pos.CENTER);
        
        Button cancelButton = new Button("Annulla");
        cancelButton.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; " +
                             "-fx-border-color: #cbd5e1; -fx-border-width: 1; " +
                             "-fx-padding: 12 24 12 24; -fx-background-radius: 8; " +
                             "-fx-font-weight: 600; -fx-font-size: 14px;");
        
        Button saveButton = new Button("Salva Modifiche");
        saveButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                           "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                           "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        
        buttonsContainer.getChildren().addAll(cancelButton, saveButton);

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

        // Assemble main layout
        mainContainer.getChildren().addAll(
            headerSection,
            rolesCard,
            competenceCard,
            buttonsContainer
        );

        // Create scene with scroll support
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.getStyleClass().add("modern-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Gestione Ruoli Utente");
        stage.setResizable(true);
        stage.setMinWidth(500);
        stage.setMinHeight(600);
        stage.show();
    }
}