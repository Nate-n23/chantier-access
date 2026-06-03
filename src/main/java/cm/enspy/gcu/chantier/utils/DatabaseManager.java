package cm.enspy.gcu.chantier.utils;

import cm.enspy.gcu.chantier.config.Constants;
import cm.enspy.gcu.chantier.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Gestionnaire de base de données SQLite — Singleton.
 * Gère la connexion unique, l'initialisation du schéma et le chargement des données de base.
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static volatile DatabaseManager instance;
    private Connection connection;
    private String jdbcUrl = DatabaseConfig.JDBC_URL;

    /** Constructeur privé. */
    private DatabaseManager() {}

    /**
     * Retourne l'instance unique du gestionnaire de base de données.
     *
     * @return instance DatabaseManager
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Définit une URL JDBC personnalisée (utilisé pour les tests).
     *
     * @param url URL JDBC à utiliser
     */
    public void setJdbcUrl(String url) {
        this.jdbcUrl = url;
    }

    /**
     * Initialise la base de données: crée le répertoire, la connexion,
     * active les clés étrangères et exécute le script SQL d'initialisation.
     *
     * @throws SQLException en cas d'erreur d'initialisation
     */
    public void initialize() throws SQLException {
        // Crée les répertoires nécessaires (sauf pour :memory:)
        if (!jdbcUrl.contains(":memory:")) {
            File dataDir = new File(Constants.DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                LOGGER.info("Répertoire de données créé: " + Constants.DATA_DIR);
            }
            File rapportsDir = new File(Constants.RAPPORTS_DIR);
            if (!rapportsDir.exists()) rapportsDir.mkdirs();
            File backupDir = new File(Constants.BACKUP_DIR);
            if (!backupDir.exists()) backupDir.mkdirs();
        }

        // Connexion SQLite
        connection = DriverManager.getConnection(jdbcUrl);

        // Active les clés étrangères
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        // Exécute le script d'initialisation
        executeSqlScript();
        LOGGER.info("Base de données initialisée: " + jdbcUrl);
    }

    /**
     * Charge et exécute le script SQL d'initialisation depuis les ressources.
     *
     * @throws SQLException en cas d'erreur d'exécution SQL
     */
    private void executeSqlScript() throws SQLException {
        try (InputStream is = getClass().getResourceAsStream(
                "/cm/enspy/gcu/chantier/sql/init_db.sql")) {
            if (is == null) {
                throw new SQLException("Script SQL init_db.sql introuvable dans les ressources.");
            }
            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Découpe par point-virgule et exécute chaque instruction
            try (Statement stmt = connection.createStatement()) {
                for (String instruction : sql.split(";")) {
                    String trimmed = instruction.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("Erreur lecture script SQL: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne la connexion active à la base de données.
     *
     * @return connexion JDBC active
     * @throws IllegalStateException si la connexion n'a pas été initialisée
     */
    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("La base de données n'a pas été initialisée. Appelez initialize() d'abord.");
        }
        return connection;
    }

    /**
     * Ferme la connexion à la base de données proprement.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connexion à la base de données fermée.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Réinitialise l'instance (pour les tests uniquement).
     */
    public void reset() {
        close();
        connection = null;
        jdbcUrl = DatabaseConfig.JDBC_URL;
    }
}
