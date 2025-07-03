package com.cms.gestioneConferenze;

import com.cms.gestioneAccount.ControlAccount;
import com.cms.gestioneConferenze.ControlConferenze;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ModuloCreazione {

    private final ControlConferenze ctrl;
    private final ControlAccount ctrlAccount;

    // Form fields as instance variables for easier access
    private TextField acronimoField;
    private TextField titoloField;
    private TextArea descrizioneArea;
    private TextField luogoField;
    private DatePicker scadenzaSottomissione;
    private DatePicker scadenzaRevisioni;
    private DatePicker dataGraduatoria;
    private DatePicker scadenzaCameraReady;
    private DatePicker scadenzaFeedbackEditore;
    private DatePicker scadenzaVersioneFinale;
    private Spinner<Integer> numeroMinimoRevisori;
    private Spinner<Integer> valutazioneMinima;
    private Spinner<Integer> valutazioneMassima;
    private Spinner<Integer> numeroVincitori;
    private ChoiceBox<String> modalitaDistribuzione;

    public ModuloCreazione(ControlConferenze ctrl, ControlAccount ctrlAccount) {
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
    }

    public void show() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Crea Nuova Conferenza");
        dialog.getDialogPane().getStyleClass().add("modern-dialog");

        ButtonType createBtnType = new ButtonType("Crea Conferenza", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        // Main container
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.getStyleClass().add("creation-form-container");

        // Header section
        VBox headerSection = createHeaderSection();

        // Create tabs for better organization
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("modern-tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Basic Information Tab
        Tab basicInfoTab = new Tab("📝 Informazioni Base");
        VBox basicInfoContent = createBasicInfoSection();
        ScrollPane basicScrollPane = new ScrollPane(basicInfoContent);
        basicScrollPane.setFitToWidth(true);
        basicScrollPane.getStyleClass().add("form-scroll-pane");
        basicInfoTab.setContent(basicScrollPane);

        // Dates Tab
        Tab datesTab = new Tab("📅 Scadenze");
        VBox datesContent = createDatesSection();
        ScrollPane datesScrollPane = new ScrollPane(datesContent);
        datesScrollPane.setFitToWidth(true);
        datesScrollPane.getStyleClass().add("form-scroll-pane");
        datesTab.setContent(datesScrollPane);

        // Configuration Tab
        Tab configTab = new Tab("⚙️ Configurazione");
        VBox configContent = createConfigurationSection();
        ScrollPane configScrollPane = new ScrollPane(configContent);
        configScrollPane.setFitToWidth(true);
        configScrollPane.getStyleClass().add("form-scroll-pane");
        configTab.setContent(configScrollPane);

        tabPane.getTabs().addAll(basicInfoTab, datesTab, configTab);

        // Validation message
        Text validationMessage = new Text();
        validationMessage.getStyleClass().add("validation-message");
        VBox validationBox = new VBox(validationMessage);
        validationBox.getStyleClass().add("validation-container");
        validationBox.setAlignment(Pos.CENTER);

        mainContainer.getChildren().addAll(headerSection, tabPane, validationBox);
        dialog.getDialogPane().setContent(mainContainer);

        // Style buttons
        Node createBtn = dialog.getDialogPane().lookupButton(createBtnType);
        createBtn.getStyleClass().add("primary-button");
        createBtn.setDisable(true);

        Node cancelBtn = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().add("secondary-button");

        // Setup validation
        setupValidation(createBtn, validationMessage);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createBtnType) {
                Map<String, String> data = collectFormData();
                ctrl.creaConferenza(data, ctrlAccount.getUtenteCorrente());
                ctrlAccount.apriHomepageChair();
            }
            return null;
        });

        // Set dialog size and show
        dialog.getDialogPane().setPrefSize(900, 700);
        dialog.showAndWait();
    }

    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.getStyleClass().add("form-header");
        
        Text titleText = new Text("Nuova Conferenza Accademica");
        titleText.getStyleClass().add("form-title");
        
        Text subtitleText = new Text("Compila tutti i campi per creare una nuova conferenza accademica");
        subtitleText.getStyleClass().add("form-subtitle");
        
        headerSection.getChildren().addAll(titleText, subtitleText);
        return headerSection;
    }

    private VBox createBasicInfoSection() {
        VBox section = new VBox(20);
        section.getStyleClass().add("form-section");
        section.setPadding(new Insets(20));

        // Acronimo
        VBox acroBox = new VBox(8);
        Label acroLabel = new Label("Acronimo della Conferenza");
        acroLabel.getStyleClass().add("form-label");
        acronimoField = new TextField();
        acronimoField.getStyleClass().add("modern-text-field");
        acronimoField.setPromptText("es. ICML, NIPS, ICLR...");
        acronimoField.setPrefWidth(400);
        acroBox.getChildren().addAll(acroLabel, acronimoField);

        // Titolo
        VBox titleBox = new VBox(8);
        Label titleLabel = new Label("Titolo Completo");
        titleLabel.getStyleClass().add("form-label");
        titoloField = new TextField();
        titoloField.getStyleClass().add("modern-text-field");
        titoloField.setPromptText("Inserisci il titolo completo della conferenza");
        titoloField.setPrefWidth(400);
        titleBox.getChildren().addAll(titleLabel, titoloField);

        // Descrizione
        VBox descBox = new VBox(8);
        Label descLabel = new Label("Descrizione");
        descLabel.getStyleClass().add("form-label");
        descrizioneArea = new TextArea();
        descrizioneArea.getStyleClass().add("modern-textarea");
        descrizioneArea.setPromptText("Inserisci una descrizione dettagliata della conferenza, obiettivi e argomenti principali...");
        descrizioneArea.setWrapText(true);
        descrizioneArea.setPrefRowCount(4);
        descrizioneArea.setPrefWidth(400);
        descBox.getChildren().addAll(descLabel, descrizioneArea);

        // Luogo
        VBox luogoBox = new VBox(8);
        Label luogoLabel = new Label("Sede della Conferenza");
        luogoLabel.getStyleClass().add("form-label");
        luogoField = new TextField();
        luogoField.getStyleClass().add("modern-text-field");
        luogoField.setPromptText("es. Milano, Italia");
        luogoField.setPrefWidth(400);
        luogoBox.getChildren().addAll(luogoLabel, luogoField);

        section.getChildren().addAll(acroBox, titleBox, descBox, luogoBox);
        return section;
    }

    private VBox createDatesSection() {
        VBox section = new VBox(20);
        section.getStyleClass().add("form-section");
        section.setPadding(new Insets(20));

        // Info box about dates
        VBox infoBox = new VBox(10);
        infoBox.getStyleClass().add("info-box");
        Text infoTitle = new Text("📅 Informazioni sulle Scadenze");
        infoTitle.getStyleClass().add("info-title");
        Text infoText = new Text("Le date devono essere in ordine cronologico e successive alla data odierna. " +
                "Assicurati che ogni scadenza sia ragionevole rispetto alle precedenti.");
        infoText.getStyleClass().add("info-text");
        infoText.setWrappingWidth(800);
        infoBox.getChildren().addAll(infoTitle, infoText);

        // Create grid for dates
        GridPane datesGrid = new GridPane();
        datesGrid.setHgap(30);
        datesGrid.setVgap(20);
        datesGrid.getStyleClass().add("dates-grid");

        // Initialize date pickers
        scadenzaSottomissione = createDatePicker("Scadenza Sottomissione");
        scadenzaRevisioni = createDatePicker("Scadenza Revisioni");
        dataGraduatoria = createDatePicker("Pubblicazione Graduatoria");
        scadenzaCameraReady = createDatePicker("Scadenza Camera-ready");
        scadenzaFeedbackEditore = createDatePicker("Scadenza Feedback Editore");
        scadenzaVersioneFinale = createDatePicker("Scadenza Versione Finale");

        // Add to grid
        datesGrid.add(createDateBox("1. Scadenza Sottomissione", scadenzaSottomissione), 0, 0);
        datesGrid.add(createDateBox("2. Scadenza Revisioni", scadenzaRevisioni), 1, 0);
        datesGrid.add(createDateBox("3. Pubblicazione Graduatoria", dataGraduatoria), 0, 1);
        datesGrid.add(createDateBox("4. Scadenza Camera-ready", scadenzaCameraReady), 1, 1);
        datesGrid.add(createDateBox("5. Scadenza Feedback Editore", scadenzaFeedbackEditore), 0, 2);
        datesGrid.add(createDateBox("6. Scadenza Versione Finale", scadenzaVersioneFinale), 1, 2);

        section.getChildren().addAll(infoBox, datesGrid);
        return section;
    }

    private VBox createConfigurationSection() {
        VBox section = new VBox(20);
        section.getStyleClass().add("form-section");
        section.setPadding(new Insets(20));

        GridPane configGrid = new GridPane();
        configGrid.setHgap(40);
        configGrid.setVgap(25);
        configGrid.getStyleClass().add("config-grid");

        // Left column
        VBox leftColumn = new VBox(20);

        // Numero Minimo Revisori
        VBox minRevBox = new VBox(8);
        Label minRevLabel = new Label("Numero Minimo Revisori per Paper");
        minRevLabel.getStyleClass().add("form-label");
        numeroMinimoRevisori = new Spinner<>(1, 10, 3);
        numeroMinimoRevisori.getStyleClass().add("modern-spinner");
        numeroMinimoRevisori.setPrefWidth(150);
        numeroMinimoRevisori.setEditable(true);
        minRevBox.getChildren().addAll(minRevLabel, numeroMinimoRevisori);

        // Numero Vincitori
        VBox nWinBox = new VBox(8);
        Label nWinLabel = new Label("Numero Massimo Paper Vincitori");
        nWinLabel.getStyleClass().add("form-label");
        numeroVincitori = new Spinner<>(1, 100, 10);
        numeroVincitori.getStyleClass().add("modern-spinner");
        numeroVincitori.setPrefWidth(150);
        numeroVincitori.setEditable(true);
        nWinBox.getChildren().addAll(nWinLabel, numeroVincitori);

        leftColumn.getChildren().addAll(minRevBox, nWinBox);

        // Right column
        VBox rightColumn = new VBox(20);

        // Valutazione Range
        VBox valBox = new VBox(8);
        Label valLabel = new Label("Range di Valutazione");
        valLabel.getStyleClass().add("form-label");
        
        HBox valRange = new HBox(15);
        valRange.setAlignment(Pos.CENTER_LEFT);
        
        Label minLabel = new Label("Minima:");
        minLabel.getStyleClass().add("field-label");
        valutazioneMinima = new Spinner<>(-10, 10, 0);
        valutazioneMinima.getStyleClass().add("modern-spinner");
        valutazioneMinima.setPrefWidth(100);
        valutazioneMinima.setEditable(true);
        
        Label maxLabel = new Label("Massima:");
        maxLabel.getStyleClass().add("field-label");
        valutazioneMassima = new Spinner<>(-10, 10, 5);
        valutazioneMassima.getStyleClass().add("modern-spinner");
        valutazioneMassima.setPrefWidth(100);
        valutazioneMassima.setEditable(true);
        
        valRange.getChildren().addAll(minLabel, valutazioneMinima, maxLabel, valutazioneMassima);
        valBox.getChildren().addAll(valLabel, valRange);

        // Distribuzione
        VBox distribBox = new VBox(8);
        Label distribLabel = new Label("Modalità Distribuzione Paper");
        distribLabel.getStyleClass().add("form-label");
        modalitaDistribuzione = new ChoiceBox<>();
        modalitaDistribuzione.getStyleClass().add("modern-choice-box");
        modalitaDistribuzione.getItems().addAll("MANUALE", "AUTOMATICA", "BROADCAST");
        modalitaDistribuzione.setValue("MANUALE");
        modalitaDistribuzione.setPrefWidth(200);
        distribBox.getChildren().addAll(distribLabel, modalitaDistribuzione);

        rightColumn.getChildren().addAll(valBox, distribBox);

        configGrid.add(leftColumn, 0, 0);
        configGrid.add(rightColumn, 1, 0);

        // Configuration info
        VBox configInfoBox = new VBox(10);
        configInfoBox.getStyleClass().add("info-box");
        Text configInfoTitle = new Text("⚙️ Configurazione Parametri");
        configInfoTitle.getStyleClass().add("info-title");
        Text configInfoText = new Text("Definisci i parametri di valutazione e gestione per la conferenza. " +
                "I revisori valuteranno i paper nel range specificato e la distribuzione determina come vengono assegnati.");
        configInfoText.getStyleClass().add("info-text");
        configInfoText.setWrappingWidth(800);
        configInfoBox.getChildren().addAll(configInfoTitle, configInfoText);

        section.getChildren().addAll(configGrid, configInfoBox);
        return section;
    }

    private DatePicker createDatePicker(String promptText) {
        DatePicker datePicker = new DatePicker();
        datePicker.getStyleClass().add("modern-date-picker");
        datePicker.setPromptText(promptText);
        datePicker.setPrefWidth(250);
        return datePicker;
    }

    private VBox createDateBox(String labelText, DatePicker datePicker) {
        VBox box = new VBox(8);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        box.getChildren().addAll(label, datePicker);
        return box;
    }

    private void setupValidation(Node createBtn, Text validationMessage) {
        Runnable validate = () -> {
            boolean emptyFields = acronimoField.getText().isEmpty() || titoloField.getText().isEmpty() || 
                    descrizioneArea.getText().isEmpty() || luogoField.getText().isEmpty() || 
                    scadenzaSottomissione.getValue() == null || scadenzaRevisioni.getValue() == null ||
                    dataGraduatoria.getValue() == null || scadenzaCameraReady.getValue() == null || 
                    scadenzaFeedbackEditore.getValue() == null || scadenzaVersioneFinale.getValue() == null;

            LocalDate today = LocalDate.now();
            boolean datesOk = !emptyFields &&
                    scadenzaSottomissione.getValue().isAfter(today) &&
                    scadenzaRevisioni.getValue().isAfter(today) &&
                    dataGraduatoria.getValue().isAfter(today) &&
                    scadenzaCameraReady.getValue().isAfter(today) &&
                    scadenzaFeedbackEditore.getValue().isAfter(today) &&
                    scadenzaVersioneFinale.getValue().isAfter(today);

            boolean inOrder = !emptyFields &&
                    scadenzaSottomissione.getValue().isBefore(scadenzaRevisioni.getValue()) &&
                    scadenzaRevisioni.getValue().isBefore(dataGraduatoria.getValue()) &&
                    dataGraduatoria.getValue().isBefore(scadenzaCameraReady.getValue()) &&
                    scadenzaCameraReady.getValue().isBefore(scadenzaFeedbackEditore.getValue()) &&
                    scadenzaFeedbackEditore.getValue().isBefore(scadenzaVersioneFinale.getValue());

            boolean numericValid = numeroMinimoRevisori.getValue() >= 1 && numeroVincitori.getValue() >= 1;
            boolean rangeValid = valutazioneMinima.getValue() < valutazioneMassima.getValue();

            String errorMessage = "";
            if (emptyFields) {
                errorMessage = "⚠️ Tutti i campi devono essere compilati";
            } else if (!datesOk) {
                errorMessage = "📅 Le date devono essere future (a partire da domani)";
            } else if (!inOrder) {
                errorMessage = "🔄 Le scadenze devono essere in ordine cronologico";
            } else if (!numericValid) {
                errorMessage = "🔢 Revisori e vincitori devono essere almeno 1";
            } else if (!rangeValid) {
                errorMessage = "📊 Valutazione minima deve essere inferiore alla massima";
            } else {
                errorMessage = "✅ Tutti i campi sono validi";
            }

            validationMessage.setText(errorMessage);
            boolean isValid = !errorMessage.startsWith("⚠️") && !errorMessage.startsWith("📅") && 
                             !errorMessage.startsWith("🔄") && !errorMessage.startsWith("🔢") && 
                             !errorMessage.startsWith("📊");
            createBtn.setDisable(!isValid);
            
            // Add visual feedback
            validationMessage.getStyleClass().removeAll("error-message", "warning-message", "success-message");
            if (isValid) {
                validationMessage.getStyleClass().add("success-message");
            } else {
                validationMessage.getStyleClass().add("error-message");
            }
        };

        // Add listeners to all form fields
        acronimoField.textProperty().addListener((obs, oldV, newV) -> validate.run());
        titoloField.textProperty().addListener((obs, oldV, newV) -> validate.run());
        descrizioneArea.textProperty().addListener((obs, oldV, newV) -> validate.run());
        luogoField.textProperty().addListener((obs, oldV, newV) -> validate.run());
        scadenzaSottomissione.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        scadenzaRevisioni.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        dataGraduatoria.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        scadenzaCameraReady.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        scadenzaFeedbackEditore.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        scadenzaVersioneFinale.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        numeroMinimoRevisori.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        valutazioneMinima.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        valutazioneMassima.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        numeroVincitori.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        modalitaDistribuzione.valueProperty().addListener((obs, oldV, newV) -> validate.run());

        validate.run(); // Initial validation
    }

    private Map<String, String> collectFormData() {
        Map<String, String> data = new HashMap<>();
        
        data.put("acronimo", acronimoField.getText());
        data.put("titolo", titoloField.getText());
        data.put("descrizione", descrizioneArea.getText());
        data.put("luogo", luogoField.getText());
        data.put("scadenzaSottomissione", scadenzaSottomissione.getValue().toString());
        data.put("scadenzaRevisioni", scadenzaRevisioni.getValue().toString());
        data.put("dataGraduatoria", dataGraduatoria.getValue().toString());
        data.put("scadenzaCameraReady", scadenzaCameraReady.getValue().toString());
        data.put("scadenzaFeedbackEditore", scadenzaFeedbackEditore.getValue().toString());
        data.put("scadenzaVersioneFinale", scadenzaVersioneFinale.getValue().toString());
        data.put("numeroMinimoRevisori", numeroMinimoRevisori.getValue().toString());
        data.put("valutazioneMinima", valutazioneMinima.getValue().toString());
        data.put("valutazioneMassima", valutazioneMassima.getValue().toString());
        data.put("numeroVincitori", numeroVincitori.getValue().toString());
        data.put("modalitaDistribuzione", modalitaDistribuzione.getValue());

        return data;
    }
}
