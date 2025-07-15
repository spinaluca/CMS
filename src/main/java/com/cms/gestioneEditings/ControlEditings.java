package com.cms.gestioneEditings;

import com.cms.common.BoundaryDBMS;
import com.cms.common.PopupAvviso;
import com.cms.common.PopupErrore;
import com.cms.common.SelezioneFile;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.utils.DownloadUtil;
import com.cms.utils.MailUtil;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ControlEditings {
    private final BoundaryDBMS db;
    private final EntityUtente editor;
    private final ControlAccount ctrlAccount;
    private final Stage stage;

    // Costruttore della classe ControlEditings
    public ControlEditings(BoundaryDBMS db, EntityUtente editor, ControlAccount ctrlAccount, Stage stage) {
        this.db = db;
        this.editor = editor;
        this.ctrlAccount = ctrlAccount;
        this.stage = stage;
    }

    // Restituisce la lista delle conferenze dell'editor
    public List<EntityConferenza> getConferenzeEditor() {
        return db.getConferenzeEditor(editor.getEmail());
    }

    // Restituisce la conferenza dato l'id
    public Optional<EntityConferenza> getConferenza(String idConferenza) {
        return db.getConferenzaEditor(idConferenza, editor.getEmail());
    }

    // Restituisce la lista degli articoli della conferenza
    public List<EntityArticolo> getArticoliConferenza(String idConferenza) {
        return db.getArticoliConferenza(idConferenza);
    }

    // Visualizza la versione camera-ready dell'articolo
    public void visualizzaVersioneCameraready(String idArticolo) {
        Optional<File> fileOpt = db.getVersioneCameraready(idArticolo);
        if (fileOpt.isPresent()) {
            DownloadUtil.salvaInDownload(fileOpt.get(), "Versione Camera-ready Articolo");
        } else {
            new PopupErrore("Versione camera-ready non presente").show();
        }
    }

    // Verifica se è già presente un feedback per l'articolo
    public boolean hasFeedback(String idArticolo) {
        return db.getPresenzaFeedback(idArticolo);
    }

    // Invia un feedback per un articolo
    public void inviaFeedback(String idConferenza, String idArticolo) {
        LocalDate oggi = LocalDate.now();
        LocalDate scadenza = db.getDataScadenzaFeedbackEditore(idConferenza);

        if (oggi.isAfter(scadenza)) {
            new PopupAvviso("Data di scadenza per l'invio dei feedback oltrepassata").show();
            return;
        }

        if (db.getPresenzaFeedback(idArticolo)) {
            new PopupAvviso("Feedback già inviato per questo articolo").show();
            return;
        }

        Optional<File> fileOpt = SelezioneFile.scegliFile(stage);
        if (fileOpt.isPresent()) {
            db.inviaFeedback(editor.getEmail(), fileOpt.get(), idArticolo);
            // Notifica e mail all'autore
            Optional<EntityArticolo> artOpt = db.getDatiArticoloById(idArticolo);
            if (artOpt.isPresent()) {
                EntityArticolo articolo = artOpt.get();
                String emailAutore = articolo.getAutoreId();
                String messaggio = "Gentile Autore,\n" +
                        "Hai ricevuto un feedback dall'editor per il tuo articolo '" + articolo.getTitolo() + "'.";
                boolean notificaInserita = db.inserisciNotifica(emailAutore, messaggio);
                // if (notificaInserita)
                //     MailUtil.inviaMail(messaggio, emailAutore, "Feedback editor - " + articolo.getTitolo());
            }
            new PopupAvviso("Feedback inviato con successo").show();
        }
    }

    // Restituisce il controller dell'account
    public ControlAccount getAccountController() {
        return ctrlAccount;
    }

    // Apre la homepage dell'editor
    public void apriHomepageEditor() {
        new HomepageEditor(stage, this, ctrlAccount).show();
    }

    // Restituisce la lista degli articoli camera-ready per una conferenza
    public List<EntityArticolo> getCameraReadyArticoli(String confId) {
        return db.getCameraReadyArticoli(confId);
    }

    // Restituisce il nome e cognome dell'utente dato l'email
    public Optional<String> getLabelUtente(String email) {
        return db.getUtente(email).map(u -> u.getNome() + " " + u.getCognome());
    }
} 