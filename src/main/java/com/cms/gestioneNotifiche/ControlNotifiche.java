package com.cms.gestioneNotifiche;

import com.cms.common.BoundaryDBMS;
import com.cms.entity.EntityConferenza;
import com.cms.gestioneAccount.ControlAccount;
import com.cms.utils.MailUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.stage.Stage;

public class ControlNotifiche {
    private final BoundaryDBMS db = new BoundaryDBMS();
    private final ControlAccount ctrlAccount;

    // Costruttore della classe ControlNotifiche
    public ControlNotifiche(ControlAccount ctrlAccount) {
        this.ctrlAccount = ctrlAccount;
    }
    
    // Mostra il pannello delle notifiche per l'utente corrente
    public void mostraPannelloNotifiche() {
        String email = ctrlAccount.getUtenteCorrente().getEmail();
        List<Map<String, String>> notifiche = db.getNotificheNonLette(email);
        new PannelloNotifiche(new Stage(), this, notifiche).show();
    }

    // Cancella una notifica dato l'id e restituisce la lista aggiornata
    public List<Map<String, String>> cancellaNotifica(String idNotifica) {
        db.cancellaNotifica(idNotifica);
        String email = ctrlAccount.getUtenteCorrente().getEmail();
        List<Map<String, String>> notifiche = db.getNotificheNonLette(email);
        return notifiche;
    }

    // Gestisce le notifiche automatiche basate sulle date delle conferenze
    public void gestisciNotificheAutomatiche() {
        LocalDate dataCorrente = LocalDate.now();
        List<EntityConferenza> tutteConferenze = db.getAllConferenze();
        
        for (EntityConferenza conferenza : tutteConferenze) {
            // 4.1. SE la data corrente corrisponde al giorno di pubblicazione della graduatoria
            if (conferenza.getDataGraduatoria() != null && 
                conferenza.getDataGraduatoria().equals(dataCorrente)) {
                notificaGraduatoriaAutori(conferenza);
            }
            
            // 4.2. SE la data corrente corrisponde a due giorni prima della scadenza delle revisioni
            if (conferenza.getScadenzaRevisioni() != null && 
                conferenza.getScadenzaRevisioni().minusDays(2).equals(dataCorrente)) {
                notificaRevisioniMancantiChair(conferenza);
            }
            
            // 4.3. SE la data corrente corrisponde a sette giorni prima della scadenza delle revisioni
            if (conferenza.getScadenzaRevisioni() != null && 
                conferenza.getScadenzaRevisioni().minusDays(7).equals(dataCorrente)) {
                notificaRevisoriScadenza(conferenza, 7);
            }
            
            // 4.3. SE la data corrente corrisponde a due giorni prima della scadenza delle revisioni
            if (conferenza.getScadenzaRevisioni() != null && 
                conferenza.getScadenzaRevisioni().minusDays(2).equals(dataCorrente)) {
                notificaRevisoriScadenza(conferenza, 2);
            }
            
            // 4.4. SE la data corrente corrisponde a due giorni prima della scadenza delle sottomissioni
            if (conferenza.getScadenzaSottomissione() != null && 
                conferenza.getScadenzaSottomissione().minusDays(2).equals(dataCorrente)) {
                notificaAutoriScadenzaSottomissione(conferenza);
            }
            
            // 4.5. SE la data corrente corrisponde a due giorni prima della scadenza della consegna delle versioni camera-ready
            if (conferenza.getScadenzaCameraReady() != null && 
                conferenza.getScadenzaCameraReady().minusDays(2).equals(dataCorrente)) {
                notificaAutoriScadenzaCameraReady(conferenza);
            }
            
            // 4.6. SE la data corrente corrisponde a due giorni prima della scadenza della consegna delle versioni finali
            if (conferenza.getScadenzaVersioneFinale() != null && 
                conferenza.getScadenzaVersioneFinale().minusDays(2).equals(dataCorrente)) {
                notificaAutoriScadenzaVersioneFinale(conferenza);
            }
            
            // 4.7. SE la data corrente corrisponde a due giorni prima della scadenza per l'invio del feedback Editor
            if (conferenza.getScadenzaFeedbackEditore() != null && 
                conferenza.getScadenzaFeedbackEditore().minusDays(2).equals(dataCorrente)) {
                notificaEditorScadenzaFeedback(conferenza);
            }
        }
    }
    
    // Notifica agli autori la loro posizione nella graduatoria
    private void notificaGraduatoriaAutori(EntityConferenza conferenza) {
        Map<String, Integer> graduatoria = db.getGraduatoriaConferenza(conferenza.getId());
        List<String> autori = db.getAutoriConferenza(conferenza.getId());
        
        for (String autore : autori) {
            // Cerca la posizione dell'autore nella graduatoria
            Optional<Integer> posizioneAutore = graduatoria.entrySet().stream()
                .filter(entry -> {
                    Optional<com.cms.entity.EntityArticolo> articolo = db.getDatiArticoloById(entry.getKey());
                    return articolo.isPresent() && articolo.get().getAutoreId().equals(autore);
                })
                .map(Map.Entry::getValue)
                .findFirst();
            
            String messaggio;
            if (posizioneAutore.isPresent()) {
                messaggio = "Gentile Autore,\n" +
                           "La graduatoria della conferenza '" + conferenza.getTitolo() + "' è stata pubblicata.\n" +
                           "La sua posizione nella graduatoria è: " + posizioneAutore.get() + "\n";
            } else {
                messaggio = "Gentile Autore,\n" +
                           "La graduatoria della conferenza '" + conferenza.getTitolo() + "' è stata pubblicata.\n" +
                           "Il suo articolo non è presente nella graduatoria.\n";
            }
            
            // Inserisce notifica nel database
            boolean notificaInserita = db.inserisciNotifica(autore, messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, autore, "Graduatoria pubblicata - " + conferenza.getTitolo());
        }
    }
    
