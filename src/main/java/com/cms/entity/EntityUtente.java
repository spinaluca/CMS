package com.cms.entity;

import java.time.LocalDate;

public class EntityUtente {
    private final String email;
    private String nome;
    private String cognome;
    private String ruolo;
    public String aree;
    private LocalDate dataNascita;
    private String password;
    private boolean passwordTemporanea;

    public EntityUtente(String email, String nome, String cognome, String ruolo, String aree,
                        LocalDate dataNascita, String password, boolean passwordTemporanea) {
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.aree = aree;
        this.dataNascita = dataNascita;
        this.password = password;
        this.passwordTemporanea = passwordTemporanea;
    }

    public String getEmail() { return email; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getRuolo() { return ruolo; }
    public String getAree() { return aree; }
    public LocalDate getDataNascita() { return dataNascita; }
    public String getPassword() { return password; }
    public boolean isPasswordTemporanea() { return passwordTemporanea; }

    public void setNome(String nome) { this.nome = nome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
    public void setAree(String aree) { this.aree = aree; }
    public void setDataNascita(LocalDate dataNascita) { this.dataNascita = dataNascita; }
    public void setPassword(String password) { this.password = password; }
    public void setPasswordTemporanea(boolean passwordTemporanea) { this.passwordTemporanea = passwordTemporanea; }
}