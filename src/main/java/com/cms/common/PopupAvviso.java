package com.cms.common;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class PopupAvviso {
    private final String msg;
    public PopupAvviso(String msg) { this.msg = msg; }
    public void show() {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
