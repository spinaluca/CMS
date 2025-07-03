package com.cms.app;

import com.cms.common.BoundaryDBMS;
import com.cms.gestioneAccount.ControlAccount;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        BoundaryDBMS db = new BoundaryDBMS();
        ControlAccount ctrlAccount = new ControlAccount(primaryStage, db);
        ctrlAccount.apriLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}