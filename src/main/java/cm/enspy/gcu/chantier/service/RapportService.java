package cm.enspy.gcu.chantier.service;

import cm.enspy.gcu.chantier.model.dao.AccesDAO;
import cm.enspy.gcu.chantier.model.dao.AlerteDAO;
import cm.enspy.gcu.chantier.model.dao.BadgeDAO;
import cm.enspy.gcu.chantier.model.entities.Acces;
import cm.enspy.gcu.chantier.model.entities.Alerte;
import cm.enspy.gcu.chantier.utils.DateUtils;
import cm.enspy.gcu.chantier.utils.ExportUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service de génération des rapports d'activité du chantier.
 * Prend en charge les rapports journaliers, hebdomadaires, mensuels et d'incidents.
 */
public class RapportService {

    private static final Logger LOGGER = Logger.getLogger(RapportService.class.getName());

    private final AccesDAO accesDAO;
    private final AlerteDAO alerteDAO;
    private final BadgeDAO badgeDAO;

    /** Constructeur par défaut. */
    public RapportService() {
        this.accesDAO = new AccesDAO();
        this.alerteDAO = new AlerteDAO();
        this.badgeDAO = new BadgeDAO();
    }

    /**
     * Constructeur pour les tests.
     *
     * @param accesDAO   DAO accès
     * @param alerteDAO  DAO alerte
     * @param badgeDAO   DAO badge
     */
    public RapportService(AccesDAO accesDAO, AlerteDAO alerteDAO, BadgeDAO badgeDAO) {
        this.accesDAO = accesDAO;
        this.alerteDAO = alerteDAO;
        this.badgeDAO = badgeDAO;
    }

    /**
     * Génère un rapport journalier pour la date donnée.
     *
     * @param date date du rapport
     * @return liste des accès de la journée
     */
    public List<Acces> genererRapportJournalier(LocalDate date) {
        return accesDAO.findByPeriode(date, date);
    }

    /**
     * Génère un rapport hebdomadaire pour la semaine contenant la date donnée.
     *
     * @param date date de référence (n'importe quel jour de la semaine)
     * @return liste des accès de la semaine
     */
    public List<Acces> genererRapportHebdomadaire(LocalDate date) {
        LocalDate debut = date.minusDays(date.getDayOfWeek().getValue() - 1);
        LocalDate fin = debut.plusDays(6);
        return accesDAO.findByPeriode(debut, fin);
    }

    /**
     * Génère un rapport mensuel pour le mois de la date donnée.
     *
     * @param date date de référence
     * @return liste des accès du mois
     */
    public List<Acces> genererRapportMensuel(LocalDate date) {
        LocalDate debut = date.withDayOfMonth(1);
        LocalDate fin = date.withDayOfMonth(date.lengthOfMonth());
        return accesDAO.findByPeriode(debut, fin);
    }

    /**
     * Génère un rapport d'incidents (accès refusés) sur une période.
     *
     * @param debut date de début
     * @param fin   date de fin
     * @return liste des accès refusés
     */
    public List<Acces> genererRapportIncidents(LocalDate debut, LocalDate fin) {
        return accesDAO.findRefusesByPeriode(debut, fin);
    }

    /**
     * Formate un résumé textuel d'un rapport pour affichage dans la TextArea.
     *
     * @param acces liste des accès à résumer
     * @param titre titre du rapport
     * @return chaîne de texte formatée
     */
    public String formaterResume(List<Acces> acces, String titre) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(titre).append(" ===\n");
        sb.append("Généré le: ").append(DateUtils.formaterDateHeure(java.time.LocalDateTime.now())).append("\n");
        sb.append("Nombre total d'accès: ").append(acces.size()).append("\n");
        long autorises = acces.stream().filter(a -> a.getStatut() == Acces.StatutAcces.AUTORISE).count();
        long refuses = acces.stream().filter(a -> a.getStatut() == Acces.StatutAcces.REFUSE).count();
        sb.append("Accès autorisés: ").append(autorises).append("\n");
        sb.append("Accès refusés: ").append(refuses).append("\n\n");
        sb.append("DÉTAIL DES PASSAGES:\n");
        sb.append("-".repeat(80)).append("\n");
        for (Acces a : acces) {
            sb.append(String.format("[%s] %-25s %-20s %-8s %s%n",
                    DateUtils.formaterDateHeure(a.getDateHeure()),
                    a.getIntervenantNom() != null ? a.getIntervenantNom() : "N/A",
                    a.getZoneNom() != null ? a.getZoneNom() : "N/A",
                    a.getType() != null ? a.getType().name() : "N/A",
                    a.getStatut() != null ? a.getStatut().name() : "N/A"));
            if (a.getMotifRefus() != null && !a.getMotifRefus().isBlank()) {
                sb.append("    Motif: ").append(a.getMotifRefus()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Exporte un rapport en PDF.
     *
     * @param acces      liste des accès
     * @param titre      titre du rapport
     * @param nomFichier nom du fichier sans extension
     * @return chemin absolu du fichier PDF créé
     * @throws IOException en cas d'erreur d'écriture
     */
    public String exporterPDF(List<Acces> acces, String titre, String nomFichier) throws IOException {
        List<String> lignes = new ArrayList<>();
        lignes.add("Total: " + acces.size() + " accès");
        long autorises = acces.stream().filter(a -> a.getStatut() == Acces.StatutAcces.AUTORISE).count();
        long refuses = acces.stream().filter(a -> a.getStatut() == Acces.StatutAcces.REFUSE).count();
        lignes.add("Autorisés: " + autorises + " | Refusés: " + refuses);
        lignes.add(" ");
        lignes.add("DÉTAIL:");
        lignes.add("-".repeat(70));
        for (Acces a : acces) {
            lignes.add(String.format("%s | %s | %s | %s | %s",
                    DateUtils.formaterDateHeure(a.getDateHeure()),
                    a.getIntervenantNom() != null ? a.getIntervenantNom() : "N/A",
                    a.getZoneNom() != null ? a.getZoneNom() : "N/A",
                    a.getType() != null ? a.getType().name() : "N/A",
                    a.getStatut() != null ? a.getStatut().name() : "N/A"));
            if (a.getMotifRefus() != null && !a.getMotifRefus().isBlank()) {
                lignes.add("  -> Motif refus: " + a.getMotifRefus());
            }
        }
        return ExportUtil.exporterPDF(titre, lignes, nomFichier);
    }

    /**
     * Exporte un rapport en CSV.
     *
     * @param acces      liste des accès
     * @param nomFichier nom du fichier sans extension
     * @return chemin absolu du fichier CSV créé
     * @throws IOException en cas d'erreur d'écriture
     */
    public String exporterCSV(List<Acces> acces, String nomFichier) throws IOException {
        String[] entetes = {"Date/Heure", "Intervenant", "Zone", "Badge", "Type", "Statut", "Motif refus"};
        List<String[]> donnees = new ArrayList<>();
        for (Acces a : acces) {
            donnees.add(new String[]{
                    DateUtils.formaterDateHeure(a.getDateHeure()),
                    a.getIntervenantNom() != null ? a.getIntervenantNom() : "",
                    a.getZoneNom() != null ? a.getZoneNom() : "",
                    a.getBadgeCode() != null ? a.getBadgeCode() : "",
                    a.getType() != null ? a.getType().name() : "",
                    a.getStatut() != null ? a.getStatut().name() : "",
                    a.getMotifRefus() != null ? a.getMotifRefus() : ""
            });
        }
        return ExportUtil.exporterCSV(entetes, donnees, nomFichier);
    }
}
