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

    public ControlEditings(BoundaryDBMS db, EntityUtente editor, ControlAccount ctrlAccount, Stage stage) {
        this.db = db;
        this.editor = editor;
        this.ctrlAccount = ctrlAccount;
        this.stage = stage;
    }

    /* ---------------------- Conferenze ---------------------- */
    public List<EntityConferenza> getConferenzeEditor() {
        return db.getConferenzeEditor(editor.getEmail());
    }

    public Optional<EntityConferenza> getConferenza(String idConferenza) {
        return db.getConferenzaEditor(idConferenza, editor.getEmail());
    }

    public List<EntityArticolo> getArticoliConferenza(String idConferenza) {
        return db.getArticoliConferenza(idConferenza);
    }

    /* ------------------ Versione Camera-ready ------------------ */
    public void visualizzaVersioneCameraready(String idArticolo) {
        Optional<File> fileOpt = db.getVersioneCameraready(idArticolo);
        if (fileOpt.isPresent()) {
            DownloadUtil.salvaInDownload(fileOpt.get(), "Versione Camera-ready Articolo");
        } else {
            new PopupErrore("Versione camera-ready non presente").show();
        }
    }

    /* -------------------- Invio Feedback -------------------- */
    public boolean hasFeedback(String idArticolo) {
        return db.getPresenzaFeedback(idArticolo);
    }

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
            db.notificaAutore(idArticolo);
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

    /* -------------------- Navigation helpers -------------------- */
    public ControlAccount getAccountController() {
        return ctrlAccount;
    }

    public void apriHomepageEditor() {
        new HomepageEditor(stage, this, ctrlAccount).show();
    }

    /* --------- Articoli con camera-ready --------- */
    public List<EntityArticolo> getCameraReadyArticoli(String confId) {
        return db.getCameraReadyArticoli(confId);
    }

    public Optional<String> getLabelUtente(String email) {
        return db.getUtente(email).map(u -> u.getNome() + " " + u.getCognome());
    }
} 