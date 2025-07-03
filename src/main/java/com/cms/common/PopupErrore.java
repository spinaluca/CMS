package com.cms.common;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class PopupErrore {
    private final String msg;
    public PopupErrore(String msg) { this.msg = msg; }
    public void show() {
        Alert a = new Alert(AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
