package com.cms.common;

import com.cms.gestioneAccount.ModuloRuoliUtente;
import com.cms.gestioneAccount.ModuloPassword;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.gestioneNotifiche.ControlNotifiche;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HeaderBar extends HBox {

    private final Button btnBack;
    private final Button btnModificaRuoli;
    private final Button btnModificaPw;
    private final Button btnLogout;
    private final Button btnNotifiche;
    private final ControlNotifiche ctrlNotifiche;

    public HeaderBar(ControlAccount ctrl, Runnable onReturn) {
        super(10);

        btnBack = new Button("Indietro");
        btnModificaRuoli = new Button("Modifica Ruoli");
        btnModificaPw = new Button("Modifica Password");
        btnLogout = new Button("Logout");
        btnNotifiche = new Button();
        ImageView bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/bell.png")));
        bellIcon.setFitWidth(22);
        bellIcon.setFitHeight(22);
        btnNotifiche.setGraphic(bellIcon);
        btnNotifiche.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-border-color: transparent;");
        btnNotifiche.setPrefSize(36, 36);
        btnNotifiche.setMinSize(36, 36);
        btnNotifiche.setMaxSize(36, 36);
        btnNotifiche.setFocusTraversable(false);

        //btnBack.setOnAction(e -> ctrl.apriHomepageGenerale());
        btnModificaRuoli.setOnAction(e ->
                new ModuloRuoliUtente(ctrl.getStage(), ctrl, onReturn).show()
        );

        btnModificaPw.setOnAction(e ->
                new ModuloPassword(ctrl.getStage(), ctrl, false, onReturn).show()
        );

        btnLogout.setOnAction(e -> ctrl.richiestaLogout());

        ctrlNotifiche = new ControlNotifiche(ctrl);
        btnNotifiche.setOnAction(e -> ctrlNotifiche.mostraPannelloNotifiche());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(btnBack, spacer, btnModificaRuoli, btnModificaPw, btnLogout, btnNotifiche);
        
        // Apply modern header bar styling
        this.setStyle("-fx-background-color: linear-gradient(to right, #1e293b 0%, #334155 100%);" +
                      "-fx-padding: 16 24 16 24;" +
                      "-fx-spacing: 12;" +
                      "-fx-border-color: transparent transparent #e2e8f0 transparent;" +
                      "-fx-border-width: 0 0 1 0;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 2);");
        
        // Apply modern button styles
        String headerButtonStyle = "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                                   "-fx-text-fill: #ffffff;" +
                                   "-fx-border-color: rgba(255, 255, 255, 0.2);" +
                                   "-fx-border-width: 1;" +
                                   "-fx-padding: 8 16 8 16;" +
                                   "-fx-background-radius: 8;" +
                                   "-fx-border-radius: 8;" +
                                   "-fx-font-size: 13px;" +
                                   "-fx-font-weight: 500;";
        
        String logoutButtonStyle = "-fx-background-color: rgba(239, 68, 68, 0.8);" +
                                   "-fx-text-fill: #ffffff;" +
                                   "-fx-border-color: rgba(239, 68, 68, 0.9);" +
                                   "-fx-border-width: 1;" +
                                   "-fx-padding: 8 16 8 16;" +
                                   "-fx-background-radius: 8;" +
                                   "-fx-border-radius: 8;" +
                                   "-fx-font-size: 13px;" +
                                   "-fx-font-weight: 500;";
        
        btnBack.setStyle(headerButtonStyle);
        btnModificaRuoli.setStyle(headerButtonStyle);
        btnModificaPw.setStyle(headerButtonStyle);
        btnLogout.setStyle(logoutButtonStyle);
    }

    // Getter per accesso ai pulsanti
    public Button getBtnBack() {
        return btnBack;
    }
}