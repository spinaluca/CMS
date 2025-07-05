package com.cms.gestioneAccount;

import com.cms.common.PopupAvviso;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

public class ModuloPassword {
    private final Stage stage;
    private final ControlAccount ctrl;
    private final boolean temporanea;
    private final String email;
    private final Runnable onCancel;

    public ModuloPassword(Stage stage, ControlAccount ctrl, boolean temporanea, Runnable onCancel) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.temporanea = temporanea;
        this.email = ctrl.getUtenteCorrente().getEmail();
        this.onCancel = onCancel;
    }

    public void show() {
        // Main container with modern styling
        VBox mainContainer = new VBox(25);
        mainContainer.getStyleClass().add("main-container");
        mainContainer.setStyle("-fx-padding: 30");

        // Header section
        VBox headerSection = new VBox(5);
        headerSection.getStyleClass().add("header-section");
        
        String iconText = temporanea ? "🔑" : "🔐";
        String titleText = temporanea ? "Imposta Nuova Password" : "Cambio Password";
        String subtitleText = temporanea ? "Configura la tua password permanente" : "Modifica la tua password di accesso";
        
        Label titleLabel = new Label(iconText + " " + titleText);
        titleLabel.getStyleClass().add("page-title");
        
        Label subtitleLabel = new Label(subtitleText);
        subtitleLabel.getStyleClass().add("page-subtitle");
        
        headerSection.getChildren().addAll(titleLabel, subtitleLabel);

        // Password form card
        VBox formCard = new VBox(20);
        formCard.getStyleClass().add("modern-card");
        
        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Password fields container
        VBox fieldsContainer = new VBox(15);
        
        // Old password field (only if not temporary)
        VBox oldPasswordContainer = null;
        PasswordField oldPwField = null;
        
        if (!temporanea) {
            oldPasswordContainer = new VBox(5);
            
            Label oldPwLabel = new Label("Password Attuale:");
            oldPwLabel.getStyleClass().add("field-label");
            
            oldPwField = new PasswordField();
            oldPwField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                               "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                               "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
            oldPwField.setPromptText("Inserisci la password attuale");
            
            oldPasswordContainer.getChildren().addAll(oldPwLabel, oldPwField);
            fieldsContainer.getChildren().add(oldPasswordContainer);
        }
        
        // New password field
        VBox newPasswordContainer = new VBox(5);
        
        Label newPwLabel = new Label("Nuova Password:");
        newPwLabel.getStyleClass().add("field-label");
        
        PasswordField newPwField = new PasswordField();
        newPwField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                           "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                           "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        newPwField.setPromptText("Inserisci la nuova password");
        
        // Password strength indicator
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.getStyleClass().add("password-strength-bar");
        
        Label strengthLabel = new Label("Forza password: Debole");
        strengthLabel.getStyleClass().add("password-strength-label");
        
        newPasswordContainer.getChildren().addAll(newPwLabel, newPwField, strengthBar, strengthLabel);
        
        // Confirm password field
        VBox confirmPasswordContainer = new VBox(5);
        
        Label confirmPwLabel = new Label("Conferma Nuova Password:");
        confirmPwLabel.getStyleClass().add("field-label");
        
        PasswordField confirmPwField = new PasswordField();
        confirmPwField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                               "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                               "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        confirmPwField.setPromptText("Conferma la nuova password");
        
        // Match indicator
        Label matchLabel = new Label();
        matchLabel.getStyleClass().add("match-indicator");
        matchLabel.setVisible(false);
        
        confirmPasswordContainer.getChildren().addAll(confirmPwLabel, confirmPwField, matchLabel);
        
        fieldsContainer.getChildren().addAll(newPasswordContainer, confirmPasswordContainer);
        
        // Password requirements info
        VBox requirementsBox = new VBox(5);
        requirementsBox.getStyleClass().add("password-requirements");
        
        Label requirementsTitle = new Label("Requisiti password:");
        requirementsTitle.getStyleClass().add("requirements-title");
        
        Label requirements = new Label("• Almeno 8 caratteri\n• Almeno una lettera maiuscola\n• Almeno una lettera minuscola\n• Almeno un numero");
        requirements.getStyleClass().add("requirements-text");
        
        requirementsBox.getChildren().addAll(requirementsTitle, requirements);
        
        formCard.getChildren().addAll(errorLabel, fieldsContainer, requirementsBox);

        // Password strength validation
        final PasswordField finalOldPwField = oldPwField;
        newPwField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal, strengthBar, strengthLabel);
            validatePasswordMatch(newVal, confirmPwField.getText(), matchLabel);
        });
        
        confirmPwField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordMatch(newPwField.getText(), newVal, matchLabel);
        });

        // Action buttons
        HBox buttonsContainer = new HBox(12);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.setAlignment(Pos.CENTER);
        
        Button backButton = new Button("Annulla");
        backButton.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; " +
                           "-fx-border-color: #cbd5e1; -fx-border-width: 1; " +
                           "-fx-padding: 12 24 12 24; -fx-background-radius: 8; " +
                           "-fx-font-weight: 600; -fx-font-size: 14px;");
        
        Button confirmButton = new Button("Conferma");
        confirmButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                              "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                              "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                              "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        
        buttonsContainer.getChildren().addAll(backButton, confirmButton);

        // Button actions
        confirmButton.setOnAction(e -> {
            String nuova = newPwField.getText();
            String conferma = confirmPwField.getText();
            String vecchia = temporanea ? "" : finalOldPwField.getText();

            // Reset error display
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            // Validation
            if (nuova.isEmpty() || conferma.isEmpty() || (!temporanea && vecchia.isEmpty())) {
                showError(errorLabel, "Tutti i campi sono obbligatori.");
                return;
            }

            if (!nuova.equals(conferma)) {
                showError(errorLabel, "Le nuove password non coincidono.");
                return;
            }

            if (nuova.length() < 8) {
                showError(errorLabel, "La password deve contenere almeno 8 caratteri.");
                return;
            }

            if (temporanea) {
                ctrl.aggiornaPasswordTemporanea(email, nuova, false);
                new PopupAvviso("Password aggiornata con successo.").show();
                ctrl.apriLogin();
            } else {
                boolean success = ctrl.richiestaModificaPassword(vecchia, nuova, conferma);
                if (success) {
                    new PopupAvviso("Password aggiornata con successo.").show();
                    onCancel.run();
                } else {
                    showError(errorLabel, "Password attuale errata.");
                }
            }
        });

        backButton.setOnAction(e -> {
            if (temporanea) ctrl.apriLogin();
            else onCancel.run();
        });

        // Assemble main layout
        mainContainer.getChildren().addAll(
            headerSection,
            formCard,
            buttonsContainer
        );

        // Create scene with scroll support
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.getStyleClass().add("modern-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 1050, 750);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle(titleText);
        stage.setResizable(true);
        stage.setMinWidth(450);
        stage.setMinHeight(temporanea ? 500 : 600);
        stage.show();
    }
    
    private void updatePasswordStrength(String password, ProgressBar strengthBar, Label strengthLabel) {
        int score = 0;
        String strength = "Molto Debole";
        String colorClass = "strength-very-weak";
        
        if (password.length() >= 8) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;
        
        switch (score) {
            case 0:
            case 1:
                strength = "Molto Debole";
                colorClass = "strength-very-weak";
                break;
            case 2:
                strength = "Debole";
                colorClass = "strength-weak";
                break;
            case 3:
                strength = "Media";
                colorClass = "strength-medium";
                break;
            case 4:
                strength = "Forte";
                colorClass = "strength-strong";
                break;
            case 5:
                strength = "Molto Forte";
                colorClass = "strength-very-strong";
                break;
        }
        
        strengthBar.setProgress(score / 5.0);
        strengthBar.getStyleClass().removeAll("strength-very-weak", "strength-weak", "strength-medium", "strength-strong", "strength-very-strong");
        strengthBar.getStyleClass().add(colorClass);
        strengthLabel.setText("Forza password: " + strength);
    }
    
    private void validatePasswordMatch(String password, String confirm, Label matchLabel) {
        if (confirm.isEmpty()) {
            matchLabel.setVisible(false);
            matchLabel.setManaged(false);
            return;
        }
        
        matchLabel.setVisible(true);
        matchLabel.setManaged(true);
        
        if (password.equals(confirm)) {
            matchLabel.setText("✓ Le password coincidono");
            matchLabel.getStyleClass().removeAll("match-error");
            matchLabel.getStyleClass().add("match-success");
        } else {
            matchLabel.setText("✗ Le password non coincidono");
            matchLabel.getStyleClass().removeAll("match-success");
            matchLabel.getStyleClass().add("match-error");
        }
    }
    
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}