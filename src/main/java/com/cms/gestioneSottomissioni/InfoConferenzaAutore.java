package com.cms.gestioneSottomissioni;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class InfoConferenzaAutore {
    private final Stage stage;
    private final ControlSottomissioni ctrl;
    private final String idConferenza;
    private final boolean isIscritto;
    private Label stato;

    public InfoConferenzaAutore(Stage stage, ControlSottomissioni ctrl, String idConferenza, boolean isIscritto) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.idConferenza = idConferenza;
        this.isIscritto = isIscritto;
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenza(idConferenza)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata: " + idConferenza));

        double width = 1050;
        double height = 750;
        double leftWidth = 400;
        double rightWidth = 450;

        // Styling constants
        String cardStyle = "-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);";
        String titleStyle = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
        String sectionTitleStyle = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-padding: 0 0 10 0;";
        String labelStyle = "-fx-text-fill: #7f8c8d; -fx-font-size: 13px;";
        String valueStyle = "-fx-text-fill: #2c3e50; -fx-font-size: 13px; -fx-font-weight: 500;";
        String buttonStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;";
        String buttonHoverStyle = "-fx-background-color: #2980b9;";

        // Main title
        Label lbl = new Label(conf.getTitolo());
        lbl.setStyle(titleStyle);
        Label subTitle = new Label("[" + conf.getAcronimo() + "]");
        subTitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
        VBox titleBox = new VBox(2, lbl, subTitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 20, 0));

        // Create a helper class to handle the info section creation
        class InfoSectionCreator {
            private final String sectionTitleStyle;
            private final String labelStyle;
            private final String valueStyle;
            
            public InfoSectionCreator(String sectionTitleStyle, String labelStyle, String valueStyle) {
                this.sectionTitleStyle = sectionTitleStyle;
                this.labelStyle = labelStyle;
                this.valueStyle = valueStyle;
            }
            
            public VBox createInfoSection(String title, String... items) {
                VBox section = new VBox(5);
                Label titleLabel = new Label(title);
                titleLabel.setStyle(sectionTitleStyle);
                section.getChildren().add(titleLabel);

                for (String item : items) {
                    if (item == null || item.isEmpty()) continue;
                    String[] parts = item.split(":", 2);
                    if (parts.length == 2) {
                        HBox row = new HBox(5);
                        Label label = new Label(parts[0] + ":");
                        label.setStyle(labelStyle);
                        Label value = new Label(parts[1].trim());
                        value.setStyle(valueStyle);
                        value.setWrapText(true);
                        row.getChildren().addAll(label, value);
                        section.getChildren().add(row);
                    }
                }
                return section;
            }
        }
        
        // Initialize the section creator with styles
        InfoSectionCreator sectionCreator = new InfoSectionCreator(sectionTitleStyle, labelStyle, valueStyle);

        // Conference info section
        VBox infoConf = new VBox(15);
        infoConf.setStyle(cardStyle);
        infoConf.setPadding(new Insets(20));
        infoConf.setPrefWidth(leftWidth);

        VBox generalInfo = sectionCreator.createInfoSection("Informazioni Generali",
            "Luogo: " + conf.getLuogo(),
            "Distribuzione: " + conf.getModalitaDistribuzione(),
            "Numero Minimo Revisori: " + conf.getNumeroMinimoRevisori(),
            "Valutazione: " + conf.getValutazioneMinima() + " | " + conf.getValutazioneMassima(),
            "Numero Vincitori: " + conf.getNumeroVincitori(),
            "Editor: " + (conf.getEditor().isPresent() ? 
                conf.getEditor().get() + (ctrl.getNomeCompleto(conf.getEditor().get()).isPresent() ? 
                    " | " + ctrl.getNomeCompleto(conf.getEditor().get()).get() : "") : "<nessuno>")
        );

        VBox deadlines = sectionCreator.createInfoSection("Scadenze",
            "Sottomissione: " + conf.getScadenzaSottomissione(),
            "Revisioni: " + conf.getScadenzaRevisioni(),
            "Pubblicazione Graduatoria: " + conf.getDataGraduatoria(),
            "Versione Camera-ready: " + conf.getScadenzaCameraReady(),
            "Feedback Editore: " + conf.getScadenzaFeedbackEditore(),
            "Versione Finale: " + conf.getScadenzaVersioneFinale()
        );

        infoConf.getChildren().addAll(generalInfo, new Separator(), deadlines);

        VBox left = new VBox(20);
        left.setPrefWidth(leftWidth);

        if (isIscritto) {
            EntityArticolo art = ctrl.getDatiArticolo(idConferenza);
            VBox articoloBox = new VBox(15);
            articoloBox.setStyle(cardStyle);

            Label articoloTitle = new Label("Il Tuo Articolo");
            articoloTitle.setStyle(sectionTitleStyle);

            // Article details
            VBox articoloDetails = new VBox(8);

            Label titoloArt = new Label(art.getTitolo() != null ? art.getTitolo() : "<Nessun titolo inserito>");
            titoloArt.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            titoloArt.setWrapText(true);

            stato = new Label(art.getStato() != null ? art.getStato() : "<non assegnato>");
            stato.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");

            HBox statusBox = new HBox(5, new Label("Stato:"), stato);
            String posizioneText = art.getPosizione() != null ? art.getPosizione().toString() : "<nessuna>";
            HBox posizioneBox = new HBox(5, new Label("Posizione:"), new Label(posizioneText));
            String punteggioText = art.getPunteggio() != null ? art.getPunteggio().toString() : "0";
            HBox punteggioBox = new HBox(5, new Label("Punteggio:"), new Label(punteggioText));

            // Keywords section
            VBox keywordsSection = new VBox(5);
            Label keywordsLabel = new Label("Parole Chiave:");
            keywordsLabel.setStyle(labelStyle);

            Label paroleChiaveLabel = new Label(art.getParoleChiave() != null ? 
                art.getParoleChiave() : "<Nessuna parola chiave inserita>");
            paroleChiaveLabel.setWrapText(true);
            paroleChiaveLabel.setStyle(valueStyle);

            ScrollPane paroleChiave = new ScrollPane(paroleChiaveLabel);
            paroleChiave.setFitToWidth(true);
            paroleChiave.setPrefHeight(60);
            paroleChiave.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-focus-color: transparent;" +
                "-fx-faint-focus-color: transparent;" +
                "-fx-background-insets: 0;" +
                "-fx-padding: 0;" +
                "-fx-border-width: 0;"
            );

            keywordsSection.getChildren().addAll(keywordsLabel, paroleChiave);

            // Add all components to article details
            articoloDetails.getChildren().addAll(
                titoloArt,
                new Separator(),
                statusBox,
                posizioneBox,
                punteggioBox,
                new Separator(),
                keywordsSection
            );

            articoloBox.getChildren().addAll(articoloTitle, articoloDetails);
            left.getChildren().addAll(infoConf, articoloBox);
        } else {
            left.getChildren().add(infoConf);
        }

        // Description section
        VBox descriptionBox = new VBox(10);
        descriptionBox.setStyle(cardStyle);
        descriptionBox.setPrefWidth(rightWidth);

        Label descTitle = new Label("Descrizione");
        descTitle.setStyle(sectionTitleStyle);

        Label descrizione = new Label(conf.getDescrizione());
        descrizione.setWrapText(true);
        descrizione.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 13.5px; -fx-line-spacing: 3px;");

        ScrollPane descr = new ScrollPane(descrizione);
        descr.setFitToWidth(true);
        descr.setPrefHeight(200);
        descr.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        descr.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;" +
            "-fx-border-width: 0;"
        );

        descriptionBox.getChildren().addAll(descTitle, descr);
        VBox layoutRight = new VBox(20, descriptionBox);

        if (isIscritto) {
            VBox reviewsBox = new VBox(10);
            reviewsBox.setStyle(cardStyle);

            Label revisioniLbl = new Label("Revisioni Ricevute");
            revisioniLbl.setStyle(sectionTitleStyle);

            ListView<String> listRevisioni = new ListView<>();
            ctrl.getRevisioniArticolo(idConferenza).forEach((idRev, desc) ->
                listRevisioni.getItems().add(desc + " [" + idRev + "]")
            );

            listRevisioni.setPrefHeight(120);
            listRevisioni.setStyle(
                "-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 4;" +
                "-fx-border-color: #e9ecef;" +
                "-fx-border-radius: 4;"
            );

            // Custom cell factory for better looking list items
            listRevisioni.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-padding: 8 10;" +
                            "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 4;" +
                            "-fx-border-color: #e9ecef;" +
                            "-fx-border-radius: 4;"
                        );
                        setOnMouseEntered(e -> setStyle(
                            "-fx-padding: 8 10;" +
                            "-fx-background-color: #f1f8ff;" +
                            "-fx-background-radius: 4;" +
                            "-fx-border-color: #b3d7ff;" +
                            "-fx-border-radius: 4;"
                        ));
                        setOnMouseExited(e -> setStyle(
                            "-fx-padding: 8 10;" +
                            "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 4;" +
                            "-fx-border-color: #e9ecef;" +
                            "-fx-border-radius: 4;"
                        ));
                    }
                }
            });

            Button btnViewRev = new Button("Visualizza Revisione Selezionata");
            btnViewRev.setStyle(buttonStyle);
            btnViewRev.setOnMouseEntered(e -> btnViewRev.setStyle(buttonStyle + buttonHoverStyle));
            btnViewRev.setOnMouseExited(e -> btnViewRev.setStyle(buttonStyle));

            btnViewRev.setDisable(true);
            listRevisioni.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                btnViewRev.setDisable(newVal == null);
            });

            btnViewRev.setOnAction(e -> {
                String sel = listRevisioni.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    String idRev = sel.substring(sel.lastIndexOf("[")+1, sel.lastIndexOf("]"));
                    ctrl.visualizzaRevisione(idRev);
                }
            });

            if (listRevisioni.getItems().isEmpty()) {
                Label noReviews = new Label("Nessuna revisione disponibile");
                noReviews.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                reviewsBox.getChildren().addAll(revisioniLbl, noReviews);
            } else {
                reviewsBox.getChildren().addAll(revisioniLbl, listRevisioni, btnViewRev);
            }

            layoutRight.getChildren().add(reviewsBox);
        }

        HBox infoSection = new HBox(20, left, layoutRight);
        infoSection.setPadding(new Insets(10));
        infoSection.setAlignment(Pos.TOP_LEFT);

        VBox layout = new VBox(15, titleBox, infoSection);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f7fa;");

        // Add subtle animation to the main container
        FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300), layout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        if (isIscritto) {
            // Create action buttons
            Button btnSottometti = createButton("Sottometti Articolo", "#2ecc71");
            Button btnCameraReady = createButton("Invia Camera-ready", "#3498db");
            Button btnFinal = createButton("Invia Versione Finale", "#9b59b6");

            // Create view buttons
            Button btnVisualizza = createButton("Visualizza Articolo", "#95a5a6");
            Button btnVisualCamera = createButton("Visualizza Camera-ready", "#95a5a6");
            Button btnVisualFinal = createButton("Visualizza Versione Finale", "#95a5a6");
            Button btnFeedback = createButton("Feedback Editor", "#e67e22");

            // Set button actions
            btnSottometti.setOnAction(e -> {
                boolean successo = ctrl.sottomettiArticolo(idConferenza);
                if (successo) {
                    stato.setText("Sottomesso");
                    showSuccessNotification("Articolo sottomesso con successo!");
                }
            });

            btnCameraReady.setOnAction(e -> {
                ctrl.inviaCameraready(idConferenza);
                showSuccessNotification("Versione camera-ready inviata con successo!");
            });

            btnFinal.setOnAction(e -> {
                ctrl.inviaVersioneFinale(idConferenza);
                showSuccessNotification("Versione finale inviata con successo!");
            });

            btnVisualizza.setOnAction(e -> ctrl.visualizzaArticolo(idConferenza));
            btnVisualCamera.setOnAction(e -> ctrl.visualizzaCameraready(idConferenza));
            btnVisualFinal.setOnAction(e -> ctrl.visualizzaVersioneFinale(idConferenza));
            btnFeedback.setOnAction(e -> ctrl.visualizzaFeedback(idConferenza));

            // Create button containers with icons
            HBox actionButtons = new HBox(10, btnSottometti, btnCameraReady, btnFinal);
            HBox viewButtons = new HBox(10, btnVisualizza, btnVisualCamera, btnVisualFinal, btnFeedback);

            // Set button container styles
            actionButtons.setPadding(new Insets(10, 0, 0, 0));
            viewButtons.setPadding(new Insets(0, 0, 10, 0));
            actionButtons.setAlignment(Pos.CENTER);
            viewButtons.setAlignment(Pos.CENTER);

            // Create button box with subtle background
            VBox buttonBox = new VBox(5, actionButtons, viewButtons);
            buttonBox.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
            );

            // Add button box to layout
            layout.getChildren().add(buttonBox);
        }

        // Create header with custom style
        HeaderBar header = new HeaderBar(ctrl.getAccountController(), this::show);
        header.getBtnBack().setOnAction(e -> ctrl.apriHomepageAutore());

        // Create root container with proper spacing
        VBox root = new VBox(header, layout);
        VBox.setVgrow(layout, Priority.ALWAYS);

        // Set up the scene with a nice background color
        Scene scene = new Scene(root, width, height);
        scene.setFill(Color.web("#f5f7fa"));

        // Apply CSS styles
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Dettagli Conferenza - " + conf.getAcronimo());
        stage.show();
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

    // Helper method to show success notifications
    private void showSuccessNotification(String message) {
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
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Move up
        TranslateTransition moveUp = new TranslateTransition(Duration.millis(300), notification);
        moveUp.setFromY(50);
        moveUp.setToY(20);

        // After delay, fade out and remove
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
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
}