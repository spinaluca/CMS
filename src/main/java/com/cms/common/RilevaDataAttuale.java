package com.cms.common;

import com.cms.gestioneAccount.ControlAccount;
import com.cms.gestioneNotifiche.ControlNotifiche;
import com.cms.gestioneRevisioni.ControlRevisioni;
import com.cms.gestioneConferenze.ControlConferenze;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RilevaDataAttuale {
    // Costruttore della classe RilevaDataAttuale: avvia le operazioni automatiche giornaliere
    public RilevaDataAttuale() {
        LocalDate dataOggi = LocalDate.now();

        ControlConferenze ctrlConferenze = new ControlConferenze(new BoundaryDBMS());
        ctrlConferenze.eliminaArticoliScaduti();
        
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