package com.cms.gestioneRevisioni;

import com.cms.common.BoundaryDBMS;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityConferenza.Distribuzione;
import com.cms.common.PopupInserimento;
import com.cms.common.PopupAvviso;
import com.cms.gestioneRevisioni.InfoConferenzaRevisore;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.utils.DownloadUtil;
import com.cms.common.PopupErrore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.util.ArrayList;

/**
 * ControlRevisioni gestisce la logica relativa alle revisioni.
 * Per ora implementa l'uso caso 4.1.7.2 "Assegnazione Articoli Automatica".
 */
public class ControlRevisioni {

    private final BoundaryDBMS db;

    public ControlRevisioni(BoundaryDBMS db) {
        this.db = db;
    }

    /**
     * Avvia l'assegnazione automatica degli articoli ai revisori.
     * <p>
     * La logica segue l'uso caso AUTOMATIC_PAPERS_ASSIGNMENT:
     * 1. Viene chiamata idealmente ad intervalli regolari da un componente di scheduling (es. cron interno).
     * 2. Se l'orario corrente è mezzanotte (00:00) allora:
     *    a. Recupera tutte le conferenze che avevano scadenza sottomissione ieri e hanno modalitaDistribuzione AUTOMATICA.
     *    b. Per ogni conferenza calcola le assegnazioni articolo → revisori sulla base della corrispondenza fra parole chiave
     *       degli articoli e competenze dichiarate dai revisori, garantendo il rispetto del numero minimo di revisori per articolo.
     *    c. Comunica al DBMS le assegnazioni effettuate.
     */
    public void avviaAssegnazioneAutomatica() {
        // Passo 1: controlla che sia mezzanotte (tolleranza di 1 minuto)
        LocalTime now = LocalTime.now();
        if (now.getHour() != 0) {
            return; // Non è il momento previsto
        }

        LocalDate ieri = LocalDate.now().minusDays(1);

        // Passo 3.1: Recupera conferenze interessate
        List<EntityConferenza> conferenze = db.getConferenzeAutomaticheConScadenza(ieri).stream()
                .filter(conf -> conf.getModalitaDistribuzione() == Distribuzione.AUTOMATICA)
                .collect(Collectors.toList());

        for (EntityConferenza conf : conferenze) {
            assegnaArticoliConferenza(conf);
        }
    }

