package com.cms.common;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;

public class PopupAvviso {
    private final String msg;

    public PopupAvviso(String msg) {
        this.msg = msg;
    }

    public void show() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Avviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        styleDialogPane(pane);

        Button okButton = (Button) pane.lookupButton(alert.getButtonTypes().get(0));
        stylePrimaryButton(okButton);

        alert.showAndWait();
    }

    private void styleDialogPane(DialogPane pane) {
        pane.setStyle("-fx-background-color: #ffffff;");
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                "-fx-border-color: transparent; -fx-padding: 12 24 12 24; -fx-background-radius: 8; " +
                "-fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.3), 4, 0, 0, 2);");

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