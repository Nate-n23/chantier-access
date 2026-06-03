package cm.enspy.gcu.chantier.service;

import cm.enspy.gcu.chantier.config.Constants;
import cm.enspy.gcu.chantier.model.dao.AlerteDAO;
import cm.enspy.gcu.chantier.model.dao.BadgeDAO;
import cm.enspy.gcu.chantier.model.dao.EpiDAO;
import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.entities.Alerte;
import cm.enspy.gcu.chantier.model.entities.Badge;
import cm.enspy.gcu.chantier.model.entities.Epi;
import cm.enspy.gcu.chantier.model.entities.Intervenant;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service de gestion des alertes de sécurité.
 * Génère automatiquement les alertes pour les badges expirants et les EPI manquants.
 */
public class AlerteService {

    private static final Logger LOGGER = Logger.getLogger(AlerteService.class.getName());

    private final AlerteDAO alerteDAO;
    private final BadgeDAO badgeDAO;
    private final IntervenantDAO intervenantDAO;
    private final EpiDAO epiDAO;

    /** Constructeur par défaut. */
    public AlerteService() {
        this.alerteDAO = new AlerteDAO();
        this.badgeDAO = new BadgeDAO();
        this.intervenantDAO = new IntervenantDAO();
        this.epiDAO = new EpiDAO();
    }

    /**
     * Constructeur pour les tests avec injection de dépendances.
     *
     * @param alerteDAO      DAO alerte
     * @param badgeDAO       DAO badge
     * @param intervenantDAO DAO intervenant
     * @param epiDAO         DAO EPI
     */
    public AlerteService(AlerteDAO alerteDAO, BadgeDAO badgeDAO,
                         IntervenantDAO intervenantDAO, EpiDAO epiDAO) {
        this.alerteDAO = alerteDAO;
        this.badgeDAO = badgeDAO;
        this.intervenantDAO = intervenantDAO;
        this.epiDAO = epiDAO;
    }

    /**
     * Vérifie les badges expirant dans les 7 prochains jours et crée les alertes.
     * N'est appelée qu'une fois par session pour éviter les doublons.
     */
    public void verifierBadgesExpirants() {
        List<Badge> badgesExpirants = badgeDAO.findExpirantDans(Constants.ALERTE_BADGE_JOURS);
        for (Badge badge : badgesExpirants) {
            String msg = "Badge " + badge.getCode() + " expire le " + badge.getDateExpiration();
            Alerte alerte = new Alerte(
                    Alerte.TypeAlerte.BADGE_EXPIRANT.name(),
                    msg,
                    Alerte.NiveauGravite.AVERTISSEMENT,
                    badge.getId()
            );
            alerteDAO.save(alerte);
            LOGGER.info("Alerte badge expirant créée: " + badge.getCode());
        }
    }

    /**
     * Vérifie que tous les intervenants actifs ont au moins un casque et un gilet.
     * Crée une alerte pour chaque EPI obligatoire manquant.
     */
    public void verifierEpiManquants() {
        List<Intervenant> actifs = intervenantDAO.findActifs();
        for (Intervenant intervenant : actifs) {
            if (!epiDAO.hasEpi(intervenant.getId(), Epi.TypeEpi.CASQUE)) {
                creerAlerteEpiManquant(intervenant, "CASQUE");
            }
            if (!epiDAO.hasEpi(intervenant.getId(), Epi.TypeEpi.GILET)) {
                creerAlerteEpiManquant(intervenant, "GILET");
            }
        }
    }

    /**
     * Crée une alerte de badge expiré pour le badge donné.
     *
     * @param badge le badge expiré
     * @return l'alerte créée
     */
    public Alerte creerAlerteBadgeExpire(Badge badge) {
        String msg = "Badge " + badge.getCode() + " est expiré depuis le " + badge.getDateExpiration();
        Alerte alerte = new Alerte(
                Alerte.TypeAlerte.BADGE_EXPIRE.name(),
                msg,
                Alerte.NiveauGravite.CRITIQUE,
                badge.getId()
        );
        alerteDAO.save(alerte);
        LOGGER.warning("Alerte badge expiré créée: " + badge.getCode());
        return alerte;
    }

    /**
     * Retourne toutes les alertes non lues.
     *
     * @return liste des alertes non lues
     */
    public List<Alerte> getAlertesNonLues() {
        return alerteDAO.findNonLues();
    }

    /**
     * Compte les alertes non lues.
     *
     * @return nombre d'alertes non lues
     */
    public int countAlertesNonLues() {
        return alerteDAO.countNonLues();
    }

    /**
     * Marque une alerte comme lue.
     *
     * @param alerteId identifiant de l'alerte à marquer
     */
    public void marquerCommeLue(int alerteId) {
        alerteDAO.marquerCommeLue(alerteId);
    }

    /**
     * Retourne toutes les alertes.
     *
     * @return liste complète des alertes
     */
    public List<Alerte> getToutesAlertes() {
        return alerteDAO.findAll();
    }

    /**
     * Crée une alerte pour un EPI manquant chez un intervenant.
     *
     * @param intervenant intervenant concerné
     * @param typeEpi     type d'EPI manquant
     */
    private void creerAlerteEpiManquant(Intervenant intervenant, String typeEpi) {
        String msg = "Intervenant " + intervenant.getNomComplet()
                + " (" + intervenant.getMatricule() + ") n'a pas de " + typeEpi + " attribué.";
        Alerte alerte = new Alerte(
                Alerte.TypeAlerte.EPI_MANQUANT.name(),
                msg,
                Alerte.NiveauGravite.AVERTISSEMENT,
                intervenant.getId()
        );
        alerteDAO.save(alerte);
        LOGGER.warning("Alerte EPI manquant: " + msg);
    }
}
