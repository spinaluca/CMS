package com.cms.gestioneConferenze;

import com.cms.common.PopupInserimento;
import com.cms.common.HeaderBar;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import javafx.animation.FadeTransition;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class InfoConferenzaChair {
    private final Stage stage;
    private final ControlConferenze ctrl;
    private final ControlAccount ctrl2;
    private final String confId;

    public InfoConferenzaChair(Stage stage, ControlConferenze ctrl, ControlAccount ctrl2, String confId) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
        this.confId = confId;
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenza(confId)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + confId));

        // Styling constants
        String cardStyle = "-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);";
        String titleStyle = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
        String sectionTitleStyle = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-padding: 0 0 10 0;";
        String labelStyle = "-fx-text-fill: #7f8c8d; -fx-font-size: 13px;";
        String valueStyle = "-fx-text-fill: #2c3e50; -fx-font-size: 13px; -fx-font-weight: 500;";

        // Main title
        Label titleLabel = new Label(conf.getTitolo());
        titleLabel.setStyle(titleStyle);
        Label subTitle = new Label("[" + conf.getAcronimo() + "]");
        subTitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
        VBox titleBox = new VBox(2, titleLabel, subTitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 20, 0));

        // Main content container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-padding: 20;"
        );

        // Conference details section
        VBox conferenceDetails = new VBox(8);
        Label detailsTitle = new Label("Informazioni Conferenza");
        detailsTitle.setStyle(sectionTitleStyle);

        // Add conference details
        addDetailRow(conferenceDetails, "Luogo:", conf.getLuogo(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Distribuzione:", conf.getModalitaDistribuzione(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Scadenza Sottomissione:", conf.getScadenzaSottomissione(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Scadenza Revisioni:", conf.getScadenzaRevisioni(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Pubblicazione Graduatoria:", conf.getDataGraduatoria(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Scadenza Camera-ready:", conf.getScadenzaCameraReady(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Scadenza Feedback Editori:", conf.getScadenzaFeedbackEditore(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Scadenza Versione Finale:", conf.getScadenzaVersioneFinale(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Numero Minimo Revisori:", String.valueOf(conf.getNumeroMinimoRevisori()), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Valutazione Minima:", String.valueOf(conf.getValutazioneMinima()) + " | Massima: " + conf.getValutazioneMassima(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Numero Vincitori:", String.valueOf(conf.getNumeroVincitori()), labelStyle, valueStyle);

        // Editor info
        String editorInfo = conf.getEditor()
                .map(email -> email + ctrl2.getDatiUtente(email).map(u -> " | " + u.getNome() + " " + u.getCognome()).orElse(""))
                .orElse("<nessuno>");
        addDetailRow(conferenceDetails, "Editor:", editorInfo, labelStyle, valueStyle);

        // Description section
        VBox descriptionBox = new VBox(8);
        Label descTitle = new Label("Descrizione");
        descTitle.setStyle(sectionTitleStyle);

        Label descriptionLabel = new Label(conf.getDescrizione());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle(valueStyle + " -fx-line-spacing: 3px;");

        ScrollPane descScroll = new ScrollPane(descriptionLabel);
        descScroll.setFitToWidth(true);
        descScroll.setPrefHeight(120);
        descScroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-border-width: 0;"
        );

        descriptionBox.getChildren().addAll(descTitle, descScroll);
        
        // Create a horizontal container for the main content
        HBox mainContent = new HBox(30);
        mainContent.setAlignment(Pos.TOP_LEFT);
        mainContent.setStyle(
            "-fx-padding: 20;" +
            "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);"
        );
        
        // Left side - Conference details
        VBox leftPanel = new VBox(15);
        leftPanel.setStyle("-fx-min-width: 300; -fx-max-width: 400;");
        leftPanel.getChildren().add(conferenceDetails);
        
        // Vertical separator
        Separator vSeparator = new Separator(Orientation.VERTICAL);
        vSeparator.setPadding(new Insets(0, 20, 0, 20));
        
        // Right side - Description and statistics
        VBox rightPanel = new VBox(30);
        rightPanel.setStyle("-fx-padding: 0; -fx-pref-width: 400;");
        
        // Description section
        descriptionBox.setStyle("-fx-padding: 0;");
        rightPanel.getChildren().add(descriptionBox);
        
        // Statistics section
        VBox statsSection = new VBox(15);
        statsSection.setStyle("-fx-padding: 20; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        Label statsTitle = new Label("Statistiche");
        statsTitle.setStyle(sectionTitleStyle);
        
        // Simple stats that don't rely on tableArticoli or revisori
        String statoConferenza = "Non specificato";
        if (conf.getScadenzaSottomissione() != null) {
            statoConferenza = "In corso";
        }
        if (conf.getScadenzaRevisioni() != null && LocalDate.now().isAfter(conf.getScadenzaRevisioni())) {
            statoConferenza = "Chiusa";
        }
        
        VBox statsCards = new VBox(10,
            createStatCard("Stato", statoConferenza, "#9b59b6")
        );
        
        // Add deadline warning if close
        LocalDate now = LocalDate.now();
        LocalDate submissionDeadline = conf.getScadenzaSottomissione();
        if (submissionDeadline != null) {
            long daysLeft = ChronoUnit.DAYS.between(now, submissionDeadline);
            if (daysLeft <= 7 && daysLeft >= 0) {
                String warningText = "Scadenza sottomissioni tra " + daysLeft + " giorni";
                Label deadlineWarning = new Label(warningText);
                deadlineWarning.setStyle(
                    "-fx-text-fill: #d35400;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 8 12;" +
                    "-fx-background-color: #fdebd0;" +
                    "-fx-background-radius: 4;"
                );
                statsSection.getChildren().add(deadlineWarning);
            }
        }
        
        statsSection.getChildren().addAll(statsTitle, statsCards);
        
        // Add statistics to the right panel
        rightPanel.getChildren().add(statsSection);
        
        // Add panels to main content with vertical separator
        mainContent.getChildren().addAll(leftPanel, vSeparator, rightPanel);
        
        // Add main content to the container
        mainContainer.getChildren().addAll(titleBox, mainContent);
        
        // Add articles table section below the main content
        VBox articlesCard = new VBox(15);
        articlesCard.setStyle(cardStyle);
        articlesCard.setPadding(new Insets(20));
        
        Label articlesTitle = new Label("Articoli Sottomessi");
        articlesTitle.setStyle(sectionTitleStyle);

        // Create table with modern styling
        TableView<EntityArticolo> tableArticoli = new TableView<>();
        tableArticoli.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableArticoli.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #e9ecef;" +
                        "-fx-border-radius: 4;"
        );

        // Position column
        TableColumn<EntityArticolo, Integer> colPos = new TableColumn<>("#");
        colPos.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPosizione()));
        colPos.setStyle("-fx-alignment: CENTER;");

        // Title column
        TableColumn<EntityArticolo, String> colTit = new TableColumn<>("Titolo");
        colTit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitolo()));
        colTit.setMinWidth(200);

        // Author column
        TableColumn<EntityArticolo, String> colAut = new TableColumn<>("Autore");
        colAut.setCellValueFactory(data -> {
            String email = data.getValue().getAutoreId();
            return new ReadOnlyStringWrapper(
                    ctrl2.getDatiUtente(email)
                            .map(u -> u.getNome() + " " + u.getCognome() + " (" + email + ")")
                            .orElse(email)
            );
        });

        // Reviews count column
        TableColumn<EntityArticolo, Integer> colRev = new TableColumn<>("Rev.");
        colRev.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNumRevisioni()));
        colRev.setStyle("-fx-alignment: CENTER;");

        // Score column
        TableColumn<EntityArticolo, String> colScore = new TableColumn<>("Punteggio");
        colScore.setCellValueFactory(data -> {
            Double score = data.getValue().getPunteggio();
            return new ReadOnlyStringWrapper(score != null ? String.format("%.2f", score) : "N/A");
        });
        colScore.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Status column with color coding
        TableColumn<EntityArticolo, String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStato()));
        colStato.setCellFactory(column -> new TableCell<EntityArticolo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(
                            "-fx-alignment: CENTER;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 3 8;" +
                                    "-fx-background-radius: 10;"
                    );

                    // Color coding based on status
                    switch (item.toLowerCase()) {
                        case "accettato":
                            setStyle(getStyle() + "-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "rifiutato":
                            setStyle(getStyle() + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        case "in revisione":
                            setStyle(getStyle() + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        default:
                            setStyle(getStyle() + "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;");
                    }
                }
            }
        });

        // Add columns to table
        tableArticoli.getColumns().addAll(colPos, colTit, colAut, colRev, colScore, colStato);

        // Load and sort articles
        tableArticoli.getItems().addAll(ctrl.getArticoliConferenza(confId).stream()
                .sorted(Comparator.comparingInt(EntityArticolo::getPosizione))
                .collect(Collectors.toList()));

        // Add table to articles card
        articlesCard.getChildren().addAll(articlesTitle, tableArticoli);
        VBox.setVgrow(tableArticoli, Priority.ALWAYS);

        // Reviewers Section
        VBox reviewersCard = new VBox(15);
        reviewersCard.setStyle(cardStyle);

        Label reviewersTitle = new Label("Revisori");
        reviewersTitle.setStyle(sectionTitleStyle);

        // Create reviewers list with status
        ListView<String> lvRev = new ListView<>();
        lvRev.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-color: #e9ecef;" +
                        "-fx-border-radius: 4;"
        );

        // Populate reviewers list with status
        Map<String, String> revisori = ctrl.getRevisoriConStato(confId);
        revisori.forEach((email, statoRevisore) -> {
            String nome = ctrl2.getDatiUtente(email)
                    .map(u -> u.getNome() + " " + u.getCognome())
                    .orElse("");
            String statusText = "";

            // Color code status
            switch (statoRevisore.toLowerCase()) {
                case "attivo":
                    statusText = "[✅ " + statoRevisore + "]";
                    break;
                case "in attesa":
                    statusText = "[⏳ " + statoRevisore + "]";
                    break;
                case "rifiutato":
                    statusText = "[❌ " + statoRevisore + "]";
                    break;
                default:
                    statusText = "[" + statoRevisore + "]";
            }
            
            lvRev.getItems().add(email + (nome.isEmpty() ? "" : " | " + nome) + " " + statusText);
        });

        // Action buttons
        Button btnInvite = createButton("Invita Revisore", "#3498db");
        btnInvite.setOnAction(e -> new PopupInserimento()
                .promptEmail("revisore")
                .ifPresent(email -> {
                    ctrl.invitaRevisore(email, confId);
                    showNotification("Invito inviato a " + email);
                    show();
                }));

        Button btnRemove = createButton("Rimuovi", "#e74c3c");
        btnRemove.setDisable(true);
        btnRemove.setOnAction(e -> {
            String sel = lvRev.getSelectionModel().getSelectedItem();
            if (sel != null) {
                String email = sel.split(" ")[0]; // Extract just the email
                ctrl.rimuoviRevisore(email, confId);
                showNotification("Revisore rimosso: " + email);
                show();
            }
        });

        // ...

        // Editor Section
        VBox editorCard = new VBox(15);
        editorCard.setStyle(cardStyle);

        Label editorTitle = new Label("Gestione Editor");
        editorTitle.setStyle(sectionTitleStyle);

        // Editor info
        String currentEditor = conf.getEditor()
                .map(email -> {
                    String userInfo = ctrl2.getDatiUtente(email)
                            .map(u -> u.getNome() + " " + u.getCognome() + " (" + email + ")")
                            .orElse(email);
                    return "Editor attuale: " + userInfo;
                })
                .orElse("Nessun editor assegnato");

        Label editorLabel = new Label(currentEditor);
        editorLabel.setStyle(valueStyle);

        // Editor action buttons
        HBox editorButtons = new HBox(10);
        editorButtons.setAlignment(Pos.CENTER);

        Button btnAddEditor = createButton("Aggiungi Editor", "#2ecc71");
        btnAddEditor.setDisable(conf.getEditor().isPresent());
        btnAddEditor.setOnAction(e -> new PopupInserimento()
                .promptEmail("editor")
                .ifPresent(email -> {
                    ctrl.aggiungiEditor(email, confId);
                    showNotification("Editor aggiunto: " + email);
                    show();
                }));

        Button btnRemoveEditor = createButton("Rimuovi Editor", "#e74c3c");
        btnRemoveEditor.setDisable(!conf.getEditor().isPresent());
        btnRemoveEditor.setOnAction(e -> {
            // Remove editor by setting it to null
            conf.getEditor().ifPresent(email -> {
                ctrl.aggiungiEditor(null, confId);
                showNotification("Editor rimosso");
                show();
            });
        });

        editorButtons.getChildren().addAll(btnAddEditor, btnRemoveEditor);

        // Add components to editor card
        editorCard.getChildren().addAll(editorTitle, editorLabel, editorButtons);

        // Navigation button
        Button btnBack = createButton("Torna alla Home", "#6b7280");
        btnBack.setOnAction(e -> new HomepageChair(stage, ctrl, ctrl2).show());

        // Create articles and reviewers container
        HBox listsBox = new HBox(20, articlesCard, reviewersCard);
        listsBox.setPadding(new Insets(0, 0, 20, 0));

        // Create main layout container
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setStyle("-fx-background-color: #f5f7fa;");
        
        // Add all components to main content
        contentContainer.getChildren().addAll(titleBox, mainContent, listsBox, editorCard, btnBack);

        // Add fade animation
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300), contentContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Create header with back button
        HeaderBar header = new HeaderBar(ctrl2, this::show);
        header.getBtnBack().setOnAction(e -> ctrl2.apriHomepageChair());

        // Create root layout
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(new ScrollPane(contentContainer));

        // Set up scene
        Scene scene = new Scene(root, 1050, 750);
        scene.setFill(Color.web("#f5f7fa"));

        // Apply CSS styles if available
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // Stylesheet not found, continue without it
        }

        // Configure and show stage
        stage.setScene(scene);
        stage.setTitle("Gestione Conferenza - " + conf.getAcronimo());

        // Play animation
        fadeIn.play();
        stage.show();
    }

    private void addDetailRow(VBox container, String label, Object value, String labelStyle, String valueStyle) {
        if (value == null) return;
        
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelLbl = new Label(label);
        labelLbl.setStyle(labelStyle);

        String valueStr = value.toString();
        if (value instanceof java.time.LocalDate) {
            valueStr = ((java.time.LocalDate) value).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        
        Label valueLbl = new Label(valueStr);
        valueLbl.setStyle(valueStyle);

        row.getChildren().addAll(labelLbl, valueLbl);
        container.getChildren().add(row);
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 12 24 12 24; " +
            "-fx-background-radius: 8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);"
        );

        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: " + darkenColor(color) + "; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 12 24 12 24; " +
            "-fx-background-radius: 8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 3);"
        ));

        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 12 24 12 24; " +
            "-fx-background-radius: 8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);"
        ));

        return button;
    }
    
    private String darkenColor(String color) {
        // Simple color darkening for hover effect
        switch(color) {
            case "#10b981": return "#0d9f74"; // teal
            case "#2563eb": return "#1d4ed8"; // blue
            case "#8b5cf6": return "#7c3aed"; // purple
            case "#f59e0b": return "#d97706"; // amber
            case "#ef4444": return "#dc2626"; // red
            case "#6b7280": return "#4b5563"; // gray
            default: return color;
        }
    }

    private HBox createStatCard(String title, String value, String color) {
        HBox card = new HBox(10);
        card.setStyle(
            "-fx-background-color: " + color + "20;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: " + color + "40;"
        );
        
        // Color indicator
        Region indicator = new Region();
        indicator.setPrefSize(4, 40);
        indicator.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");
        
        // Content
        VBox content = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        content.getChildren().addAll(titleLabel, valueLabel);
        card.getChildren().addAll(indicator, content);
        
        return card;
    }
    
    private void showNotification(String message) {
        // Implement notification logic here
    }
}