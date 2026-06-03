package cm.enspy.gcu.chantier.service;

import cm.enspy.gcu.chantier.model.dao.AccesDAO;
import cm.enspy.gcu.chantier.model.dao.BadgeDAO;
import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.dao.ZoneDAO;
import cm.enspy.gcu.chantier.model.entities.Acces;
import cm.enspy.gcu.chantier.model.entities.Badge;
import cm.enspy.gcu.chantier.model.entities.Intervenant;
import cm.enspy.gcu.chantier.model.entities.Zone;
import cm.enspy.gcu.chantier.utils.SessionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service gérant le contrôle d'accès au chantier.
 * Vérifie la validité des badges, l'habilitation par zone et enregistre les passages.
 */
public class AccesService {

    private static final Logger LOGGER = Logger.getLogger(AccesService.class.getName());

    private final AccesDAO accesDAO;
    private final BadgeDAO badgeDAO;
    private final IntervenantDAO intervenantDAO;
    private final ZoneDAO zoneDAO;

    /** Constructeur par défaut. */
    public AccesService() {
        this.accesDAO = new AccesDAO();
        this.badgeDAO = new BadgeDAO();
        this.intervenantDAO = new IntervenantDAO();
        this.zoneDAO = new ZoneDAO();
    }

    /**
     * Constructeur pour les tests avec injection de dépendances.
     *
     * @param accesDAO       DAO accès
     * @param badgeDAO       DAO badge
     * @param intervenantDAO DAO intervenant
     * @param zoneDAO        DAO zone
     */
    public AccesService(AccesDAO accesDAO, BadgeDAO badgeDAO,
                        IntervenantDAO intervenantDAO, ZoneDAO zoneDAO) {
        this.accesDAO = accesDAO;
        this.badgeDAO = badgeDAO;
        this.intervenantDAO = intervenantDAO;
        this.zoneDAO = zoneDAO;
    }

    /**
     * Résultat d'une tentative d'accès.
     */
    public record ResultatAcces(boolean autorise, String message, Intervenant intervenant) {}

    /**
     * Enregistre et évalue une tentative d'accès.
     * Vérifie : badge actif, non expiré, intervenant ACTIF, habilitation sur la zone.
     *
     * @param codeBadge code du badge scanné
     * @param zoneId    identifiant de la zone cible
     * @param type      type d'accès (ENTREE ou SORTIE)
     * @return résultat de la tentative avec message explicatif
     */
    public ResultatAcces enregistrer(String codeBadge, int zoneId, Acces.TypeAcces type) {
        // 1. Vérifier le badge
        Optional<Badge> optBadge = badgeDAO.findByCode(codeBadge);
        if (optBadge.isEmpty()) {
            return enregistrerRefus(null, null, zoneId, type, "Badge inconnu: " + codeBadge);
        }
        Badge badge = optBadge.get();

        // 2. Vérifier l'intervenant
        Optional<Intervenant> optIntervenant = intervenantDAO.findById(badge.getIntervenantId());
        if (optIntervenant.isEmpty()) {
            return enregistrerRefus(badge, null, zoneId, type, "Intervenant non trouvé");
        }
        Intervenant intervenant = optIntervenant.get();

        // 3. Badge actif et non expiré
        if (!badge.isValide()) {
            return enregistrerRefus(badge, intervenant, zoneId, type,
                    "Badge invalide ou expiré");
        }

        // 4. Intervenant actif
        if (intervenant.getStatut() != Intervenant.Statut.ACTIF) {
            return enregistrerRefus(badge, intervenant, zoneId, type,
                    "Intervenant non actif: " + intervenant.getStatut());
        }

        // 5. Vérifier la zone
        Optional<Zone> optZone = zoneDAO.findById(zoneId);
        if (optZone.isEmpty()) {
            return enregistrerRefus(badge, intervenant, zoneId, type, "Zone inconnue");
        }
        Zone zone = optZone.get();

        // 6. Habilitation sur la zone
        List<Zone> zonesAutorisees = badgeDAO.findZonesForBadge(badge.getId());
        boolean habilite = zonesAutorisees.stream().anyMatch(z -> z.getId() == zoneId);
        if (!habilite) {
            return enregistrerRefus(badge, intervenant, zoneId, type,
                    "Non habilité pour la zone: " + zone.getNom());
        }

        // 7. Niveau d'habilitation suffisant
        if (intervenant.getNiveauHabilitation() < zone.getNiveauSecurite()) {
            return enregistrerRefus(badge, intervenant, zoneId, type,
                    "Niveau d'habilitation insuffisant (requis: " + zone.getNiveauSecurite() + ")");
        }

        // Accès autorisé
        Acces acces = new Acces();
        acces.setIntervenantId(intervenant.getId());
        acces.setBadgeId(badge.getId());
        acces.setZoneId(zoneId);
        acces.setDateHeure(LocalDateTime.now());
        acces.setType(type);
        acces.setStatut(Acces.StatutAcces.AUTORISE);
        Integer agentId = SessionManager.getInstance().isConnecte()
                ? SessionManager.getInstance().getUtilisateurCourant().getId() : null;
        acces.setAgentId(agentId);
        accesDAO.save(acces);

        String msg = "Accès autorisé — Bienvenue " + intervenant.getNomComplet();
        LOGGER.info(msg + " | Zone: " + zone.getNom());
        return new ResultatAcces(true, msg, intervenant);
    }

