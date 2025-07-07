package com.cms.common;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
        Button okButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);

        Label warning = new Label("Inserisci una email valida.");
        warning.setStyle("-fx-text-fill: red; -fx-font-size: 11;");
        warning.setVisible(false);

        VBox content = new VBox(5, inputField, warning);
        dlg.getDialogPane().setContent(content);

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
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField titolo = new TextField();
        TextField paroleChiave = new TextField();

        grid.addRow(0, new Label("Titolo:"), titolo);
        grid.addRow(1, new Label("Parole Chiave:"), paroleChiave);

        Text warning = new Text();
        warning.setFill(Color.RED);
        warning.setFont(Font.font(11));
        grid.add(warning, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Node confermaButton = dialog.getDialogPane().lookupButton(okButtonType);
        confermaButton.setDisable(true);

        Runnable validate = () -> {
            boolean empty = titolo.getText().trim().isEmpty() || paroleChiave.getText().trim().isEmpty();
            if (empty) {
                warning.setText("Compila entrambi i campi.");
                confermaButton.setDisable(true);
            } else {
                warning.setText("");
                confermaButton.setDisable(false);
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
        dialog.getDialogPane().getButtonTypes().addAll(accettaButtonType, rifiutaButtonType, ButtonType.CANCEL);

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
        javafx.scene.control.Dialog<java.util.List<com.cms.entity.EntityArticolo>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Seleziona Articoli");
        dialog.setHeaderText("Scegli gli articoli da revisionare");

        javafx.scene.control.ButtonType okBtn = new javafx.scene.control.ButtonType("Conferma", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.control.ListView<com.cms.entity.EntityArticolo> listView = new javafx.scene.control.ListView<>();
        listView.getItems().addAll(articoli);
        listView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        listView.setCellFactory(lv -> new javafx.scene.control.ListCell<com.cms.entity.EntityArticolo>() {
            @Override
            protected void updateItem(com.cms.entity.EntityArticolo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitolo());
                }
            }
        });

        dialog.getDialogPane().setContent(listView);
        dialog.setResultConverter(btn -> btn == okBtn ? new java.util.ArrayList<>(listView.getSelectionModel().getSelectedItems()) : null);

        return dialog.showAndWait();
    }
}