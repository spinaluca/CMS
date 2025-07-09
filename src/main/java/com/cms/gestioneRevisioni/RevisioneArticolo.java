package com.cms.gestioneRevisioni;

import com.cms.common.HeaderBar;
import com.cms.common.PopupAvviso;
import com.cms.gestioneAccount.ControlAccount;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class RevisioneArticolo {
    private final Stage stage;
    private final ControlRevisioni ctrl;
    private final ControlAccount ctrl2;
    private final String idArticolo;
    private final String confId;
    private final boolean isChair;
    
    public RevisioneArticolo(Stage stage, ControlRevisioni ctrl, ControlAccount ctrl2, String idArticolo, String confId, boolean isChair) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.ctrl2 = ctrl2;
        this.idArticolo = idArticolo;
        this.confId = confId;
        this.isChair = isChair;
    }

    public void show() {
        Label title = new Label("Revisione Articolo");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        // Recupera info articolo
        com.cms.entity.EntityArticolo articolo = ctrl.getArticoloById(idArticolo).orElse(null);
        String titolo = articolo != null ? articolo.getTitolo() : "[da caricare]";
        String autore = articolo != null ? ctrl2.getNomeCompleto(articolo.getAutoreId()).orElse(articolo.getAutoreId()) : "[da caricare]";
        String paroleChiave = articolo != null ? articolo.getParoleChiave() : "[da caricare]";

        VBox infoArticolo = new VBox(8,
                new Label("Titolo: " + titolo),
                new Label("Autore: " + autore),
                new Label("Parole chiave: " + paroleChiave)
        );
        infoArticolo.setPrefWidth(800);

        HBox infoPanel = new HBox(20, infoArticolo);
        infoPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),8,0,0,2);");
        infoPanel.setPrefWidth(5000);
        infoPanel.setMaxHeight(200);

        HBox infoSection = new HBox(20, infoPanel);

        Button btnCarica = new Button("Carica Revisione");
        btnCarica.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.3),4,0,0,2);");
        btnCarica.setOnAction(e -> caricaRevisione());

        Button btnVisualizza = new Button("Visualizza Articolo");
        btnVisualizza.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-border-color: transparent;" +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.3),4,0,0,2);");
        btnVisualizza.setOnAction(e -> visualizzaArticolo());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(10, spacer, btnCarica, btnVisualizza);

        VBox layout = new VBox(16, title, infoSection);
        layout.setSpacing(30);
        layout.getChildren().add(buttons);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f8fafc;");

        HeaderBar header = new HeaderBar(ctrl2, this::show);
        if (isChair) {
            header.getBtnBack().setOnAction(e -> ctrl2.apriInfoConferenzaChair(confId));
        } else {
            header.getBtnBack().setOnAction(e -> ctrl2.apriInfoConferenzaRevisore(confId));
        }

        VBox root = new VBox(header, layout);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Revisione Articolo");
        stage.show();
    }

    private void visualizzaArticolo() {
        ctrl.visualizzaArticolo(idArticolo);
    }

    private void caricaRevisione() {
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
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleziona file revisione");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                String emailRevisore = ctrl2.getUtenteCorrente().getEmail();
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