package com.cms.common;

import com.cms.entity.EntityArticolo;
import com.cms.entity.EntityConferenza;
import com.cms.entity.EntityUtente;

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

    // ... (tutti i metodi queryGetConferenze, queryGetConferenza, queryCreaConferenza,
    //     queryRevisorePresente, queryInvitaRevisore, queryRimuoviRevisore,
    //     queryAggiungiEditor, queryGetUltimaVersione rimangono identici)
    // Li riporto per completezza, ma li puoi copiare pari pari da prima:

    public List<EntityConferenza> queryGetConferenze(String currentChairId) {
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
            throw new RuntimeException("Errore durante queryGetConferenze", e);
        }
        return list;
    }

    public Optional<EntityConferenza> queryGetConferenza(String id) {
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
            throw new RuntimeException("Errore durante queryGetConferenza", e);
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

    public Optional<String> queryGetUltimaVersione(String idArticolo) {
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
            throw new RuntimeException("Errore durante queryGetUltimaVersione", e);
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
        int rp      = rs.getInt("num_articoli_vincitori");
        int rn      = rs.getInt("num_articoli_vincitori");
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

    public List<EntityArticolo> queryGetArticoliConferenza(String confId) {
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
            throw new RuntimeException("Errore durante queryGetArticoliConferenza", e);
        }
        return list;
    }

    public Map<String, String> queryGetRevisoriConStato(String confId) {
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
            throw new RuntimeException("Errore durante queryGetRevisoriConStato", e);
        }
        return map;
    }

    public Optional<String> queryGetNomeCompleto(String email) {
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
            throw new RuntimeException("Errore durante queryGetNomeCompleto", e);
        }
        return Optional.empty();
    }

    private EntityUtente mapUtente(ResultSet rs) throws SQLException {
        return new EntityUtente(
                rs.getString("email"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("ruolo"),
                rs.getString("aree_competenza"),
                LocalDate.parse(rs.getString("data_nascita")),
                rs.getString("password"),
                rs.getString("password_temporanea").equalsIgnoreCase("true")
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
            System.out.println("Registrazione utente riuscita: " + utente.getEmail());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore inserimento utente: " + e.getMessage());
            return false; // già presente o errore
        }
    }


    // Recupera dati utente
    public Optional<EntityUtente> queryGetUtente(String email) {
        String sql = "SELECT * FROM utenti WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new EntityUtente(
                        rs.getString("email"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("ruolo"),
                        rs.getString("aree_competenza"),
                        LocalDate.parse(rs.getString("data_nascita")),
                        rs.getString("password"),
                        rs.getString("password_temporanea").equalsIgnoreCase("true")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante queryGetUtente", e);
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

    public void inviaArticolo(String idConferenza, String emailAutore, File file) {
        String idArticolo = getArticoloId(idConferenza, emailAutore);
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

    public void inviaCameraready(String idConferenza, String emailAutore, File file) {
        String idArticolo = getArticoloId(idConferenza, emailAutore);
        inserisciVersione(idArticolo, "camera_ready", file);
    }

    public void inviaVersioneFinale(String idConferenza, String emailAutore, File file) {
        String idArticolo = getArticoloId(idConferenza, emailAutore);
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

    public Optional<File> getArticolo(String idConferenza, String emailAutore) {
        String idArticolo = getArticoloId(idConferenza, emailAutore);
        return recuperaUltimaVersione(idArticolo, "articolo");
    }

    public Optional<File> getCameraready(String idConferenza, String emailAutore) {
        String idArticolo = getArticoloId(idConferenza, emailAutore);
        return recuperaUltimaVersione(idArticolo, "camera_ready");
    }

    public Optional<File> getVersioneFinale(String idConferenza, String emailAutore) {
        String idArticolo = getArticoloId(idConferenza, emailAutore);
        return recuperaUltimaVersione(idArticolo, "versione_finale");
    }

    private Optional<File> recuperaUltimaVersione(String idArticolo, String tipo) {
        String sql = "SELECT file_url FROM versioni WHERE articolo_id = ? AND tipo = ? "
                + "ORDER BY data_caricamento DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ps.setString(2, tipo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new File(rs.getString("file_url")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante recuperaUltimaVersione", e);
        }
        return Optional.empty();
    }

    private String getArticoloId(String idConferenza, String emailAutore) {
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

    public Optional<File> getFeedback(String idConferenza, String emailAutore) {
        String idArticolo = getArticoloId(idConferenza, emailAutore);
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
                return Optional.of(new File(rs.getString("file_url")));
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


    public Map<String, String> getRevisioniArticolo(String idConferenza, String emailAutore) {
        Map<String, String> map = new LinkedHashMap<>();
        String idArticolo = getArticoloId(idConferenza, emailAutore);
        String sql = "SELECT id, voto, expertise FROM revisioni WHERE articolo_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String descr = "Voto: " + rs.getInt("voto") + " - Expertise: " + rs.getString("expertise");
                map.put(id, descr);
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

    public Optional<EntityArticolo> getDatiArticolo(String idConferenza, String emailAutore) {
        String sql = "SELECT * FROM articoli WHERE conferenza_id = ? AND autore_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ps.setString(2, emailAutore);
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


    public boolean haArticoloSottomesso(String idConferenza, String emailAutore) {
        String sql = "SELECT COUNT(*) as cnt FROM articoli WHERE conferenza_id = ? AND autore_id = ? AND stato = \"Sottomesso\"";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idConferenza);
            ps.setString(2, emailAutore);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore haArticoloSottomesso", e);
        }
        return false;
    }

    public void inviaDettagliArticolo(String idConferenza, String emailAutore, String titolo, String paroleChiave) {
        String sql = "UPDATE articoli SET titolo = ?, parole_chiave = ? " +
                "WHERE conferenza_id = ? AND autore_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, titolo);
            ps.setString(2, paroleChiave);
            ps.setString(3, idConferenza);
            ps.setString(4, emailAutore);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                // se non esiste ancora, inserisci
                String insertSql = "INSERT INTO articoli (conferenza_id, autore_id, titolo, parole_chiave) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setString(1, idConferenza);
                    psInsert.setString(2, emailAutore);
                    psInsert.setString(3, titolo);
                    psInsert.setString(4, paroleChiave);
                    psInsert.executeUpdate();
                }
            }

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
        return recuperaUltimaVersione(idArticolo, "camera_ready");
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
        String sql = "INSERT INTO feedback_editor(id, articolo_id, email_editor, file_url, data_invio) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, idArticolo);
            ps.setString(3, emailEditor);
            ps.setString(4, file.getAbsolutePath());
            ps.setString(5, LocalDate.now().toString());
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
                com.cms.utils.MailUtil.inviaMail(msg, emailAutore, subject);
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
}