package com.cms.gestioneNotifiche;

import com.cms.common.BoundaryDBMS;
import com.cms.gestioneAccount.ControlAccount;
import java.util.List;
import java.util.Map;
import javafx.stage.Stage;

public class ControlNotifiche {
    private final BoundaryDBMS db = new BoundaryDBMS();
    private final ControlAccount ctrlAccount;

    public ControlNotifiche(ControlAccount ctrlAccount) {
        this.ctrlAccount = ctrlAccount;
    }
    
    public void mostraPannelloNotifiche() {
        String email = ctrlAccount.getUtenteCorrente().getEmail();
        List<Map<String, String>> notifiche = db.getNotifiche(email);
        new PannelloNotifiche(new Stage(), this, notifiche).show();
    }

    public List<Map<String, String>> cancellaNotifica(String idNotifica) {
        db.cancellaNotifica(idNotifica);
        String email = ctrlAccount.getUtenteCorrente().getEmail();
        List<Map<String, String>> notifiche = db.getNotifiche(email);
        return notifiche;
    }
} 