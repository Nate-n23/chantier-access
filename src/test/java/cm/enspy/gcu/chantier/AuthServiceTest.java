package cm.enspy.gcu.chantier;

import cm.enspy.gcu.chantier.config.DatabaseConfig;
import cm.enspy.gcu.chantier.model.dao.UtilisateurDAO;
import cm.enspy.gcu.chantier.model.entities.UtilisateurSysteme;
import cm.enspy.gcu.chantier.service.AuthService;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.HashUtil;
import cm.enspy.gcu.chantier.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour AuthService.
 * Vérifient la connexion et le rejet des mauvais identifiants.
 */
class AuthServiceTest {

    private AuthService authService;
    private UtilisateurDAO utilisateurDAO;

    /**
     * Initialise la base en mémoire et crée un utilisateur de test.
     */
    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager db = DatabaseManager.getInstance();
        db.reset();
        db.setJdbcUrl(DatabaseConfig.JDBC_URL_MEMORY);
        db.initialize();

        utilisateurDAO = new UtilisateurDAO();
        authService = new AuthService(utilisateurDAO, SessionManager.getInstance());

        // Créer un utilisateur de test
        String hashMdp = HashUtil.sha256("TestPass1");
        UtilisateurSysteme testUser = new UtilisateurSysteme("testuser", hashMdp, UtilisateurSysteme.Role.AGENT);
        testUser.setEstActif(true);
        utilisateurDAO.save(testUser);
    }

    /**
     * Test 5: Des identifiants corrects doivent retourner l'utilisateur connecté.
     */
    @Test
    void login_credentialsCorrects_retourneUtilisateur() {
        UtilisateurSysteme user = authService.login("testuser", "TestPass1");

        assertNotNull(user, "L'utilisateur ne doit pas être null avec des identifiants corrects");
        assertEquals("testuser", user.getLogin());
        assertEquals(UtilisateurSysteme.Role.AGENT, user.getRole());
        assertTrue(SessionManager.getInstance().isConnecte());
    }

    /**
     * Test 6: Un mauvais mot de passe doit retourner null.
     */
    @Test
    void login_mauvaisMotDePasse_retourneNull() {
        UtilisateurSysteme user = authService.login("testuser", "MauvaisMdp999");

        assertNull(user, "Un mot de passe incorrect doit retourner null");
    }
}
