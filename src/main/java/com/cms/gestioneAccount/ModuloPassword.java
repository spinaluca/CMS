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

    // Costruttore della classe ModuloPassword
    public ModuloPassword(Stage stage, ControlAccount ctrl, boolean temporanea, Runnable onCancel) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.temporanea = temporanea;
        this.email = ctrl.getUtenteCorrente().getEmail();
        this.onCancel = onCancel;
    }

    // Mostra la finestra per il cambio o impostazione della password
    public void show() {
        // Title and subtitle
        Label titleLabel = new Label(temporanea ? "Imposta Nuova Password" : "Cambio Password");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label(temporanea ? "Configura la tua password permanente" : "Modifica la tua password di accesso");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 24 0;");

        // Style input
        String inputStyle = "-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                            "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                            "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;";

        // Old password (if needed)
        VBox oldPwContainer = new VBox(5);
        PasswordField oldPwField = null;
        if (!temporanea) {
            Label oldPwLabel = new Label("Password Attuale:");
            oldPwLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
            oldPwField = new PasswordField();
            oldPwField.setStyle(inputStyle);
            oldPwField.setPromptText("Inserisci la password attuale");
            oldPwContainer.getChildren().addAll(oldPwLabel, oldPwField);
        }

        // New password
        Label newPwLabel = new Label("Nuova Password:");
        newPwLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        PasswordField newPwField = new PasswordField();
        newPwField.setStyle(inputStyle);
        newPwField.setPromptText("Inserisci la nuova password");

        // Confirm password
        Label confirmPwLabel = new Label("Conferma Nuova Password:");
        confirmPwLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        PasswordField confirmPwField = new PasswordField();
        confirmPwField.setStyle(inputStyle);
        confirmPwField.setPromptText("Conferma la nuova password");

        // Requirements
        Label requirementsTitle = new Label("Requisiti password:");
        requirementsTitle.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        Label requirements = new Label("• Almeno 8 caratteri\n• Almeno una lettera maiuscola\n• Almeno una lettera minuscola\n• Almeno un numero");
        requirements.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        // Error label unico sotto
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px; -fx-max-width: 350px; -fx-wrap-text: true;");
        errorLabel.setVisible(false);

        // Buttons
        Button confirmButton = new Button("Conferma");
        confirmButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                              "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                              "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                              "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");

        Button backButton = new Button("Annulla");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                           "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                           "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(107, 114, 128, 0.3), 4, 0, 0, 2);");

        HBox buttonContainer = new HBox(12, confirmButton, backButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // Form container
        VBox formContainer = new VBox(16,
                titleLabel, subtitleLabel
        );
        if (!temporanea) formContainer.getChildren().add(oldPwContainer);
        formContainer.getChildren().addAll(
                newPwLabel, newPwField,
                confirmPwLabel, confirmPwField,
                requirementsTitle, requirements,
                errorLabel,
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
        final PasswordField finalOldPwField = oldPwField;
        // Validazione live
        newPwField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(newPwField, confirmPwField, finalOldPwField, errorLabel, confirmButton));
        confirmPwField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(newPwField, confirmPwField, finalOldPwField, errorLabel, confirmButton));
        if (finalOldPwField != null) {
            finalOldPwField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(newPwField, confirmPwField, finalOldPwField, errorLabel, confirmButton));
        }
        validateForm(newPwField, confirmPwField, finalOldPwField, errorLabel, confirmButton);

        confirmButton.setOnAction(e -> {
            String nuova = newPwField.getText();
            String conferma = confirmPwField.getText();
            String vecchia = temporanea ? "" : finalOldPwField.getText();

            errorLabel.setVisible(false);
            if (nuova.isEmpty() || conferma.isEmpty() || (!temporanea && vecchia.isEmpty())) {
                showError(errorLabel, "Tutti i campi sono obbligatori.");
                return;
            }
            if (!nuova.equals(conferma)) {
                showError(errorLabel, "Le nuove password non coincidono.");
                return;
            }
            if (!isPasswordValid(nuova)) {
                showError(errorLabel, "La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola e un numero.");
                return;
            }

            if (temporanea) {
                ctrl.aggiornaPasswordTemporanea(email, nuova, false);
                new PopupAvviso("Password aggiornata con successo.").show();
                ctrl.apriLogin();
            } else {
                boolean success = ctrl.richiestaModificaPassword(email, vecchia, nuova);
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

        Scene scene = new Scene(layout, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(titleLabel.getText());
        stage.show();
    }

    // Mostra un messaggio di errore
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    // Verifica se la password rispetta i requisiti di sicurezza
    private boolean isPasswordValid(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }

    // Valida i campi del form e aggiorna lo stato del bottone di conferma
    private void validateForm(PasswordField newPwField, PasswordField confirmPwField, PasswordField oldPwField, Label errorLabel, Button confirmButton) {
        String nuova = newPwField.getText();
        String conferma = confirmPwField.getText();
        String vecchia = oldPwField == null ? "" : oldPwField.getText();
        String error = null;
        if (nuova.isEmpty() || conferma.isEmpty() || (oldPwField != null && vecchia.isEmpty())) {
            error = "Tutti i campi sono obbligatori.";
        } else if (!nuova.equals(conferma)) {
            error = "Le nuove password non coincidono.";
        } else if (!isPasswordValid(nuova)) {
            error = "La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola e un numero.";
        }
        if (error != null) {
            errorLabel.setText(error);
            errorLabel.setVisible(true);
            confirmButton.setDisable(true);
        } else {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            confirmButton.setDisable(false);
        }
    }
}