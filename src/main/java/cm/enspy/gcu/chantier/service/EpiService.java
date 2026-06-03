package cm.enspy.gcu.chantier.service;

import cm.enspy.gcu.chantier.model.dao.EpiDAO;
import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.entities.Epi;
import cm.enspy.gcu.chantier.model.entities.Intervenant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service de gestion des EPI (Équipements de Protection Individuelle).
 * Gère l'attribution, la mise à jour d'état et les vérifications de conformité.
 */
public class EpiService {

    private static final Logger LOGGER = Logger.getLogger(EpiService.class.getName());

    private final EpiDAO epiDAO;
    private final IntervenantDAO intervenantDAO;

    /** Constructeur par défaut. */
    public EpiService() {
        this.epiDAO = new EpiDAO();
        this.intervenantDAO = new IntervenantDAO();
    }

    /**
     * Constructeur pour les tests avec injection de dépendances.
     *
     * @param epiDAO         DAO EPI
     * @param intervenantDAO DAO intervenant
     */
    public EpiService(EpiDAO epiDAO, IntervenantDAO intervenantDAO) {
        this.epiDAO = epiDAO;
        this.intervenantDAO = intervenantDAO;
    }

    /**
     * Attribue un EPI à un intervenant actif.
     * L'état initial est toujours BON.
     *
     * @param intervenantId identifiant de l'intervenant
     * @param type          type d'EPI à attribuer
     * @param taille        taille de l'EPI
     * @return l'EPI créé et persisté
     * @throws IllegalStateException si l'intervenant n'est pas actif
     * @throws IllegalArgumentException si l'intervenant est introuvable
     */
    public Epi attribuer(int intervenantId, Epi.TypeEpi type, String taille) {
        Optional<Intervenant> optIntervenant = intervenantDAO.findById(intervenantId);
        if (optIntervenant.isEmpty()) {
            throw new IllegalArgumentException("Intervenant introuvable: " + intervenantId);
        }
        Intervenant intervenant = optIntervenant.get();
        if (intervenant.getStatut() != Intervenant.Statut.ACTIF) {
            throw new IllegalStateException(
                    "Impossible d'attribuer un EPI à un intervenant non actif: " + intervenant.getStatut());
        }

        Epi epi = new Epi();
        epi.setType(type);
        epi.setTaille(taille);
        epi.setEtat(Epi.EtatEpi.BON);
        epi.setDateAttribution(LocalDateTime.now());
        epi.setIntervenantId(intervenantId);
        epiDAO.save(epi);
        LOGGER.info("EPI " + type + " attribué à l'intervenant " + intervenantId);
        return epi;
    }

    /**
     * Met à jour l'état d'un EPI (BON → USAGÉ → À REMPLACER).
     *
     * @param epiId     identifiant de l'EPI
     * @param nouvelEtat nouveau état
     */
    public void changerEtat(int epiId, Epi.EtatEpi nouvelEtat) {
        epiDAO.updateEtat(epiId, nouvelEtat);
        LOGGER.info("État EPI " + epiId + " changé vers " + nouvelEtat);
    }

    /**
     * Retourne tous les EPIs d'un intervenant.
     *
     * @param intervenantId identifiant de l'intervenant
     * @return liste des EPIs attribués
     */
    public List<Epi> getEpisParIntervenant(int intervenantId) {
        return epiDAO.findByIntervenantId(intervenantId);
    }

    /**
     * Retourne tous les EPIs enregistrés.
     *
     * @return liste complète des EPIs
     */
    public List<Epi> getTousEpis() {
        return epiDAO.findAll();
    }

    /**
     * Vérifie si un intervenant possède tous les EPIs obligatoires (casque + gilet).
     *
     * @param intervenantId identifiant de l'intervenant
     * @return true si l'intervenant a casque et gilet
     */
    public boolean aEpisObligatoires(int intervenantId) {
        return epiDAO.hasEpi(intervenantId, Epi.TypeEpi.CASQUE)
                && epiDAO.hasEpi(intervenantId, Epi.TypeEpi.GILET);
    }
}