    private void assegnaArticoliConferenza(EntityConferenza conf) {
        String confId = conf.getId();
        int minimoRevisori = conf.getNumeroMinimoRevisori();

        List<EntityArticolo> articoli = db.getArticoliConferenza(confId);
        Map<String, List<String>> competenzeRevisori = db.getCompetenzeRevisori(confId);

        // Pre-calcola lower-case keywords per articoli
        Map<String, Set<String>> paroleChiaveArticolo = articoli.stream()
                .collect(Collectors.toMap(EntityArticolo::getId, a -> splitKeywords(a.getParoleChiave())));

        // Assegnazioni finali articoloId -> lista emailRevisori
        Map<String, List<String>> assegnazioni = new HashMap<>();

        for (EntityArticolo art : articoli) {
            // Ordina revisori in base alla corrispondenza delle competenze
            List<String> revisoriOrdinati = competenzeRevisori.entrySet().stream()
                    .sorted((e1, e2) -> {
                        int m1 = matchScore(paroleChiaveArticolo.get(art.getId()), e1.getValue());
                        int m2 = matchScore(paroleChiaveArticolo.get(art.getId()), e2.getValue());
                        return Integer.compare(m2, m1); // desc
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // Seleziona i primi N revisori con miglior match, scartando eventuali duplicati
            List<String> selezionati = revisoriOrdinati.stream()
                    .limit(minimoRevisori)
                    .collect(Collectors.toList());

            assegnazioni.put(art.getId(), selezionati);
        }

        // Passo 3.3: comunica al DBMS
        db.comunicaAssegnazioni(confId, assegnazioni);
    }

    private Set<String> splitKeywords(String paroleChiave) {
        if (paroleChiave == null) return Collections.emptySet();
        return Arrays.stream(paroleChiave.split("[,;\\s]+"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private int matchScore(Set<String> keywords, List<String> competenze) {
        if (keywords.isEmpty() || competenze == null) return 0;
        Set<String> compSet = competenze.stream().map(String::toLowerCase).collect(Collectors.toSet());
        compSet.retainAll(keywords);
        return compSet.size();
    }

    /** Restituisce lo stato delle revisioni per la conferenza. */
    public List<InfoRevisioniChair.RevisionRow> getStatoRevisioni(String confId) {
        java.util.List<java.util.Map<String, String>> raw = db.getDatiRevisioni(confId);
        java.util.List<InfoRevisioniChair.RevisionRow> list = new java.util.ArrayList<>();

        for (java.util.Map<String, String> m : raw) {
            String artId = m.get("art_id");
            String titolo = m.get("titolo");
            String autoreEmail = m.get("autore_id");
            String revisoreEmail = m.get("revisore_id");
            String idReale = m.get("id"); // id reale della revisione, se presente

            String autore = db.getNomeCompleto(autoreEmail).orElse(autoreEmail);
            if (revisoreEmail == null) {
                continue; // non mostrare revisioni senza revisore assegnato
            }
            String revisore = db.getNomeCompleto(revisoreEmail).orElse(revisoreEmail);

            Integer voto = null;
            Integer expertise = null;
            try { if (m.get("voto") != null) voto = Integer.parseInt(m.get("voto")); } catch (NumberFormatException ignored) {}
            try { if (m.get("expertise") != null) expertise = Integer.parseInt(m.get("expertise")); } catch (NumberFormatException ignored) {}

            boolean completata = voto != null && voto > 0;

            // Costruiamo un id combinato articolo|revisore per gestire rimozione/visualizzazione
            String idRevisione = artId + "|" + revisoreEmail;

            list.add(new InfoRevisioniChair.RevisionRow(idRevisione, idReale, titolo, autore, revisore, completata, voto, expertise));
        }

        return list;
    }

    /** Rimuove l'assegnazione (caso d'uso 4.1.7.6). */
    public void rimuoviAssegnazione(String idRevisione) {
        db.rimuoviAssegnazione(idRevisione);
    }

    /** Visualizza revisione (download) (4.1.7.4). Ritorna Optional true se presente. */
    public void visualizzaRevisione(String idRevisione, String revisore) {
        Optional<File> revisione = db.getRevisione(idRevisione);
        if (revisione.isPresent() && revisione != null) {
            DownloadUtil.salvaInDownload(revisione.get(), "Revisione di " + revisore);
        } else {
            new PopupErrore("Revisione non disponibile").show();
        }
    }

    /** Avvia procedura di aggiunta assegnazione (stub). */
    public void avviaAggiungiAssegnazione(String confId, Runnable onRefresh) {
        java.time.LocalDate oggi = java.time.LocalDate.now();
        java.time.LocalDate scadRev = db.getDataScadenzaRevisioni(confId);

        if (oggi.isBefore(scadRev)) {
            // Recupera articoli e revisori
            java.util.List<com.cms.entity.EntityArticolo> articoli = db.getArticoliConferenza(confId);
            java.util.Map<String, String> revisoriStato = db.getRevisoriConStato(confId);
            java.util.List<String> revisori = new java.util.ArrayList<>();
            for (var entry : revisoriStato.entrySet()) {
                if ("Accettato".equalsIgnoreCase(entry.getValue())) {
                    revisori.add(entry.getKey());
                }
            }

            if (articoli.isEmpty() || revisori.isEmpty()) {
                new com.cms.common.PopupAvviso("Nessun articolo o revisore disponibile").show();
                return;
            }

            javafx.application.Platform.runLater(() -> {
                new com.cms.common.PopupInserimento()
                        .promptAssegnazione(articoli, revisori)
                        .ifPresent(sel -> {
                            String artId = sel.get("articolo_id");
                            String revEmail = sel.get("revisore_email");
                            db.aggiungiAssegnazione(artId, revEmail);
                            new com.cms.common.PopupAvviso("Assegnazione inserita").show();
                            if (onRefresh != null) onRefresh.run();
                        });
            });
        } else {
            new com.cms.common.PopupAvviso("Errore, non sei nel periodo delle revisioni").show();
        }
    }

    // ==================== Graduatoria (UC 4.1.7.7) =====================

    public void avviaGraduatoria() {
        LocalTime now = LocalTime.now();
        if (now.getHour() != 0) return;

        LocalDate ieri = LocalDate.now().minusDays(1);
        List<EntityConferenza> confs = db.getConferenzeConScadenzaRevisioni(ieri);
        for (EntityConferenza conf : confs) {
            creaGraduatoriaConferenza(conf);
        }
    }

    private void creaGraduatoriaConferenza(EntityConferenza conf) {
        String confId = conf.getId();
        // Recupera articoli e calcola punteggio medio (stub)
        List<EntityArticolo> arts = db.getArticoliConferenza(confId);
        Map<String, Integer> ranking = new HashMap<>();
        arts.sort(Comparator.comparingDouble(a -> -(a.getPunteggio()==null?0:a.getPunteggio())));
        int pos=1;
        for (EntityArticolo a: arts){
            ranking.put(a.getId(), pos++);
        }
        db.comunicaGraduatoria(confId, ranking);
    }

    // ==================== Inviti Revisore (UC 4.1.7.8/9) =================

    public Map<EntityConferenza,String> getInvitiRevisore(String email) {
        return db.getConferenzeRevisore(email);
    }

    public void aggiornaInvito(String confId, String emailRevisore, String stato) {
        db.aggiornaInvitoConferenza(confId, emailRevisore, stato);
    }

    // ==================== Conferenza Revisore (UC 4.1.7.10) =============

    public Optional<EntityConferenza> getConferenzaRevisore(String confId, String email) {
        return db.getConferenzaRevisore(confId, email);
    }

    /**
     * Visualizza i dettagli della conferenza per il revisore (UC 4.1.7.8)
     */
    public void visualizzaConferenza(String idConferenza, String emailRevisore) {
        Optional<EntityConferenza> conferenzaOpt = db.getConferenzaRevisore(idConferenza, emailRevisore);
        if (conferenzaOpt.isPresent()) {
            // Apri la finestra InfoConferenzaRevisore
            javafx.application.Platform.runLater(() -> {
                // Creiamo un nuovo stage per la finestra InfoConferenzaRevisore
                javafx.stage.Stage newStage = new javafx.stage.Stage();
                // TODO: Questo metodo deve essere aggiornato per passare ControlAccount
                // Per ora usiamo un approccio temporaneo
                new PopupAvviso("Funzionalità in fase di aggiornamento").show();
            });
        }
    }

    /**
     * Visualizza i dettagli della conferenza per il revisore con ControlAccount (UC 4.1.7.8)
     */
    public void visualizzaConferenza(String idConferenza, com.cms.gestioneAccount.ControlAccount ctrlAccount) {
        String emailRevisore = ctrlAccount.getUtenteCorrente().getEmail();
        Optional<EntityConferenza> conferenzaOpt = db.getConferenzaRevisore(idConferenza, emailRevisore);
        if (conferenzaOpt.isPresent()) {
            // Apri la finestra InfoConferenzaRevisore
            javafx.application.Platform.runLater(() -> {
                // Creiamo un nuovo stage per la finestra InfoConferenzaRevisore
                javafx.stage.Stage newStage = new javafx.stage.Stage();
                InfoConferenzaRevisore infoConf = new InfoConferenzaRevisore(
                    newStage, 
                    this, 
                    ctrlAccount, 
                    idConferenza
                );
                infoConf.show();
            });
        }
    }

    public List<String> getArticoliRevisore(String confId, String email) {
        return db.getArticoliRevisore(confId, email);
    }

    // ==================== Revisione Articolo (UC 4.1.7.11/12) ===========

    public void visualizzaArticolo(String idArticolo) {
        Optional<File> articolo = db.getArticolo(idArticolo);
        if (articolo.isPresent() && articolo != null) {
            DownloadUtil.salvaInDownload(articolo.get(), "Articolo");
        } else {
            new PopupErrore("Articolo non disponibile").show();
        }
    }

    public void caricaRevisione(String idArticolo, String emailRevisore, int voto, int expertise, File file) {
        db.caricaRevisione(emailRevisore, idArticolo, voto, expertise, file);
    }

    // ==================== Aggiungi Articolo Revisore (UC 4.1.7.15) ======

    public void aggiungiArticoloRevisore(String confId, String emailRevisore) {
        LocalDate oggi = LocalDate.now();
        LocalDate scadRev = db.getDataScadenzaRevisioni(confId);

        if (oggi.isBefore(scadRev)) {
            if (db.isModalitaBroadcast(confId)) {
                java.util.List<com.cms.entity.EntityArticolo> articoli = db.getArticoliDisponibili(confId);
                if (articoli.isEmpty()) {
                    new com.cms.common.PopupAvviso("Nessun articolo disponibile per l'assegnazione").show();
                    return;
                }

                // Utilizza il PopupInserimento
                new com.cms.common.PopupInserimento()
                        .promptSelezionaArticoli(articoli)
                        .ifPresent(selezionati -> {
                            for (com.cms.entity.EntityArticolo art : selezionati) {
                                db.assegnaArticoloRevisore(art.getId(), emailRevisore);
                            }
                            new com.cms.common.PopupAvviso("Articoli assegnati con successo").show();
                        });
            } else {
                new com.cms.common.PopupAvviso("Funzionalità non disponibile, la modalità di assegnazione non è broadcast").show();
            }
        } else {
            new com.cms.common.PopupAvviso("Scadenza per revisioni oltrepassata").show();
        }
    }

    // Ottiene voto della revisione per articolo e revisore
    public Optional<Integer> getVotoRevisione(String idArticolo, String emailRevisore) {
        return db.getVotoRevisione(idArticolo, emailRevisore);
    }

    // ==================== Delega sotto-revisore (UC DELEGATE_REVIEWER) ======
    public void delegaSottoRevisore(String confId, String titoloArticolo, String emailRevisore) {
        java.time.LocalDate oggi = java.time.LocalDate.now();
        java.time.LocalDate scadRev = db.getDataScadenzaRevisioni(confId);
        if (!oggi.isBefore(scadRev)) {
            new com.cms.common.PopupAvviso("Scadenza per revisioni oltrepassata").show();
            return;
        }

        // Recupera articolo per titolo
        java.util.Optional<com.cms.entity.EntityArticolo> artOpt = db.getArticoliConferenza(confId).stream()
                .filter(a -> a.getTitolo().equalsIgnoreCase(titoloArticolo))
                .findFirst();
        if (artOpt.isEmpty()) {
            new com.cms.common.PopupAvviso("Articolo non trovato").show();
            return;
        }
        com.cms.entity.EntityArticolo articolo = artOpt.get();
        String idArticolo = articolo.getId();

        // Verifica stato (voto)
        java.util.Optional<Integer> votoOpt = db.getVotoRevisione(idArticolo, emailRevisore);
        if (votoOpt.isPresent() && votoOpt.get() > 0) {
            new com.cms.common.PopupAvviso("Articolo già revisionato").show();
            return;
        }

        // Chiedi email sotto-revisore
        new com.cms.common.PopupInserimento().promptEmail("Sotto-revisore")
                .ifPresent(emailSR -> {
                    // Controlla partecipazione alla conferenza
                    if (!db.queryRevisorePresente(emailSR, confId)) {
                        new com.cms.common.PopupAvviso("Impossibile invitare sotto-revisore esterno alla conferenza").show();
                        return;
                    }
                    // Inserisci delega (assegni articolo al sotto-revisore)
                    db.assegnaArticoloRevisore(idArticolo, emailSR);
                    // Notifica (solo popup per ora)
                    new com.cms.common.PopupAvviso("Delegato con successo").show();
                });
    }

    public java.util.Optional<com.cms.entity.EntityArticolo> getArticoloById(String idArticolo) {
        return db.getDatiArticoloById(idArticolo);
    }

    public java.util.Optional<Integer> getExpertiseRevisione(String idArticolo, String emailRevisore) {
        return db.getExpertiseRevisione(idArticolo, emailRevisore);
    }
} 