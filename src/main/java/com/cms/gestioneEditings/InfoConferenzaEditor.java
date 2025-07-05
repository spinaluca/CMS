package com.cms.gestioneEditings;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.gestioneAccount.ControlAccount;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InfoConferenzaEditor {
    private final Stage stage;
    private final ControlEditings ctrl;
    private final ControlAccount ctrlAccount;
    private final String confId;

    public InfoConferenzaEditor(Stage stage, ControlEditings ctrl, ControlAccount ctrlAccount, String confId) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
        this.confId = confId;
    }
    
    // Helper method to add a detail row with label and value
    private void addDetailRow(VBox container, String label, Object value, String labelStyle, String valueStyle) {
        if (value == null) return;
        
        HBox row = new HBox(8);
        Label lbl = new Label(label);
        lbl.setStyle(labelStyle);
        
        Label val = new Label(value.toString());
        val.setStyle(valueStyle);
        val.setWrapText(true);
        
        row.getChildren().addAll(lbl, val);
        container.getChildren().add(row);
    }
    
    // Helper method to create styled buttons with modern look
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

        // Disabled state style
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

        return button;
    }
    
    // Helper method to darken color for hover effect
    private String darkenColor(String color) {
        // Simple color darkening for hover effect
        switch(color) {
            case "#2ecc71": return "#27ae60"; // green
            case "#3498db": return "#2980b9"; // blue
            case "#9b59b6": return "#8e44ad"; // purple
            case "#e67e22": return "#d35400"; // orange
            case "#e74c3c": return "#c0392b"; // red
            case "#95a5a6": return "#7f8c8d"; // gray
            default: return color;
        }
    }
    
    // Helper method to show notifications
    private void showNotification(String message) {
        Label notification = new Label(message);
        notification.setStyle(
            "-fx-text-fill: white;" +
            "-fx-background-color: rgba(46, 204, 113, 0.9);" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 10 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
        );

        StackPane root = (StackPane) stage.getScene().getRoot();
        notification.setTranslateY(20);

        // Add and animate notification
        root.getChildren().add(notification);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Move up
        TranslateTransition moveUp = new TranslateTransition(javafx.util.Duration.millis(300), notification);
        moveUp.setFromY(50);
        moveUp.setToY(20);

        // After delay, fade out and remove
        PauseTransition delay = new PauseTransition(javafx.util.Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(300), notification);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> root.getChildren().remove(notification));
            fadeOut.play();
        });

        // Play animations
        fadeIn.play();
        moveUp.play();
        delay.play();
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
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-padding: 20;"
        );

        // Conference details section
        VBox conferenceDetails = new VBox(8);
        Label detailsTitle = new Label("Informazioni Conferenza");
        detailsTitle.setStyle(sectionTitleStyle);

        addDetailRow(conferenceDetails, "Luogo:", conf.getLuogo(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Distribuzione:", conf.getModalitaDistribuzione(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Scadenza Camera-ready:", conf.getScadenzaCameraReady(), labelStyle, valueStyle);
        addDetailRow(conferenceDetails, "Scadenza Feedback:", conf.getScadenzaFeedbackEditore(), labelStyle, valueStyle);

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
        
        // Right side - Description
        VBox rightPanel = new VBox(15);
        rightPanel.setStyle("-fx-padding: 0; -fx-pref-width: 400;");
        rightPanel.getChildren().add(descriptionBox);
        
        // Add panels to main content
        mainContent.getChildren().addAll(leftPanel, vSeparator, rightPanel);
        
        // Articles Table Section
        VBox articlesCard = new VBox(15);
        articlesCard.setStyle(cardStyle + " -fx-padding: 20;");
        
        // Add main content to container
        contentContainer.getChildren().addAll(titleBox, mainContent, articlesCard);

        Label articlesTitle = new Label("Versioni Camera-ready");
        articlesTitle.setStyle(sectionTitleStyle);

        // Create table with modern styling
        TableView<EntityArticolo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-background-radius: 4;" +
            "-fx-border-color: #e9ecef;" +
            "-fx-border-radius: 4;"
        );

        // Title column
        TableColumn<EntityArticolo, String> colTitolo = new TableColumn<>("Titolo");
        colTitolo.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTitolo()));
        colTitolo.setMinWidth(200);

        // Author column
        TableColumn<EntityArticolo, String> colAut = new TableColumn<>("Autore");
        colAut.setCellValueFactory(data -> {
            String email = data.getValue().getAutoreId();
            String label = ctrlAccount.getDatiUtente(email)
                .map(u -> u.getNome() + " " + u.getCognome() + " (" + email + ")")
                .orElse(email);
            return new ReadOnlyStringWrapper(label);
        });

        // Feedback status column
        TableColumn<EntityArticolo, String> colFeed = new TableColumn<>("Feedback");
        colFeed.setCellValueFactory(data -> {
            boolean hasFeedback = ctrl.hasFeedback(data.getValue().getId());
            String status = hasFeedback ? "✅ Inviato" : "❌ Mancante";
            return new ReadOnlyStringWrapper(status);
        });
        colFeed.setStyle("-fx-alignment: CENTER;");

        // Add columns to table
        table.getColumns().addAll(colTitolo, colAut, colFeed);

        // Style table rows
        table.setRowFactory(tv -> new TableRow<EntityArticolo>() {
            @Override
            protected void updateItem(EntityArticolo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle(
                        "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #e9ecef;" +
                        "-fx-border-width: 0 0 1 0;"
                    );
                    setOnMouseEntered(e -> setStyle(
                        "-fx-background-color: #f1f8ff;" +
                        "-fx-border-color: #b3d7ff;" +
                        "-fx-border-width: 1 0 1 0;"
                    ));
                    setOnMouseExited(e -> setStyle(
                        "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #e9ecef;" +
                        "-fx-border-width: 0 0 1 0;"
                    ));
                }
            }
        });

        // Load articles
        List<EntityArticolo> articoli = ctrl.getCameraReadyArticoli(confId).stream()
                .sorted(Comparator.comparing(EntityArticolo::getTitolo))
                .collect(Collectors.toList());
        table.getItems().addAll(articoli);

        // Action buttons
        Button btnVisualizza = createButton("Visualizza Camera-ready", "#3498db");
        btnVisualizza.setOnAction(e -> {
            EntityArticolo sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.visualizzaVersioneCameraready(sel.getId());
            } else {
                showNotification("Seleziona una versione camera-ready");
            }
        });

        Button btnFeedback = createButton("Invia Feedback", "#2ecc71");
        btnFeedback.setDisable(true);
        btnFeedback.setOnAction(e -> {
            EntityArticolo sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ctrl.inviaFeedback(confId, sel.getId());
                showNotification("Feedback inviato con successo!");
                show(); // refresh view
            }
        });

        // Disable feedback button if feedback already exists for selected article
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean hasFeedback = ctrl.hasFeedback(newVal.getId());
                btnFeedback.setDisable(hasFeedback);
                btnFeedback.setText(hasFeedback ? "Feedback Inviato" : "Invia Feedback");
            }
        });

        HBox buttonsBox = new HBox(15, btnVisualizza, btnFeedback);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        // Add components to articles card
        articlesCard.getChildren().addAll(articlesTitle, table, buttonsBox);

        // Add fade animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Create header with back button
        HeaderBar header = new HeaderBar(ctrlAccount, this::show);
        header.getBtnBack().setOnAction(e -> ctrlAccount.apriHomepageEditor());

        // Create root layout
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(new ScrollPane(contentContainer));
        
        // Set scene
        Scene scene = new Scene(root, 1200, 800);
        scene.setFill(Color.web("#f5f7fa"));
        
        // Apply CSS styles if available
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // Stylesheet not found, continue without it
        }

        // Configure and show stage
        stage.setScene(scene);
        stage.setTitle("Dettagli Conferenza - " + conf.getAcronimo());

        // Play animation
        fadeIn.play();
        stage.show();
    }
}