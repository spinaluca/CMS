package com.cms.entity;

import java.time.LocalDate;
import java.util.*;

/**
 * Rappresenta una conferenza con tutti i suoi dati, inclusi chairId e modalità di distribuzione.
 */
public class EntityConferenza {
    private final String id;
    private final String acronimo;
    private final String titolo;
    private final String descrizione;
    private final String luogo;
    private final LocalDate scadenzaSottomissione;
    private final LocalDate scadenzaRevisioni;
    private final LocalDate dataGraduatoria;
    private final LocalDate scadenzaCameraReady;
    private final LocalDate scadenzaFeedbackEditore;
    private final LocalDate scadenzaVersioneFinale;
    private final int numeroMinimoRevisori;
    private final int valutazioneMinima;
    private final int valutazioneMassima;
    private final int numeroVincitori;
    public enum Distribuzione {
        MANUALE, AUTOMATICA, BROADCAST;

        public static Distribuzione fromString(String s) {
            if (s == null) return null;
            return Distribuzione.valueOf(s.trim().toUpperCase());
        }
    }
    private final Distribuzione modalitaDistribuzione;

    // campi gestiti dopo creazione
    private String chairId;
    private final List<String> revisori = new ArrayList<>();
    private Optional<String> editor = Optional.empty();

    public EntityConferenza(String id,
                            String acronimo,
                            String titolo,
                            String descrizione,
                            String luogo,
                            LocalDate scadenzaSottomissione,
                            LocalDate scadenzaRevisioni,
                            LocalDate dataGraduatoria,
                            LocalDate scadenzaCameraReady,
                            LocalDate scadenzaFeedbackEditore,
                            LocalDate scadenzaVersioneFinale,
                            int numeroMinimoRevisori,
                            int valutazioneMinima,
                            int valutazioneMassima,
                            int numeroVincitori,
                            Distribuzione modalitaDistribuzione) {
        this.id = id;
        this.acronimo = acronimo;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.luogo = luogo;
        this.scadenzaSottomissione = scadenzaSottomissione;
        this.scadenzaRevisioni = scadenzaRevisioni;
        this.dataGraduatoria = dataGraduatoria;
        this.scadenzaCameraReady = scadenzaCameraReady;
        this.scadenzaFeedbackEditore = scadenzaFeedbackEditore;
        this.scadenzaVersioneFinale = scadenzaVersioneFinale;
        this.numeroMinimoRevisori = numeroMinimoRevisori;
        this.valutazioneMinima = valutazioneMinima;
        this.valutazioneMassima = valutazioneMassima;
        this.numeroVincitori = numeroVincitori;
        this.modalitaDistribuzione = modalitaDistribuzione;
    }

    // --- getter per JDBC insertion -----------------------------------------

    public String getId() {
        return id;
    }

    public String getAcronimo() {
        return acronimo;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getLuogo() {
        return luogo;
    }

    public LocalDate getScadenzaSottomissione() {
        return scadenzaSottomissione;
    }

    public LocalDate getScadenzaRevisioni() {
        return scadenzaRevisioni;
    }

    public LocalDate getDataGraduatoria() {
        return dataGraduatoria;
    }

    public LocalDate getScadenzaCameraReady() {
        return scadenzaCameraReady;
    }

    public LocalDate getScadenzaFeedbackEditore() {
        return scadenzaFeedbackEditore;
    }

    public LocalDate getScadenzaVersioneFinale() {
        return scadenzaVersioneFinale;
    }

    public int getNumeroMinimoRevisori() {
        return numeroMinimoRevisori;
    }

    public int getValutazioneMinima() {
        return valutazioneMinima;
    }

    public int getValutazioneMassima() {
        return valutazioneMassima;
    }

    public int getNumeroVincitori() {
        return numeroVincitori;
    }

    public Distribuzione getModalitaDistribuzione() {
        return modalitaDistribuzione;
    }

    public String getChairId() {
        return chairId;
    }

    // setter per chairId (popolato da mapConf o da DAO)
    public void setChairId(String chairId) {
        this.chairId = chairId;
    }

    // --- metodi per gestione revisori/editor post-creazione ---------------

    public List<String> getRevisori() {
        return Collections.unmodifiableList(revisori);
    }

    public void addRevisore(String email) {
        if (!revisori.contains(email)) {
            revisori.add(email);
        }
    }

    public void removeRevisore(String email) {
        revisori.remove(email);
    }

    public Optional<String> getEditor() {
        return editor;
    }

    public void setEditor(String email) {
        this.editor = Optional.ofNullable(email);
    }

    public Optional<String> getUltimaVersione(String idArticolo) {
        return Optional.empty();
    }
}

