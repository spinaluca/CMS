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

    public HomepageChair(Stage stage, ControlConferenze ctrl, ControlAccount ctrl2) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
    }

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
        colD.setCellValueFactory(c -> {
            String descrizione = c.getValue().getDescrizione();
            if (descrizione.length() > 200) {
                descrizione = descrizione.substring(0, 200) + "...";
            }
            return new SimpleStringProperty(descrizione);
        });

        colD.setCellFactory(tc -> new TableCell<EntityConferenza, String>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(tc.widthProperty());
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

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colD.prefWidthProperty().bind(
                table.widthProperty()
                        .subtract(table.snappedLeftInset() + table.snappedRightInset())
                        .subtract(colA.widthProperty())
                        .subtract(colT.widthProperty())
                        .subtract(colL.widthProperty())
                        .subtract(2)
        );

        table.getColumns().addAll(colA, colT, colL, colD);
        table.setFixedCellSize(40);
        table.setItems(data);

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

        // Create title and subtitle
        Label titleLabel = new Label("Gestione Conferenze");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        Label subtitleLabel = new Label("Gestisci le tue conferenze come Chair");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 20 0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonContainer = new HBox(12, spacer, btnCreate, btnDetail);
        buttonContainer.setPadding(new Insets(16, 0, 16, 0));

        // Style the table
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                      "-fx-border-width: 1; -fx-border-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 2);");

        VBox contentContainer = new VBox(16, titleLabel, subtitleLabel, table, buttonContainer);
        contentContainer.setPadding(new Insets(24));
        contentContainer.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrl2, this::show);
        header.getBtnBack().setOnAction(e -> ctrl2.apriHomepageGenerale());

        VBox root = new VBox(header, contentContainer);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Gestione Conferenze");
        stage.show();
    }
}