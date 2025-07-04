package com.cms.gestioneSottomissioni;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class HomepageAutore {
    private final Stage stage;
    private final ControlSottomissioni ctrl;
    private final ControlAccount ctrlAccount;
    private final EntityUtente autore;

    public HomepageAutore(Stage stage, ControlSottomissioni ctrl, ControlAccount ctrlAccount, EntityUtente autore) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
        this.autore = autore;
    }

    public void show() {
        ObservableList<EntityConferenza> conferenzeIscritto = FXCollections.observableArrayList();
        ObservableList<EntityConferenza> conferenzeNonIscritto = FXCollections.observableArrayList();

        for (EntityConferenza conf : ctrl.getConferenzeAutore()) {
            if (ctrl.isAutoreIscritto(conf.getId())) {
                conferenzeIscritto.add(conf);
            } else {
                conferenzeNonIscritto.add(conf);
            }
        }

        // Create main layout with modern card-based design
        VBox mainLayout = new VBox(25);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getStyleClass().add("dashboard-container");

        // Welcome section
        VBox welcomeSection = createWelcomeSection();

        // Enrolled conferences section
        VBox enrolledSection = createEnrolledConferencesSection(conferenzeIscritto);

        // Available conferences section
        VBox availableSection = createAvailableConferencesSection(conferenzeNonIscritto);

        mainLayout.getChildren().addAll(welcomeSection, enrolledSection, availableSection);

        // Wrap in scroll pane for better responsiveness
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-scroll-pane");

        HeaderBar header = new HeaderBar(ctrlAccount, this::show);
        header.getBtnBack().setOnAction(e -> ctrlAccount.apriHomepageGenerale());
        VBox root = new VBox(header, scrollPane);

        Scene scene = new Scene(root, 1100, 750);
        // Load the CSS file
        String cssFile = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(cssFile);
        
        stage.setScene(scene);
        stage.setTitle("Dashboard Autore - " + autore.getNome() + " " + autore.getCognome());
        stage.show();
    }

    private VBox createWelcomeSection() {
        VBox welcomeSection = new VBox(10);
        welcomeSection.getStyleClass().add("welcome-section");
        welcomeSection.setAlignment(Pos.CENTER_LEFT);

        Text welcomeTitle = new Text("Benvenuto, " + autore.getNome() + "!");
        welcomeTitle.getStyleClass().add("welcome-title");

        welcomeSection.getChildren().add(welcomeTitle);
        return welcomeSection;
    }



    @SuppressWarnings("unchecked")
    private VBox createEnrolledConferencesSection(ObservableList<EntityConferenza> conferenzeIscritto) {
        VBox section = new VBox(15);
        section.getStyleClass().add("conference-section");

        Label sectionTitle = new Label("Le tue conferenze");
        sectionTitle.getStyleClass().add("section-title");

        if (conferenzeIscritto.isEmpty()) {
            Label emptyLabel = new Label("Non sei iscritto a nessuna conferenza al momento.");
            emptyLabel.getStyleClass().add("empty-message");
            section.getChildren().addAll(sectionTitle, emptyLabel);
            return section;
        }

        // Create table with the same structure as HomepageChair
        TableView<EntityConferenza> table = new TableView<>();
        table.setItems(conferenzeIscritto);

        // Acronimo column
        TableColumn<EntityConferenza, String> colA = new TableColumn<>("Acronimo");
        colA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcronimo()));
        colA.setPrefWidth(150);
        colA.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT;");

        // Titolo column
        TableColumn<EntityConferenza, String> colT = new TableColumn<>("Titolo");
        colT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitolo()));
        colT.setPrefWidth(300);
        colT.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");

        // Luogo column
        TableColumn<EntityConferenza, String> colL = new TableColumn<>("Luogo");
        colL.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLuogo()));
        colL.setPrefWidth(150);
        colL.setStyle("-fx-font-size: 13px; -fx-alignment: CENTER_LEFT;");

        // Descrizione column with text wrapping
        TableColumn<EntityConferenza, String> colD = new TableColumn<>("Descrizione");
        colD.setCellValueFactory(c -> {
            String descrizione = c.getValue().getDescrizione();
            if (descrizione != null && descrizione.length() > 200) {
                descrizione = descrizione.substring(0, 200) + "...";
            }
            return new SimpleStringProperty(descrizione);
        });
        
        colD.setCellFactory(tc -> new TableCell<EntityConferenza, String>() {
            private final Text text = new Text();
            private static final int TEXT_PADDING = 24;

            {
                text.setStyle("-fx-font-size: 13px; -fx-fill: #4b5563;");
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(TEXT_PADDING));
                setPrefHeight(Region.USE_COMPUTED_SIZE);
                setWrapText(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });

        // Bind column widths
        colD.prefWidthProperty().bind(
            table.widthProperty()
                .subtract(colA.widthProperty())
                .subtract(colT.widthProperty())
                .subtract(colL.widthProperty())
                .subtract(2) // Account for borders
        );

        table.getColumns().addAll(colA, colT, colL, colD);
        table.setFixedCellSize(50);
        table.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 2);" +
            "-fx-background-radius: 8;"
        );
        
        // Add hover and click effects
        table.setRowFactory(tv -> {
            TableRow<EntityConferenza> row = new TableRow<>();
            
            // Hover effect that doesn't override selection
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (row.isSelected()) return; // Skip hover effect if row is selected
                if (isNowHovered) {
                    row.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand;");
                } else {
                    row.setStyle(
                        row.getIndex() % 2 == 0 ? 
                        "-fx-background-color: #ffffff;" : 
                        "-fx-background-color: #f8fafc;"
                    );
                }
            });
            
            // Alternating row colors with selection support
            row.styleProperty().bind(
                Bindings.when(row.selectedProperty())
                    .then("-fx-background-color: #e2e8f0; -fx-cursor: default;")
                    .otherwise(
                        Bindings.when(
                            Bindings.createBooleanBinding(
                                () -> row.getIndex() % 2 == 0,
                                row.indexProperty()
                            )
                        )
                        .then("-fx-background-color: #ffffff;")
                        .otherwise("-fx-background-color: #f8fafc;")
                    )
            );
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    EntityConferenza selected = row.getItem();
                    new InfoConferenzaAutore(stage, ctrl, selected.getId(), false).show();
                }
            });
            return row;
        });

        // Add action buttons
        HBox actionButtons = createActionButtons(table, true);
        
        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().addAll(sectionTitle, table, actionButtons);
        return section;
    }

    private VBox createAvailableConferencesSection(ObservableList<EntityConferenza> conferenzeNonIscritto) {
        VBox section = new VBox(15);
        section.getStyleClass().add("table-section");

        // Section header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("section-header");

        Text sectionTitle = new Text("🔍 Conferenze Disponibili");
        sectionTitle.getStyleClass().add("section-title");

        Label countBadge = new Label(String.valueOf(conferenzeNonIscritto.size()));
        countBadge.getStyleClass().add("count-badge");

        headerBox.getChildren().addAll(sectionTitle, countBadge);

        // Create table with modern styling
        TableView<EntityConferenza> tableNonIscritto = creaTabella(conferenzeNonIscritto);
        tableNonIscritto.getStyleClass().add("modern-table-view");
        tableNonIscritto.setPrefHeight(300);

        // Action buttons
        HBox actionButtons = createActionButtons(tableNonIscritto, false);

        section.getChildren().addAll(headerBox, tableNonIscritto, actionButtons);
        return section;
    }

    private HBox createActionButtons(TableView<EntityConferenza> table, boolean isEnrolled) {
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(16, 0, 8, 0));

        // Create Details button with modern styling
        Button btnDetails = new Button("Dettagli");
        btnDetails.setStyle(
            "-fx-background-color: #2563eb; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 12px 24px; " +
            "-fx-background-radius: 8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);"
        );
        
        // Add hover effect for details button
        btnDetails.setOnMouseEntered(e -> 
            btnDetails.setStyle(
                "-fx-background-color: #1d4ed8; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: transparent; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 8; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(29, 78, 216, 0.4), 6, 0, 0, 2);"
            )
        );
        btnDetails.setOnMouseExited(e -> 
            btnDetails.setStyle(
                "-fx-background-color: #2563eb; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: transparent; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 8; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);"
            )
        );
        
        // Create Action button (Subscribe/Unsubscribe) with modern styling
        String actionButtonText = isEnrolled ? "Disiscriviti" : "Iscriviti";
        String actionButtonColor = isEnrolled ? "#ef4444" : "#10b981";
        String actionButtonHover = isEnrolled ? "#dc2626" : "#059669";
        String actionButtonShadow = isEnrolled ? "rgba(239, 68, 68, 0.3)" : "rgba(16, 185, 129, 0.3)";
        String actionButtonHoverShadow = isEnrolled ? "rgba(220, 38, 38, 0.4)" : "rgba(5, 150, 105, 0.4)";
        
        Button btnAction = new Button(actionButtonText);
        btnAction.setStyle(
            "-fx-background-color: " + actionButtonColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 12px 24px; " +
            "-fx-background-radius: 8; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 14px; " +
            "-fx-effect: dropshadow(gaussian, " + actionButtonShadow + ", 4, 0, 0, 2);"
        );
        
        // Add hover effect for action button
        btnAction.setOnMouseEntered(e -> 
            btnAction.setStyle(
                "-fx-background-color: " + actionButtonHover + "; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: transparent; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 8; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, " + actionButtonHoverShadow + ", 6, 0, 0, 2);"
            )
        );
        btnAction.setOnMouseExited(e -> 
            btnAction.setStyle(
                "-fx-background-color: " + actionButtonColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: transparent; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 8; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, " + actionButtonShadow + ", 4, 0, 0, 2);"
            )
        );
        // Set button actions based on enrollment status
        if (isEnrolled) {
            // For enrolled conferences
            btnDetails.setOnAction(e -> {
                EntityConferenza selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    new InfoConferenzaAutore(stage, ctrl, selected.getId(), false).show();
                } else {
                    showSelectionAlert("Seleziona una conferenza per visualizzare i dettagli");
                }
            });
            
            btnAction.setOnAction(e -> {
                EntityConferenza selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showConfirmationDialog("Conferma operazione", 
                        "Sei sicuro di voler annullare l'iscrizione a questa conferenza?", 
                        () -> {
                            // Note: There's no direct method to remove subscription in ControlSottomissioni
                            // This will need to be implemented in the DB layer if needed
                            showSelectionAlert("Funzionalità non ancora implementata");
                            // ctrl.rimuoviIscrizione(selected.getId()); // This method doesn't exist yet
                            // show(); // Refresh the view
                        }
                    );
                } else {
                    showSelectionAlert("Seleziona una conferenza per procedere");
                }
            });
        } else {
            // For available conferences
            btnDetails.setOnAction(e -> {
                EntityConferenza selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    new InfoConferenzaAutore(stage, ctrl, selected.getId(), false).show();
                } else {
                    showSelectionAlert("Seleziona una conferenza per visualizzare i dettagli");
                }
            });
            
            btnAction.setOnAction(e -> {
                EntityConferenza selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showConfirmationDialog("Conferma iscrizione", 
                        "Sei sicuro di voler confermare l'iscrizione a questa conferenza?", 
                        () -> {
                            ctrl.iscrivitiConferenza(selected.getId());
                            show(); // Refresh the view
                        }
                    );
                } else {
                    showSelectionAlert("Seleziona una conferenza per procedere");
                }
            });
        }

        // Add tooltips for better UX
        Tooltip.install(btnDetails, new Tooltip("Visualizza i dettagli della conferenza selezionata"));
        Tooltip.install(btnAction, new Tooltip(isEnrolled ? "Annulla l'iscrizione alla conferenza" : "Conferma l'iscrizione alla conferenza"));

        buttonBox.getChildren().addAll(btnDetails, btnAction);
        return buttonBox;
    }

    private void showSelectionAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Selezione Richiesta");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("modern-alert");
        alert.showAndWait();
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("modern-alert");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private TableView<EntityConferenza> creaTabella(ObservableList<EntityConferenza> data) {
        TableView<EntityConferenza> table = new TableView<>();
        table.setItems(data);
        
        // Acronimo column
        TableColumn<EntityConferenza, String> colA = new TableColumn<>("Acronimo");
        colA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcronimo()));
        colA.setPrefWidth(150);
        colA.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; -fx-alignment: CENTER_LEFT;");

        // Titolo column
        TableColumn<EntityConferenza, String> colT = new TableColumn<>("Titolo");
        colT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitolo()));
        colT.setPrefWidth(300);
        colT.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");

        // Luogo column
        TableColumn<EntityConferenza, String> colL = new TableColumn<>("Luogo");
        colL.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLuogo()));
        colL.setPrefWidth(150);
        colL.setStyle("-fx-font-size: 13px; -fx-alignment: CENTER_LEFT;");

        // Descrizione column with text wrapping
        TableColumn<EntityConferenza, String> colD = new TableColumn<>("Descrizione");
        colD.setCellValueFactory(c -> {
            String descrizione = c.getValue().getDescrizione();
            if (descrizione != null && descrizione.length() > 200) {
                descrizione = descrizione.substring(0, 200) + "...";
            }
            return new SimpleStringProperty(descrizione);
        });
        
        colD.setCellFactory(tc -> new TableCell<EntityConferenza, String>() {
            private final Text text = new Text();
            private static final int TEXT_PADDING = 24;

            {
                text.setStyle("-fx-font-size: 13px; -fx-fill: #4b5563;");
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(TEXT_PADDING));
                setPrefHeight(Region.USE_COMPUTED_SIZE);
                setWrapText(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });

        // Bind column widths
        colD.prefWidthProperty().bind(
            table.widthProperty()
                .subtract(colA.widthProperty())
                .subtract(colT.widthProperty())
                .subtract(colL.widthProperty())
                .subtract(2) // Account for borders
        );

        table.getColumns().addAll(colA, colT, colL, colD);
        table.setFixedCellSize(50);
        table.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 2);" +
            "-fx-background-radius: 8;"
        );

        // Set up row factory with alternating colors, hover effects, and click handling
        table.setRowFactory(tv -> {
            TableRow<EntityConferenza> row = new TableRow<>();
            
            // Hover effect that doesn't override selection
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (row.isSelected()) return; // Skip hover effect if row is selected
                if (isNowHovered) {
                    row.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand;");
                } else {
                    row.setStyle(
                        row.getIndex() % 2 == 0 ? 
                        "-fx-background-color: #ffffff;" : 
                        "-fx-background-color: #f8fafc;"
                    );
                }
            });
            
            // Alternating row colors
            row.styleProperty().bind(
                Bindings.when(row.selectedProperty())
                    .then("-fx-background-color: #e2e8f0; -fx-cursor: default;")
                    .otherwise(
                        Bindings.when(
                            Bindings.createBooleanBinding(
                                () -> row.getIndex() % 2 == 1,
                                row.indexProperty()
                            )
                        ).then("-fx-background-color: #f8fafc;")
                         .otherwise("-fx-background-color: #ffffff;")
                    )
            );
            
            // Handle selection style
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    row.setStyle("-fx-background-color: #e2e8f0; -fx-cursor: hand;");
                } else {
                    row.setStyle("");
                }
            });
            
            // Double-click handler
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    EntityConferenza selectedConf = row.getItem();
                    boolean isEnrolled = ctrl.isAutoreIscritto(selectedConf.getId());
                    new InfoConferenzaAutore(stage, ctrl, selectedConf.getId(), isEnrolled).show();
                }
            });
            
            return row;
        });
        
        return table;
    }
}