    /**
     * Crée et enregistre un accès refusé.
     *
     * @param badge       badge utilisé (peut être null)
     * @param intervenant intervenant concerné (peut être null)
     * @param zoneId      identifiant de la zone cible
     * @param type        type d'accès
     * @param motif       motif du refus
     * @return résultat d'accès refusé
     */
    private ResultatAcces enregistrerRefus(Badge badge, Intervenant intervenant,
                                           int zoneId, Acces.TypeAcces type, String motif) {
        if (badge != null && intervenant != null) {
            Acces acces = new Acces();
            acces.setIntervenantId(intervenant.getId());
            acces.setBadgeId(badge.getId());
            acces.setZoneId(zoneId);
            acces.setDateHeure(LocalDateTime.now());
            acces.setType(type);
            acces.setStatut(Acces.StatutAcces.REFUSE);
            acces.setMotifRefus(motif);
            Integer agentId = SessionManager.getInstance().isConnecte()
                    ? SessionManager.getInstance().getUtilisateurCourant().getId() : null;
            acces.setAgentId(agentId);
            accesDAO.save(acces);
        }
        LOGGER.warning("Accès refusé: " + motif);
        return new ResultatAcces(false, "Accès refusé — " + motif, intervenant);
    }

    /**
     * Retourne les N derniers accès.
     *
     * @param limite nombre maximum de résultats
     * @return liste des derniers accès
     */
    public List<Acces> getDerniersAcces(int limite) {
        return accesDAO.findDerniers(limite);
    }

    /**
     * Retourne la liste des personnes actuellement présentes sur le chantier.
     *
     * @return liste des accès ENTREE sans SORTIE correspondante aujourd'hui
     */
    public List<Acces> getPresents() {
        return accesDAO.findPresents();
    }

    /**
     * Retourne le nombre de présents aujourd'hui.
     *
     * @return nombre d'intervenants présents aujourd'hui
     */
    public int countPresentsAujourdhui() {
        return accesDAO.countPresentsAujourdhui();
    }

    /**
     * Retourne le nombre d'incidents (accès refusés) aujourd'hui.
     *
     * @return nombre d'accès refusés aujourd'hui
     */
    public int countIncidentsAujourdhui() {
        return accesDAO.countIncidentsAujourdhui();
    }

    /**
     * Retourne la fréquentation par zone pour les N derniers jours.
     *
     * @param jours nombre de jours
     * @return liste de tableaux [nom_zone, count]
     */
    public List<Object[]> getFrequentationParZone(int jours) {
        return accesDAO.getFrequentationParZone(jours);
    }
}
