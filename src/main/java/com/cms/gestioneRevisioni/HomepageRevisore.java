package com.cms.gestioneRevisioni;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.entity.EntityConferenza;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

/** Homepage Revisore – UC 4.1.7.8 */
public class HomepageRevisore {
    private final Stage stage;
    private final ControlRevisioni ctrl;
    private final String emailRevisore;

    public HomepageRevisore(Stage stage, ControlRevisioni ctrl, String emailRevisore) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.emailRevisore = emailRevisore;
    }

    public void show() {
        TableView<Row> table = new TableView<>();

        TableColumn<Row,String> colA = new TableColumn<>("Acronimo");
        colA.setCellValueFactory(c-> new SimpleStringProperty(c.getValue().conf.getAcronimo()));
        colA.setPrefWidth(100);

        TableColumn<Row,String> colT = new TableColumn<>("Titolo");
        colT.setCellValueFactory(c-> new SimpleStringProperty(c.getValue().conf.getTitolo()));
        colT.setPrefWidth(200);

        TableColumn<Row,String> colSt = new TableColumn<>("Stato Invito");
        colSt.setCellValueFactory(c-> new SimpleStringProperty(c.getValue().stato));
        colSt.setPrefWidth(120);

        table.getColumns().addAll(colA,colT,colSt);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(45);
        table.setPrefHeight(5000);
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");

        Map<EntityConferenza,String> inviti = ctrl.getInvitiRevisore(emailRevisore);
        ObservableList<Row> data = FXCollections.observableArrayList();
        inviti.forEach((conf, stato)-> data.add(new Row(conf,stato)));
        FXCollections.reverse(data);
        table.setItems(data);

        Button btnDettagli = new Button("Dettagli");
        btnDettagli.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;"+
                "-fx-padding: 12 24; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px;"+
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.3),4,0,0,2);");
        btnDettagli.setOnAction(e-> {
            Row sel = table.getSelectionModel().getSelectedItem();
            if(sel==null) return;
            ctrl.visualizzaConferenza(sel.conf.getId(), emailRevisore);
        });

        Button btnGestInvito = new Button("Accetta/Rifiuta invito");
        btnGestInvito.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;"+
                "-fx-padding: 12 24; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px;"+
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");
        btnGestInvito.setOnAction(e-> {
            Row sel= table.getSelectionModel().getSelectedItem();
            if(sel==null) return;
            if(!"In attesa".equals(sel.stato)) {new PopupAvviso("Invito già gestito").show();return;}
            ChoiceDialog<String> dlg = new ChoiceDialog<>("Accetta", "Accetta", "Rifiuta");
            dlg.setTitle("Gestisci invito");
            dlg.setHeaderText("Accetta o rifiuta l'invito");
            dlg.showAndWait().ifPresent(choice-> {
                ctrl.aggiornaInvito(sel.conf.getId(), emailRevisore, choice.equals("Accetta")?"Accettato":"Rifiutato");
                sel.stato = choice.equals("Accetta")?"Accettato":"Rifiutato";
                table.refresh();
            });
        });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(12, spacer, btnGestInvito, btnDettagli);
        buttons.setPadding(new Insets(16,0,16,0));

        Label title = new Label("Homepage Revisore");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        VBox layout = new VBox(16, title, table, buttons);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(null, this::show);
        header.getBtnBack().setOnAction(e-> stage.close());

        VBox root = new VBox(header, layout);
        Scene scene = new Scene(root,1050,850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS – Homepage Revisore");
        stage.show();
    }

    private static class Row {
        final EntityConferenza conf;
        String stato;
        Row(EntityConferenza c, String s){this.conf=c;this.stato=s;}
    }
} 