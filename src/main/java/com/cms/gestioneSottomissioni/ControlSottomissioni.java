package com.cms.gestioneSottomissioni;

import com.cms.common.BoundaryDBMS;
import com.cms.common.PopupAvviso;
import com.cms.common.PopupInserimento;
import com.cms.common.SelezioneFile;
import com.cms.common.PopupErrore;
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

    public boolean sottomettiArticolo(String idArticolo) {
        LocalDate oggi = LocalDate.now();
        LocalDate dataScadenza = db.getDataScadenzaSottomissione(getDatiArticolo(idArticolo).getConferenzaId());

        if (oggi.isBefore(dataScadenza) || oggi.isEqual(dataScadenza)) {
            boolean giaSottomesso = db.haArticoloSottomesso(idArticolo);

            if (!giaSottomesso) {
                // Primo invio: inserisci titolo + parole chiave
                Optional<Map<String, String>> dati = new PopupInserimento().promptDatiArticolo();
                if (dati.isPresent()) {
                    db.inviaDettagliArticolo(idArticolo,
                            dati.get().get("titolo"), dati.get().get("paroleChiave"));

                    Optional<File> file = SelezioneFile.scegliFile(new Stage());
                    if (file.isPresent()) {
                        db.inviaArticolo(idArticolo, file.get());
                        new PopupAvviso("Articolo sottomesso").show();
                        return true;
                    }
                }
            } else {
                new PopupAvviso("Carica la versione aggiornata dell'articolo").show();
                Optional<File> file = SelezioneFile.scegliFile(new Stage());
                if (file.isPresent()) {
                    db.inviaArticolo(idArticolo, file.get());
                    new PopupAvviso("Articolo aggiornato").show();
                    return true;
                }
            }
        } else {
            new PopupAvviso("Data di scadenza delle sottomissioni già oltrepassata").show();
        }
        return false;
    }

    public void inviaCameraready(String idArticolo) {
        EntityArticolo articolo = getDatiArticolo(idArticolo);
        String idConferenza = articolo.getConferenzaId();
        Optional<EntityConferenza> confOpt = getConferenza(idConferenza);
        if (confOpt.isEmpty()) {
            new PopupAvviso("Conferenza non trovata").show();
            return;
        }
        EntityConferenza conf = confOpt.get();
        LocalDate oggi = LocalDate.now();
        LocalDate dataGraduatoria = conf.getDataGraduatoria();
        LocalDate scadenzaCameraReady = conf.getScadenzaCameraReady();

        if (oggi.isBefore(dataGraduatoria) || oggi.isEqual(dataGraduatoria) || oggi.isAfter(scadenzaCameraReady)) {
            new PopupAvviso("Periodo di invio versione camera-ready non valido").show();
            return;
        }

        Integer posizione = articolo.getPosizione();
        int numVincitori = conf.getNumeroVincitori();
        if (posizione == null || posizione < 1 || posizione > numVincitori) {
            new PopupAvviso("Solo i vincitori possono inviare la camera-ready").show();
            return;
        }

        Optional<File> file = SelezioneFile.scegliFile(new Stage());
        if (file.isPresent() && file != null) {
            db.inviaCameraready(idArticolo, file.get());
            new PopupAvviso("Camera-ready inviata").show();
        }
    }

    public void inviaVersioneFinale(String idArticolo) {
        EntityArticolo articolo = getDatiArticolo(idArticolo);
        String idConferenza = articolo.getConferenzaId();
        Optional<EntityConferenza> confOpt = getConferenza(idConferenza);
        if (confOpt.isEmpty()) {
            new PopupAvviso("Conferenza non trovata").show();
            return;
        }
        EntityConferenza conf = confOpt.get();
        LocalDate oggi = LocalDate.now();
        LocalDate dataGraduatoria = conf.getDataGraduatoria();
        LocalDate scadenzaFeedbackEditore = conf.getScadenzaFeedbackEditore();
        LocalDate scadenzaVersioneFinale = conf.getScadenzaVersioneFinale();

        if (oggi.isBefore(scadenzaFeedbackEditore) || oggi.isEqual(scadenzaFeedbackEditore) || oggi.isAfter(scadenzaVersioneFinale)) {
            new PopupAvviso("Periodo di invio versione finale non valido").show();
            return;
        }

        Integer posizione = articolo.getPosizione();
        int numVincitori = conf.getNumeroVincitori();
        if (posizione == null || posizione < 1 || posizione > numVincitori) {
            new PopupAvviso("Solo i vincitori possono inviare la versione finale").show();
            return;
        }

        Optional<File> file = SelezioneFile.scegliFile(new Stage());
        if (file.isPresent() && file != null) {
            db.inviaVersioneFinale(idArticolo, file.get());
            new PopupAvviso("Versione finale inviata").show();
        }
    }

    public void visualizzaArticolo(String idArticolo) {
        Optional<File> articolo = db.getArticolo(idArticolo);
        if (articolo.isPresent() && articolo != null) {
            DownloadUtil.salvaInDownload(articolo.get(), "Articolo");
        } else {
            new PopupErrore("Articolo non presente").show();
        }
    }

    public void visualizzaCameraready(String idArticolo) {
        Optional<File> file = db.getCameraready(idArticolo);
        if (file.isPresent() && file != null) {
            DownloadUtil.salvaInDownload(file.get(), "Versione Camera-ready Articolo");
        } else {
            new PopupErrore("Versione Camera-ready non presente").show();
        }
    }

    public void visualizzaVersioneFinale(String idArticolo) {
        Optional<File> file = db.getVersioneFinale(idArticolo);
        if (file.isPresent() && file != null) {
            DownloadUtil.salvaInDownload(file.get(), "Versione Finale Articolo");
        } else {
            new PopupErrore("Versione Finale non presente").show();
        }
    }

    public void visualizzaFeedback(String idArticolo) {
        Optional<File> file = db.getFeedback(idArticolo);
        if (file.isPresent()) {
            DownloadUtil.salvaInDownload(file.get(), "Feedback Editor");
        } else {
            new PopupErrore("Feedback non presente").show();
        }
    }

    public void visualizzaRevisione(String idArticolo, String emailRevisore) {
        Optional<String> idRevisioneOpt = db.getIdRevisione(idArticolo, emailRevisore);
        if (idRevisioneOpt.isPresent()) {
            Optional<File> file = db.getRevisione(idRevisioneOpt.get());
            if (file.isPresent()) {
                DownloadUtil.salvaInDownload(file.get(), "Revisione");
            } else {
                new PopupErrore("Revisione non presente").show();
            }
        } else {
            new PopupErrore("Revisione non trovata per questo revisore").show();
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

    public EntityArticolo getDatiArticolo(String idArticolo) {
        return db.getDatiArticolo(idArticolo)
                .orElseThrow(() -> new RuntimeException("Articolo non trovato"));
    }

    public String getArticoloId(String idConferenza, String emailAutore) {
        return db.getArticoloId(idConferenza, emailAutore);
    }
}