package com.cms.gestioneRevisioni;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.entity.EntityConferenza;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

/** Vista conferenza per Revisore – UC 4.1.7.10 */
public class InfoConferenzaRevisore {
    private final Stage stage;
    private final ControlRevisioni ctrl;
    private final String confId;
    private final String email;

    public InfoConferenzaRevisore(Stage stage, ControlRevisioni ctrl, String confId, String email) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.confId = confId;
        this.email = email;
    }

    public void show() {
        EntityConferenza conf = ctrl.getConferenzaRevisore(confId, email)
                .orElseThrow(() -> new RuntimeException("Conferenza non trovata"));

        Label title = new Label("["+conf.getAcronimo()+"] "+conf.getTitolo());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        TableView<Row> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(45);
        table.setPrefHeight(5000);
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;"+
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");

        TableColumn<Row,String> colTit = new TableColumn<>("Titolo");
        colTit.setCellValueFactory(d-> new ReadOnlyStringWrapper(d.getValue().titolo));
        colTit.setMinWidth(200);

        TableColumn<Row,String> colSt = new TableColumn<>("Stato");
        colSt.setCellValueFactory(d-> new ReadOnlyStringWrapper(d.getValue().stato));

        TableColumn<Row,String> colEsito = new TableColumn<>("Esito");
        colEsito.setCellValueFactory(d-> new ReadOnlyStringWrapper(d.getValue().esito==null?"-":d.getValue().esito));

        table.getColumns().addAll(colTit,colSt,colEsito);

        Map<String,String> articoli = ctrl.getArticoliRevisore(confId, email);
        ObservableList<Row> rows = FXCollections.observableArrayList();
        articoli.forEach((titolo, stato)-> rows.add(new Row(titolo, stato, null)));
        table.setItems(rows);

        Button btnRevisiona = new Button("Revisiona");
        btnRevisiona.setOnAction(e-> {
            Row sel = table.getSelectionModel().getSelectedItem();
            if(sel==null) return;
            new RevisioneArticolo(stage, ctrl, sel.titolo, email).show();
        });
        Button btnDelega = new Button("Delega sotto-revisore");
        btnDelega.setOnAction(e-> new PopupAvviso("Funzione delega da implementare").show());
        Button btnAggiungi = new Button("Aggiungi articolo da revisionare");
        btnAggiungi.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(139,92,246,0.3),4,0,0,2);");
        btnAggiungi.setOnAction(e-> ctrl.aggiungiArticoloRevisore(confId, email));
        HBox buttons = new HBox(10, btnRevisiona, btnDelega, btnAggiungi);

        VBox layout = new VBox(15, title, table, buttons);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(null, ()->{});
        header.getBtnBack().setOnAction(e-> stage.close());
        VBox root = new VBox(header, layout);

        Scene scene = new Scene(root,1050,850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dettagli Conferenza – Revisore");
        stage.show();
    }

    private static class Row {
        final String titolo;
        final String stato;
        final String esito;
        Row(String t,String s,String e){titolo=t;stato=s;esito=e;}
    }
} 