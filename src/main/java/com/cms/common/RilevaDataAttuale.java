package com.cms.common;

import com.cms.gestioneRevisioni.ControlRevisioni;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RilevaDataAttuale {
    public RilevaDataAttuale() {
        LocalDate dataOggi = LocalDate.now();
        ControlRevisioni ctrlRevisioni = new ControlRevisioni(new BoundaryDBMS());
        ctrlRevisioni.avviaGraduatoria(dataOggi);
        ctrlRevisioni.avviaAssegnazioneAutomatica(dataOggi);
    }
} 