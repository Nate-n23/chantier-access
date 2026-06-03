package cm.enspy.gcu.chantier.model.dao;

import cm.enspy.gcu.chantier.model.entities.Alerte;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des alertes de sécurité en base de données.
 */
public class AlerteDAO {

    public List<Alerte> findAll() {
        List<Alerte> alertes = new ArrayList<>();
        String sql = "SELECT * FROM alertes_securite ORDER BY date_heure DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                alertes.add(mapResultSetToAlerte(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des alertes", e);
        }
        return alertes;
    }

    public List<Alerte> findNonLues() {
        List<Alerte> alertes = new ArrayList<>();
        String sql = "SELECT * FROM alertes_securite WHERE est_lue = 0 ORDER BY date_heure DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                alertes.add(mapResultSetToAlerte(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des alertes non lues", e);
        }
        return alertes;
    }

    public int countNonLues() {
        String sql = "SELECT COUNT(*) FROM alertes_securite WHERE est_lue = 0";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DAOException("Erreur lors du comptage des alertes non lues", e);
        }
        return 0;
    }

    public void save(Alerte alerte) {
        String sql = "INSERT INTO alertes_securite (type, message, niveau_gravite, source_id, est_lue, date_heure) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, alerte.getType());
            pstmt.setString(2, alerte.getMessage());
            pstmt.setString(3, alerte.getNiveauGravite() != null ? alerte.getNiveauGravite().name() : null);
            pstmt.setObject(4, alerte.getSourceId());
            pstmt.setInt(5, alerte.isEstLue() ? 1 : 0);
            pstmt.setString(6, DateUtils.formaterDateHeure(alerte.getDateHeure()));
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    alerte.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la sauvegarde de l'alerte", e);
        }
    }

    public void marquerCommeLue(int id) {
        String sql = "UPDATE alertes_securite SET est_lue = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de l'alerte", e);
        }
    }

    private Alerte mapResultSetToAlerte(ResultSet rs) throws SQLException {
        Alerte alerte = new Alerte();
        alerte.setId(rs.getInt("id"));
        alerte.setType(rs.getString("type"));
        alerte.setMessage(rs.getString("message"));
        String gravite = rs.getString("niveau_gravite");
        if (gravite != null) alerte.setNiveauGravite(Alerte.NiveauGravite.valueOf(gravite));
        alerte.setSourceId(rs.getInt("source_id"));
        alerte.setEstLue(rs.getInt("est_lue") == 1);
        String dh = rs.getString("date_heure");
        if (dh != null) alerte.setDateHeure(DateUtils.parseDateTime(dh));
        return alerte;
    }
}
