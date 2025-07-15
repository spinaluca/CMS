package com.cms.gestioneAccount;

import com.cms.entity.EntityUtente;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.time.LocalDate;

public class ModuloRegistrazione {
    private final Stage stage;
    private final ControlAccount ctrl;

    // Costruttore della classe ModuloRegistrazione
    public ModuloRegistrazione(Stage stage, ControlAccount ctrl) {
        this.stage = stage;
        this.ctrl = ctrl;
    }

    // Mostra la finestra di registrazione utente
    public void show() {
        // Titolo e sottotitolo
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Inserisci il tuo indirizzo email");
        
        Label nomeLabel = new Label("Nome:");
        TextField nomeField = new TextField();
        nomeField.setPromptText("Inserisci il tuo nome");
        
        Label cognomeLabel = new Label("Cognome:");
        TextField cognomeField = new TextField();
        cognomeField.setPromptText("Inserisci il tuo cognome");
        
        Label nascitaLabel = new Label("Data di nascita:");
        DatePicker nascitaPicker = new DatePicker();
        nascitaPicker.setPromptText("Seleziona la data di nascita");
        
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Inserisci una password sicura");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px; -fx-max-width: 350px; -fx-wrap-text: true;");
        errorLabel.setVisible(false);

        Button registerButton = new Button("Registrati");
        Button backButton = new Button("Indietro");
        
        // Crea titolo e sottotitolo
        Label titleLabel = new Label("Registrati al CMS");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        Label subtitleLabel = new Label("Crea il tuo account per accedere al sistema");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 24 0;");
        
        // Stile etichette del form
        emailLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        nomeLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        cognomeLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        nascitaLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        passwordLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        
        // Stile campi del form
        String inputStyle = "-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                           "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                           "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;";
        
        emailField.setStyle(inputStyle);
        nomeField.setStyle(inputStyle);
        cognomeField.setStyle(inputStyle);
        passwordField.setStyle(inputStyle);

        // Stile DatePicker
        nascitaPicker.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-border-color: #cbd5e1;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-background-insets: 0;" +
            "-fx-border-insets: 0;" +
            "-fx-padding: 0;" +
            "-fx-font-size: 14px;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        nascitaPicker.setPrefWidth(400); // Stessa larghezza degli altri campi

        nascitaPicker.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            // Arrow button
            nascitaPicker.lookupAll(".arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });

            // TextField interno
            nascitaPicker.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });

        
        // Stile pulsanti
        registerButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                               "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                               "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                               "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                              "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                              "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(107, 114, 128, 0.3), 4, 0, 0, 2);");
        
        // Crea contenitore dei pulsanti
        HBox buttonContainer = new HBox(12, registerButton, backButton);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // Crea contenitore del form con spaziatura corretta
        VBox formContainer = new VBox(16,
                titleLabel, subtitleLabel,
                emailLabel, emailField,
                nomeLabel, nomeField,
                cognomeLabel, cognomeField,
                nascitaLabel, nascitaPicker,
                passwordLabel, passwordField,
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

        // Azioni dei pulsanti
        // Validazione live
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(emailField, nomeField, cognomeField, passwordField, nascitaPicker, errorLabel, registerButton));
        nomeField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(emailField, nomeField, cognomeField, passwordField, nascitaPicker, errorLabel, registerButton));
        cognomeField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(emailField, nomeField, cognomeField, passwordField, nascitaPicker, errorLabel, registerButton));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(emailField, nomeField, cognomeField, passwordField, nascitaPicker, errorLabel, registerButton));
        nascitaPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(emailField, nomeField, cognomeField, passwordField, nascitaPicker, errorLabel, registerButton));
        validateForm(emailField, nomeField, cognomeField, passwordField, nascitaPicker, errorLabel, registerButton);

        registerButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String nome = nomeField.getText().trim();
            String cognome = cognomeField.getText().trim();
            String password = passwordField.getText();
            LocalDate nascita = nascitaPicker.getValue();

            errorLabel.setVisible(false);
            // Validation
            if (email.isEmpty() || nome.isEmpty() || cognome.isEmpty() || password.isEmpty() || nascita == null) {
                errorLabel.setText("Tutti i campi sono obbligatori.");
                errorLabel.setVisible(true);
                return;
            }
            if (!isValidEmail(email)) {
                errorLabel.setText("Formato email non valido.");
                errorLabel.setVisible(true);
                return;
            }
            if (!isPasswordValid(password)) {
                errorLabel.setText("La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola e un numero.");
                errorLabel.setVisible(true);
                return;
            }
            if (nascita.isAfter(java.time.LocalDate.now().minusDays(1))) {
                errorLabel.setText("La data di nascita non può essere nel futuro.");
                errorLabel.setVisible(true);
                return;
            }

            // Crea utente e registra
            EntityUtente utente = new EntityUtente(email, nome, cognome, "---", null, nascita, password, false);
            if (ctrl.registraUtente(utente)) {
            ctrl.apriLogin();
            } else {
                errorLabel.setText("Errore durante la registrazione. Email già esistente?");
                errorLabel.setVisible(true);
            }
        });

        backButton.setOnAction(e -> ctrl.apriLogin());

        Scene scene = new Scene(layout, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Registrazione");
        stage.show();
    }
    
    // Verifica se l'email ha un formato valido
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
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

    // Valida i campi del form e aggiorna lo stato del bottone di registrazione
    private void validateForm(TextField emailField, TextField nomeField, TextField cognomeField, PasswordField passwordField, DatePicker nascitaPicker, Label errorLabel, Button registerButton) {
        String email = emailField.getText().trim();
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String password = passwordField.getText();
        LocalDate nascita = nascitaPicker.getValue();
        String error = null;
        if (email.isEmpty() || nome.isEmpty() || cognome.isEmpty() || password.isEmpty() || nascita == null) {
            error = "Tutti i campi sono obbligatori.";
        } else if (!isValidEmail(email)) {
            error = "Formato email non valido.";
        } else if (!isPasswordValid(password)) {
            error = "La password deve contenere almeno 8 caratteri, una lettera maiuscola, una minuscola e un numero.";
        } else if (nascita.isAfter(java.time.LocalDate.now().minusDays(1))) {
            error = "La data di nascita non può essere nel futuro.";
        }
        if (error != null) {
            errorLabel.setText(error);
            errorLabel.setVisible(true);
            registerButton.setDisable(true);
        } else {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            registerButton.setDisable(false);
        }
    }
}