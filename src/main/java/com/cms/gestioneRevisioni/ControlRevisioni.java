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
import com.cms.utils.MailUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ControlRevisioni {

    private final BoundaryDBMS db;

    // Costruttore della classe ControlRevisioni
    public ControlRevisioni(BoundaryDBMS db) {
        this.db = db;
    }

    // Avvia l'assegnazione automatica degli articoli ai revisori per una data specifica
    public void avviaAssegnazioneAutomatica(LocalDate data) {
        List<EntityConferenza> conferenze = db.getConferenzeAutomaticheConScadenzaSottomissione(data).stream()
                .filter(conf -> conf.getModalitaDistribuzione() == Distribuzione.AUTOMATICA)
                .collect(Collectors.toList());

        for (EntityConferenza conf : conferenze) {
            assegnaArticoliConferenza(conf);
        }
    }

    // Assegna gli articoli di una conferenza ai revisori
    private void assegnaArticoliConferenza(EntityConferenza conf) {
        String confId = conf.getId();
        int minimoRevisori = conf.getNumeroMinimoRevisori();

        List<EntityArticolo> articoli = db.getArticoliConferenza(confId).stream()
                .filter(art -> art.getStato().equals("Sottomesso"))
                .filter(art -> db.getRevisioniArticolo(art.getId()).isEmpty())
                .collect(Collectors.toList());
        Map<String, List<String>> competenzeRevisori = db.getCompetenzeRevisori(confId);

        Map<String, Set<String>> paroleChiaveArticolo = articoli.stream()
                .collect(Collectors.toMap(EntityArticolo::getId, a -> splitKeywords(a.getParoleChiave())));

        // Contatore globale per numero di articoli assegnati a ciascun revisore
        Map<String, Integer> revisoreAssegnazioni = new HashMap<>();
        competenzeRevisori.keySet().forEach(r -> revisoreAssegnazioni.put(r, 0));

        Map<String, List<String>> assegnazioni = new HashMap<>();

        for (EntityArticolo art : articoli) {
            Set<String> keywords = paroleChiaveArticolo.get(art.getId());

            List<String> revisoriOrdinati = competenzeRevisori.entrySet().stream()
                .sorted((e1, e2) -> {
                    int m1 = matchScore(keywords, e1.getValue());
                    int m2 = matchScore(keywords, e2.getValue());

                    if (m1 != m2) {
                        return Integer.compare(m2, m1); // prima per match decrescente
                    } else {
                        // a parità di match, scegli chi ha meno articoli assegnati
                        return Integer.compare(revisoreAssegnazioni.get(e1.getKey()), revisoreAssegnazioni.get(e2.getKey()));
                    }
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            // seleziona i primi N revisori
            List<String> selezionati = revisoriOrdinati.stream()
                .limit(minimoRevisori)
                .collect(Collectors.toList());

            // aggiorna il carico
            for (String revisore : selezionati) {
                revisoreAssegnazioni.put(revisore, revisoreAssegnazioni.get(revisore) + 1);
            }

            assegnazioni.put(art.getId(), selezionati);
        }

        db.comunicaAssegnazioni(confId, assegnazioni);
    }


    // Divide le parole chiave in un set
    private Set<String> splitKeywords(String paroleChiave) {
        if (paroleChiave == null) return Collections.emptySet();
        return Arrays.stream(paroleChiave.split("[,;\\s]+"))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    // Calcola il punteggio di match tra parole chiave e competenze
    private int matchScore(Set<String> keywords, List<String> competenze) {
        if (keywords.isEmpty() || competenze == null) return 0;
        Set<String> compSet = competenze.stream().map(String::toLowerCase).collect(Collectors.toSet());
        compSet.retainAll(keywords);
        return compSet.size();
    }

    // Restituisce lo stato delle revisioni per la conferenza
    public List<InfoRevisioniChair.RevisionRow> getStatoRevisioni(String confId) {
        List<Map<String, String>> raw = db.getDatiRevisioni(confId);
        List<InfoRevisioniChair.RevisionRow> list = new ArrayList<>();

        for (Map<String, String> m : raw) {
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

    // Rimuove l'assegnazione di una revisione
    public void rimuoviAssegnazione(String idRevisione) {
        db.rimuoviAssegnazione(idRevisione);
    }

    // Visualizza la revisione (download)
    public void visualizzaRevisione(String idRevisione, String revisore) {
        Optional<File> revisione = db.getRevisione(idRevisione);
        if (revisione.isPresent() && revisione != null) {
            DownloadUtil.salvaInDownload(revisione.get(), "Revisione di " + revisore);
        } else {
            new PopupErrore("Revisione non presente").show();
        }
    }

    // Avvia la procedura di aggiunta assegnazione
    public void avviaAggiungiAssegnazione(String confId, Runnable onRefresh) {
        LocalDate oggi = LocalDate.now();
        LocalDate scadRev = db.getDataScadenzaRevisioni(confId);

        if (oggi.isBefore(scadRev) || oggi.isEqual(scadRev)) {
            // Recupera articoli e revisori
            List<EntityArticolo> articoli = db.getArticoliConferenza(confId);
            Map<String, String> revisoriStato = db.getRevisoriConStato(confId);
            List<String> revisori = new ArrayList<>();
            for (var entry : revisoriStato.entrySet()) {
                if ("Accettato".equalsIgnoreCase(entry.getValue())) {
                    revisori.add(entry.getKey());
                }
            }

            if (articoli.isEmpty() || revisori.isEmpty()) {
                new PopupAvviso("Nessun articolo o revisore disponibile").show();
                return;
            }

            Platform.runLater(() -> {
                new PopupInserimento()
                        .promptAssegnazione(articoli, revisori)
                        .ifPresent(sel -> {
                            String artId = sel.get("articolo_id");
                            String revEmail = sel.get("revisore_email");
                            boolean inserito = db.aggiungiAssegnazione(artId, revEmail);
                            if (!inserito) {
                                new PopupErrore("L'assegnazione tra revisore e articolo è già presente.").show();
                            } else {
                                new PopupAvviso("Assegnazione inserita").show();
                                if (onRefresh != null) onRefresh.run();
                            }
                        });
            });
        } else {
            new PopupAvviso("Errore, non sei nel periodo delle revisioni").show();
        }
    }

    // Avvia la generazione della graduatoria per le conferenze senza graduatoria
    public void avviaGraduatoria(LocalDate data) {
        List<EntityConferenza> confs = db.getConferenzeSenzaGraduatoria(data);
        for (EntityConferenza conf : confs) {
            calcolaPunteggioArticoli(conf);
            creaGraduatoria(conf);
        }
    }

    // Calcola il punteggio degli articoli di una conferenza
    private void calcolaPunteggioArticoli(EntityConferenza conf) {
        String confId = conf.getId();
        List<EntityArticolo> arts = db.getArticoliConferenza(confId);
        for (EntityArticolo art : arts) {
            double numeratore = 0.0;
            double denominatore = 0.0;
            Map<String, String> revisioni = db.getRevisioniArticolo(art.getId());
            
            for (Map.Entry<String, String> entry : revisioni.entrySet()) {
                String descr = entry.getValue();
                // Estrai voto ed expertise dalla descrizione
                // Formato: "Revisore: email - Voto: X - Expertise: Y"
                String[] parts = descr.split(" - ");
                if (parts.length >= 3) {
                    try {
                        int voto = Integer.parseInt(parts[1].replace("Voto: ", ""));
                        int expertise = Integer.parseInt(parts[2].replace("Expertise: ", ""));
                        numeratore += voto * expertise;
                        denominatore += expertise;
                    } catch (NumberFormatException e) {
                        // Ignora revisioni con valori non validi
                    }
                }
            }
            
            Double punteggio = (denominatore != 0) ? (numeratore / denominatore) : null;
            db.aggiornaPunteggioArticolo(art.getId(), punteggio);   
        }
    }

    // Crea la graduatoria per una conferenza
    private void creaGraduatoria(EntityConferenza conf) {
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

    // Restituisce la mappa degli inviti per un revisore
    public Map<EntityConferenza,String> getInvitiRevisore(String email) {
        return db.getConferenzeRevisore(email);
    }

    // Aggiorna lo stato di un invito revisore
    public void aggiornaInvito(String confId, String emailRevisore, String stato) {
        db.aggiornaInvitoConferenza(confId, emailRevisore, stato);
    }

    // Restituisce lo stato dell'invito di un revisore
    public String getStatoInvitoRevisore(String confId, String emailRevisore) {
        return db.getStatoInvitoRevisore(confId, emailRevisore);
    }

    // Restituisce la conferenza per un revisore
    public Optional<EntityConferenza> getConferenzaRevisore(String confId, String email) {
        return db.getConferenzaRevisore(confId, email);
    }

    // Visualizza la conferenza per un revisore
    public void visualizzaConferenza(String idConferenza, String emailRevisore) {
        Optional<EntityConferenza> conferenzaOpt = db.getConferenzaRevisore(idConferenza, emailRevisore);
        if (conferenzaOpt.isPresent()) {
            // Apri la finestra InfoConferenzaRevisore
            Platform.runLater(() -> {
                // Creiamo un nuovo stage per la finestra InfoConferenzaRevisore
                Stage newStage = new Stage();
                new PopupAvviso("Funzionalità in fase di aggiornamento").show();
            });
        }
    }

    // Visualizza la conferenza per un revisore (con ControlAccount)
    public void visualizzaConferenza(String idConferenza, ControlAccount ctrlAccount) {
        String emailRevisore = ctrlAccount.getUtenteCorrente().getEmail();
        Optional<EntityConferenza> conferenzaOpt = db.getConferenzaRevisore(idConferenza, emailRevisore);
        if (conferenzaOpt.isPresent()) {
            // Apri la finestra InfoConferenzaRevisore
            Platform.runLater(() -> {
                // Creiamo un nuovo stage per la finestra InfoConferenzaRevisore
                Stage newStage = new Stage();
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

    // Restituisce la lista degli articoli assegnati a un revisore
    public List<String> getArticoliRevisore(String confId, String email) {
        return db.getArticoliRevisore(confId, email);
    }

    // Visualizza il file dell'articolo assegnato
    public void visualizzaArticolo(String idArticolo) {
        Optional<File> articolo = db.getArticolo(idArticolo);
        if (articolo.isPresent() && articolo != null) {
            DownloadUtil.salvaInDownload(articolo.get(), "Articolo");
        } else {
            new PopupErrore("Articolo non presente").show();
        }
    }

    // Carica una revisione per un articolo
    public void caricaRevisione(String idArticolo, String emailRevisore, int voto, int expertise, File file) {
        db.caricaRevisione(emailRevisore, idArticolo, voto, expertise, file);
        // Notifica e mail all'autore
        Optional<EntityArticolo> artOpt = getArticoloById(idArticolo);
        if (artOpt.isPresent()) {
            EntityArticolo articolo = artOpt.get();
            String emailAutore = articolo.getAutoreId();
            String messaggio = "Gentile Autore,\n" +
                    "È stata caricata una revisione per il tuo articolo '" + articolo.getTitolo() + "'.";
            boolean notificaInserita = db.inserisciNotifica(emailAutore, messaggio);
            // if (notificaInserita)
            //     MailUtil.inviaMail(messaggio, emailAutore, "Revisione caricata - " + articolo.getTitolo());
        }
    }

    // Aggiunge un articolo da revisionare per un revisore
    public void aggiungiArticoloRevisore(String confId, String emailRevisore) {
        LocalDate oggi = LocalDate.now();
        LocalDate scadRev = db.getDataScadenzaRevisioni(confId);

        if (oggi.isBefore(scadRev) || oggi.isEqual(scadRev)) {
            if (db.isModalitaBroadcast(confId)) {
                List<EntityArticolo> articoli = db.getArticoliDisponibili(confId, emailRevisore);
                if (articoli.isEmpty()) {
                    new PopupAvviso("Nessun articolo disponibile per l'assegnazione").show();
                    return;
                }

                // Utilizza il PopupInserimento
                new PopupInserimento()
                        .promptSelezionaArticoli(articoli, this)
                        .ifPresent(selezionati -> {
                            for (EntityArticolo art : selezionati) {
                                db.assegnaArticoloRevisore(art.getId(), emailRevisore);
                                // Notifica e mail all'autore
                                String emailAutore = art.getAutoreId();
                                String messaggio = "Gentile Autore,\n" +
                                        "Il tuo articolo '" + art.getTitolo() + "' è stato assegnato a un revisore.";
                                boolean notificaInserita = db.inserisciNotifica(emailAutore, messaggio);
                                // if (notificaInserita)
                                //     MailUtil.inviaMail(messaggio, emailAutore, "Nuova assegnazione revisore - " + art.getTitolo());
                            }
                            new PopupAvviso("Articolo assegnato con successo").show();
                        });
            } else {
                new PopupErrore("Funzionalità non presente, la modalità di assegnazione non è broadcast").show();
            }
        } else {
            new PopupAvviso("Scadenza per revisioni oltrepassata").show();
        }
    }

    // Restituisce il voto della revisione per articolo e revisore
    public Optional<Integer> getVotoRevisione(String idArticolo, String emailRevisore) {
        return db.getVotoRevisione(idArticolo, emailRevisore);
    }

    // Delega la revisione a un sotto-revisore
    public void delegaSottoRevisore(String confId, String titoloArticolo, String emailRevisore) {
        LocalDate oggi = LocalDate.now();
        LocalDate scadRev = db.getDataScadenzaRevisioni(confId);
        if (!oggi.isBefore(scadRev) || !oggi.isEqual(scadRev)) {
            new PopupAvviso("Scadenza per revisioni oltrepassata").show();
            return;
        }

        // Recupera articolo per titolo
        Optional<EntityArticolo> artOpt = db.getArticoliConferenza(confId).stream()
                .filter(a -> a.getTitolo().equalsIgnoreCase(titoloArticolo))
                .findFirst();
        if (artOpt.isEmpty()) {
            new PopupAvviso("Articolo non trovato").show();
            return;
        }
        EntityArticolo articolo = artOpt.get();
        String idArticolo = articolo.getId();

        // Verifica stato (voto)
        Optional<Integer> votoOpt = db.getVotoRevisione(idArticolo, emailRevisore);
        if (votoOpt.isPresent() && votoOpt.get() > 0) {
            new PopupAvviso("Articolo già revisionato").show();
            return;
        }

        // Chiedi email sotto-revisore
        new PopupInserimento().promptEmail("Sotto-revisore")
                .ifPresent(emailSR -> {
                    // Controlla partecipazione alla conferenza
                    if (!db.queryRevisorePresente(emailSR, confId)) {
                        new PopupAvviso("Impossibile invitare sotto-revisore esterno alla conferenza").show();
                        return;
                    }
                    // Inserisci delega (assegni articolo al sotto-revisore)
                    db.assegnaArticoloRevisore(idArticolo, emailSR);
                    // Elimina la revisione del revisore principale
                    db.getIdRevisione(idArticolo, emailRevisore).ifPresent(db::rimuoviAssegnazione);
                    // Notifica e mail all'autore
                    Optional<EntityArticolo> artOpt2 = getArticoloById(idArticolo);
                    
                    // Notifica e mail al sotto-revisore
                    String messaggio = "Gentile Revisore,\n" +
                            "Ti è stata delegata la revisione dell'articolo '" + titoloArticolo + "'.";
                    boolean notificaInserita = db.inserisciNotifica(emailSR, messaggio);
                    // if (notificaInserita)
                    //     MailUtil.inviaMail(messaggio, emailSR, "Delegata revisione - " + titoloArticolo);
                    new PopupAvviso("Revisione delegata con successo a " + db.getNomeCompleto(emailSR).orElse(emailSR)).show();
                });
    }

    // Restituisce l'articolo dato l'id
    public Optional<EntityArticolo> getArticoloById(String idArticolo) {
        return db.getDatiArticoloById(idArticolo);
    }

    // Restituisce l'expertise della revisione per articolo e revisore
    public Optional<Integer> getExpertiseRevisione(String idArticolo, String emailRevisore) {
        return db.getExpertiseRevisione(idArticolo, emailRevisore);
    }

    // Restituisce il nome completo dell'autore dato l'email
    public String getNomeCompletoAutore(String email) {
        return db.getNomeCompleto(email).orElse(email);
    }
} 