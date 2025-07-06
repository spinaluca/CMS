package com.cms.gestioneRevisioni;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.common.PopupInserimento;
import com.cms.entity.EntityConferenza;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Map;

public class HomepageRevisore {
    private final Stage stage;
    private final ControlRevisioni ctrl;
    private final ControlAccount ctrl2;

    public HomepageRevisore(Stage stage, ControlRevisioni ctrl, ControlAccount ctrl2) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
    }

    private String getEmailRevisore() {
        return ctrl2.getUtenteCorrente().getEmail();
    }

    public void show() {
        // Title and subtitle
        Label titleLabel = new Label("Gestione Revisioni");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("Gestisci le tue conferenze come Revisore");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 20 0;");

        // Table
        TableView<Row> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Row,String> colAcronimo = new TableColumn<>("Acronimo");
        colAcronimo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().conf.getAcronimo()));
        colAcronimo.setPrefWidth(200);

        TableColumn<Row,String> colTitolo = new TableColumn<>("Titolo");
        colTitolo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().conf.getTitolo()));
        colTitolo.setPrefWidth(200);

        TableColumn<Row,String> colLuogo = new TableColumn<>("Luogo");
        colLuogo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().conf.getLuogo()));
        colLuogo.setPrefWidth(150);

        TableColumn<Row,String> colStato = new TableColumn<>("Stato Invito");
        colStato.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().stato));
        colStato.setPrefWidth(200);

        table.getColumns().addAll(colAcronimo, colTitolo, colLuogo, colStato);

        table.setFixedCellSize(45);
        table.setPrefHeight(5000);
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                      "-fx-border-width: 1; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

        // Load data
        Map<EntityConferenza,String> inviti = ctrl.getInvitiRevisore(getEmailRevisore());
        ObservableList<Row> data = FXCollections.observableArrayList();
        inviti.forEach((conf, stato) -> data.add(new Row(conf, stato)));
        FXCollections.reverse(data);
        table.setItems(data);

        // Buttons
        Button btnGestInvito = new Button("Accetta/Rifiuta invito");
        btnGestInvito.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                              "-fx-border-color: transparent; -fx-padding: 12 24; " +
                              "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                              "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.3), 4, 0, 0, 2);");
        btnGestInvito.setOnAction(e -> {
            Row sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            if (!"In attesa".equals(sel.stato)) {
                new PopupAvviso("Invito già gestito").show();
                return;
            }
            PopupInserimento popup = new PopupInserimento();
            popup.promptGestioneInvito(sel.conf.getTitolo()).ifPresent(nuovoStato -> {
                ctrl.aggiornaInvito(sel.conf.getId(), getEmailRevisore(), nuovoStato);
                sel.stato = nuovoStato;
                table.refresh();
            });
        });

        Button btnDettagli = new Button("Dettagli");
        btnDettagli.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                            "-fx-border-color: transparent; -fx-padding: 12 24; " +
                            "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        btnDettagli.setOnAction(e -> {
            Row sel = table.getSelectionModel().getSelectedItem();
            if (sel != null)
                new InfoConferenzaRevisore(stage, ctrl, ctrl2, sel.conf.getId()).show();
        });

        // Buttons aligned to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttonBox = new HBox(12, spacer, btnGestInvito, btnDettagli);
        buttonBox.setPadding(new Insets(16, 0, 16, 0));

        // Main content container
        VBox contentContainer = new VBox(16, titleLabel, subtitleLabel, table, buttonBox);
        contentContainer.setPadding(new Insets(24));
        contentContainer.setStyle("-fx-background-color: #f8fafc;");

        // Header
        HeaderBar header = new HeaderBar(ctrl2, this::show);
        header.getBtnBack().setOnAction(e -> ctrl2.apriHomepageGenerale());

        VBox root = new VBox(header, contentContainer);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Homepage Revisore");
        stage.show();
    }

    private static class Row {
        final EntityConferenza conf;
        String stato;
        Row(EntityConferenza c, String s) {
            this.conf = c;
            this.stato = s;
        }
    }
}