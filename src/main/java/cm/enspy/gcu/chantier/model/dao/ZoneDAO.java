package cm.enspy.gcu.chantier.model.dao;

import cm.enspy.gcu.chantier.model.entities.Zone;
import cm.enspy.gcu.chantier.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entité Zone.
 */
public class ZoneDAO {

    /**
     * Sauvegarde une nouvelle zone.
     *
     * @param zone la zone à sauvegarder
     * @return la zone avec son identifiant généré
     * @throws DAOException en cas d'erreur SQL
     */
    public Zone save(Zone zone) {
        String sql = """
                INSERT INTO zones (nom, description, niveau_securite, capacite_max, est_active)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, zone.getNom());
            ps.setString(2, zone.getDescription());
            ps.setInt(3, zone.getNiveauSecurite());
            ps.setInt(4, zone.getCapaciteMax());
            ps.setBoolean(5, zone.isEstActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) zone.setId(rs.getInt(1));
            }
            return zone;
        } catch (SQLException e) {
            throw new DAOException("Erreur sauvegarde zone: " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour une zone existante.
     *
     * @param zone la zone à mettre à jour
     * @throws DAOException en cas d'erreur SQL
     */
    public void update(Zone zone) {
        String sql = """
                UPDATE zones SET nom=?, description=?, niveau_securite=?, capacite_max=?, est_active=?
                WHERE id=?
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, zone.getNom());
            ps.setString(2, zone.getDescription());
            ps.setInt(3, zone.getNiveauSecurite());
            ps.setInt(4, zone.getCapaciteMax());
            ps.setBoolean(5, zone.isEstActive());
            ps.setInt(6, zone.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur mise à jour zone: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne toutes les zones actives.
     *
     * @return liste des zones actives
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Zone> findAll() {
        String sql = "SELECT * FROM zones ORDER BY niveau_securite, nom";
        List<Zone> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération zones: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne uniquement les zones actives.
     *
     * @return liste des zones avec est_active = 1
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Zone> findActives() {
        String sql = "SELECT * FROM zones WHERE est_active = 1 ORDER BY niveau_securite, nom";
        List<Zone> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération zones actives: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Trouve une zone par son identifiant.
     *
     * @param id identifiant de la zone
     * @return Optional contenant la zone
     * @throws DAOException en cas d'erreur SQL
     */
    public Optional<Zone> findById(int id) {
        String sql = "SELECT * FROM zones WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur recherche zone: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Mappe une ligne ResultSet vers une Zone.
     *
     * @param rs ResultSet positionné
     * @return instance Zone construite
     * @throws SQLException en cas d'erreur de lecture
     */
    private Zone mapRow(ResultSet rs) throws SQLException {
        Zone z = new Zone();
        z.setId(rs.getInt("id"));
        z.setNom(rs.getString("nom"));
        z.setDescription(rs.getString("description"));
        z.setNiveauSecurite(rs.getInt("niveau_securite"));
        z.setCapaciteMax(rs.getInt("capacite_max"));
        z.setEstActive(rs.getBoolean("est_active"));
        return z;
    }
}
