package cm.enspy.gcu.chantier.config;

/**
 * Constantes globales de l'application Chantier Accès.
 * Regroupe les valeurs immuables utilisées dans toute l'application.
 */
public final class Constants {

    /** Répertoire racine de l'application dans le dossier utilisateur. */
    public static final String APP_DIR = System.getProperty("user.home") + "/ChantierAcces";

    /** Répertoire de la base de données. */
    public static final String DATA_DIR = APP_DIR + "/data";

    /** Chemin complet vers le fichier SQLite. */
    public static final String DB_PATH = DATA_DIR + "/chantier.db";

    /** Répertoire des rapports exportés. */
    public static final String RAPPORTS_DIR = APP_DIR + "/rapports";

    /** Répertoire des sauvegardes. */
    public static final String BACKUP_DIR = APP_DIR + "/backup";

    /** Durée du timeout de session en minutes. */
    public static final int SESSION_TIMEOUT_MINUTES = 15;

    /** Durée de validité par défaut d'un badge permanent en jours. */
    public static final int BADGE_PERMANENT_DUREE_JOURS = 365;

    /** Nombre de jours avant expiration pour déclencher une alerte badge. */
    public static final int ALERTE_BADGE_JOURS = 7;

    /** Longueur du code badge généré automatiquement. */
    public static final int BADGE_CODE_LENGTH = 8;

    /** Nombre maximum de derniers accès affichés dans le tableau de bord. */
    public static final int MAX_DERNIERS_ACCES = 10;

    /** Nombre maximum de passages affichés dans l'historique du contrôle. */
    public static final int MAX_HISTORIQUE_PASSAGES = 20;

    /** Intervalle de rafraîchissement du tableau de bord en secondes. */
    public static final int DASHBOARD_REFRESH_SECONDS = 30;

    /** Intervalle de mise à jour de la liste "présents" en secondes. */
    public static final int PRESENTS_REFRESH_SECONDS = 5;

    /** Regex de validation du mot de passe : min 8 chars, 1 majuscule, 1 chiffre. */
    public static final String MDP_REGEX = "^(?=.*[A-Z])(?=.*\\d).{8,}$";

    /** Constructeur privé — classe utilitaire non instanciable. */
    private Constants() {
    }
}
