package com.cms.gestioneSottomissioni;

import com.cms.common.BoundaryDBMS;
import com.cms.common.PopupAvviso;
import com.cms.common.PopupInserimento;
import com.cms.common.SelezioneFile;
import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityUtente;
import com.cms.entity.EntityConferenza;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.utils.DownloadUtil;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ControlSottomissioni {
    private final BoundaryDBMS db;
    private final EntityUtente autore;
    private final ControlAccount ctrlAccount;
    private final Stage stage;

    public ControlSottomissioni(BoundaryDBMS db, EntityUtente autore, ControlAccount ctrlAccount, Stage stage) {
        this.db = db;
        this.autore = autore;
        this.ctrlAccount = ctrlAccount;
        this.stage = stage;
    }


    public List<EntityConferenza> getConferenzeAutore() {
        return db.getConferenzeAutore(autore.getEmail());
    }

    public void iscrivitiConferenza(String idConferenza) {
        db.iscrizioneConferenza(idConferenza, autore.getEmail());
        new PopupAvviso("Iscrizione effettuata").show();
    }

    public boolean sottomettiArticolo(String idConferenza) {
        LocalDate oggi = LocalDate.now();
        LocalDate dataScadenza = db.getDataScadenzaSottomissione(idConferenza);

        if (oggi.isBefore(dataScadenza)) {
            boolean giaSottomesso = db.haArticoloSottomesso(idConferenza, autore.getEmail());

            if (!giaSottomesso) {
                // Primo invio: inserisci titolo + parole chiave
                Optional<Map<String, String>> dati = new PopupInserimento().promptDatiArticolo();
                if (dati.isPresent()) {
                    db.inviaDettagliArticolo(idConferenza, autore.getEmail(),
                            dati.get().get("titolo"), dati.get().get("paroleChiave"));

                    Optional<File> file = SelezioneFile.scegliFile(new Stage());
                    if (file.isPresent()) {
                        db.inviaArticolo(idConferenza, autore.getEmail(), file.get());
                        new PopupAvviso("Articolo sottomesso").show();
                        return true;
                    }
                }
            } else {
                new PopupAvviso("Carica la versione aggiornata dell'articolo").show();
                Optional<File> file = SelezioneFile.scegliFile(new Stage());
                if (file.isPresent()) {
                    db.inviaArticolo(idConferenza, autore.getEmail(), file.get());
                    new PopupAvviso("Articolo aggiornato").show();
                    return true;
                }
            }
        } else {
            new PopupAvviso("Data di scadenza delle sottomissioni già oltrepassata").show();
        }
        return false;
    }

    public void inviaCameraready(String idConferenza) {
        Optional<File> file = SelezioneFile.scegliFile(new Stage());
        if (file.isPresent()) {
            db.inviaCameraready(idConferenza, autore.getEmail(), file.get());
            new PopupAvviso("Camera-ready inviata").show();
        }
    }

    public void inviaVersioneFinale(String idConferenza) {
        Optional<File> file = SelezioneFile.scegliFile(new Stage());
        if (file.isPresent()) {
            db.inviaVersioneFinale(idConferenza, autore.getEmail(), file.get());
            new PopupAvviso("Versione finale inviata").show();
        }
    }

    public void visualizzaArticolo(String idConferenza) {
        Optional<File> articolo = db.getArticolo(idConferenza, autore.getEmail());
        if (articolo.isPresent()) {
            DownloadUtil.salvaInDownload(articolo.get(), "Articolo");
        } else {
            new PopupAvviso("Articolo non presente").show();
        }
    }

    public void visualizzaCameraready(String idConferenza) {
        Optional<File> file = db.getCameraready(idConferenza, autore.getEmail());
        if (file.isPresent()) {
            DownloadUtil.salvaInDownload(file.get(), "Versione Camera-ready Articolo");
        } else {
            new PopupAvviso("Versione Camera-ready non presente").show();
        }
    }

    public void visualizzaVersioneFinale(String idConferenza) {
        Optional<File> file = db.getVersioneFinale(idConferenza, autore.getEmail());
        if (file.isPresent()) {
            DownloadUtil.salvaInDownload(file.get(), "Versione Finale Articolo");
        } else {
            new PopupAvviso("Versione Finale non presente").show();
        }
    }

    public void visualizzaFeedback(String idConferenza) {
        Optional<File> file = db.getFeedback(idConferenza, autore.getEmail());
        if (file.isPresent()) {
            DownloadUtil.salvaInDownload(file.get(), "Feedback Editor");
        } else {
            new PopupAvviso("Feedback non presente").show();
        }
    }

    public void visualizzaRevisione(String idRevisione) {
        Optional<File> file = db.getRevisione(idRevisione);
        if (file.isPresent()) {
            DownloadUtil.salvaInDownload(file.get(), "Revisione");
        } else {
            new PopupAvviso("Revisione non presente").show();
        }
    }

    public Optional<EntityConferenza> getConferenza(String idConferenza) {
        return db.getConferenzaAutore(idConferenza);
    }

    public Map<String, String> getRevisioniArticolo(String idConferenza) {
        return db.getRevisioniArticolo(db.getArticoloId(idConferenza, autore.getEmail()));
    }

    public Map<String, String> getRevisioniArticoloById(String idArticolo) {
        return db.getRevisioniArticolo(idArticolo);
    }

    public ControlAccount getAccountController() {
        return ctrlAccount;
    }

    public void apriHomepageAutore() {
        new HomepageAutore(stage, this, ctrlAccount, autore).show();
    }

    public boolean isAutoreIscritto(String idConferenza) {
        return db.isAutoreIscritto(idConferenza, autore.getEmail());
    }

    public Optional<String> getNomeCompleto(String email) {
        return db.getNomeCompleto(email);
    }

    public EntityArticolo getDatiArticolo(String idConferenza) {
        return db.getDatiArticolo(idConferenza, autore.getEmail())
                .orElseThrow(() -> new RuntimeException("Articolo non trovato per conferenza " + idConferenza));
    }
}