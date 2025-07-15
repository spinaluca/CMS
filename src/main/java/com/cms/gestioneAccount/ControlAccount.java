package com.cms.gestioneAccount;

import com.cms.gestioneConferenze.ControlConferenze;
import com.cms.common.Homepage;
import com.cms.gestioneConferenze.*;
import com.cms.common.BoundaryDBMS;
import com.cms.common.PopupAvviso;
import com.cms.common.PopupErrore;
import com.cms.common.PopupInserimento;
import com.cms.entity.EntityUtente;
import com.cms.utils.MailUtil;
import com.cms.gestioneRevisioni.HomepageRevisore;
import com.cms.gestioneRevisioni.ControlRevisioni;
import com.cms.gestioneRevisioni.InfoConferenzaRevisore;
import javafx.stage.Stage;

import java.security.SecureRandom;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import com.cms.gestioneEditings.ControlEditings;
import com.cms.gestioneEditings.HomepageEditor;

public class ControlAccount {
    private final BoundaryDBMS db;
    private final SecureRandom random = new SecureRandom();
    private EntityUtente utenteCorrente;

    private static final String LETTERE_MAIUSCOLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LETTERE_MINUSCOLE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERI = "0123456789";
    private static final String SPECIALI = "!@#$%&*?";
    private static final String TUTTI = LETTERE_MAIUSCOLE + LETTERE_MINUSCOLE + NUMERI + SPECIALI;
    private final Stage stage;

    // Costruttore della classe ControlAccount
    public ControlAccount(Stage stage, BoundaryDBMS db) {
        this.stage = stage;
        this.db = db;
    }

    // Restituisce lo stage principale
    public Stage getStage() {
        return stage;
    }

    // Imposta l'utente corrente dato l'email
    public void setUtenteCorrente(String email) {
        Optional<EntityUtente> opt = getDatiUtente(email);
        opt.ifPresent(utente -> this.utenteCorrente = utente);
    }


    // Restituisce l'utente corrente
    public EntityUtente getUtenteCorrente() {
        return utenteCorrente;
    }

    // Effettua il login verificando email e password
    public boolean login(String email, String password) {
        return db.queryLogin(email, password);
    }

    // Verifica se la password dell'utente è temporanea
    public boolean isPasswordTemporanea(String email) {
        return db.queryIsPasswordTemporanea(email);
    }

    // Registra un nuovo utente nel database
    public boolean registraUtente(EntityUtente utente) {
        return db.queryInsertUtente(utente);
    }

    // Restituisce i dati dell'utente dato l'email
    public Optional<EntityUtente> getDatiUtente(String email) {
        return db.getUtente(email);
    }

    // Gestisce la richiesta di modifica password
    public boolean richiestaModificaPassword(String email, String vecchiaPw, String nuovaPw) {
        if (login(email, vecchiaPw)) {
            System.out.println("Password vecchia corretta");
            return db.queryUpdatePassword(email, nuovaPw, false);
        }
        return false;
    }

    // Genera una password temporanea rispettando i criteri di sicurezza
    private String generaPasswordTemporanea(int length) {
        StringBuilder sb = new StringBuilder();

        sb.append(randomChar(LETTERE_MAIUSCOLE));
        sb.append(randomChar(LETTERE_MINUSCOLE));
        sb.append(randomChar(NUMERI));
        sb.append(randomChar(SPECIALI));

        for (int i = 4; i < length; i++) {
            sb.append(randomChar(TUTTI));
        }

        return shuffleString(sb.toString());
    }

    // Restituisce un carattere casuale dalla stringa fornita
    private char randomChar(String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }

    // Mescola i caratteri di una stringa
    private String shuffleString(String input) {
        char[] arr = input.toCharArray();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return new String(arr);
    }

    // Apre la finestra di login
    public void apriLogin() {
        new ModuloLogin(stage, this).show();
    }

    // Apre la finestra di registrazione
    public void apriRegistrazione() {
        new ModuloRegistrazione(stage, this).show();
    }

