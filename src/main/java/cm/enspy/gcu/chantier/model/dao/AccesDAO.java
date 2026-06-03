package cm.enspy.gcu.chantier.model.dao;

import cm.enspy.gcu.chantier.model.entities.Acces;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.DateUtils;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour l'entité Acces (enregistrements de passages).
 */
public class AccesDAO {

    /**
     * Sauvegarde un enregistrement d'accès.
     *
     * @param acces l'accès à sauvegarder
     * @return l'accès avec son identifiant généré
     * @throws DAOException en cas d'erreur SQL
     */
    public Acces save(Acces acces) {
        String sql = """
                INSERT INTO acces_zones
                (intervenant_id, badge_id, zone_id, date_heure, type, statut, motif_refus, agent_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, acces.getIntervenantId());
            ps.setInt(2, acces.getBadgeId());
            ps.setInt(3, acces.getZoneId());
            ps.setString(4, acces.getDateHeure() != null ? acces.getDateHeure().toString() : null);
            ps.setString(5, acces.getType() != null ? acces.getType().name() : null);
            ps.setString(6, acces.getStatut() != null ? acces.getStatut().name() : null);
            ps.setString(7, acces.getMotifRefus());
            if (acces.getAgentId() != null) {
                ps.setInt(8, acces.getAgentId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) acces.setId(rs.getInt(1));
            }
            return acces;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la sauvegarde de l'accès: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne les N derniers accès toutes zones confondues.
     *
     * @param limite nombre maximum de résultats
     * @return liste des derniers accès
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Acces> findDerniers(int limite) {
        String sql = """
                SELECT az.*, i.nom || ' ' || i.prenom as intervenant_nom, z.nom as zone_nom, b.code as badge_code
                FROM acces_zones az
                LEFT JOIN intervenants i ON az.intervenant_id = i.id
                LEFT JOIN zones z ON az.zone_id = z.id
                LEFT JOIN badges b ON az.badge_id = b.id
                ORDER BY az.date_heure DESC
                LIMIT ?
                """;
        List<Acces> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des derniers accès: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne les accès d'un intervenant donné.
     *
     * @param intervenantId identifiant de l'intervenant
     * @return liste des accès de l'intervenant
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Acces> findByIntervenantId(int intervenantId) {
        String sql = """
                SELECT az.*, i.nom || ' ' || i.prenom as intervenant_nom, z.nom as zone_nom, b.code as badge_code
                FROM acces_zones az
                LEFT JOIN intervenants i ON az.intervenant_id = i.id
                LEFT JOIN zones z ON az.zone_id = z.id
                LEFT JOIN badges b ON az.badge_id = b.id
                WHERE az.intervenant_id = ?
                ORDER BY az.date_heure DESC
                LIMIT 50
                """;
        List<Acces> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, intervenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération accès intervenant: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne les présents actuellement dans le chantier.
     * Un intervenant est présent si sa dernière action "ENTREE" dans une zone
     * n'est pas suivie d'une "SORTIE".
     *
     * @return liste des accès ENTREE sans SORTIE correspondante
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Acces> findPresents() {
        String sql = """
                SELECT DISTINCT az.*, i.nom || ' ' || i.prenom as intervenant_nom,
                z.nom as zone_nom, b.code as badge_code
                FROM acces_zones az
                LEFT JOIN intervenants i ON az.intervenant_id = i.id
                LEFT JOIN zones z ON az.zone_id = z.id
                LEFT JOIN badges b ON az.badge_id = b.id
                WHERE az.type = 'ENTREE' AND az.statut = 'AUTORISE'
                AND NOT EXISTS (
                    SELECT 1 FROM acces_zones az2
                    WHERE az2.intervenant_id = az.intervenant_id
                    AND az2.zone_id = az.zone_id
                    AND az2.type = 'SORTIE'
                    AND az2.date_heure > az.date_heure
                )
                AND date(az.date_heure) = date('now')
                ORDER BY az.date_heure DESC
                """;
        List<Acces> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des présents: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne le nombre d'accès ENTREE AUTORISE pour aujourd'hui.
     *
     * @return nombre de présents aujourd'hui
     * @throws DAOException en cas d'erreur SQL
     */
    public int countPresentsAujourdhui() {
        String sql = """
                SELECT COUNT(DISTINCT intervenant_id) FROM acces_zones
                WHERE type = 'ENTREE' AND statut = 'AUTORISE'
                AND date(date_heure) = date('now')
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DAOException("Erreur comptage présents: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Retourne le nombre d'accès refusés aujourd'hui.
     *
     * @return nombre d'incidents aujourd'hui
     * @throws DAOException en cas d'erreur SQL
     */
    public int countIncidentsAujourdhui() {
        String sql = """
                SELECT COUNT(*) FROM acces_zones
                WHERE statut = 'REFUSE'
                AND date(date_heure) = date('now')
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DAOException("Erreur comptage incidents: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Retourne les accès sur une période donnée.
     *
     * @param debut date de début
     * @param fin   date de fin
     * @return liste des accès sur la période
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Acces> findByPeriode(LocalDate debut, LocalDate fin) {
        String sql = """
                SELECT az.*, i.nom || ' ' || i.prenom as intervenant_nom, z.nom as zone_nom, b.code as badge_code
                FROM acces_zones az
                LEFT JOIN intervenants i ON az.intervenant_id = i.id
                LEFT JOIN zones z ON az.zone_id = z.id
                LEFT JOIN badges b ON az.badge_id = b.id
                WHERE date(az.date_heure) BETWEEN ? AND ?
                ORDER BY az.date_heure DESC
                """;
        List<Acces> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, debut.toString());
            ps.setString(2, fin.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération par période: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne les accès refusés sur une période.
     *
     * @param debut date de début
     * @param fin   date de fin
     * @return liste des accès refusés
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Acces> findRefusesByPeriode(LocalDate debut, LocalDate fin) {
        String sql = """
                SELECT az.*, i.nom || ' ' || i.prenom as intervenant_nom, z.nom as zone_nom, b.code as badge_code
                FROM acces_zones az
                LEFT JOIN intervenants i ON az.intervenant_id = i.id
                LEFT JOIN zones z ON az.zone_id = z.id
                LEFT JOIN badges b ON az.badge_id = b.id
                WHERE az.statut = 'REFUSE'
                AND date(az.date_heure) BETWEEN ? AND ?
                ORDER BY az.date_heure DESC
                """;
        List<Acces> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, debut.toString());
            ps.setString(2, fin.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération refus: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne la fréquentation par zone pour les N derniers jours.
     *
     * @param jours nombre de jours à analyser
     * @return tableau de résultats [nom_zone, count]
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Object[]> getFrequentationParZone(int jours) {
        String sql = """
                SELECT z.nom, COUNT(*) as nb
                FROM acces_zones az
                LEFT JOIN zones z ON az.zone_id = z.id
                WHERE az.statut = 'AUTORISE'
                AND date(az.date_heure) >= date('now', '-' || ? || ' days')
                GROUP BY z.id, z.nom
                ORDER BY nb DESC
                """;
        List<Object[]> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, jours);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Object[]{rs.getString("nom"), rs.getInt("nb")});
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur fréquentation: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Mappe une ligne ResultSet vers un Acces.
     *
     * @param rs ResultSet positionné
     * @return instance Acces construite
     * @throws SQLException en cas d'erreur de lecture
     */
    private Acces mapRow(ResultSet rs) throws SQLException {
        Acces a = new Acces();
        a.setId(rs.getInt("id"));
        a.setIntervenantId(rs.getInt("intervenant_id"));
        a.setBadgeId(rs.getInt("badge_id"));
        a.setZoneId(rs.getInt("zone_id"));
        String dh = rs.getString("date_heure");
        if (dh != null) a.setDateHeure(DateUtils.parseDateTime(dh));
        String type = rs.getString("type");
        if (type != null) a.setType(Acces.TypeAcces.valueOf(type));
        String statut = rs.getString("statut");
        if (statut != null) a.setStatut(Acces.StatutAcces.valueOf(statut));
        a.setMotifRefus(rs.getString("motif_refus"));
        int agentId = rs.getInt("agent_id");
        if (!rs.wasNull()) a.setAgentId(agentId);
        // Champs joints
        try { a.setIntervenantNom(rs.getString("intervenant_nom")); } catch (SQLException ignored) {}
        try { a.setZoneNom(rs.getString("zone_nom")); } catch (SQLException ignored) {}
        try { a.setBadgeCode(rs.getString("badge_code")); } catch (SQLException ignored) {}
        return a;
    }
}
