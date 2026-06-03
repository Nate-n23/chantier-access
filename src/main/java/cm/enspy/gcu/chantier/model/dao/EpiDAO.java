package cm.enspy.gcu.chantier.model.dao;

import cm.enspy.gcu.chantier.model.entities.Epi;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour l'entité EPI (Équipements de Protection Individuelle).
 */
public class EpiDAO {

    /**
     * Sauvegarde un EPI en base de données.
     *
     * @param epi l'EPI à sauvegarder
     * @return l'EPI avec son identifiant généré
     * @throws DAOException en cas d'erreur SQL
     */
    public Epi save(Epi epi) {
        String sql = """
                INSERT INTO epi (type, taille, etat, date_attribution, intervenant_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, epi.getType() != null ? epi.getType().name() : null);
            ps.setString(2, epi.getTaille());
            ps.setString(3, epi.getEtat() != null ? epi.getEtat().name() : Epi.EtatEpi.BON.name());
            ps.setString(4, epi.getDateAttribution() != null
                    ? epi.getDateAttribution().toString() : null);
            ps.setInt(5, epi.getIntervenantId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) epi.setId(rs.getInt(1));
            }
            return epi;
        } catch (SQLException e) {
            throw new DAOException("Erreur sauvegarde EPI: " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour l'état d'un EPI.
     *
     * @param epiId   identifiant de l'EPI
     * @param nouvelEtat nouvel état
     * @throws DAOException en cas d'erreur SQL
     */
    public void updateEtat(int epiId, Epi.EtatEpi nouvelEtat) {
        String sql = "UPDATE epi SET etat = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, nouvelEtat.name());
            ps.setInt(2, epiId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur mise à jour état EPI: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne tous les EPIs d'un intervenant.
     *
     * @param intervenantId identifiant de l'intervenant
     * @return liste des EPIs attribués à l'intervenant
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Epi> findByIntervenantId(int intervenantId) {
        String sql = "SELECT * FROM epi WHERE intervenant_id = ? ORDER BY type, date_attribution DESC";
        List<Epi> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, intervenantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération EPIs: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne tous les EPIs enregistrés.
     *
     * @return liste de tous les EPIs
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Epi> findAll() {
        String sql = """
                SELECT e.*, i.nom || ' ' || i.prenom as intervenant_nom
                FROM epi e LEFT JOIN intervenants i ON e.intervenant_id = i.id
                ORDER BY e.type, e.etat
                """;
        List<Epi> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Epi epi = mapRow(rs);
                liste.add(epi);
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur récupération tous EPIs: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Vérifie si un intervenant possède un EPI du type donné.
     *
     * @param intervenantId identifiant de l'intervenant
     * @param type          type d'EPI recherché
     * @return true si l'EPI est attribué à cet intervenant
     * @throws DAOException en cas d'erreur SQL
     */
    public boolean hasEpi(int intervenantId, Epi.TypeEpi type) {
        String sql = "SELECT COUNT(*) FROM epi WHERE intervenant_id = ? AND type = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, intervenantId);
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur vérification EPI: " + e.getMessage(), e);
        }
    }

    /**
     * Mappe une ligne ResultSet vers un EPI.
     *
     * @param rs ResultSet positionné
     * @return instance Epi construite
     * @throws SQLException en cas d'erreur de lecture
     */
    private Epi mapRow(ResultSet rs) throws SQLException {
        Epi e = new Epi();
        e.setId(rs.getInt("id"));
        String type = rs.getString("type");
        if (type != null) e.setType(Epi.TypeEpi.valueOf(type));
        e.setTaille(rs.getString("taille"));
        String etat = rs.getString("etat");
        if (etat != null) e.setEtat(Epi.EtatEpi.valueOf(etat));
        String da = rs.getString("date_attribution");
        if (da != null) e.setDateAttribution(DateUtils.parseDateTime(da));
        e.setIntervenantId(rs.getInt("intervenant_id"));
        return e;
    }
}
