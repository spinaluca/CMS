package com.cms.gestioneNotifiche;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.util.List;
import java.util.Map;

public class PannelloNotifiche {
    private final Stage stage;
    private final ControlNotifiche ctrl;
    private List<Map<String, String>> notifiche;

    public PannelloNotifiche(Stage stage, ControlNotifiche ctrl, List<Map<String, String>> notifiche) {
        this.ctrl = ctrl;
        this.stage = stage;
        this.notifiche = notifiche;
    }

    public void show() {
        // Recupera la lista aggiornata delle notifiche ogni volta
        // Titolo
        Label titleLabel = new Label("Pannello Notifiche");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        // Contenitore notifiche
        VBox notificationsContainer = new VBox(12);
        notificationsContainer.setAlignment(Pos.TOP_CENTER);

        if (notifiche.isEmpty()) {
            Label emptyLabel = new Label("Nessuna notifica.");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            notificationsContainer.getChildren().add(emptyLabel);
        } else {
            for (Map<String, String> notifica : notifiche) {
                VBox box = new VBox(6);
                box.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 16; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

                Label msgLabel = new Label(notifica.getOrDefault("messaggio", ""));
                msgLabel.setWrapText(true);
                msgLabel.setMaxWidth(350);
                msgLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");

                Label dateLabel = new Label(notifica.getOrDefault("data", ""));
                dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

                Button btnCancella = new Button("Cancella");
                btnCancella.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; " +
                        "-fx-border-color: transparent; -fx-padding: 8 16 8 16; -fx-background-radius: 8; " +
                        "-fx-font-weight: 600; -fx-font-size: 13px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.3), 4, 0, 0, 2);");

                btnCancella.setOnAction(e -> {
                    notifiche = ctrl.cancellaNotifica(notifica.get("id"));
                    show();
                });

                // Pulsante cancella a destra
                HBox buttonBox = new HBox(btnCancella);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                box.getChildren().addAll(msgLabel, dateLabel, buttonBox);
                notificationsContainer.getChildren().add(box);
            }
        }

        // Scroll pane
        ScrollPane scrollPane = new ScrollPane(notificationsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Bottone chiudi
        Button btnChiudi = new Button("Chiudi");
        btnChiudi.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                "-fx-border-color: transparent; -fx-padding: 12 24 12 24; -fx-background-radius: 8; " +
                "-fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(107,114,128,0.3), 4, 0, 0, 2);");
        btnChiudi.setOnAction(e -> stage.close());

        // Bottone chiudi ancorato a destra
        HBox chiudiBox = new HBox(btnChiudi);
        chiudiBox.setAlignment(Pos.CENTER_RIGHT);
        
        VBox formContainer = new VBox(24);
        formContainer.setMinSize(410, 870);
        formContainer.setAlignment(Pos.TOP_CENTER);
        formContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-padding: 32; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),10,0,0,2); " +
                "-fx-max-width: 400;");
        
        formContainer.getChildren().addAll(titleLabel, scrollPane, chiudiBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox layout = new VBox(formContainer);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 430, 890);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Pannello Notifiche");
        stage.show();
    }
}