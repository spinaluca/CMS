package com.cms.gestioneRevisioni;

import com.cms.common.BoundaryDBMS;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityConferenza.Distribuzione;
import com.cms.common.PopupInserimento;
import com.cms.common.PopupAvviso;
import com.cms.gestioneRevisioni.InfoConferenzaRevisore;
import com.cms.gestioneAccount.ControlAccount;

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

        List<EntityArticolo> articoli = db.queryGetArticoliConferenza(confId);
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
        Map<String, String> statoRevisioni = db.getStatoRevisioni(confId);
        List<InfoRevisioniChair.RevisionRow> result = new ArrayList<>();
        
        // TODO: Implementare la conversione da Map a List<RevisionRow>
        // Per ora restituiamo una lista vuota
        return result;
    }

    /** Rimuove l'assegnazione (caso d'uso 4.1.7.6). */
    public void rimuoviAssegnazione(String idRevisione) {
        db.rimuoviAssegnazione(idRevisione);
    }

    /** Visualizza revisione (download) (4.1.7.4). Ritorna Optional true se presente. */
    public java.util.Optional<Boolean> visualizzaRevisioneChair(String idRevisione) {
        return db.getRevisione(idRevisione).map(file -> {
            com.cms.utils.DownloadUtil.salvaInDownload(file, "revisione_" + idRevisione);
            return true;
        });
    }

    /** Avvia procedura di aggiunta assegnazione (stub). */
    public void avviaAggiungiAssegnazione(String confId) {
        java.time.LocalDate oggi = java.time.LocalDate.now();
        java.time.LocalDate scadSottom = db.getDataScadenzaSottomissione(confId);
        java.time.LocalDate scadRev = db.getDataScadenzaRevisioni(confId);

        if (oggi.isAfter(scadSottom) && oggi.isBefore(scadRev)) {
            // Recupera articoli e revisori
            java.util.List<com.cms.entity.EntityArticolo> articoli = db.queryGetArticoliConferenza(confId);
            java.util.List<String> revisori = db.getRevisoriConferenza(confId);

            if (articoli.isEmpty() || revisori.isEmpty()) {
                new com.cms.common.PopupAvviso("Nessun articolo o revisore disponibile").show();
                return;
            }

            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
                dialog.setTitle("Nuova Assegnazione");
                dialog.setHeaderText("Seleziona articolo e revisore");

                javafx.scene.control.ButtonType okBtn = new javafx.scene.control.ButtonType("Conferma", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(okBtn, javafx.scene.control.ButtonType.CANCEL);

                javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                grid.setHgap(10); grid.setVgap(10);

                javafx.scene.control.ChoiceBox<com.cms.entity.EntityArticolo> cbArt = new javafx.scene.control.ChoiceBox<>();
                cbArt.getItems().addAll(articoli);
                cbArt.setConverter(new javafx.util.StringConverter<>() {
                    @Override public String toString(com.cms.entity.EntityArticolo a){return a.getTitolo();}
                    @Override public com.cms.entity.EntityArticolo fromString(String s){return null;}
                });

                javafx.scene.control.ChoiceBox<String> cbRev = new javafx.scene.control.ChoiceBox<>();
                revisori.forEach(email -> cbRev.getItems().add(email));

                grid.addRow(0, new javafx.scene.control.Label("Articolo:"), cbArt);
                grid.addRow(1, new javafx.scene.control.Label("Revisore:"), cbRev);

                dialog.getDialogPane().setContent(grid);

                dialog.setResultConverter(btn -> btn==okBtn?null:null);

                java.util.Optional<Void> res = dialog.showAndWait();
                if (res.isPresent()) {
                    com.cms.entity.EntityArticolo artSel = cbArt.getSelectionModel().getSelectedItem();
                    String revSel = cbRev.getSelectionModel().getSelectedItem();
                    if (artSel==null || revSel==null) {
                        new com.cms.common.PopupAvviso("Seleziona articolo e revisore").show();
                        return;
                    }
                    String emailRevisore = revSel.split(" ")[0];
                    db.aggiungiAssegnazione(artSel.getId(), emailRevisore);
                    new com.cms.common.PopupAvviso("Assegnazione inserita").show();
                }
            });
        } else {
            new com.cms.common.PopupAvviso("Errore, periodo delle revisioni già concluso").show();
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
        List<EntityArticolo> arts = db.queryGetArticoliConferenza(confId);
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

    public Optional<Boolean> visualizzaArticolo(String idArticolo) {
        return db.getArticolo(idArticolo).map(file -> {
            com.cms.utils.DownloadUtil.salvaInDownload(file, "articolo_" + idArticolo);
            return true;
        });
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
                List<EntityArticolo> articoli = db.getArticoliDisponibili(confId);
                if (articoli.isEmpty()) {
                    new com.cms.common.PopupAvviso("Nessun articolo disponibile per l'assegnazione").show();
                    return;
                }

                // Dialog selezione articoli
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Dialog<List<EntityArticolo>> dialog = new javafx.scene.control.Dialog<>();
                    dialog.setTitle("Seleziona Articoli");
                    dialog.setHeaderText("Scegli gli articoli da revisionare");

                    javafx.scene.control.ButtonType okBtn = new javafx.scene.control.ButtonType("Conferma", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(okBtn, javafx.scene.control.ButtonType.CANCEL);

                    javafx.scene.control.ListView<EntityArticolo> listView = new javafx.scene.control.ListView<>();
                    listView.getItems().addAll(articoli);
                    listView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
                    listView.setCellFactory(lv -> new javafx.scene.control.ListCell<EntityArticolo>() {
                        @Override
                        protected void updateItem(EntityArticolo item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getTitolo());
                            }
                        }
                    });

                    dialog.getDialogPane().setContent(listView);
                    dialog.setResultConverter(btn -> btn == okBtn ? 
                        new ArrayList<>(listView.getSelectionModel().getSelectedItems()) : null);

                    java.util.Optional<List<EntityArticolo>> result = dialog.showAndWait();
                    result.ifPresent(selezionati -> {
                        for (EntityArticolo art : selezionati) {
                            db.assegnaArticoloRevisore(art.getId(), emailRevisore);
                        }
                        new com.cms.common.PopupAvviso("Articoli assegnati con successo").show();
                    });
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
} 