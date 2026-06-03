package cm.enspy.gcu.chantier;

import cm.enspy.gcu.chantier.config.DatabaseConfig;
import cm.enspy.gcu.chantier.model.dao.AccesDAO;
import cm.enspy.gcu.chantier.model.dao.BadgeDAO;
import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.dao.ZoneDAO;
import cm.enspy.gcu.chantier.model.entities.*;
import cm.enspy.gcu.chantier.service.AccesService;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour AccesService.
 * Utilise une base de données SQLite en mémoire.
 */
class AccesServiceTest {

    private AccesService accesService;
    private BadgeDAO badgeDAO;
    private IntervenantDAO intervenantDAO;
    private ZoneDAO zoneDAO;

    /**
     * Initialise une base en mémoire et le schéma avant chaque test.
     */
    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager db = DatabaseManager.getInstance();
        db.reset();
        db.setJdbcUrl(DatabaseConfig.JDBC_URL_MEMORY);
        db.initialize();

        badgeDAO = new BadgeDAO();
        intervenantDAO = new IntervenantDAO();
        zoneDAO = new ZoneDAO();
        accesService = new AccesService(new AccesDAO(), badgeDAO, intervenantDAO, zoneDAO);
    }

    /**
     * Test 3: Un accès avec badge valide et zone autorisée doit être enregistré comme AUTORISE.
     */
    @Test
    void enregistrer_badgeValideZoneAutorisee_accesEnregistre() {
        // Arrange — créer intervenant, zone, badge
        Intervenant intervenant = new Intervenant("MAT001", "Dupont", "Jean", "BTP SA",
                Intervenant.Categorie.OUVRIER, 2);
        intervenantDAO.save(intervenant);

        Zone zone = new Zone("Entrée test", "Zone test", 1, 50);
        zoneDAO.save(zone);

        Badge badge = new Badge();
        badge.setCode("VALID001");
        badge.setTypeBadge(Badge.TypeBadge.PERMANENT);
        badge.setEstActif(true);
        badge.setDateExpiration(LocalDate.now().plusDays(365));
        badge.setDateCreation(LocalDateTime.now());
        badge.setIntervenantId(intervenant.getId());
        badgeDAO.save(badge);
        badgeDAO.ajouterZone(badge.getId(), zone.getId());

        // Act
        AccesService.ResultatAcces resultat = accesService.enregistrer(
                "VALID001", zone.getId(), Acces.TypeAcces.ENTREE);

        // Assert
        assertTrue(resultat.autorise(), "L'accès doit être autorisé");
        assertTrue(resultat.message().contains("Bienvenue"));
        assertNotNull(resultat.intervenant());
    }

    /**
     * Test 4: Un accès avec une zone non habilitée dans le badge doit être refusé.
     */
    @Test
    void enregistrer_zoneNonHabilitee_accesRefuse() {
        // Arrange
        Intervenant intervenant = new Intervenant("MAT002", "Martin", "Paul", "ACME",
                Intervenant.Categorie.TECHNICIEN, 3);
        intervenantDAO.save(intervenant);

        Zone zoneAutorisee = new Zone("Zone A", "Access A", 1, 50);
        zoneDAO.save(zoneAutorisee);
        Zone zoneNonAutorisee = new Zone("Zone B", "Access B", 2, 30);
        zoneDAO.save(zoneNonAutorisee);

        Badge badge = new Badge();
        badge.setCode("BADGE002");
        badge.setTypeBadge(Badge.TypeBadge.PERMANENT);
        badge.setEstActif(true);
        badge.setDateExpiration(LocalDate.now().plusDays(365));
        badge.setDateCreation(LocalDateTime.now());
        badge.setIntervenantId(intervenant.getId());
        badgeDAO.save(badge);
        // N'autorise QUE zoneAutorisee
        badgeDAO.ajouterZone(badge.getId(), zoneAutorisee.getId());

        // Act — tente d'accéder à zoneNonAutorisee
        AccesService.ResultatAcces resultat = accesService.enregistrer(
                "BADGE002", zoneNonAutorisee.getId(), Acces.TypeAcces.ENTREE);

        // Assert
        assertFalse(resultat.autorise(), "L'accès à une zone non habilitée doit être refusé");
        assertTrue(resultat.message().contains("Accès refusé"));
    }
}
