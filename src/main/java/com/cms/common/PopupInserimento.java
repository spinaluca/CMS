package com.cms.common;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Insets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.cms.entity.EntityArticolo;
import java.util.List;
import java.util.ArrayList;
import com.cms.gestioneRevisioni.ControlRevisioni;
import javafx.util.StringConverter;

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
        dlg.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valid = newVal.matches("^[\\w.-]+@[\\w-]+(\\.[\\w-]+)+$");;
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
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

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
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

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

    public Optional<List<EntityArticolo>> promptSelezionaArticoli(List<EntityArticolo> articoli, ControlRevisioni ctrlRevisioni) {
        Dialog<List<EntityArticolo>> dialog = new Dialog<>();
        dialog.setTitle("Seleziona Articoli");
        dialog.setHeaderText("Scegli gli articoli da revisionare");

        ButtonType okBtn = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, cancelBtn);

        ListView<EntityArticolo> listView = new ListView<>();
        listView.getItems().addAll(articoli);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EntityArticolo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String nomeAutore = ctrlRevisioni.getNomeCompletoAutore(item.getAutoreId());
                    setText(item.getTitolo() + "  [" + nomeAutore + "]");
                }
            }
        });

        dialog.getDialogPane().setContent(listView);
        styleDialogPane(dialog.getDialogPane());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(430);
        dialog.getDialogPane().setPrefHeight(430);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(okBtn);
        stylePrimaryButton(confirmButton);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelBtn);
        styleSecondaryButton(cancelButton);

        dialog.setResultConverter(btn -> btn == okBtn ? new ArrayList<>(listView.getSelectionModel().getSelectedItems()) : null);

        return dialog.showAndWait();
    }

    public Optional<Map<String, String>> promptAssegnazione(List<EntityArticolo> articoli,
                                                            List<String> revisori) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Nuova Assegnazione");
        dialog.setHeaderText("Seleziona articolo e revisore");

        ButtonType okBtn = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        Label artLbl = new Label("Articolo:");
        artLbl.setStyle(getLabelStyle());
        ChoiceBox<EntityArticolo> cbArt = new ChoiceBox<>();
        cbArt.getItems().addAll(articoli);
        cbArt.setConverter(new StringConverter<>() {
            @Override
            public String toString(EntityArticolo a) {
                return a == null ? "" : a.getTitolo();
            }
            @Override
            public EntityArticolo fromString(String s) { return null; }
        });

        Label revLbl = new Label("Revisore:");
        revLbl.setStyle(getLabelStyle());
        ChoiceBox<String> cbRev = new ChoiceBox<>();
        cbRev.getItems().addAll(revisori);

        grid.addRow(0, artLbl, cbArt);
        grid.addRow(1, revLbl, cbRev);

        Text warning = new Text("Seleziona articolo e revisore.");
        warning.setFill(Color.web("#dc2626"));
        warning.setFont(Font.font(12));
        grid.add(warning, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        styleDialogPane(dialog.getDialogPane());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(okBtn);
        stylePrimaryButton(confirmButton);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelBtn);
        styleSecondaryButton(cancelButton);

        Runnable validate = () -> {
            boolean valid = cbArt.getSelectionModel().getSelectedItem() != null && cbRev.getSelectionModel().getSelectedItem() != null;
            confirmButton.setDisable(!valid);
            warning.setVisible(!valid);
        };

        cbArt.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validate.run());
        cbRev.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validate.run());
        validate.run();

        dialog.setResultConverter(btn -> {
            if (btn == okBtn) {
                Map<String, String> map = new HashMap<>();
                map.put("articolo_id", cbArt.getSelectionModel().getSelectedItem().getId());
                map.put("revisore_email", cbRev.getSelectionModel().getSelectedItem());
                return map;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static class RevisionData {
        public final int voto;
        public final int expertise;
        public RevisionData(int voto, int expertise) {
            this.voto = voto;
            this.expertise = expertise;
        }
    }

    public Optional<RevisionData> promptVotoExpertise() {
        Dialog<RevisionData> dialog = new Dialog<>();
        dialog.setTitle("Carica Revisione");
        dialog.setHeaderText("Inserisci voto e livello di expertise");

        ButtonType okButtonType = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Spinner<Integer> spinnerVoto = new Spinner<>(1, 10, 5);
        spinnerVoto.setEditable(true);
        spinnerVoto.setPrefWidth(100);

        Spinner<Integer> spinnerExpertise = new Spinner<>(1, 5, 3);
        spinnerExpertise.setEditable(true);
        spinnerExpertise.setPrefWidth(100);

        content.getChildren().addAll(
            new Label("Voto (1-10):"), spinnerVoto,
            new Label("Livello Expertise (1-5):"), spinnerExpertise
        );

        dialog.getDialogPane().setContent(content);
        styleDialogPane(dialog.getDialogPane());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        stylePrimaryButton(confirmButton);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        styleSecondaryButton(cancelButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new RevisionData(spinnerVoto.getValue(), spinnerExpertise.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void styleDialogPane(DialogPane pane) {
        pane.setStyle("-fx-background-color: #ffffff;");
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
            "-fx-border-color: transparent; -fx-padding: 12 12 12 12; -fx-background-radius: 8; " +
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
            "-fx-border-color: transparent; -fx-padding: 12 12 12 12; -fx-background-radius: 8; " +
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
                "-fx-border-color: transparent; -fx-padding: 12 12 12 12; -fx-background-radius: 8; " +
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