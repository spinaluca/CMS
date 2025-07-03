package com.cms.entity;

public class EntityArticolo {
    private final String id;
    private final String conferenzaId;
    private final String titolo;
    private final String paroleChiave;
    private final String stato;
    private final String autoreId;
    private Integer posizione;       // modificabile dal Chair dopo graduatoria
    private Integer numRevisioni;    // aggiornabile in base ai dati di review
    private Double punteggio;        // disponibile dopo graduatoria

    public EntityArticolo(String id, String conferenzaId, String titolo,
                          String paroleChiave, String stato, String autoreId,
                          Integer posizione, Integer numRevisioni, Double punteggio) {
        this.id = id;
        this.conferenzaId = conferenzaId;
        this.titolo = titolo;
        this.paroleChiave = paroleChiave;
        this.stato = stato;
        this.autoreId = autoreId;
        this.posizione = posizione;
        this.numRevisioni = numRevisioni;
        this.punteggio = punteggio;
    }

    public String getId() {
        return id;
    }

    public String getConferenzaId() {
        return conferenzaId;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getParoleChiave() {
        return paroleChiave;
    }

    public String getStato() {
        return stato;
    }

    public String getAutoreId() {
        return autoreId;
    }

    public Integer getPosizione() {
        return posizione;
    }

    public void setPosizione(Integer posizione) {
        this.posizione = posizione;
    }

    public Integer getNumRevisioni() {
        return numRevisioni;
    }

    public void setNumRevisioni(Integer numRevisioni) {
        this.numRevisioni = numRevisioni;
    }

    public Double getPunteggio() {
        return punteggio;
    }

    public void setPunteggio(Double punteggio) {
        this.punteggio = punteggio;
    }
}
