package com.cms.common;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;

public class PopupErrore {
    private final String msg;

    // Costruttore della classe PopupErrore
    public PopupErrore(String msg) {
        this.msg = msg;
    }

    // Mostra il popup di errore
    public void show() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        styleDialogPane(pane);

        Button okButton = (Button) pane.lookupButton(alert.getButtonTypes().get(0));
        styleErrorButton(okButton);

        alert.showAndWait();
    }

    // Applica lo stile al DialogPane
    private void styleDialogPane(DialogPane pane) {
        pane.setStyle("-fx-background-color: #ffffff;");
    }

    // Applica lo stile al bottone di errore
    private void styleErrorButton(Button button) {
        button.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; " +
                "-fx-border-color: transparent; -fx-padding: 12 24 12 24; -fx-background-radius: 8; " +
                "-fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.3), 4, 0, 0, 2);");

        button.setOnMouseEntered(e -> {
            button.setScaleX(1.02);
            button.setScaleY(1.02);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
    }
}