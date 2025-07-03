package com.cms.gestioneSottomissioni;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

        // Statistics cards
        HBox statsSection = createStatsSection(conferenzeIscritto.size(), conferenzeNonIscritto.size());

        // Enrolled conferences section
        VBox enrolledSection = createEnrolledConferencesSection(conferenzeIscritto);

        // Available conferences section
        VBox availableSection = createAvailableConferencesSection(conferenzeNonIscritto);

        mainLayout.getChildren().addAll(welcomeSection, statsSection, enrolledSection, availableSection);

        // Wrap in scroll pane for better responsiveness
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("main-scroll-pane");

        HeaderBar header = new HeaderBar(ctrlAccount, this::show);
        header.getBtnBack().setOnAction(e -> ctrlAccount.apriHomepageGenerale());
        VBox root = new VBox(header, scrollPane);

        stage.setScene(new Scene(root, 1100, 750));
        stage.setTitle("Dashboard Autore - " + autore.getNome() + " " + autore.getCognome());
        stage.show();
    }

    private VBox createWelcomeSection() {
        VBox welcomeSection = new VBox(10);
        welcomeSection.getStyleClass().add("welcome-section");
        welcomeSection.setAlignment(Pos.CENTER_LEFT);

        Text welcomeTitle = new Text("Benvenuto, " + autore.getNome() + "!");
        welcomeTitle.getStyleClass().add("welcome-title");

        Text welcomeSubtitle = new Text("Gestisci le tue conferenze e sottomissioni da questo pannello");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");

        welcomeSection.getChildren().addAll(welcomeTitle, welcomeSubtitle);
        return welcomeSection;
    }

    private HBox createStatsSection(int enrolledCount, int availableCount) {
        HBox statsSection = new HBox(20);
        statsSection.setAlignment(Pos.CENTER);
        statsSection.getStyleClass().add("stats-section");

        // Enrolled conferences card
        VBox enrolledCard = createStatCard("📚", String.valueOf(enrolledCount), "Conferenze Iscritto", "primary");
        
        // Available conferences card
        VBox availableCard = createStatCard("🔍", String.valueOf(availableCount), "Conferenze Disponibili", "secondary");

        // Total conferences card
        VBox totalCard = createStatCard("📊", String.valueOf(enrolledCount + availableCount), "Totale Conferenze", "accent");

        statsSection.getChildren().addAll(enrolledCard, availableCard, totalCard);
        return statsSection;
    }

    private VBox createStatCard(String icon, String number, String label, String style) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("stat-card", style + "-stat-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);

        Text iconText = new Text(icon);
        iconText.getStyleClass().add("stat-icon");

        Text numberText = new Text(number);
        numberText.getStyleClass().add("stat-number");

        Text labelText = new Text(label);
        labelText.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconText, numberText, labelText);
        return card;
    }

    private VBox createEnrolledConferencesSection(ObservableList<EntityConferenza> conferenzeIscritto) {
        VBox section = new VBox(15);
        section.getStyleClass().add("table-section");

        // Section header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("section-header");

        Text sectionTitle = new Text("📚 Le Tue Conferenze");
        sectionTitle.getStyleClass().add("section-title");

        Label countBadge = new Label(String.valueOf(conferenzeIscritto.size()));
        countBadge.getStyleClass().add("count-badge");

        headerBox.getChildren().addAll(sectionTitle, countBadge);

        // Create table with modern styling
        TableView<EntityConferenza> tableIscritto = creaTabella(conferenzeIscritto);
        tableIscritto.getStyleClass().add("modern-table-view");
        tableIscritto.setPrefHeight(300);

        // Action buttons
        HBox actionButtons = createActionButtons(tableIscritto, true);

        section.getChildren().addAll(headerBox, tableIscritto, actionButtons);
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
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getStyleClass().add("action-buttons-section");

        if (isEnrolled) {
            Button btnDettagli = new Button("📄 Dettagli Conferenza");
            btnDettagli.getStyleClass().addAll("action-button", "primary-button");
            btnDettagli.setOnAction(e -> {
                EntityConferenza sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    new InfoConferenzaAutore(stage, ctrl, sel.getId(), true).show();
                } else {
                    showSelectionAlert("Seleziona una conferenza per visualizzare i dettagli");
                }
            });

            Button btnSottometti = new Button("📝 Gestisci Sottomissioni");
            btnSottometti.getStyleClass().addAll("action-button", "success-button");
            btnSottometti.setOnAction(e -> {
                EntityConferenza sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    new InfoConferenzaAutore(stage, ctrl, sel.getId(), true).show();
                } else {
                    showSelectionAlert("Seleziona una conferenza per gestire le sottomissioni");
                }
            });

            buttonBox.getChildren().addAll(btnDettagli, btnSottometti);
        } else {
            Button btnDettagli = new Button("📄 Dettagli");
            btnDettagli.getStyleClass().addAll("action-button", "secondary-button");
            btnDettagli.setOnAction(e -> {
                EntityConferenza sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    new InfoConferenzaAutore(stage, ctrl, sel.getId(), false).show();
                } else {
                    showSelectionAlert("Seleziona una conferenza per visualizzare i dettagli");
                }
            });

            Button btnIscriviti = new Button("✅ Iscriviti");
            btnIscriviti.getStyleClass().addAll("action-button", "success-button");
            btnIscriviti.setOnAction(e -> {
                EntityConferenza sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    showConfirmationDialog(
                        "Conferma Iscrizione",
                        "Vuoi iscriverti alla conferenza \"" + sel.getTitolo() + "\"?",
                        () -> {
                            ctrl.iscrivitiConferenza(sel.getId());
                            show(); // Refresh the view
                        }
                    );
                } else {
                    showSelectionAlert("Seleziona una conferenza per iscriverti");
                }
            });

            buttonBox.getChildren().addAll(btnDettagli, btnIscriviti);
        }

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

    private TableView<EntityConferenza> creaTabella(ObservableList<EntityConferenza> data) {
        TableView<EntityConferenza> table = new TableView<>();
        table.getStyleClass().add("modern-table-view");

        // Acronimo column with icon
        TableColumn<EntityConferenza, String> colA = new TableColumn<>("📌 Acronimo");
        colA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcronimo()));
        colA.setPrefWidth(120);
        colA.getStyleClass().add("table-column-center");

        // Titolo column
        TableColumn<EntityConferenza, String> colT = new TableColumn<>("📋 Titolo");
        colT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitolo()));
        colT.setPrefWidth(250);

        // Luogo column with icon
        TableColumn<EntityConferenza, String> colL = new TableColumn<>("📍 Luogo");
        colL.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLuogo()));
        colL.setPrefWidth(150);

        // Status column (new)
        TableColumn<EntityConferenza, String> colS = new TableColumn<>("📊 Stato");
        colS.setCellValueFactory(c -> {
            // You can add logic here to determine conference status
            return new SimpleStringProperty("Attiva");
        });
        colS.setPrefWidth(100);
        colS.setCellFactory(tc -> new TableCell<EntityConferenza, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    Label statusLabel = new Label(item);
                    statusLabel.getStyleClass().addAll("status-badge", "active-status");
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });

        // Descrizione column with improved text wrapping
        TableColumn<EntityConferenza, String> colD = new TableColumn<>("📝 Descrizione");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        colD.prefWidthProperty().bind(
                table.widthProperty()
                        .subtract(table.snappedLeftInset() + table.snappedRightInset())
                        .subtract(colA.widthProperty())
                        .subtract(colT.widthProperty())
                        .subtract(colL.widthProperty())
                        .subtract(colS.widthProperty())
                        .subtract(20) // Account for column separators and scrollbar
        );

        colD.setCellValueFactory(c -> {
            String descrizione = c.getValue().getDescrizione();
            if (descrizione.length() > 150) {
                descrizione = descrizione.substring(0, 150) + "...";
            }
            return new SimpleStringProperty(descrizione);
        });

        colD.setCellFactory(tc -> new TableCell<EntityConferenza, String>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(20));
                text.getStyleClass().add("table-description-text");
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

        table.getColumns().addAll(colA, colT, colL, colS, colD);
        table.setItems(data);
        table.setRowFactory(tv -> {
            TableRow<EntityConferenza> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    EntityConferenza selectedConf = row.getItem();
                    boolean isEnrolled = ctrl.isAutoreIscritto(selectedConf.getId());
                    new InfoConferenzaAutore(stage, ctrl, selectedConf.getId(), isEnrolled).show();
                }
            });
            return row;
        });

        // Set empty state
        table.setPlaceholder(createEmptyStateNode());

        return table;
    }

    private VBox createEmptyStateNode() {
        VBox emptyState = new VBox(15);
        emptyState.getStyleClass().add("table-empty-state");
        emptyState.setAlignment(Pos.CENTER);

        Text emptyIcon = new Text("📋");
        emptyIcon.getStyleClass().add("empty-state-icon");

        Text emptyTitle = new Text("Nessuna conferenza trovata");
        emptyTitle.getStyleClass().add("empty-state-title");

        Text emptyMessage = new Text("Non ci sono conferenze disponibili al momento");
        emptyMessage.getStyleClass().add("empty-state-message");

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyMessage);
        return emptyState;
    }
}