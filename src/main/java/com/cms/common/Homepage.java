package com.cms.common;

import com.cms.entity.EntityUtente;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.gestioneSottomissioni.ControlSottomissioni;
import com.cms.gestioneSottomissioni.HomepageAutore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

public class Homepage {
    private final Stage stage;
    private final ControlAccount ctrl;
    private final EntityUtente utente;

    public Homepage(Stage stage, ControlAccount ctrl, EntityUtente utente) {
        this.stage = stage;
        this.ctrl = ctrl;
        this.utente = utente;
    }

    public void show() {
        // Create modern welcome section
        Label welcomeLabel = new Label("Benvenuto nel Sistema CMS");
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        
        Label userLabel = new Label("Ciao, " + utente.getNome() + " " + utente.getCognome());
        userLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b; -fx-padding: 0 0 32 0;");
        
        Label rolePromptLabel = new Label("Seleziona il tuo ruolo per accedere alle funzionalità:");
        rolePromptLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #374151; -fx-padding: 0 0 24 0;");

        // Create role cards container
        TilePane roleContainer = new TilePane();
        roleContainer.setHgap(20);
        roleContainer.setVgap(20);
        roleContainer.setAlignment(Pos.CENTER);
        roleContainer.setPrefColumns(2);

        String ruolo = utente.getRuolo(); // esempio: "CA--", "C--E", ecc.
        boolean haRuoli = false;

        if (ruolo.charAt(0) == 'C') {
            VBox chairCard = createRoleCard("Chair", "📋", "Gestisci conferenze", "#10b981", 
                e -> ctrl.apriHomepageChair());
            roleContainer.getChildren().add(chairCard);
            haRuoli = true;
        }

        if (ruolo.charAt(1) == 'A') {
            VBox autoreCard = createRoleCard("Autore", "📝", "Sottometti articoli", "#2563eb", 
                e -> {
                    BoundaryDBMS db = new BoundaryDBMS();
                    ControlSottomissioni ctrlSottom = new ControlSottomissioni(db, utente, ctrl, stage);
                    HomepageAutore homepageAutore = new HomepageAutore(stage, ctrlSottom, ctrl, utente);
                    homepageAutore.show();
                });
            roleContainer.getChildren().add(autoreCard);
            haRuoli = true;
        }

        if (ruolo.charAt(2) == 'R') {
            VBox revisoreCard = createRoleCard("Revisore", "🔍", "Revisiona articoli", "#f59e0b", 
                e -> {
                    // revisoreBtn.setOnAction(e -> ctrl.apriHomepageRevisore());
                    showAlert("Funzionalità in fase di sviluppo", Alert.AlertType.INFORMATION);
                });
            roleContainer.getChildren().add(revisoreCard);
            haRuoli = true;
        }

        if (ruolo.charAt(3) == 'E') {
            VBox editoreCard = createRoleCard("Editore", "✏", "Gestisci pubblicazioni", "#8b5cf6", 
                e -> {
                    ctrl.apriHomepageEditor();
                });
            roleContainer.getChildren().add(editoreCard);
            haRuoli = true;
        }

        VBox mainContent = new VBox(24);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        if (!haRuoli) {
            VBox errorCard = new VBox(16);
            errorCard.setAlignment(Pos.CENTER);
            errorCard.setPadding(new Insets(32));
            errorCard.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; " +
                              "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                              "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.1), 8, 0, 0, 2); " +
                              "-fx-max-width: 500;");
            
            Label errorIcon = new Label("⚠⚠");
            errorIcon.setStyle("-fx-font-size: 48px;");
            
            Label errorTitle = new Label("Nessun Ruolo Assegnato");
            errorTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #dc2626;");
            
            Label errorMessage = new Label("Non hai ruoli attivi nel sistema.");
            errorMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f1d1d; -fx-text-alignment: center; -fx-wrap-text: true;");
            
            errorCard.getChildren().addAll(errorIcon, errorTitle, errorMessage);
            mainContent.getChildren().addAll(welcomeLabel, userLabel, errorCard);
        } else {
            mainContent.getChildren().addAll(welcomeLabel, userLabel, rolePromptLabel, roleContainer);
        }

        HeaderBar header = new HeaderBar(ctrl, this::show);
        header.getBtnBack().setOnAction(e -> ctrl.richiestaLogout());
        
        VBox root = new VBox(header, mainContent);

        Scene scene = new Scene(root, 1050, 850);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Dashboard");
        stage.show();
    }

    private VBox createRoleCard(String title, String icon, String description, String accentColor, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32));
        card.setPrefWidth(220);
        card.setPrefHeight(160);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                     "-fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 4); " +
                     "-fx-cursor: hand;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-text-alignment: center; -fx-wrap-text: true;");

        Button actionButton = new Button("Accedi");
        actionButton.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: white; " +
                             "-fx-border-color: transparent; -fx-padding: 10 24 10 24; " +
                             "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 13px;");
        actionButton.setOnAction(action);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel, actionButton);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #ffffff; -fx-border-color: " + accentColor + "; " +
                         "-fx-border-width: 2; -fx-border-radius: 16; -fx-background-radius: 16; " +
                         "-fx-effect: dropshadow(gaussian, " + hexToRgba(accentColor, 0.2) + ", 15, 0, 0, 6); " +
                         "-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                         "-fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 4); " +
                         "-fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        return card;
    }

    private String hexToRgba(String hex, double alpha) {
        // Simple conversion for common colors
        switch (hex) {
            case "#10b981": return "rgba(16, 185, 129, " + alpha + ")";
            case "#2563eb": return "rgba(37, 99, 235, " + alpha + ")";
            case "#f59e0b": return "rgba(245, 158, 11, " + alpha + ")";
            case "#8b5cf6": return "rgba(139, 92, 246, " + alpha + ")";
            default: return "rgba(0, 0, 0, " + alpha + ")";
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}