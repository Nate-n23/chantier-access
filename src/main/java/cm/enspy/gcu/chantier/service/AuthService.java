package cm.enspy.gcu.chantier.service;

import cm.enspy.gcu.chantier.model.dao.UtilisateurDAO;
import cm.enspy.gcu.chantier.model.entities.UtilisateurSysteme;
import cm.enspy.gcu.chantier.utils.HashUtil;
import cm.enspy.gcu.chantier.utils.SessionManager;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service d'authentification et de gestion des sessions.
 * Gère la connexion, la déconnexion et la validation des mots de passe.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static final Pattern MDP_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UtilisateurDAO utilisateurDAO;
    private final SessionManager sessionManager;

    /**
     * Constructeur par défaut utilisant le SessionManager singleton.
     */
    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Constructeur pour les tests avec injection de dépendances.
     *
     * @param utilisateurDAO DAO utilisateur à injecter
     * @param sessionManager gestionnaire de session à injecter
     */
    public AuthService(UtilisateurDAO utilisateurDAO, SessionManager sessionManager) {
        this.utilisateurDAO = utilisateurDAO;
        this.sessionManager = sessionManager;
    }

    /**
     * Tente de connecter un utilisateur avec ses identifiants.
     * Vérifie le login, le mot de passe, et que le compte est actif.
     *
     * @param login      identifiant de connexion
     * @param motDePasse mot de passe en clair
     * @return l'utilisateur connecté, ou null si les identifiants sont incorrects
     */
    public UtilisateurSysteme login(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            return null;
        }
        Optional<UtilisateurSysteme> optUser = utilisateurDAO.findByLogin(login);
        if (optUser.isEmpty()) {
            LOGGER.warning("Tentative de connexion avec login inconnu: " + login);
            return null;
        }
        UtilisateurSysteme user = optUser.get();
        if (!user.isEstActif()) {
            LOGGER.warning("Tentative de connexion sur compte inactif: " + login);
            return null;
        }
        String hashSaisi = HashUtil.sha256(motDePasse);
        if (!hashSaisi.equals(user.getMotDePasseHash())) {
            LOGGER.warning("Mot de passe incorrect pour: " + login);
            return null;
        }
        // Succès: démarrer la session et mettre à jour la dernière connexion
        sessionManager.connect(user);
        utilisateurDAO.updateDerniereConnexion(user.getId());
        LOGGER.info("Connexion réussie pour: " + login);
        return user;
    }

    /**
     * Déconnecte l'utilisateur courant.
     */
    public void logout() {
        sessionManager.deconnect();
    }

    /**
     * Valide la complexité d'un mot de passe.
     *
     * @param motDePasse mot de passe à valider
     * @return true si le mot de passe respecte la politique (8+ chars, 1 maj, 1 chiffre)
     */
    public boolean validerMotDePasse(String motDePasse) {
        if (motDePasse == null) return false;
        return MDP_PATTERN.matcher(motDePasse).matches();
    }

    /**
     * Crée un nouveau compte utilisateur avec hachage du mot de passe.
     *
     * @param login      identifiant unique
     * @param motDePasse mot de passe en clair (sera haché)
     * @param role       rôle attribué
     * @return l'utilisateur créé
     * @throws IllegalArgumentException si le mot de passe ne respecte pas la politique
     */
    public UtilisateurSysteme creerUtilisateur(String login, String motDePasse, UtilisateurSysteme.Role role) {
        if (!validerMotDePasse(motDePasse)) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre.");
        }
        String hashMdp = HashUtil.sha256(motDePasse);
        UtilisateurSysteme user = new UtilisateurSysteme(login, hashMdp, role);
        return utilisateurDAO.save(user);
    }

    /**
     * Modifie le mot de passe d'un utilisateur.
     *
     * @param userId        identifiant de l'utilisateur
     * @param nouveauMdp    nouveau mot de passe en clair
     * @throws IllegalArgumentException si le mot de passe ne respecte pas la politique
     */
    public void changerMotDePasse(int userId, String nouveauMdp) {
        if (!validerMotDePasse(nouveauMdp)) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre.");
        }
        // Récupère et met à jour seulement le hash
        utilisateurDAO.findAll().stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .ifPresent(u -> {
                    u.setMotDePasseHash(HashUtil.sha256(nouveauMdp));
                    utilisateurDAO.update(u);
                });
    }
}
