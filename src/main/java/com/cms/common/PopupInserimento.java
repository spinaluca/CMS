package com.cms.common;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PopupInserimento {

    public Optional<String> promptEmail(String role) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle(role);
        dlg.setHeaderText("Inserisci email per " + role);

        TextField inputField = dlg.getEditor();
        inputField.setStyle(getInputStyle());

        Button okButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        stylePrimaryButton(okButton);

        Button cancelButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.CANCEL);
        styleSecondaryButton(cancelButton);

        Label warning = new Label("Inserisci una email valida.");
        warning.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12;");
        warning.setVisible(false);

        VBox content = new VBox(8, inputField, warning);
        dlg.getDialogPane().setContent(content);
        styleDialogPane(dlg.getDialogPane());

        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valid = newVal.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$");
            warning.setVisible(!valid);
            okButton.setDisable(!valid);
        });

        Platform.runLater(() -> okButton.setDisable(true));

        return dlg.showAndWait();
    }

    public Optional<Map<String, String>> promptDatiArticolo() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Sottometti Articolo");
        dialog.setHeaderText("Inserisci titolo e parole chiave dell'articolo");

        ButtonType okButtonType = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        Label titoloLabel = new Label("Titolo:");
        titoloLabel.setStyle(getLabelStyle());
        TextField titolo = new TextField();
        titolo.setStyle(getInputStyle());

        Label paroleLabel = new Label("Parole Chiave:");
        paroleLabel.setStyle(getLabelStyle());
        TextField paroleChiave = new TextField();
        paroleChiave.setStyle(getInputStyle());

        grid.addRow(0, titoloLabel, titolo);
        grid.addRow(1, paroleLabel, paroleChiave);

        Text warning = new Text();
        warning.setFill(Color.web("#dc2626"));
        warning.setFont(Font.font(12));
        grid.add(warning, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        styleDialogPane(dialog.getDialogPane());

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        stylePrimaryButton(confirmButton);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        styleSecondaryButton(cancelButton);

        confirmButton.setDisable(true);

        Runnable validate = () -> {
            boolean empty = titolo.getText().trim().isEmpty() || paroleChiave.getText().trim().isEmpty();
            if (empty) {
                warning.setText("Compila entrambi i campi.");
                confirmButton.setDisable(true);
            } else {
                warning.setText("");
                confirmButton.setDisable(false);
            }
        };

        titolo.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        paroleChiave.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        validate.run();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Map<String, String> dati = new HashMap<>();
                dati.put("titolo", titolo.getText());
                dati.put("paroleChiave", paroleChiave.getText());
                return dati;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<String> promptGestioneInvito(String conferenza) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Gestisci Invito");
        dialog.setHeaderText("Conferenza: " + conferenza);
        dialog.setContentText("Accetta o rifiuta l'invito per questa conferenza");

        ButtonType accettaButtonType = new ButtonType("Accetta", ButtonBar.ButtonData.OK_DONE);
        ButtonType rifiutaButtonType = new ButtonType("Rifiuta", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(accettaButtonType, rifiutaButtonType, cancelButtonType);

        styleDialogPane(dialog.getDialogPane());

        Button acceptButton = (Button) dialog.getDialogPane().lookupButton(accettaButtonType);
        stylePrimaryButton(acceptButton);
        Button refuseButton = (Button) dialog.getDialogPane().lookupButton(rifiutaButtonType);
        styleErrorButton(refuseButton);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        styleSecondaryButton(cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == accettaButtonType) {
                return "Accettato";
            } else if (dialogButton == rifiutaButtonType) {
                return "Rifiutato";
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<java.util.List<com.cms.entity.EntityArticolo>> promptSelezionaArticoli(java.util.List<com.cms.entity.EntityArticolo> articoli) {
        Dialog<java.util.List<com.cms.entity.EntityArticolo>> dialog = new Dialog<>();
        dialog.setTitle("Seleziona Articoli");
        dialog.setHeaderText("Scegli gli articoli da revisionare");

        ButtonType okBtn = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, cancelBtn);

        ListView<com.cms.entity.EntityArticolo> listView = new ListView<>();
        listView.getItems().addAll(articoli);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dialog.getDialogPane().setContent(listView);
        styleDialogPane(dialog.getDialogPane());

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(okBtn);
        stylePrimaryButton(confirmButton);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelBtn);
        styleSecondaryButton(cancelButton);

        dialog.setResultConverter(btn -> btn == okBtn ? new java.util.ArrayList<>(listView.getSelectionModel().getSelectedItems()) : null);

        return dialog.showAndWait();
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

    private void styleSecondaryButton(Button button) {
        button.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
            "-fx-border-color: transparent; -fx-padding: 12 24 12 24; -fx-background-radius: 8; " +
            "-fx-font-weight: 600; -fx-font-size: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(107,114,128,0.3),4,0,0,2);");
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.02);
            button.setScaleY(1.02);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
    }

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

    private String getLabelStyle() {
        return "-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;";
    }

    private String getInputStyle() {
        return "-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;";
    }
}