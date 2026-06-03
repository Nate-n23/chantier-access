package cm.enspy.gcu.chantier;

import cm.enspy.gcu.chantier.config.DatabaseConfig;
import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.entities.Intervenant;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour IntervenantDAO.
 * Vérifient la persistance des intervenants en base de données.
 */
class IntervenantDAOTest {

    private IntervenantDAO intervenantDAO;

    /**
     * Initialise la base en mémoire et le schéma avant chaque test.
     */
    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager db = DatabaseManager.getInstance();
        db.reset();
        db.setJdbcUrl(DatabaseConfig.JDBC_URL_MEMORY);
        db.initialize();
        intervenantDAO = new IntervenantDAO();
    }

    /**
     * Test 7: La sauvegarde d'un intervenant complet doit générer un identifiant positif.
     */
    @Test
    void save_intervenantComplet_idGenerePositif() {
        Intervenant intervenant = new Intervenant();
        intervenant.setMatricule("MAT-TEST-001");
        intervenant.setNom("Kamga");
        intervenant.setPrenom("Jean-Baptiste");
        intervenant.setDateNaissance(LocalDate.of(1985, 6, 15));
        intervenant.setTelephone("+237 691 234 567");
        intervenant.setEmail("jb.kamga@btp.cm");
        intervenant.setEntreprise("BTP Cameroun SA");
        intervenant.setCategorie(Intervenant.Categorie.INGENIEUR);
        intervenant.setStatut(Intervenant.Statut.ACTIF);
        intervenant.setNiveauHabilitation(3);

        Intervenant saved = intervenantDAO.save(intervenant);

        assertTrue(saved.getId() > 0, "L'identifiant généré doit être positif");
        assertEquals("MAT-TEST-001", saved.getMatricule());
        assertEquals("Kamga", saved.getNom());
        assertEquals(Intervenant.Categorie.INGENIEUR, saved.getCategorie());
    }
}
