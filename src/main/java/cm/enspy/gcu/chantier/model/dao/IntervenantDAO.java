package cm.enspy.gcu.chantier.model.dao;

import cm.enspy.gcu.chantier.model.entities.Intervenant;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object) pour l'entité Intervenant.
 * Toutes les opérations sont effectuées via PreparedStatement.
 */
public class IntervenantDAO {

    private static final Logger LOGGER = Logger.getLogger(IntervenantDAO.class.getName());

    /**
     * Sauvegarde un nouvel intervenant en base de données.
     *
     * @param intervenant l'intervenant à sauvegarder
     * @return l'intervenant avec l'identifiant généré
     * @throws DAOException en cas d'erreur SQL
     */
    public Intervenant save(Intervenant intervenant) {
        String sql = """
                INSERT INTO intervenants
                (matricule, nom, prenom, date_naissance, telephone, email,
                 entreprise, categorie, statut, photo_path, niveau_habilitation)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, intervenant.getMatricule());
            ps.setString(2, intervenant.getNom());
            ps.setString(3, intervenant.getPrenom());
            ps.setString(4, intervenant.getDateNaissance() != null
                    ? intervenant.getDateNaissance().toString() : null);
            ps.setString(5, intervenant.getTelephone());
            ps.setString(6, intervenant.getEmail());
            ps.setString(7, intervenant.getEntreprise());
            ps.setString(8, intervenant.getCategorie() != null
                    ? intervenant.getCategorie().name() : null);
            ps.setString(9, intervenant.getStatut() != null
                    ? intervenant.getStatut().name() : Intervenant.Statut.ACTIF.name());
            ps.setString(10, intervenant.getPhotoPath());
            ps.setInt(11, intervenant.getNiveauHabilitation());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    intervenant.setId(rs.getInt(1));
                }
            }
            return intervenant;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la sauvegarde de l'intervenant: " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour un intervenant existant en base de données.
     *
     * @param intervenant l'intervenant à mettre à jour
     * @throws DAOException en cas d'erreur SQL
     */
    public void update(Intervenant intervenant) {
        String sql = """
                UPDATE intervenants SET
                matricule=?, nom=?, prenom=?, date_naissance=?, telephone=?,
                email=?, entreprise=?, categorie=?, statut=?, photo_path=?, niveau_habilitation=?
                WHERE id=?
                """;
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, intervenant.getMatricule());
            ps.setString(2, intervenant.getNom());
            ps.setString(3, intervenant.getPrenom());
            ps.setString(4, intervenant.getDateNaissance() != null
                    ? intervenant.getDateNaissance().toString() : null);
            ps.setString(5, intervenant.getTelephone());
            ps.setString(6, intervenant.getEmail());
            ps.setString(7, intervenant.getEntreprise());
            ps.setString(8, intervenant.getCategorie() != null
                    ? intervenant.getCategorie().name() : null);
            ps.setString(9, intervenant.getStatut() != null
                    ? intervenant.getStatut().name() : null);
            ps.setString(10, intervenant.getPhotoPath());
            ps.setInt(11, intervenant.getNiveauHabilitation());
            ps.setInt(12, intervenant.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de l'intervenant: " + e.getMessage(), e);
        }
    }

    /**
     * Trouve un intervenant par son identifiant.
     *
     * @param id identifiant de l'intervenant
     * @return Optional contenant l'intervenant, ou vide si non trouvé
     * @throws DAOException en cas d'erreur SQL
     */
    public Optional<Intervenant> findById(int id) {
        String sql = "SELECT * FROM intervenants WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche de l'intervenant: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Trouve un intervenant par son matricule.
     *
     * @param matricule matricule unique
     * @return Optional contenant l'intervenant, ou vide si non trouvé
     * @throws DAOException en cas d'erreur SQL
     */
    public Optional<Intervenant> findByMatricule(String matricule) {
        String sql = "SELECT * FROM intervenants WHERE matricule = ?";
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, matricule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche par matricule: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Retourne tous les intervenants non archivés.
     *
     * @return liste des intervenants actifs, suspendus ou expulsés
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Intervenant> findAll() {
        String sql = "SELECT * FROM intervenants WHERE statut != 'ARCHIVE' ORDER BY nom, prenom";
        List<Intervenant> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des intervenants: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Recherche les intervenants correspondant à un terme (nom, matricule, entreprise).
     *
     * @param terme terme de recherche
     * @return liste des intervenants correspondants
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Intervenant> search(String terme) {
        String sql = """
                SELECT * FROM intervenants
                WHERE statut != 'ARCHIVE'
                AND (nom LIKE ? OR prenom LIKE ? OR matricule LIKE ? OR entreprise LIKE ?)
                ORDER BY nom, prenom
                """;
        String pattern = "%" + terme + "%";
        List<Intervenant> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Retourne tous les intervenants avec statut ACTIF.
     *
     * @return liste des intervenants actifs
     * @throws DAOException en cas d'erreur SQL
     */
    public List<Intervenant> findActifs() {
        String sql = "SELECT * FROM intervenants WHERE statut = 'ACTIF' ORDER BY nom, prenom";
        List<Intervenant> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des actifs: " + e.getMessage(), e);
        }
        return liste;
    }

    /**
     * Mappe une ligne ResultSet vers un objet Intervenant.
     *
     * @param rs ResultSet positionné sur la ligne à mapper
     * @return instance Intervenant construite
     * @throws SQLException en cas d'erreur de lecture du ResultSet
     */
    private Intervenant mapRow(ResultSet rs) throws SQLException {
        Intervenant i = new Intervenant();
        i.setId(rs.getInt("id"));
        i.setMatricule(rs.getString("matricule"));
        i.setNom(rs.getString("nom"));
        i.setPrenom(rs.getString("prenom"));
        String dn = rs.getString("date_naissance");
        if (dn != null) i.setDateNaissance(DateUtils.parseDate(dn));
        i.setTelephone(rs.getString("telephone"));
        i.setEmail(rs.getString("email"));
        i.setEntreprise(rs.getString("entreprise"));
        String cat = rs.getString("categorie");
        if (cat != null) i.setCategorie(Intervenant.Categorie.valueOf(cat));
        String statut = rs.getString("statut");
        if (statut != null) i.setStatut(Intervenant.Statut.valueOf(statut));
        i.setPhotoPath(rs.getString("photo_path"));
        i.setNiveauHabilitation(rs.getInt("niveau_habilitation"));
        String dc = rs.getString("date_creation");
        if (dc != null) i.setDateCreation(DateUtils.parseDateTime(dc));
        return i;
    }
}
