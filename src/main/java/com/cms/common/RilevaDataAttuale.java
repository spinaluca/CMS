package com.cms.common;

import com.cms.gestioneAccount.ControlAccount;
import com.cms.gestioneNotifiche.ControlNotifiche;
import com.cms.gestioneRevisioni.ControlRevisioni;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RilevaDataAttuale {
    public RilevaDataAttuale() {
        LocalDate dataOggi = LocalDate.now();
        ControlRevisioni ctrlRevisioni = new ControlRevisioni(new BoundaryDBMS());
        ctrlRevisioni.avviaGraduatoria(dataOggi);
        ctrlRevisioni.avviaAssegnazioneAutomatica(dataOggi);
        
        // Avvia le notifiche automatiche
        BoundaryDBMS db = new BoundaryDBMS();
        ControlAccount ctrlAccount = new ControlAccount(new Stage(), db);
        ControlNotifiche ctrlNotifiche = new ControlNotifiche(ctrlAccount);
        ctrlNotifiche.gestisciNotificheAutomatiche();
    }
} 