    // Gestisce la richiesta di recupero password
    public void richiestaRecuperoPassword() {
        Optional<String> emailOpt = new PopupInserimento().promptEmail("Recupero password");

        emailOpt.ifPresent(email -> {
            // Verifica se l'utente esiste nel DB
            if (!emailRegistrata(email)) {
                new PopupErrore("Nessun utente registrato con questa email.").show();
                return;
            }

            // Genera password temporanea
            String nuovaPassword = generaPasswordTemporanea(10);

            // Invia email
            String subject = "Recupero password CMS";
            String corpo = "La tua nuova password temporanea è: " + nuovaPassword;

            boolean inviata = MailUtil.inviaMail(corpo, email, subject);

            if (inviata) {
                // Aggiorna password nel DB con flag temporanea
                aggiornaPasswordTemporanea(email, nuovaPassword, true);
                new PopupAvviso("Una password temporanea è stata inviata a: " + email).show();
            } else {
                new PopupErrore("Errore nell'invio dell'email. Riprova più tardi.").show();
            }

            apriLogin();  // Torna comunque al login dopo il processo
        });
    }

    // Gestisce la richiesta di modifica dei ruoli utente
    public void richiestaModificaRuolo(String ruolo, String aree) {
        if (utenteCorrente == null) {
            new PopupErrore("Utente non autenticato").show();
            return;
        }

        try {
            utenteCorrente.setRuolo(ruolo);
            utenteCorrente.setAree(aree);

            new BoundaryDBMS().queryAggiornaRuoliUtente(utenteCorrente);
            new PopupAvviso("Dati aggiornati con successo!").show();
        } catch (DateTimeParseException ex) {
            new PopupErrore("Data di nascita non valida (usa formato yyyy-mm-dd)").show();
        }
    }

    // Apre la homepage per il ruolo Chair
    public void apriHomepageChair() {
        new HomepageChair(stage, new ControlConferenze(db), this).show();
    }

    // Apre la homepage per il ruolo Revisore
    public void apriHomepageRevisore() {
        new HomepageRevisore(stage, new ControlRevisioni(db), this).show();
    }

    // Apre la schermata info conferenza per Revisore
    public void apriInfoConferenzaRevisore(String confId) {
        new InfoConferenzaRevisore(stage, new ControlRevisioni(db), this, confId).show();
    }

    // Apre la schermata info conferenza per Chair
    public void apriInfoConferenzaChair(String confId) {
        new InfoConferenzaChair(stage, new ControlConferenze(db), this, confId).show();
    }

    // Apre la homepage generale dopo il login
    public void apriHomepageGenerale() {
        new Homepage(stage, this, this.getUtenteCorrente()).show();
    }

    // Verifica le credenziali dell'utente e imposta l'utente corrente
    public boolean verificaCredenziali(String email, String password) {
        if (login(email, password)) {
            setUtenteCorrente(email); // Passa solo l'email
            return true;
        }
        return false;
    }

    // Aggiorna la password temporanea dell'utente
    public void aggiornaPasswordTemporanea(String email, String nuovaPassword, boolean temporanea) {
        BoundaryDBMS db = new BoundaryDBMS();
        db.aggiornaPasswordUtente(email, nuovaPassword, temporanea);
    }

    // Verifica se l'email è già registrata
    public boolean emailRegistrata(String email) {
        return new BoundaryDBMS().esisteEmail(email);
    }

    // Apre la finestra per il cambio password
    public void apriCambioPassword() {
        ModuloPassword cambio = new ModuloPassword(stage, this, true, null);
        cambio.show();
    }

    // Esegue il logout dell'utente corrente
    public void richiestaLogout() {
        utenteCorrente = null;
        apriLogin();
    }

    // Apre la homepage per il ruolo Editor
    public void apriHomepageEditor() {
        BoundaryDBMS db = new BoundaryDBMS();
        ControlEditings ctrlEd = new ControlEditings(db, getUtenteCorrente(), this, stage);
        new HomepageEditor(stage, ctrlEd, this).show();
    }

    // Restituisce il nome completo dell'utente dato l'email
    public Optional<String> getNomeCompleto(String email) {
        return db.getNomeCompleto(email);
    }
}