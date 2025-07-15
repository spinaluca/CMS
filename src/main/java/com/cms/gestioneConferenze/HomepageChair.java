package com.cms.gestioneConferenze;

import com.cms.common.HeaderBar;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.entity.EntityConferenza;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HomepageChair {
    private final ControlConferenze ctrl;
    private final ControlAccount ctrl2;
    private final Stage stage;

    // Costruttore della classe HomepageChair
    public HomepageChair(Stage stage, ControlConferenze ctrl, ControlAccount ctrl2) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
    }

    // Mostra la schermata principale per la gestione delle conferenze da parte del chair
    public void show() {
        TableView<EntityConferenza> table = new TableView<>();
        ObservableList<EntityConferenza> data =
                FXCollections.observableArrayList(ctrl.getConferenze(ctrl2.getUtenteCorrente()));
        FXCollections.reverse(data);

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

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(colA, colT, colL, colD);
        table.setFixedCellSize(45);
        table.setItems(data);
        table.setPrefHeight(5000);

        Button btnCreate = new Button("Crea Conferenza");
        btnCreate.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                          "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                          "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                          "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.3), 4, 0, 0, 2);");
        btnCreate.setOnAction(e -> new ModuloCreazione(ctrl, ctrl2).show());

        Button btnDetail = new Button("Dettagli");
        btnDetail.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                          "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                          "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                          "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        btnDetail.setOnAction(e -> {
            EntityConferenza sel = table.getSelectionModel().getSelectedItem();
            if (sel != null)
                new InfoConferenzaChair(stage, ctrl, ctrl2, sel.getId()).show();
        });

        // Crea titolo e sottotitolo
        Label titleLabel = new Label("Gestione Conferenze");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        Label subtitleLabel = new Label("Gestisci le tue conferenze come Chair");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 20 0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonContainer = new HBox(12, spacer, btnCreate, btnDetail);
        buttonContainer.setPadding(new Insets(16, 0, 16, 0));

        // Stile della tabella
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                      "-fx-border-width: 1; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

        VBox contentContainer = new VBox(16, titleLabel, subtitleLabel, table, buttonContainer);
        contentContainer.setPadding(new Insets(24));
        contentContainer.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrl2, this::show);
        header.getBtnBack().setOnAction(e -> ctrl2.apriHomepageGenerale());

        VBox root = new VBox(header, contentContainer);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Gestione Conferenze");
        stage.show();
    }
}