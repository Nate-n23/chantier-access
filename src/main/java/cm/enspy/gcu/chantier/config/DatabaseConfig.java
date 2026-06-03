package cm.enspy.gcu.chantier.config;

/**
 * Configuration de la connexion à la base de données SQLite.
 * Fournit l'URL JDBC construite à partir des constantes de l'application.
 */
public final class DatabaseConfig {

    /** URL de connexion JDBC pour SQLite. */
    public static final String JDBC_URL = "jdbc:sqlite:" + Constants.DB_PATH;

    /** URL de connexion JDBC pour les tests (base en mémoire). */
    public static final String JDBC_URL_MEMORY = "jdbc:sqlite::memory:";

    /** Pilote JDBC SQLite. */
    public static final String JDBC_DRIVER = "org.sqlite.JDBC";

    /** Constructeur privé — classe utilitaire. */
    private DatabaseConfig() {
    }
}
