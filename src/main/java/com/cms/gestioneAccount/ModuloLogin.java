package com.cms.gestioneAccount;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ModuloLogin {
    private final Stage stage;
    private final ControlAccount ctrl;

    public ModuloLogin(Stage stage, ControlAccount ctrl) {
        this.stage = stage;
        this.ctrl = ctrl;
    }

    public void show() {
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField("luca.spina@email.net");
        emailField.setPromptText("Inserisci il tuo indirizzo email");
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Inserisci la tua password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Registrati");
        Button recoverButton = new Button("Recupera Password");

        //!!!!loginButton.setDisable(true); // disabilitato all'inizio

        // listener per abilitare login solo se input valido
        ChangeListener<String> listener = (ObservableValue<? extends String> obs, String oldVal, String newVal) -> {
            aggiornaValidita(emailField, passwordField, loginButton, errorLabel);
        };

        emailField.textProperty().addListener(listener);
        //!!!!passwordField.textProperty().addListener(listener);

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            password = "hashedpass17";
            if (ctrl.verificaCredenziali(email, password)) {
                ctrl.setUtenteCorrente(email);  // salva utente loggato

                if (ctrl.isPasswordTemporanea(email)) {
                    // Reindirizza al modulo per aggiornare la password
                    ctrl.apriCambioPassword();  // deve mostrare ModuloPassword
                } else {
                    ctrl.apriHomepageGenerale();   // login normale
                }
            } else {
                showAlert("Credenziali non valide");
            }

        });

        registerButton.setOnAction(e -> ctrl.apriRegistrazione());
        recoverButton.setOnAction(e -> ctrl.richiestaRecuperoPassword());

        // Create modern form layout
        Label titleLabel = new Label("Accedi al CMS");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        Label subtitleLabel = new Label("Inserisci le tue credenziali per continuare");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 24 0;");
        
        // Style form labels
        emailLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        passwordLabel.setStyle("-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;");
        
        // Style form inputs
        emailField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                           "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                           "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        passwordField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                              "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                              "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        
        // Style buttons
        loginButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                            "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                            "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        
        registerButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                           "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                           "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(107, 114, 128, 0.3), 4, 0, 0, 2);");
        
        recoverButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563eb; " +
                              "-fx-border-color: transparent; -fx-padding: 8 16 8 16; " +
                              "-fx-font-size: 13px; -fx-underline: true;");
        
        // Create button container
        HBox buttonContainer = new HBox(12, loginButton, registerButton);
        buttonContainer.setAlignment(Pos.CENTER);
        
        VBox formContainer = new VBox(16,
                titleLabel, subtitleLabel,
                emailLabel, emailField,
                passwordLabel, passwordField,
                errorLabel,
                buttonContainer, recoverButton
        );
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                              "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                              "-fx-padding: 32; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 2); " +
                              "-fx-max-width: 400;");
        
        VBox layout = new VBox(formContainer);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8fafc; -fx-padding: 40;");

        Scene scene = new Scene(layout, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Login");
        stage.show();
    }

    private void aggiornaValidita(TextField emailField, PasswordField passwordField, Button loginButton, Label errorLabel) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email e password sono obbligatori.");
            loginButton.setDisable(true);
        } else if (!email.contains("@") || !email.contains(".")) {
            errorLabel.setText("Formato email non valido.");
            loginButton.setDisable(true);
        } else {
            errorLabel.setText("");
            loginButton.setDisable(false);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}