    // Notifica al Chair il numero di revisioni mancanti
    private void notificaRevisioniMancantiChair(EntityConferenza conferenza) {
        int revisioniMancanti = db.getNumeroRevisioniMancanti(conferenza.getId());
        
        String messaggio = "Gentile Chair,\n" +
                           "La conferenza '" + conferenza.getTitolo() + "' ha " + revisioniMancanti + 
                           " revisioni mancanti.\n" +
                           "La scadenza per le revisioni è tra 2 giorni.\n";
        
        // Inserisce notifica nel database
        boolean notificaInserita = db.inserisciNotifica(conferenza.getChairId(), messaggio);
        // if (notificaInserita)
            // MailUtil.inviaMail(messaggio, conferenza.getChairId(), 
            //                   "Revisioni mancanti - " + conferenza.getTitolo());
    }
    
    // Notifica ai Revisori di ultimare le revisioni
    private void notificaRevisoriScadenza(EntityConferenza conferenza, int giorniRimanenti) {
        List<String> revisori = db.getRevisoriConferenza(conferenza.getId());
        
        String messaggio = "Gentile Revisore,\n" +
                           "La conferenza '" + conferenza.getTitolo() + "' ha " + giorniRimanenti + 
                           " giorni rimanenti per la scadenza delle revisioni.\n" +
                           "Si prega di ultimare le revisioni e inviarle il prima possibile.\n";
        
        for (String revisore : revisori) {
            // Inserisce notifica nel database
            boolean notificaInserita = db.inserisciNotifica(revisore, messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, revisore, 
                //                   "Scadenza revisioni tra " + giorniRimanenti + " giorni - " + conferenza.getTitolo());
        }
    }
    
    // Notifica agli Autori di sottomettere l'articolo
    private void notificaAutoriScadenzaSottomissione(EntityConferenza conferenza) {
        List<String> autori = db.getAutoriConferenza(conferenza.getId());
        
        String messaggio = "Gentile Autore,\n" +
                           "La conferenza '" + conferenza.getTitolo() + "' ha 2 giorni rimanenti " +
                           "per la scadenza delle sottomissioni.\n" +
                           "Si prega di sottomettere l'articolo il prima possibile.\n";
        
        for (String autore : autori) {
            // Inserisce notifica nel database
            boolean notificaInserita = db.inserisciNotifica(autore, messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, autore, 
                //                   "Scadenza sottomissioni tra 2 giorni - " + conferenza.getTitolo());
        }
    }
    
    // Notifica agli Autori di inviare la versione camera-ready
    private void notificaAutoriScadenzaCameraReady(EntityConferenza conferenza) {
        List<String> autori = db.getAutoriConferenza(conferenza.getId());
        
        String messaggio = "Gentile Autore,\n" +
                           "La conferenza '" + conferenza.getTitolo() + "' ha 2 giorni rimanenti " +
                           "per la scadenza della consegna delle versioni camera-ready.\n" +
                           "Si prega di inviare la versione camera-ready dell'articolo il prima possibile.\n";
        
        for (String autore : autori) {
            // Inserisce notifica nel database
            boolean notificaInserita = db.inserisciNotifica(autore, messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, autore, 
                //                   "Scadenza camera-ready tra 2 giorni - " + conferenza.getTitolo());
        }
    }
    
    // Notifica agli Autori di inviare la versione finale
    private void notificaAutoriScadenzaVersioneFinale(EntityConferenza conferenza) {
        List<String> autori = db.getAutoriConferenza(conferenza.getId());
        
        String messaggio = "Gentile Autore,\n" +
                           "La conferenza '" + conferenza.getTitolo() + "' ha 2 giorni rimanenti " +
                           "per la scadenza della consegna delle versioni finali.\n" +
                           "Si prega di inviare la versione finale dell'articolo il prima possibile.\n";
        
        for (String autore : autori) {
            // Inserisce notifica nel database
            boolean notificaInserita = db.inserisciNotifica(autore, messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, autore, 
                //                   "Scadenza versione finale tra 2 giorni - " + conferenza.getTitolo());
        }
    }
    
    // Notifica all'Editor di inviare il feedback
    private void notificaEditorScadenzaFeedback(EntityConferenza conferenza) {
        Optional<String> editor = db.getEditorConferenza(conferenza.getId());
        
        if (editor.isPresent()) {
            String messaggio = "Gentile Editor,\n" +
                               "La conferenza '" + conferenza.getTitolo() + "' ha 2 giorni rimanenti " +
                               "per la scadenza dell'invio del feedback.\n" +
                               "Si prega di inviare il feedback il prima possibile.\n";
            
            // Inserisce notifica nel database
            boolean notificaInserita = db.inserisciNotifica(editor.get(), messaggio);
            // if (notificaInserita)
                // MailUtil.inviaMail(messaggio, editor.get(), 
                //                   "Scadenza feedback tra 2 giorni - " + conferenza.getTitolo());
        }
    }
} 