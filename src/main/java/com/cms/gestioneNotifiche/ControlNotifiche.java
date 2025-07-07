package com.cms.gestioneNotifiche;

import com.cms.common.BoundaryDBMS;
import com.cms.gestioneAccount.ControlAccount;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;

public class ControlNotifiche {
    private final BoundaryDBMS db;
    private final ControlAccount ctrlAccount;
    private final Stage stage;

    public ControlNotifiche(BoundaryDBMS db, ControlAccount ctrlAccount, Stage stage) {
        this.db = db;
        this.ctrlAccount = ctrlAccount;
        this.stage = stage;
    }

    public void mostraPannelloNotifiche() {
        String email = ctrlAccount.getUtenteCorrente().getEmail();
        List<Map<String, String>> notifiche = db.getNotifiche(email);
        new PannelloNotifiche(this, notifiche).show();
    }

    public void cancellaNotifica(String idNotifica) {
        db.cancellaNotifica(idNotifica);
        mostraPannelloNotifiche(); // aggiorna la lista
    }

    public void chiudiPannelloNotifiche() {
        // Logica per chiudere il pannello (gestita dal pannello stesso)
    }
} 