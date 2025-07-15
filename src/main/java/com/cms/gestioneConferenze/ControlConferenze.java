package com.cms.gestioneConferenze;

import com.cms.common.BoundaryDBMS;
import com.cms.common.PopupAvviso;
import com.cms.common.PopupErrore;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;
import com.cms.utils.DownloadUtil;
import com.cms.utils.MailUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class ControlConferenze {
    private final BoundaryDBMS db;
    public ControlConferenze(BoundaryDBMS db) {
        this.db = db;
    }

    public List<EntityConferenza> getConferenze(EntityUtente utenteCorrente) {
        return db.getConferenze(utenteCorrente.getEmail());
    }

    public EntityConferenza creaConferenza(Map<String, String> map, EntityUtente utenteCorrente) {
        String id = UUID.randomUUID().toString();

        EntityConferenza conf = new EntityConferenza(
                id,
                map.get("acronimo"),
                map.get("titolo"),
                map.get("descrizione"),
                map.get("luogo"),
                LocalDate.parse(map.get("scadenzaSottomissione")),
                LocalDate.parse(map.get("scadenzaRevisioni")),
                LocalDate.parse(map.get("dataGraduatoria")),
                LocalDate.parse(map.get("scadenzaCameraReady")),
                LocalDate.parse(map.get("scadenzaFeedbackEditore")),
                LocalDate.parse(map.get("scadenzaVersioneFinale")),
                Integer.parseInt(map.get("numeroMinimoRevisori")),
                Integer.parseInt(map.get("valutazioneMinima")),
                Integer.parseInt(map.get("valutazioneMassima")),
                Integer.parseInt(map.get("numeroVincitori")),
                EntityConferenza.Distribuzione.valueOf(map.get("modalitaDistribuzione"))
        );

        db.queryCreaConferenza(conf, utenteCorrente.getEmail());
        return conf;
    }

    public Optional<EntityConferenza> getConferenza(String id) {
        return db.getConferenza(id);
    }

    public void invitaRevisore(String email, String confId) {
        if (db.queryRevisorePresente(email, confId)) {
            new PopupErrore("Revisore già invitato").show();
        } else {
            db.queryInvitaRevisore(email, confId);
            new PopupAvviso("Invito inviato a " + email).show();
            // Notifica al revisore invitato
            Optional<EntityConferenza> conferenzaOpt = db.getConferenza(confId);
            if (conferenzaOpt.isPresent()) {
                EntityConferenza conferenza = conferenzaOpt.get();
                String messaggio = "Gentile Revisore,\n" +
                        "Sei stato invitato a partecipare come revisore alla conferenza '" + conferenza.getTitolo() + "'.";
                boolean notificaInserita = db.inserisciNotifica(email, messaggio);
                // if (notificaInserita)
                    // MailUtil.inviaMail(messaggio, email, "Invito revisore - " + conferenza.getTitolo());
            }
        }
    }

    public void rimuoviRevisore(String email, String confId) {
        db.queryRimuoviRevisore(email, confId);
        // Notifica al revisore rimosso
        Optional<EntityConferenza> conferenzaOpt = db.getConferenza(confId);
        if (conferenzaOpt.isPresent()) {
            EntityConferenza conferenza = conferenzaOpt.get();
            String messaggio = "Gentile Revisore,\n" +
                    "Sei stato rimosso come revisore dalla conferenza '" + conferenza.getTitolo() + "'.";
            boolean notificaInserita = db.inserisciNotifica(email, messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, email, "Rimozione revisore - " + conferenza.getTitolo());
        }
    }

    public void aggiungiEditor(String email, String confId) {
        db.queryAggiungiEditor(email, confId);
        new PopupAvviso("Editor aggiunto: " + email).show();
        // Notifica all'editor aggiunto
        Optional<EntityConferenza> conferenzaOpt = db.getConferenza(confId);
        if (conferenzaOpt.isPresent()) {
            EntityConferenza conferenza = conferenzaOpt.get();
            String messaggio = "Gentile Editor,\n" +
                    "Sei stato aggiunto come editor alla conferenza '" + conferenza.getTitolo() + "'.";
            boolean notificaInserita = db.inserisciNotifica(email, messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, email, "Aggiunta editor - " + conferenza.getTitolo());
        }
    }

    public List<EntityArticolo> getArticoliConferenza(String confId) {
        return db.getArticoliConferenza(confId);
    }

    public Map<String, String> getRevisoriConStato(String confId) {
        return db.getRevisoriConStato(confId); // email → stato
    }

    public Optional<String> getLabelUtente(String email) {
        return db.getUtente(email).map(u -> u.getNome() + " " + u.getCognome());
    }

    public int getNumRevisioni(String articoloId) {
        return db.getNumRevisioni(articoloId);
    }

    public void visualizzaUltimaVersione(String idArticolo) {
        Optional<File> ultimaVersione = db.getUltimaVersione(idArticolo, null);
        if (ultimaVersione.isPresent() && ultimaVersione != null) {
            DownloadUtil.salvaInDownload(ultimaVersione.get(), "Ultima Versione");
        } else {
            new PopupErrore("Articolo non presente").show();
        }
    }

    public void aggiornaPosizioneArticolo(String idArticolo, Integer posizione) {
        db.aggiornaPosizioneArticolo(idArticolo, posizione);
    }

    public void eliminaArticoliScaduti() {
        db.eliminaArticoliScaduti();
    }
}