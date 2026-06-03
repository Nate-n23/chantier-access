package cm.enspy.gcu.chantier.utils;

import cm.enspy.gcu.chantier.config.Constants;
import cm.enspy.gcu.chantier.model.entities.UtilisateurSysteme;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Gestionnaire de session utilisateur — Singleton.
 * Stocke les informations de l'utilisateur connecté et gère le timeout d'inactivité.
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static volatile SessionManager instance;

    private UtilisateurSysteme utilisateurCourant;
    private LocalDateTime derniereActivite;

    /** Constructeur privé. */
    private SessionManager() {}

    /**
     * Retourne l'instance unique du gestionnaire de session.
     *
     * @return instance SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Démarre une session pour l'utilisateur fourni.
     *
     * @param utilisateur utilisateur qui vient de se connecter
     */
    public void connect(UtilisateurSysteme utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.derniereActivite = LocalDateTime.now();
        LOGGER.info("Session démarrée pour: " + utilisateur.getLogin());
    }

    /**
     * Termine la session courante.
     */
    public void deconnect() {
        if (utilisateurCourant != null) {
            LOGGER.info("Session terminée pour: " + utilisateurCourant.getLogin());
        }
        utilisateurCourant = null;
        derniereActivite = null;
    }

    /**
     * Met à jour le timestamp de la dernière activité (réinitialise le timeout).
     */
    public void rafraichirActivite() {
        if (isConnecte()) {
            derniereActivite = LocalDateTime.now();
        }
    }

    /**
     * Vérifie si une session est active et non expirée.
     *
     * @return true si l'utilisateur est connecté et la session valide
     */
    public boolean isConnecte() {
        return utilisateurCourant != null && derniereActivite != null;
    }

    /**
     * Vérifie si la session a expiré par inactivité.
     *
     * @return true si le timeout de session est dépassé
     */
    public boolean isSessionExpiree() {
        if (!isConnecte()) return true;
        return LocalDateTime.now().isAfter(
                derniereActivite.plusMinutes(Constants.SESSION_TIMEOUT_MINUTES));
    }

    /**
     * Retourne l'utilisateur actuellement connecté.
     *
     * @return utilisateur connecté, ou null si aucune session active
     */
    public UtilisateurSysteme getUtilisateurCourant() {
        return utilisateurCourant;
    }

    /**
     * Retourne le rôle de l'utilisateur courant.
     *
     * @return rôle de l'utilisateur, ou null si non connecté
     */
    public UtilisateurSysteme.Role getRoleCourant() {
        return utilisateurCourant != null ? utilisateurCourant.getRole() : null;
    }

    /**
     * Vérifie si l'utilisateur courant est administrateur.
     *
     * @return true si le rôle est ADMIN
     */
    public boolean isAdmin() {
        return UtilisateurSysteme.Role.ADMIN.equals(getRoleCourant());
    }

    /**
     * Vérifie si l'utilisateur courant a au moins le rôle RESPONSABLE.
     *
     * @return true si le rôle est ADMIN ou RESPONSABLE
     */
    public boolean isResponsableOuPlus() {
        UtilisateurSysteme.Role role = getRoleCourant();
        return role == UtilisateurSysteme.Role.ADMIN
                || role == UtilisateurSysteme.Role.RESPONSABLE;
    }

    /**
     * Vérifie si l'utilisateur courant est seulement CONSULTANT.
     *
     * @return true si le rôle est CONSULTANT
     */
    public boolean isConsultantOnly() {
        return UtilisateurSysteme.Role.CONSULTANT.equals(getRoleCourant());
    }
}
