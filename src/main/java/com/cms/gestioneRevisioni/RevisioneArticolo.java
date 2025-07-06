package com.cms.gestioneRevisioni;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.entity.EntityArticolo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/** Pagina di revisione articolo – UC 4.1.7.11/12 */
public class RevisioneArticolo {
    private final Stage stage;
    private final ControlRevisioni ctrl;
    private final String idArticolo;
    private final String emailRevisore;

    public RevisioneArticolo(Stage stage, ControlRevisioni ctrl, String idArticolo, String emailRevisore) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.idArticolo = idArticolo;
        this.emailRevisore = emailRevisore;
    }

    public void show() {
        Label title = new Label("Revisione Articolo");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        // Dati articolo (stub)
        Label lblTitolo = new Label("Titolo: [da caricare]");
        Label lblAutore = new Label("Autore: [da caricare]");
        Label lblParoleChiave = new Label("Parole chiave: [da caricare]");

        Button btnVisualizza = new Button("Visualizza Articolo");
        btnVisualizza.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.3),4,0,0,2);");
        btnVisualizza.setOnAction(e -> visualizzaArticolo());

        Button btnCarica = new Button("Carica Revisione");
        btnCarica.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");
        btnCarica.setOnAction(e -> caricaRevisione());

        HBox buttons = new HBox(10, btnVisualizza, btnCarica);

        VBox layout = new VBox(15, title, lblTitolo, lblAutore, lblParoleChiave, buttons);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(null, () -> {});
        header.getBtnBack().setOnAction(e -> stage.close());

        VBox root = new VBox(header, layout);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Revisione Articolo");
        stage.show();
    }

    private void visualizzaArticolo() {
        ctrl.visualizzaArticolo(idArticolo).ifPresentOrElse(
            ok -> new PopupAvviso("Articolo scaricato").show(),
            () -> new PopupAvviso("Articolo non disponibile").show()
        );
    }

    private void caricaRevisione() {
        // Dialog per inserimento voto e expertise
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

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new RevisionData(spinnerVoto.getValue(), spinnerExpertise.getValue());
            }
            return null;
        });

        Optional<RevisionData> result = dialog.showAndWait();
        result.ifPresent(data -> {
            // Selezione file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleziona file revisione");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf")
            );
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All files", "*.*")
            );

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                ctrl.caricaRevisione(idArticolo, emailRevisore, data.voto, data.expertise, selectedFile);
                new PopupAvviso("Revisione caricata con successo").show();
            }
        });
    }

    private static class RevisionData {
        final int voto;
        final int expertise;
        RevisionData(int v, int e) { voto = v; expertise = e; }
    }
} 