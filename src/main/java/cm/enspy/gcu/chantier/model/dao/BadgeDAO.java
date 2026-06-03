package cm.enspy.gcu.chantier.model.dao;

import cm.enspy.gcu.chantier.model.entities.Badge;
import cm.enspy.gcu.chantier.model.entities.Zone;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entité Badge.
 * Gère la persistance des badges et leurs associations avec les zones.
 */
public class BadgeDAO {

    /**
     * Sauvegarde un nouveau badge en base de données.
     *
     * @param badge le badge à sauvegarder
     * @return le badge avec son identifiant généré
     * @throws DAOException en cas d'erreur SQL
     */
    public Badge save(Badge badge) {
        String sql = """
                INSERT INTO badges (code, type_badge, date_creation, date_expiration,
                est_actif, intervenant_id) VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, badge.getCode());
            ps.setString(2, badge.getTypeBadge() != null ? badge.getTypeBadge().name() : null);
            ps.setString(3, badge.getDateCreation() != null ? badge.getDateCreation().toString() : null);
            ps.setString(4, badge.getDateExpiration() != null ? badge.getDateExpiration().toString() : null);
            ps.setBoolean(5, badge.isEstActif());
            ps.setInt(6, badge.getIntervenantId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) badge.setId(rs.getInt(1));
            }
            return badge;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la sauvegarde du badge: " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour un badge existant.
     *
     * @param badge le badge à mettre à jour
     * @throws DAOException en cas d'erreur SQL
     */
    public void update(Badge badge) {
        String sql = """
                UPDATE badges SET code=?, type_badge=?, date_expiration=?, est_actif=?, intervenant_id=?
                WHERE id=?
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, badge.getCode());
            ps.setString(2, badge.getTypeBadge() != null ? badge.getTypeBadge().name() : null);
            ps.setString(3, badge.getDateExpiration() != null ? badge.getDateExpiration().toString() : null);
            ps.setBoolean(4, badge.isEstActif());
            ps.setInt(5, badge.getIntervenantId());
            ps.setInt(6, badge.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour du badge: " + e.getMessage(), e);
        }
    }

    /**
     * Trouve un badge par son code.
     *
     * @param code code du badge à chercher
     * @return Optional contenant le badge trouvé
     * @throws DAOException en cas d'erreur SQL
     */
    public Optional<Badge> findByCode(String code) {
        String sql = """
                SELECT b.*, i.nom, i.prenom FROM badges b
                LEFT JOIN intervenants i ON b.intervenant_id = i.id
                WHERE b.code = ?
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du badge: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Trouve un badge par son identifiant.
     *
     * @param id identifiant du badge
     * @return Optional contenant le badge, ou vide si non trouvé
     * @throws DAOException en cas d'erreur SQL
     */
    public Optional<Badge> findById(int id) {
        String sql = "SELECT b.* FROM badges b WHERE b.id = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du badge par ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Retourne tous les badges.
     *
     * @return liste de tous les badges
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Badge> findAll() {
        String sql = """
                SELECT b.*, i.nom, i.prenom FROM badges b
                LEFT JOIN intervenants i ON b.intervenant_id = i.id
                ORDER BY b.id DESC
                """;
        List<Badge> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des badges: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne les badges d'un intervenant donné.
     *
     * @param intervenantId identifiant de l'intervenant
     * @return liste des badges de l'intervenant
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Badge> findByIntervenantId(int intervenantId) {
        String sql = "SELECT b.* FROM badges b WHERE b.intervenant_id = ? ORDER BY b.id DESC";
        List<Badge> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, intervenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des badges de l'intervenant: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne les badges qui expirent dans les prochains jours.
     *
     * @param jours nombre de jours
     * @return liste des badges expirant bientôt
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Badge> findExpirantDans(int jours) {
        String sql = """
                SELECT b.*, i.nom, i.prenom FROM badges b
                LEFT JOIN intervenants i ON b.intervenant_id = i.id
                WHERE b.est_actif = 1
                AND b.date_expiration IS NOT NULL
                AND date(b.date_expiration) <= date('now', '+' || ? || ' days')
                AND date(b.date_expiration) >= date('now')
                """;
        List<Badge> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, jours);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur badges expirant: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Associe un badge à une zone autorisée.
     *
     * @param badgeId identifiant du badge
     * @param zoneId  identifiant de la zone
     * @throws DAOException en cas d'erreur SQL
     */
    public void ajouterZone(int badgeId, int zoneId) {
        String sql = "INSERT OR IGNORE INTO badges_zones (badge_id, zone_id) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, badgeId);
            ps.setInt(2, zoneId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de l'ajout de la zone au badge: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime toutes les associations de zones pour un badge.
     *
     * @param badgeId identifiant du badge
     * @throws DAOException en cas d'erreur SQL
     */
    public void supprimerZones(int badgeId) {
        String sql = "DELETE FROM badges_zones WHERE badge_id = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, badgeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression des zones: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne les zones autorisées pour un badge donné.
     *
     * @param badgeId identifiant du badge
     * @return liste des zones autorisées
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Zone> findZonesForBadge(int badgeId) {
        String sql = """
                SELECT z.* FROM zones z
                INNER JOIN badges_zones bz ON z.id = bz.zone_id
                WHERE bz.badge_id = ?
                """;
        List<Zone> zones = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, badgeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Zone z = new Zone();
                    z.setId(rs.getInt("id"));
                    z.setNom(rs.getString("nom"));
                    z.setNiveauSecurite(rs.getInt("niveau_securite"));
                    zones.add(z);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des zones du badge: " + e.getMessage(), e);
        }
        return zones;
    }

    /**
     * Révoque un badge (le désactive).
     *
     * @param badgeId identifiant du badge à révoquer
     * @throws DAOException en cas d'erreur SQL
     */
    public void revoquerBadge(int badgeId) {
        String sql = "UPDATE badges SET est_actif = 0 WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, badgeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la révocation du badge: " + e.getMessage(), e);
        }
    }

    /**
     * Mappe une ligne ResultSet vers un objet Badge.
     *
     * @param rs ResultSet positionné sur la ligne
     * @return instance Badge construite
     * @throws SQLException en cas d'erreur de lecture
     */
    private Badge mapRow(ResultSet rs) throws SQLException {
        Badge b = new Badge();
        b.setId(rs.getInt("id"));
        b.setCode(rs.getString("code"));
        String type = rs.getString("type_badge");
        if (type != null) b.setTypeBadge(Badge.TypeBadge.valueOf(type));
        String dc = rs.getString("date_creation");
        if (dc != null) b.setDateCreation(DateUtils.parseDateTime(dc));
        String de = rs.getString("date_expiration");
        if (de != null) b.setDateExpiration(DateUtils.parseDate(de));
        b.setEstActif(rs.getBoolean("est_actif"));
        b.setIntervenantId(rs.getInt("intervenant_id"));

        // Mapping des infos intervenant si présentes (jointure)
        try {
            String nom = rs.getString("nom");
            if (nom != null) {
                cm.enspy.gcu.chantier.model.entities.Intervenant i = new cm.enspy.gcu.chantier.model.entities.Intervenant();
                i.setId(b.getIntervenantId());
                i.setNom(nom);
                i.setPrenom(rs.getString("prenom"));
                b.setIntervenant(i);
            }
        } catch (SQLException ignored) {
            // Pas de jointure intervenant dans cette requête
        }

        return b;
    }

}
