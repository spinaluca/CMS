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

    public ModuloRegistrazione(Stage stage, ControlAccount ctrl) {
        this.stage = stage;
        this.ctrl = ctrl;
    }

    public void show() {
        // Main container with modern styling
        VBox mainContainer = new VBox(25);
        mainContainer.getStyleClass().add("main-container");
        mainContainer.setStyle("-fx-padding: 30");

        // Header section
        VBox headerSection = new VBox(5);
        headerSection.getStyleClass().add("header-section");
        
        Label titleLabel = new Label("🎯 Registrazione");
        titleLabel.getStyleClass().add("page-title");
        
        Label subtitleLabel = new Label("Crea il tuo account per accedere al sistema CMS");
        subtitleLabel.getStyleClass().add("page-subtitle");
        
        headerSection.getChildren().addAll(titleLabel, subtitleLabel);

        // Registration form card
        VBox formCard = new VBox(20);
        formCard.getStyleClass().add("modern-card");
        
        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Form fields container
        VBox fieldsContainer = new VBox(15);
        
        // Email field
        VBox emailContainer = new VBox(5);
        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("field-label");
        TextField emailField = new TextField();
        emailField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                           "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                           "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        emailField.setPromptText("Inserisci il tuo indirizzo email");
        
        // Email validation indicator
        Label emailValidationLabel = new Label();
        emailValidationLabel.getStyleClass().add("validation-indicator");
        emailValidationLabel.setVisible(false);
        
        emailContainer.getChildren().addAll(emailLabel, emailField, emailValidationLabel);
        
        // Personal info section
        Label personalInfoTitle = new Label("Informazioni Personali");
        personalInfoTitle.getStyleClass().add("section-title");
        
        // Name fields in a grid
        GridPane nameGrid = new GridPane();
        nameGrid.setHgap(15);
        nameGrid.setVgap(10);
        
        // Nome field
        VBox nomeContainer = new VBox(5);
        Label nomeLabel = new Label("Nome:");
        nomeLabel.getStyleClass().add("field-label");
        TextField nomeField = new TextField();
        nomeField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                          "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                          "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        nomeField.setPromptText("Inserisci il tuo nome");
        nomeContainer.getChildren().addAll(nomeLabel, nomeField);
        
        // Cognome field
        VBox cognomeContainer = new VBox(5);
        Label cognomeLabel = new Label("Cognome:");
        cognomeLabel.getStyleClass().add("field-label");
        TextField cognomeField = new TextField();
        cognomeField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                             "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                             "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        cognomeField.setPromptText("Inserisci il tuo cognome");
        cognomeContainer.getChildren().addAll(cognomeLabel, cognomeField);
        
        nameGrid.add(nomeContainer, 0, 0);
        nameGrid.add(cognomeContainer, 1, 0);
        
        // Date of birth field
        VBox birthContainer = new VBox(5);
        Label nascitaLabel = new Label("Data di nascita:");
        nascitaLabel.getStyleClass().add("field-label");
        DatePicker nascitaPicker = new DatePicker();
        nascitaPicker.getStyleClass().add("modern-date-picker");
        nascitaPicker.setPromptText("Seleziona la data di nascita");
        birthContainer.getChildren().addAll(nascitaLabel, nascitaPicker);
        
        // Password section
        Label passwordSectionTitle = new Label("Sicurezza");
        passwordSectionTitle.getStyleClass().add("section-title");
        
        VBox passwordContainer = new VBox(5);
        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("field-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                              "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                              "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;");
        passwordField.setPromptText("Crea una password sicura");
        
        // Password strength indicator
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.getStyleClass().add("password-strength-bar");
        
        Label strengthLabel = new Label("Forza password: Molto Debole");
        strengthLabel.getStyleClass().add("password-strength-label");
        
        passwordContainer.getChildren().addAll(passwordLabel, passwordField, strengthBar, strengthLabel);
        
        // Password requirements
        VBox requirementsBox = new VBox(5);
        requirementsBox.getStyleClass().add("password-requirements");
        
        Label requirementsTitle = new Label("Requisiti password:");
        requirementsTitle.getStyleClass().add("requirements-title");
        
        Label requirements = new Label("• Almeno 8 caratteri\n• Almeno una lettera maiuscola\n• Almeno una lettera minuscola\n• Almeno un numero");
        requirements.getStyleClass().add("requirements-text");
        
        requirementsBox.getChildren().addAll(requirementsTitle, requirements);
        
        fieldsContainer.getChildren().addAll(
            emailContainer, 
            personalInfoTitle,
            nameGrid,
            birthContainer,
            passwordSectionTitle,
            passwordContainer,
            requirementsBox
        );
        
        formCard.getChildren().addAll(errorLabel, fieldsContainer);

        // Real-time validation
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail(newVal, emailValidationLabel);
            updateFormValidation(emailField, nomeField, cognomeField, nascitaPicker, passwordField, errorLabel);
        });
        
        nomeField.textProperty().addListener((obs, oldVal, newVal) -> 
            updateFormValidation(emailField, nomeField, cognomeField, nascitaPicker, passwordField, errorLabel));
        
        cognomeField.textProperty().addListener((obs, oldVal, newVal) -> 
            updateFormValidation(emailField, nomeField, cognomeField, nascitaPicker, passwordField, errorLabel));
        
        nascitaPicker.valueProperty().addListener((obs, oldVal, newVal) -> 
            updateFormValidation(emailField, nomeField, cognomeField, nascitaPicker, passwordField, errorLabel));
        
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal, strengthBar, strengthLabel);
            updateFormValidation(emailField, nomeField, cognomeField, nascitaPicker, passwordField, errorLabel);
        });

        // Action buttons
        HBox buttonsContainer = new HBox(12);
        buttonsContainer.getStyleClass().add("buttons-container");
        buttonsContainer.setAlignment(Pos.CENTER);
        
        Button backButton = new Button("Indietro");
        backButton.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; " +
                           "-fx-border-color: #cbd5e1; -fx-border-width: 1; " +
                           "-fx-padding: 12 24 12 24; -fx-background-radius: 8; " +
                           "-fx-font-weight: 600; -fx-font-size: 14px;");
        
        Button confirmButton = new Button("Crea Account");
        confirmButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                              "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                              "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                              "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        confirmButton.setDisable(true);
        
        buttonsContainer.getChildren().addAll(backButton, confirmButton);

        // Button actions
        confirmButton.setOnAction(e -> {
            // Reset error display
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            
            String email = emailField.getText().trim();
            String nome = nomeField.getText().trim();
            String cognome = cognomeField.getText().trim();
            LocalDate nascita = nascitaPicker.getValue();
            String password = passwordField.getText();

            // Final validation
            if (email.isEmpty() || nome.isEmpty() || cognome.isEmpty() || nascita == null || password.isEmpty()) {
                showError(errorLabel, "Tutti i campi sono obbligatori.");
                return;
            }

            if (!isValidEmail(email)) {
                showError(errorLabel, "Formato email non valido.");
                return;
            }

            if (nascita.isAfter(LocalDate.now().minusYears(10))) {
                showError(errorLabel, "Devi avere almeno 10 anni.");
                return;
            }
            
            if (password.length() < 8) {
                showError(errorLabel, "La password deve contenere almeno 8 caratteri.");
                return;
            }

            EntityUtente utente = new EntityUtente(
                    email,
                    nome,
                    cognome,
                    "", // ruolo vuoto
                    null, // aree null
                    nascita,
                    password,
                    false
            );
            ctrl.registraUtente(utente);
            ctrl.apriLogin();
        });

        backButton.setOnAction(e -> ctrl.apriLogin());

        // Store reference to confirm button for validation updates
        final Button finalConfirmButton = confirmButton;
        
        // Initial validation
        updateFormValidation(emailField, nomeField, cognomeField, nascitaPicker, passwordField, errorLabel);

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

        Scene scene = new Scene(scrollPane, 550, 750);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Registrazione");
        stage.setResizable(true);
        stage.setMinWidth(500);
        stage.setMinHeight(700);
        stage.show();
    }

    private void validateEmail(String email, Label validationLabel) {
        if (email.isEmpty()) {
            validationLabel.setVisible(false);
            validationLabel.setManaged(false);
            return;
        }
        
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
        
        if (isValidEmail(email)) {
            validationLabel.setText("✓ Email valida");
            validationLabel.getStyleClass().removeAll("validation-error");
            validationLabel.getStyleClass().add("validation-success");
        } else {
            validationLabel.setText("✗ Formato email non valido");
            validationLabel.getStyleClass().removeAll("validation-success");
            validationLabel.getStyleClass().add("validation-error");
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
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

    private void updateFormValidation(TextField emailField, TextField nomeField, TextField cognomeField,
                                     DatePicker nascitaPicker, PasswordField passwordField, Label errorLabel) {
        String email = emailField.getText().trim();
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        LocalDate nascita = nascitaPicker.getValue();
        String password = passwordField.getText();

        boolean campiPieni = !email.isEmpty() && !nome.isEmpty() && !cognome.isEmpty() && nascita != null && !password.isEmpty();
        boolean emailValida = isValidEmail(email);
        boolean etaValida = nascita != null && nascita.isBefore(LocalDate.now().minusYears(10));
        boolean passwordValida = password.length() >= 8;

        // Find confirm button and update its state
        Button confirmButton = findConfirmButton();
        
        if (!campiPieni) {
            hideError(errorLabel);
            if (confirmButton != null) confirmButton.setDisable(true);
        } else if (!emailValida) {
            hideError(errorLabel);
            if (confirmButton != null) confirmButton.setDisable(true);
        } else if (!etaValida) {
            hideError(errorLabel);
            if (confirmButton != null) confirmButton.setDisable(true);
        } else if (!passwordValida) {
            hideError(errorLabel);
            if (confirmButton != null) confirmButton.setDisable(true);
        } else {
            hideError(errorLabel);
            if (confirmButton != null) confirmButton.setDisable(false);
        }
    }
    
    private Button findConfirmButton() {
        // This is a simple way to find the confirm button in the scene
        try {
            return (Button) stage.getScene().lookup(".primary-button");
        } catch (Exception e) {
            return null;
        }
    }
    
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}