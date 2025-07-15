package com.cms.gestioneSottomissioni;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HomepageAutore {
    private final Stage stage;
    private final ControlSottomissioni ctrl;
    private final ControlAccount ctrlAccount;
    private final EntityUtente autore;

    // Costruttore della classe HomepageAutore
    public HomepageAutore(Stage stage, ControlSottomissioni ctrl, ControlAccount ctrlAccount, EntityUtente autore) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
        this.autore = autore;
    }

    // Mostra la schermata principale per la gestione delle conferenze da parte dell'autore
    public void show() {
        ObservableList<EntityConferenza> conferenzeIscritto = FXCollections.observableArrayList();
        ObservableList<EntityConferenza> conferenzeNonIscritto = FXCollections.observableArrayList();

        for (EntityConferenza conf : ctrl.getConferenzeAutore()) {
            String autoreEmail = autore.getEmail();
            String chairId = conf.getChairId();
            String editorId = conf.getEditor().orElse(null);
            if ((chairId != null && chairId.equalsIgnoreCase(autoreEmail)) ||
                (editorId != null && editorId.equalsIgnoreCase(autoreEmail))) {
                continue; // Salta conferenze dove l'autore è chair o editor
            }
            if (ctrl.isAutoreIscritto(conf.getId())) {
                conferenzeIscritto.add(conf);
            } else {
                conferenzeNonIscritto.add(conf);
            }
        }

        // Titolo e sottotitolo identici a HomepageChair
        Label titleLabel = new Label("Gestione Sottomissioni");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("Gestisci le tue conferenze e scopri nuove opportunità");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 16 0;");

        VBox enrolledSection = creaSezione("Le tue conferenze", conferenzeIscritto, true);
        VBox availableSection = creaSezione("Conferenze disponibili", conferenzeNonIscritto, false);

        VBox contentContainer = new VBox(12, titleLabel, subtitleLabel, enrolledSection, availableSection);
        contentContainer.setPadding(new Insets(24));
        contentContainer.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrlAccount, this::show);
        header.getBtnBack().setOnAction(e -> ctrlAccount.apriHomepageGenerale());

        VBox root = new VBox(header, contentContainer);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dashboard Autore - " + autore.getNome() + " " + autore.getCognome());
        stage.show();
    }

    // Crea una sezione della tabella per le conferenze (iscritto o disponibili)
    private VBox creaSezione(String titolo, ObservableList<EntityConferenza> data, boolean isIscritto) {
        TableView<EntityConferenza> table = new TableView<>();
        table.setItems(data);

        TableColumn<EntityConferenza, String> colA = new TableColumn<>("Acronimo");
        colA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcronimo()));
        colA.setPrefWidth(200);

        TableColumn<EntityConferenza, String> colT = new TableColumn<>("Titolo");
        colT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitolo()));
        colT.setPrefWidth(200);

        TableColumn<EntityConferenza, String> colL = new TableColumn<>("Luogo");
        colL.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLuogo()));
        colL.setPrefWidth(150);

        TableColumn<EntityConferenza, String> colD = new TableColumn<>("Descrizione");
        colD.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescrizione()));
        colD.setPrefWidth(300);

        table.getColumns().addAll(colA, colT, colL, colD);
        table.setFixedCellSize(45);
        table.setPrefHeight(260);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                       "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");

        Button btnDetail = new Button("Dettagli");
        btnDetail.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
            "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
            "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        btnDetail.setOnAction(e -> {
            EntityConferenza sel = table.getSelectionModel().getSelectedItem();
            if (sel != null)
                new InfoConferenzaAutore(stage, ctrl, ctrlAccount, sel.getId(), ctrl.isAutoreIscritto(sel.getId())).show();
        });

        HBox buttonContainer = new HBox(12);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonContainer.getChildren().add(spacer);

        if (!isIscritto) {
            Button btnIscriviti = new Button("Iscriviti");
            btnIscriviti.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.3), 4, 0, 0, 2);");
            btnIscriviti.setOnAction(e -> {
                EntityConferenza sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    ctrl.iscrivitiConferenza(sel.getId());
                    show();
                }
            });
            buttonContainer.getChildren().addAll(btnIscriviti, btnDetail);
        } else {
            buttonContainer.getChildren().add(btnDetail);
        }

        buttonContainer.setPadding(new Insets(8, 0, 8, 0));

        Label sectionTitle = new Label(titolo);
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #334155;");

        VBox sectionBox = new VBox(8, sectionTitle, table, buttonContainer);
        return sectionBox;
    }
}