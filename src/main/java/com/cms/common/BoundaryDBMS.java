package com.cms.common;

import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;
import com.cms.utils.MailUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class BoundaryDBMS {
    private static final String URL;
    
    static {
        // Inizializza il percorso del database
        String dbPath = initializeDatabasePath();
        URL = "jdbc:sqlite:" + dbPath;
    }

    public BoundaryDBMS() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver non trovato", e);
        }
    }
    
    private static String initializeDatabasePath() {
        try {
            // Prova prima a usare il database dalla directory corrente (per compatibilità)
            File localDb = new File("db/cms.db");
            if (localDb.exists()) {
                return localDb.getAbsolutePath();
            }
            
            // Altrimenti, copia il database dalle risorse
            Path dbDir = Paths.get(System.getProperty("user.dir"), "db");
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
            }
            
            Path dbFile = dbDir.resolve("cms.db");
            
            // Se il database non esiste localmente, copialo dalle risorse
            if (!Files.exists(dbFile)) {
                InputStream dbStream = BoundaryDBMS.class.getResourceAsStream("/db/cms.db");
                if (dbStream != null) {
                    Files.copy(dbStream, dbFile, StandardCopyOption.REPLACE_EXISTING);
                    dbStream.close();
                } else {
                    // Se non c'è il database nelle risorse, crea un file vuoto
                    Files.createFile(dbFile);
                }
            }
            
            return dbFile.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'inizializzazione del database", e);
        }
    }

    // ... (tutti i metodi getConferenze, getConferenza, queryCreaConferenza,
    //     queryRevisorePresente, queryInvitaRevisore, queryRimuoviRevisore,
    //     queryAggiungiEditor, getUltimaVersione rimangono identici)
    // Li riporto per completezza, ma li puoi copiare pari pari da prima:

    public List<EntityConferenza> getConferenze(String currentChairId) {
        String sql = "SELECT * FROM conferenze WHERE chair_id = ?";
        List<EntityConferenza> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentChairId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapConf(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getConferenze", e);
        }
        return list;
    }

    public Optional<EntityConferenza> getConferenza(String id) {
        String sql = "SELECT * FROM conferenze WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                EntityConferenza conf = mapConf(rs);

                // Recupera i revisori associati
                String sqlRev = "SELECT revisore_id FROM inviti_revisori WHERE conferenza_id = ?";
                try (PreparedStatement psRev = conn.prepareStatement(sqlRev)) {
                    psRev.setString(1, id);
                    ResultSet rsRev = psRev.executeQuery();
                    while (rsRev.next()) {
                        conf.addRevisore(rsRev.getString("revisore_id"));
                    }
                }

                return Optional.of(conf);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getConferenza", e);
        }
        return Optional.empty();
    }


    public void queryCreaConferenza(EntityConferenza conf, String currentChairId) {
        String sql =
                "INSERT INTO conferenze(" +
                        " id, acronimo, titolo, descrizione, luogo," +
                        " scad_sottomissione, scad_revisioni, data_graduatoria," +
                        " scad_camera_ready, scad_feedback_editore, scad_versione_finale," +
                        " num_min_revisori, valutazione_min, valutazione_max, num_articoli_vincitori," +
                        " modalita_distribuzione, chair_id" +
                        ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  conf.getId());
            ps.setString(2,  conf.getAcronimo());
            ps.setString(3,  conf.getTitolo());
            ps.setString(4,  conf.getDescrizione());
            ps.setString(5,  conf.getLuogo());
            ps.setString(6,  conf.getScadenzaSottomissione().toString());
            ps.setString(7,  conf.getScadenzaRevisioni().toString());
            ps.setString(8,  conf.getDataGraduatoria().toString());
            ps.setString(9,  conf.getScadenzaCameraReady().toString());
            ps.setString(10, conf.getScadenzaFeedbackEditore().toString());
            ps.setString(11, conf.getScadenzaVersioneFinale().toString());
            ps.setInt(12,    conf.getNumeroMinimoRevisori());
            ps.setInt(13, conf.getValutazioneMinima());
            ps.setInt(14, conf.getValutazioneMassima());
            ps.setInt(15,    conf.getNumeroVincitori());
            ps.setString(16, conf.getModalitaDistribuzione().name());
            ps.setString(17, currentChairId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryCreaConferenza", e);
        }
    }

    public boolean queryRevisorePresente(String email, String confId) {
        String sql = "SELECT 1 FROM inviti_revisori WHERE conferenza_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ps.setString(2, email);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryRevisorePresente", e);
        }
    }

    public void queryInvitaRevisore(String email, String confId) {
        String sql = "INSERT OR IGNORE INTO inviti_revisori(conferenza_id,revisore_id,stato) VALUES(?,?, 'In attesa')";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryInvitaRevisore", e);
        }
    }

    public void queryRimuoviRevisore(String email, String confId) {
        String sql = "DELETE FROM inviti_revisori WHERE conferenza_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryRimuoviRevisore", e);
        }
    }

    public void queryAggiungiEditor(String email, String confId) {
        String sql = "UPDATE conferenze SET editor_id = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, confId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryAggiungiEditor", e);
        }
    }

    public Optional<String> getUltimaVersione(String idArticolo) {
        String sql =
                "SELECT file_url FROM versioni " +
                        "WHERE articolo_id = ? " +
                        "ORDER BY data_caricamento DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("file_url"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getUltimaVersione", e);
        }
        return Optional.empty();
    }

    // helper per mappare ResultSet → EntityConferenza
    private EntityConferenza mapConf(ResultSet rs) throws SQLException {
        String id    = rs.getString("id");
        String acr   = rs.getString("acronimo");
        String tit   = rs.getString("titolo");
        String desc  = rs.getString("descrizione");
        String luog  = rs.getString("luogo");
        LocalDate s  = LocalDate.parse(rs.getString("scad_sottomissione"));
        LocalDate r  = LocalDate.parse(rs.getString("scad_revisioni"));
        LocalDate g  = LocalDate.parse(rs.getString("data_graduatoria"));
        LocalDate cr = LocalDate.parse(rs.getString("scad_camera_ready"));
        LocalDate fe = LocalDate.parse(rs.getString("scad_feedback_editore"));
        LocalDate vf = LocalDate.parse(rs.getString("scad_versione_finale"));
        int    mnr  = rs.getInt("num_min_revisori");
        int rp      = rs.getInt("valutazione_min");
        int rn      = rs.getInt("valutazione_max");
        int nv      = rs.getInt("num_articoli_vincitori");
        String distStr = rs.getString("modalita_distribuzione");
        EntityConferenza.Distribuzione dist = EntityConferenza.Distribuzione.fromString(distStr);

        EntityConferenza c = new EntityConferenza(
                id, acr, tit, desc, luog,
                s, r, g, cr, fe, vf,
                mnr,
                rp, rn,
                nv,
                dist
        );
        c.setEditor(rs.getString("editor_id"));
        c.setChairId(rs.getString("chair_id"));
        return c;
    }

    public List<EntityArticolo> getArticoliConferenza(String confId) {
        String sql = "SELECT * FROM articoli WHERE conferenza_id = ?";
        List<EntityArticolo> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new EntityArticolo(
                        rs.getString("id"),
                        rs.getString("conferenza_id"),
                        rs.getString("titolo"),
                        rs.getString("parole_chiave"),
                        rs.getString("stato"),
                        rs.getString("autore_id"),
                        rs.getObject("posizione") != null ? rs.getInt("posizione") : null,
                        rs.getObject("num_revisioni") != null ? rs.getInt("num_revisioni") : null,
                        rs.getObject("punteggio") != null ? rs.getDouble("punteggio") : null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getArticoliConferenza", e);
        }
        return list;
    }

    public Map<String, String> getRevisoriConStato(String confId) {
        String sql = "SELECT revisore_id, stato FROM inviti_revisori WHERE conferenza_id = ?";
        Map<String, String> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("revisore_id"), rs.getString("stato"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getRevisoriConStato", e);
        }
        return map;
    }

    public Optional<String> getNomeCompleto(String email) {
        String sql = "SELECT nome, cognome FROM utenti WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                return Optional.of(nome + " " + cognome);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getNomeCompleto", e);
        }
        return Optional.empty();
    }

    private EntityUtente mapUtente(ResultSet rs) throws SQLException {
        String email = rs.getString("email");
        String nome = rs.getString("nome");
        String cognome = rs.getString("cognome");
        String ruolo = rs.getString("ruolo");
        String areeCompetenza = rs.getString("aree_competenza");
        String password = rs.getString("password");
        String passwordTemporanea = rs.getString("password_temporanea");
        
        // Controllo se tutti i campi obbligatori sono null
        if (email == null && nome == null && cognome == null && 
            ruolo == null && password == null) {
            throw new SQLException("Tutti i campi obbligatori dell'utente sono null");
        }
        
        String dataNascitaStr = rs.getString("data_nascita");
        LocalDate dataNascita = null;
        if (dataNascitaStr != null && !dataNascitaStr.trim().isEmpty()) {
            dataNascita = LocalDate.parse(dataNascitaStr);
        }
        
        return new EntityUtente(
                email,
                nome,
                cognome,
                ruolo,
                areeCompetenza,
                dataNascita,
                password,
                passwordTemporanea != null && passwordTemporanea.equalsIgnoreCase("true")
        );
    }

    // Verifica login
    public boolean queryLogin(String email, String password) {
        String sql = "SELECT 1 FROM utenti WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryLogin", e);
        }
    }

    // Controlla password attuale
    public boolean queryCheckPassword(String email, String password) {
        String sql = "SELECT 1 FROM utenti WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryCheckPassword", e);
        }
    }

    // Inserimento nuovo utente
    public boolean queryInsertUtente(EntityUtente utente) {
        String sql = "INSERT INTO utenti(email, password, nome, cognome, data_nascita, password_temporanea, ruolo) VALUES(?,?,?,?,?, false, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, utente.getEmail());
            ps.setString(2, utente.getPassword());
            ps.setString(3, utente.getNome());
            ps.setString(4, utente.getCognome());
            ps.setString(5, utente.getDataNascita().toString()); // formato ISO yyyy-MM-dd
            ps.setString(6, utente.getRuolo());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // già presente o errore
        }
    }


    // Recupera dati utente
    public Optional<EntityUtente> getUtente(String email) {
        String sql = "SELECT * FROM utenti WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String emailUtente = rs.getString("email");
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String ruolo = rs.getString("ruolo");
                String areeCompetenza = rs.getString("aree_competenza");
                String password = rs.getString("password");
                String passwordTemporanea = rs.getString("password_temporanea");
                
                // Controllo se tutti i campi obbligatori sono null
                if (emailUtente == null && nome == null && cognome == null && 
                    ruolo == null && password == null) {
                    return Optional.empty();
                }
                
                String dataNascitaStr = rs.getString("data_nascita");
                LocalDate dataNascita = null;
                if (dataNascitaStr != null && !dataNascitaStr.trim().isEmpty()) {
                    dataNascita = LocalDate.parse(dataNascitaStr);
                }
                
                return Optional.of(new EntityUtente(
                        emailUtente,
                        nome,
                        cognome,
                        ruolo,
                        areeCompetenza,
                        dataNascita,
                        password,
                        passwordTemporanea != null && passwordTemporanea.equalsIgnoreCase("true")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getUtente", e);
        }
        return Optional.empty();
    }

    // Aggiorna password (temporanea o definitiva)
    public boolean queryUpdatePassword(String email, String nuovaPw, boolean temporanea) {
        String sql = "UPDATE utenti SET password = ?, password_temporanea = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuovaPw);
            ps.setBoolean(2, temporanea);
            ps.setString(3, email);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Verifica se la password attuale è temporanea
    public boolean queryIsPasswordTemporanea(String email) {
        String sql = "SELECT password_temporanea FROM utenti WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("password_temporanea");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryIsPasswordTemporanea", e);
        }
        return false;
    }

    public void queryAggiornaRuoliUtente(EntityUtente utente) {
        String sql = "UPDATE utenti SET ruolo = ?, aree_competenza = ? WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, utente.getRuolo());
            ps.setString(2, utente.getAree());
            ps.setString(3, utente.getEmail());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryAggiornaRuoliUtente", e);
        }
    }

    public boolean aggiornaPasswordUtente(String email, String nuovaPassword, boolean temporanea) {
        String sql = "UPDATE utenti SET password = ?, password_temporanea = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuovaPassword);
            ps.setBoolean(2, temporanea);
            ps.setString(3, email);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean esisteEmail(String email) {
        String sql = "SELECT 1 FROM utenti WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true se almeno una riga trovata
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<EntityConferenza> getConferenzeAutore(String emailAutore) {
        String sql = "SELECT DISTINCT c.* FROM conferenze c "
                + "LEFT JOIN articoli a ON c.id = a.conferenza_id AND a.autore_id = ?";
        List<EntityConferenza> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailAutore);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapConf(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getConferenzeAutore", e);
        }
        return list;
    }

    public void iscrizioneConferenza(String idConferenza, String emailAutore) {
        String sql = "INSERT INTO articoli(id, conferenza_id, autore_id, stato) VALUES(?,?,?, 'In preparazione')";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, idConferenza);
            ps.setString(3, emailAutore);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante iscrizione conferenza", e);
        }
    }

    public void inviaArticolo(String idArticolo, File file) {
        inserisciVersione(idArticolo, "articolo", file);

        // Imposta lo stato come "Sottomesso"
        String sql = "UPDATE articoli SET stato = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Sottomesso");
            ps.setString(2, idArticolo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornare lo stato dell'articolo", e);
        }
    }

    public void inviaCameraready(String idArticolo, File file) {
        inserisciVersione(idArticolo, "camera_ready", file);
    }

    public void inviaVersioneFinale(String idArticolo, File file) {
        inserisciVersione(idArticolo, "versione_finale", file);
    }

    private void inserisciVersione(String idArticolo, String tipo, File file) {
        String sql = "INSERT INTO versioni(articolo_id, tipo, file_url, data_caricamento) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ps.setString(2, tipo);
            ps.setString(3, file.getAbsolutePath());
            ps.setString(4, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante inserisciVersione", e);
        }
    }

    public Optional<File> getArticolo(String idArticolo) {
        return getUltimaVersione(idArticolo, "articolo");
    }

    public Optional<File> getCameraready(String idArticolo) {
        return getUltimaVersione(idArticolo, "camera_ready");
    }

    public Optional<File> getVersioneFinale(String idArticolo) {
        return getUltimaVersione(idArticolo, "versione_finale");
    }

    public Optional<File> getUltimaVersione(String idArticolo, String tipo) {
        String sql = "SELECT file_url FROM versioni WHERE articolo_id = ?";
        if (tipo != null) {
            sql += " AND tipo = ?";
        }
        sql += " ORDER BY id DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idArticolo);
            if (tipo != null) {
                ps.setString(2, tipo);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new File(rs.getString("file_url")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getUltimaVersione", e);
        }
        return Optional.empty();
    }

    public String getArticoloId(String idConferenza, String emailAutore) {
        String sql = "SELECT id FROM articoli WHERE conferenza_id = ? AND autore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ps.setString(2, emailAutore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getArticoloId", e);
        }
        throw new RuntimeException("Articolo non trovato per conferenza=" + idConferenza
                + " autore=" + emailAutore);
    }

    public Optional<File> getFeedback(String idArticolo) {
        String sql = "SELECT file_url FROM feedback_editor WHERE articolo_id = ? ORDER BY data_invio DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new File(rs.getString("file_url")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getFeedback", e);
        }
        return Optional.empty();
    }

    public Optional<File> getRevisione(String idRevisione) {
        String sql = "SELECT file_url FROM revisioni WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRevisione);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String fileUrl = rs.getString("file_url");
                if (fileUrl == null || fileUrl.trim().isEmpty()) {
                    return Optional.empty();
                }
                return Optional.of(new File(fileUrl));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getRevisione", e);
        }
        return Optional.empty();
    }

    public Optional<EntityConferenza> getConferenzaAutore(String idConferenza) {
        String sql = "SELECT * FROM conferenze WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapConf(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getConferenza", e);
        }
        return Optional.empty();
    }

    public boolean isAutoreIscritto(String idConferenza, String emailAutore) {
        String sql = "SELECT 1 FROM articoli WHERE conferenza_id = ? AND autore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ps.setString(2, emailAutore);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante isAutoreIscritto", e);
        }
    }


    public Map<String, String> getRevisioniArticolo(String idArticolo) {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT id, voto, expertise, revisore_id FROM revisioni WHERE articolo_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idArticolo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String revisore = rs.getString("revisore_id");

                    int voto = rs.getInt("voto");
                    String expertise = rs.getString("expertise");
                    // se expertise fosse NULL, lo sostituiamo con stringa vuota
                    if (expertise == null) expertise = "";

                    String descr = "Revisore: " + revisore +
                                " - Voto: " + voto +
                                " - Expertise: " + expertise;

                    map.put(id, descr);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getRevisioniArticolo", e);
        }
        return map;
    }

    private EntityArticolo mapArticolo(ResultSet rs) throws SQLException {
        return new EntityArticolo(
                rs.getString("id"),                 // id
                rs.getString("conferenza_id"),      // conferenzaId
                rs.getString("titolo"),             // titolo
                rs.getString("parole_chiave"),      // paroleChiave
                rs.getString("stato"),              // stato
                rs.getString("autore_id"),       // autoreId
                rs.getInt("posizione"),             // posizione
                rs.getInt("num_revisioni"),         // numRevisioni
                rs.getDouble("punteggio")           // punteggio
        );
    }

    public Optional<EntityArticolo> getDatiArticolo(String idArticolo) {
        String sql = "SELECT * FROM articoli WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapArticolo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getArticolo", e);
        }
        return Optional.empty();
    }

    public LocalDate getDataScadenzaSottomissione(String idConferenza) {
        String sql = "SELECT scad_sottomissione FROM conferenze WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return LocalDate.parse(rs.getString("scad_sottomissione"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getDataScadenzaSottomissione", e);
        }
        throw new RuntimeException("Data di scadenza non trovata per conferenza " + idConferenza);
    }


    public boolean haArticoloSottomesso(String idArticolo) {
        String sql = "SELECT COUNT(*) as cnt FROM articoli WHERE id = ? AND stato = \"Sottomesso\"";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore haArticoloSottomesso", e);
        }
        return false;
    }

    public void inviaDettagliArticolo(String idArticolo, String titolo, String paroleChiave) {
        String sql = "UPDATE articoli SET titolo = ?, parole_chiave = ? " +
                "WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, titolo);
            ps.setString(2, paroleChiave);
            ps.setString(3, idArticolo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore inviaDettagliArticolo", e);
        }
    }

    // ======================  FUNZIONI EDITOR  =============================

    public List<EntityConferenza> getConferenzeEditor(String emailEditor) {
        String sql = "SELECT * FROM conferenze WHERE editor_id = ?";
        List<EntityConferenza> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailEditor);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapConf(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getConferenzeEditor", e);
        }
        return list;
    }

    public Optional<EntityConferenza> getConferenzaEditor(String idConferenza, String emailEditor) {
        String sql = "SELECT * FROM conferenze WHERE id = ? AND editor_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ps.setString(2, emailEditor);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapConf(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getConferenzaEditor", e);
        }
        return Optional.empty();
    }

    public Optional<File> getVersioneCameraready(String idArticolo) {
        return getUltimaVersione(idArticolo, "camera_ready");
    }

    public LocalDate getDataScadenzaFeedbackEditore(String idConferenza) {
        String sql = "SELECT scad_feedback_editore FROM conferenze WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return LocalDate.parse(rs.getString("scad_feedback_editore"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getDataScadenzaFeedback", e);
        }
        throw new RuntimeException("Conferenza non trovata " + idConferenza);
    }

    public boolean getPresenzaFeedback(String idArticolo) {
        String sql = "SELECT 1 FROM feedback_editor WHERE articolo_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Errore getPresenzaFeedback", e);
        }
    }

    public void inviaFeedback(String emailEditor, File file, String idArticolo) {
        String sql = "INSERT INTO feedback_editor(articolo_id, editor_id, file_url, data_invio) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ps.setString(2, emailEditor);
            ps.setString(3, file.getAbsolutePath());
            ps.setString(4, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore inviaFeedback", e);
        }
    }

    public void notificaAutore(String idArticolo) {
        String sql = "SELECT a.autore_id, c.titolo FROM articoli a JOIN conferenze c ON a.conferenza_id = c.id WHERE a.id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String emailAutore = rs.getString("autore_id");
                String titConf = rs.getString("titolo");
                String subject = "Nuovo Feedback Editor";
                String msg = "È disponibile un nuovo feedback per il tuo articolo nella conferenza " + titConf;
                MailUtil.inviaMail(msg, emailAutore, subject);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore notificaAutore", e);
        }
    }

    public List<EntityArticolo> getCameraReadyArticoli(String confId) {
        String sql = "SELECT a.* FROM articoli a " +
                     "JOIN (SELECT articolo_id, MAX(id) AS max_id FROM versioni WHERE tipo = 'camera_ready' GROUP BY articolo_id) v " +
                     "ON v.articolo_id = a.id WHERE a.conferenza_id = ?";
        List<EntityArticolo> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new EntityArticolo(
                        rs.getString("id"),
                        rs.getString("conferenza_id"),
                        rs.getString("titolo"),
                        rs.getString("parole_chiave"),
                        rs.getString("stato"),
                        rs.getString("autore_id"),
                        rs.getObject("posizione") != null ? rs.getInt("posizione") : null,
                        rs.getObject("num_revisioni") != null ? rs.getInt("num_revisioni") : null,
                        rs.getObject("punteggio") != null ? rs.getDouble("punteggio") : null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getCameraReadyArticoli", e);
        }
        return list;
    }

    // ==================== METODI PER CONTROLREVISIONI ====================

    // Recupera le competenze dei revisori di una conferenza
    public Map<String, List<String>> getCompetenzeRevisori(String confId) {
        Map<String, List<String>> result = new HashMap<>();
        String sql = "SELECT DISTINCT u.email, u.aree_competenza FROM utenti u "
                + "JOIN inviti_revisori i ON u.email = i.revisore_id "
                + "WHERE i.conferenza_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String email = rs.getString("email");
                String areeStr = rs.getString("aree_competenza");
                List<String> aree = (areeStr != null) ? Arrays.asList(areeStr.split(",")) : new ArrayList<>();
                result.put(email, aree);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getCompetenzeRevisori", e);
        }
        return result;
    }

    // Comunica le assegnazioni di articoli ai revisori
    public void comunicaAssegnazioni(String confId, Map<String, List<String>> assegnazioni) {
        String sql = "INSERT INTO revisioni(articolo_id, revisore_id) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(URL)) {
            for (Map.Entry<String, List<String>> entry : assegnazioni.entrySet()) {
                String idArticolo = entry.getKey();
                for (String revisore : entry.getValue()) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, idArticolo);
                        ps.setString(2, revisore);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore comunicaAssegnazioni", e);
        }
    }

    // Aggiorna stato dell'invito del revisore
    public void aggiornaInvitoConferenza(String confId, String emailRevisore, String stato) {
        String sql = "UPDATE inviti_revisori SET stato = ? WHERE conferenza_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stato);
            ps.setString(2, confId);
            ps.setString(3, emailRevisore);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornaInvitoConferenza", e);
        }
    }

    public String getStatoInvitoRevisore(String confId, String emailRevisore) {
        String sql = "SELECT stato FROM inviti_revisori WHERE conferenza_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ps.setString(2, emailRevisore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("stato");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getStatoInvitoRevisore", e);
        }
        return null;
    }

    // Restituisce mappa articolo -> stato revisione
    public Map<String, String> getStatoRevisioni(String confId) {
        Map<String, String> result = new HashMap<>();
        String sql = "SELECT a.id, a.stato FROM articoli a WHERE a.conferenza_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("id"), rs.getString("stato"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getStatoRevisioni", e);
        }
        return result;
    }

    // Rimuove una assegnazione di revisione
    public void rimuoviAssegnazione(String idRevisione) {
        String sql = "DELETE FROM revisioni WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idRevisione);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore rimuoviAssegnazione", e);
        }
    }

    // Ottiene data scadenza revisioni
    public LocalDate getDataScadenzaRevisioni(String confId) {
        String sql = "SELECT scad_revisioni FROM conferenze WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return LocalDate.parse(rs.getString("scad_revisioni"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getDataScadenzaRevisioni", e);
        }
        throw new RuntimeException("Scadenza revisioni non trovata per conferenza " + confId);
    }

    // Lista revisori di una conferenza
    public List<String> getRevisoriConferenza(String confId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT revisore_id FROM inviti_revisori WHERE conferenza_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("revisore_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getRevisoriConferenza", e);
        }
        return list;
    }

    // Aggiunge assegnazione revisione
    public boolean aggiungiAssegnazione(String idArticolo, String emailRevisore) {
        String checkSql = "SELECT COUNT(*) FROM revisioni WHERE articolo_id = ? AND revisore_id = ?";
        String insertSql = "INSERT INTO revisioni(articolo_id, revisore_id) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(URL)) {
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, idArticolo);
                checkPs.setString(2, emailRevisore);
                ResultSet rs = checkPs.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                if (count == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        ps.setString(1, idArticolo);
                        ps.setString(2, emailRevisore);
                        ps.executeUpdate();
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiungiAssegnazione", e);
        }
    }

    // Comunica graduatoria
    public void comunicaGraduatoria(String confId, Map<String, Integer> graduatoria) {
        String sql = "UPDATE articoli SET posizione = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL)) {
            for (Map.Entry<String, Integer> entry : graduatoria.entrySet()) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, entry.getValue());
                    ps.setString(2, entry.getKey());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore comunicaGraduatoria", e);
        }
    }

    // Conferenze revisore (con stato invito)
    public Map<EntityConferenza, String> getConferenzeRevisore(String emailRevisore) {
        Map<EntityConferenza, String> result = new HashMap<>();
        String sql = "SELECT c.*, i.stato FROM conferenze c JOIN inviti_revisori i ON c.id = i.conferenza_id WHERE i.revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailRevisore);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EntityConferenza conf = mapConf(rs);
                String stato = rs.getString("stato");
                result.put(conf, stato);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getConferenzeRevisore", e);
        }
        return result;
    }

    // Conferenza specifica revisore
    public Optional<EntityConferenza> getConferenzaRevisore(String confId, String emailRevisore) {
        String sql = "SELECT c.* FROM conferenze c JOIN inviti_revisori i ON c.id = i.conferenza_id WHERE c.id = ? AND i.revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ps.setString(2, emailRevisore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapConf(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getConferenzaRevisore", e);
        }
        return Optional.empty();
    }

    // Articoli assegnati a revisore → restituisce solo gli ID
    public List<String> getArticoliRevisore(String confId, String emailRevisore) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT a.id FROM articoli a " +
                     "JOIN revisioni r ON a.id = r.articolo_id " +
                     "WHERE a.conferenza_id = ? AND r.revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ps.setString(2, emailRevisore);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getArticoliRevisore", e);
        }
        return list;
    }

    // Carica revisione
    public void caricaRevisione(String emailRevisore, String idArticolo, int voto, int expertise, File file) {
        String updateSql = "UPDATE revisioni SET voto = ?, expertise = ?, file_url = ? WHERE articolo_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL)) {
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, voto);
                ps.setInt(2, expertise);
                ps.setString(3, file.getAbsolutePath());
                ps.setString(4, idArticolo);
                ps.setString(5, emailRevisore);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricaRevisione", e);
        }
    }

    // Controllo modalità broadcast
    public boolean isModalitaBroadcast(String confId) {
        String sql = "SELECT modalita_distribuzione FROM conferenze WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "BROADCAST".equalsIgnoreCase(rs.getString("modalita_distribuzione"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore isModalitaBroadcast", e);
        }
        return false;
    }

    // Articoli disponibili senza revisione
    public List<EntityArticolo> getArticoliDisponibili(String confId, String emailRevisore) {
        List<EntityArticolo> list = new ArrayList<>();
        String sql = "SELECT a.* FROM articoli a WHERE a.conferenza_id = ? AND a.id NOT IN (SELECT articolo_id FROM revisioni WHERE revisore_id = ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ps.setString(2, emailRevisore);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapArticolo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getArticoliDisponibili", e);
        }
        return list;
    }

        // Assegna articolo manuale a revisore
    public void assegnaArticoloRevisore(String idArticolo, String emailRevisore) {
        String sql = "INSERT INTO revisioni(articolo_id, revisore_id) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ps.setString(2, emailRevisore);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore assegnaArticoloRevisore", e);
        }
    }

    // Ottiene voto della revisione per articolo e revisore
    public Optional<Integer> getVotoRevisione(String idArticolo, String emailRevisore) {
        String sql = "SELECT voto FROM revisioni WHERE articolo_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ps.setString(2, emailRevisore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Integer voto = rs.getInt("voto");
                return Optional.ofNullable(voto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getVotoRevisione", e);
        }
        return Optional.empty();
    }

    /**
     * Restituisce il numero di revisioni con voto ed expertise non nulli per un dato articolo.
     */
    public int getNumRevisioni(String articoloId) {
        String sql = "SELECT COUNT(*) FROM revisioni WHERE articolo_id = ? AND voto IS NOT NULL AND expertise IS NOT NULL";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, articoloId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante getNumRevisioni", e);
        }
        return 0;
    }


    // Conferenze automatiche per graduatoria
    public List<EntityConferenza> getConferenzeAutomaticheConScadenzaSottomissione(LocalDate data) {
        List<EntityConferenza> list = new ArrayList<>();
        LocalDate giornoPrima = data.minusDays(1);
        String sql = "SELECT * FROM conferenze WHERE scad_sottomissione = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, giornoPrima.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapConf(rs));
            }
        } catch (SQLException e) {
                throw new RuntimeException("Errore getConferenzeAutomaticheConScadenzaSottomissione", e);
        }
        return list;
    }

    // Conferenze con scadenza revisioni il giorno specificato e nessun articolo con posizione
    public List<EntityConferenza> getConferenzeSenzaGraduatoria(LocalDate data) {
        List<EntityConferenza> list = new ArrayList<>();
        LocalDate giornoPrima = data.minusDays(1);
        String sql = "SELECT * FROM conferenze WHERE scad_revisioni = ?";
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, giornoPrima.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EntityConferenza conf = mapConf(rs);
                // Controllo che nessun articolo abbia posizione valorizzata
                boolean nessunaPosizione = true;
                for (EntityArticolo art : getArticoliConferenza(conf.getId())) {
                    if (art.getPosizione() != null) {
                        nessunaPosizione = false;
                        break;
                    }
                }
                if (nessunaPosizione) {
                    list.add(conf);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getConferenzeSenzaGraduatoria", e);
        }
        return list;
    }

    // Ottiene i dati di un articolo tramite ID
    public Optional<com.cms.entity.EntityArticolo> getDatiArticoloById(String idArticolo) {
        String sql = "SELECT * FROM articoli WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapArticolo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getDatiArticoloById", e);
        }
        return Optional.empty();
    }

    // Ottiene expertise della revisione per articolo e revisore
    public Optional<Integer> getExpertiseRevisione(String idArticolo, String emailRevisore) {
        String sql = "SELECT expertise FROM revisioni WHERE articolo_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ps.setString(2, emailRevisore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int exp = rs.getInt("expertise");
                return Optional.of(exp);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getExpertiseRevisione", e);
        }
        return Optional.empty();
    }

    // Restituisce le notifiche di un utente (come lista di mappe chiave-valore)
    public List<Map<String, String>> getNotifiche(String email) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT id, messaggio, data_invio, letta FROM notifiche WHERE utente_id = ? ORDER BY data_invio DESC";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> notifica = new HashMap<>();
                notifica.put("id", rs.getString("id"));
                notifica.put("messaggio", rs.getString("messaggio"));
                notifica.put("data_invio", rs.getString("data_invio"));
                notifica.put("letta", rs.getString("letta"));
                list.add(notifica);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getNotifiche", e);
        }
        return list;
    }

    // Cancella una notifica dato l'id
    public void cancellaNotifica(String idNotifica) {
        String sql = "DELETE FROM notifiche WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idNotifica);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore cancellaNotifica", e);
        }
    }

    // ======== NUOVO METODO: Dati Revisioni per InfoRevisioniChair ========
    public List<Map<String, String>> getDatiRevisioni(String confId) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT a.id AS art_id, a.titolo, a.autore_id, r.id, r.revisore_id, r.voto, r.expertise " +
                     "FROM articoli a LEFT JOIN revisioni r ON a.id = r.articolo_id " +
                     "WHERE a.conferenza_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, confId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put("art_id", rs.getString("art_id"));
                map.put("titolo", rs.getString("titolo"));
                map.put("autore_id", rs.getString("autore_id"));
                map.put("id", rs.getString("id"));
                map.put("revisore_id", rs.getString("revisore_id"));
                map.put("voto", rs.getString("voto"));
                map.put("expertise", rs.getString("expertise"));
                list.add(map);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getDatiRevisioni", e);
        }
        return list;
    }

    // Restituisce l'id della revisione dato articolo e revisore
    public Optional<String> getIdRevisione(String idArticolo, String emailRevisore) {
        String sql = "SELECT id FROM revisioni WHERE articolo_id = ? AND revisore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ps.setString(2, emailRevisore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getIdRevisione", e);
        }
        return Optional.empty();
    }

    // Aggiorna il punteggio di un articolo
    public void aggiornaPunteggioArticolo(String idArticolo, Double punteggio) {
        String sql = "UPDATE articoli SET punteggio = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (punteggio != null) {
                ps.setDouble(1, punteggio);
            } else {
                ps.setNull(1, Types.DOUBLE);
            }
            ps.setString(2, idArticolo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornaPunteggioArticolo", e);
        }
    }

    // Aggiorna la posizione di un articolo
    public void aggiornaPosizioneArticolo(String idArticolo, Integer posizione) {
        String sql = "UPDATE articoli SET posizione = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (posizione != null) {
                ps.setInt(1, posizione);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, idArticolo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornaPosizioneArticolo", e);
        }
    }
}