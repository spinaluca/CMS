package com.cms.gestioneEditings;

import com.cms.common.HeaderBar;
import com.cms.entity.EntityConferenza;
import com.cms.gestioneAccount.ControlAccount;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HomepageEditor {
    private final Stage stage;
    private final ControlEditings ctrl;
    private final ControlAccount ctrlAccount;

    public HomepageEditor(Stage stage, ControlEditings ctrl, ControlAccount ctrlAccount) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
    }

    public void show() {
        TableView<EntityConferenza> table = new TableView<>();
        ObservableList<EntityConferenza> data =
                FXCollections.observableArrayList(ctrl.getConferenzeEditor());
        FXCollections.reverse(data);

        TableColumn<EntityConferenza, String> colA = new TableColumn<>("Acronimo");
        colA.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcronimo()));
        colA.setPrefWidth(100);

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

        Button btnDettagli = new Button("Dettagli");
        btnDettagli.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");
        btnDettagli.setOnAction(e -> {
            EntityConferenza sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                new InfoConferenzaEditor(stage, ctrl, ctrlAccount, sel.getId()).show();
            }
        });

        Label titleLabel = new Label("Gestione Editings");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("Gestisci le tue conferenze come Editor");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 20 0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonContainer = new HBox(12, spacer, btnDettagli);
        buttonContainer.setPadding(new Insets(16, 0, 16, 0));

        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

        VBox contentContainer = new VBox(16, titleLabel, subtitleLabel, table, buttonContainer);
        contentContainer.setPadding(new Insets(24));
        contentContainer.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrlAccount, this::show);
        header.getBtnBack().setOnAction(e -> ctrlAccount.apriHomepageGenerale());

        VBox root = new VBox(header, contentContainer);

        Scene scene = new Scene(root, 1050, 750);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Gestione Editings");
        stage.show();
    }
} 