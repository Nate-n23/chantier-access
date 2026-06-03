package cm.enspy.gcu.chantier.model.dao;

import cm.enspy.gcu.chantier.model.entities.UtilisateurSysteme;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entité UtilisateurSysteme.
 */
public class UtilisateurDAO {

    /**
     * Sauvegarde un nouvel utilisateur système.
     *
     * @param utilisateur l'utilisateur à sauvegarder
     * @return l'utilisateur avec son identifiant généré
     * @throws DAOException en cas d'erreur SQL
     */
    public UtilisateurSysteme save(UtilisateurSysteme utilisateur) {
        String sql = """
                INSERT INTO utilisateurs_systeme (login, mot_de_passe_hash, role, est_actif)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilisateur.getLogin());
            ps.setString(2, utilisateur.getMotDePasseHash());
            ps.setString(3, utilisateur.getRole() != null ? utilisateur.getRole().name() : null);
            ps.setBoolean(4, utilisateur.isEstActif());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) utilisateur.setId(rs.getInt(1));
            }
            return utilisateur;
        } catch (SQLException e) {
            throw new DAOException("Erreur sauvegarde utilisateur: " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour un utilisateur existant.
     *
     * @param utilisateur l'utilisateur à mettre à jour
     * @throws DAOException en cas d'erreur SQL
     */
    public void update(UtilisateurSysteme utilisateur) {
        String sql = """
                UPDATE utilisateurs_systeme SET login=?, mot_de_passe_hash=?, role=?, est_actif=?
                WHERE id=?
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, utilisateur.getLogin());
            ps.setString(2, utilisateur.getMotDePasseHash());
            ps.setString(3, utilisateur.getRole() != null ? utilisateur.getRole().name() : null);
            ps.setBoolean(4, utilisateur.isEstActif());
            ps.setInt(5, utilisateur.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur mise à jour utilisateur: " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour l'horodatage de la dernière connexion.
     *
     * @param userId identifiant de l'utilisateur
     * @throws DAOException en cas d'erreur SQL
     */
    public void updateDerniereConnexion(int userId) {
        String sql = "UPDATE utilisateurs_systeme SET derniere_connexion = datetime('now') WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur mise à jour dernière connexion: " + e.getMessage(), e);
        }
    }

    /**
     * Trouve un utilisateur par son login.
     *
     * @param login identifiant de connexion
     * @return Optional contenant l'utilisateur
     * @throws DAOException en cas d'erreur SQL
     */
    public Optional<UtilisateurSysteme> findByLogin(String login) {
        String sql = "SELECT * FROM utilisateurs_systeme WHERE login = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur recherche par login: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Retourne tous les utilisateurs système.
     *
     * @return liste de tous les utilisateurs
     * @throws DAOException en cas d'erreur SQL
     */
    public List<UtilisateurSysteme> findAll() {
        String sql = "SELECT * FROM utilisateurs_systeme ORDER BY login";
        List<UtilisateurSysteme> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération utilisateurs: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Mappe une ligne ResultSet vers un UtilisateurSysteme.
     *
     * @param rs ResultSet positionné
     * @return instance UtilisateurSysteme construite
     * @throws SQLException en cas d'erreur de lecture
     */
    private UtilisateurSysteme mapRow(ResultSet rs) throws SQLException {
        UtilisateurSysteme u = new UtilisateurSysteme();
        u.setId(rs.getInt("id"));
        u.setLogin(rs.getString("login"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        String role = rs.getString("role");
        if (role != null) u.setRole(UtilisateurSysteme.Role.valueOf(role));
        u.setEstActif(rs.getBoolean("est_actif"));
        String dc = rs.getString("derniere_connexion");
        if (dc != null) u.setDerniereConnexion(DateUtils.parseDateTime(dc));
        return u;
    }
}
