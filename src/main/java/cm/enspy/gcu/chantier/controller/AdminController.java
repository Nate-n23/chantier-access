package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.config.AppConfig;
import cm.enspy.gcu.chantier.config.Constants;
import cm.enspy.gcu.chantier.model.dao.UtilisateurDAO;
import cm.enspy.gcu.chantier.model.entities.JournalAudit;
import cm.enspy.gcu.chantier.model.entities.UtilisateurSysteme;
import cm.enspy.gcu.chantier.service.AuthService;
import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.DateUtils;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur du module d'administration.
 * Gère les comptes utilisateurs, le journal d'audit, la sauvegarde BDD et la configuration.
 */
public class AdminController {

    // Section Utilisateurs
    @FXML private TableView<UtilisateurSysteme> utilisateurTable;
    @FXML private TableColumn<UtilisateurSysteme, String> colLogin;
    @FXML private TableColumn<UtilisateurSysteme, String> colRole;
    @FXML private TableColumn<UtilisateurSysteme, String> colStatutUser;
    @FXML private TableColumn<UtilisateurSysteme, String> colDerniereConnexion;

    @FXML private TextField loginField;
    @FXML private PasswordField mdpField;
    @FXML private PasswordField mdpConfirmField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label userFormTitle;
    @FXML private Label mdpValidLabel;

    // Section Journal d'audit
    @FXML private TableView<JournalAudit> auditTable;
    @FXML private TableColumn<JournalAudit, String> colAuditDate;
    @FXML private TableColumn<JournalAudit, String> colAuditUser;
    @FXML private TableColumn<JournalAudit, String> colAuditAction;
    @FXML private TableColumn<JournalAudit, String> colAuditTable;

    // Section Configuration
    @FXML private TextField nomChantierField;
    @FXML private TextField adresseChantierField;
    @FXML private Label configStatusLabel;
    @FXML private Label backupStatusLabel;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final AuthService authService = new AuthService();
    private UtilisateurSysteme utilisateurSelectionne = null;

    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().isAdmin()) {
            new Alert(Alert.AlertType.WARNING,
                    "Accès réservé aux administrateurs.", ButtonType.OK).showAndWait();
            return;
        }
        configurerColonnesUtilisateurs();
        configurerColonnesAudit();
        configurerFormulaire();
        chargerUtilisateurs();
        chargerAudit();
        chargerConfig();
        configurerSelectionUtilisateur();
    }

    private void configurerColonnesUtilisateurs() {
        colLogin.setCellValueFactory(c -> c.getValue().loginProperty());
        colRole.setCellValueFactory(c -> c.getValue().roleProperty());
        colStatutUser.setCellValueFactory(c -> c.getValue().statutProperty());
        colDerniereConnexion.setCellValueFactory(c -> c.getValue().derniereConnexionProperty());
    }

    private void configurerColonnesAudit() {
        colAuditDate.setCellValueFactory(c -> c.getValue().dateHeureProperty());
        colAuditUser.setCellValueFactory(c -> c.getValue().utilisateurLoginProperty());
        colAuditAction.setCellValueFactory(c -> c.getValue().actionProperty());
        colAuditTable.setCellValueFactory(c -> c.getValue().tableCibleeProperty());
    }

    private void configurerFormulaire() {
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "RESPONSABLE", "AGENT", "CONSULTANT"));
        mdpField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                mdpValidLabel.setText("");
                return;
            }
            boolean valide = authService.validerMotDePasse(val);
            mdpValidLabel.setText(valide ? "✓ Mot de passe valide" : "✗ 8+ chars, 1 maj, 1 chiffre");
            mdpValidLabel.setStyle(valide ? "-fx-text-fill: #4caf50;" : "-fx-text-fill: #e94560;");
        });
    }

    private void chargerUtilisateurs() {
        utilisateurTable.setItems(FXCollections.observableArrayList(utilisateurDAO.findAll()));
    }

    private void chargerAudit() {
        List<JournalAudit> audits = lireJournalAudit();
        auditTable.setItems(FXCollections.observableArrayList(audits));
    }

    private void chargerConfig() {
        nomChantierField.setText(AppConfig.getInstance().getNomChantier());
        adresseChantierField.setText(AppConfig.getInstance().getAdresseChantier());
    }

    private void configurerSelectionUtilisateur() {
        utilisateurTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null) {
                        utilisateurSelectionne = newVal;
                        loginField.setText(newVal.getLogin());
                        mdpField.clear();
                        mdpConfirmField.clear();
                        if (newVal.getRole() != null) roleCombo.setValue(newVal.getRole().name());
                        userFormTitle.setText("Modifier utilisateur");
                    }
                });
    }

    /** Passe en mode Nouveau utilisateur. */
    @FXML
    private void modeNouvelUtilisateur() {
        utilisateurSelectionne = null;
        utilisateurTable.getSelectionModel().clearSelection();
        loginField.clear(); mdpField.clear(); mdpConfirmField.clear();
        roleCombo.setValue(null);
        userFormTitle.setText("Nouvel utilisateur");
        mdpValidLabel.setText("");
    }

    /** Sauvegarde un utilisateur. */
    @FXML
    private void handleSauvegarderUtilisateur() {
        SessionManager.getInstance().rafraichirActivite();
        if (loginField.getText().isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Le login est obligatoire.", ButtonType.OK).showAndWait();
            return;
        }
        if (roleCombo.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Le rôle est obligatoire.", ButtonType.OK).showAndWait();
            return;
        }
        String mdp = mdpField.getText();
        String mdpConfirm = mdpConfirmField.getText();

        try {
            if (utilisateurSelectionne == null) {
                // Création — mot de passe obligatoire
                if (mdp.isBlank()) {
                    new Alert(Alert.AlertType.ERROR, "Le mot de passe est obligatoire.", ButtonType.OK).showAndWait();
                    return;
                }
                if (!mdp.equals(mdpConfirm)) {
                    new Alert(Alert.AlertType.ERROR, "Les mots de passe ne correspondent pas.", ButtonType.OK).showAndWait();
                    return;
                }
                authService.creerUtilisateur(loginField.getText().trim(), mdp,
                        UtilisateurSysteme.Role.valueOf(roleCombo.getValue()));
            } else {
                utilisateurSelectionne.setLogin(loginField.getText().trim());
                utilisateurSelectionne.setRole(UtilisateurSysteme.Role.valueOf(roleCombo.getValue()));
                if (!mdp.isBlank()) {
                    if (!mdp.equals(mdpConfirm)) {
                        new Alert(Alert.AlertType.ERROR, "Les mots de passe ne correspondent pas.", ButtonType.OK).showAndWait();
                        return;
                    }
                    authService.changerMotDePasse(utilisateurSelectionne.getId(), mdp);
                }
                utilisateurDAO.update(utilisateurSelectionne);
            }
            chargerUtilisateurs();
            modeNouvelUtilisateur();
            new Alert(Alert.AlertType.INFORMATION, "Utilisateur sauvegardé.", ButtonType.OK).showAndWait();
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /** Active ou désactive le compte utilisateur sélectionné. */
    @FXML
    private void handleToggleActif() {
        if (utilisateurSelectionne == null) return;
        SessionManager.getInstance().rafraichirActivite();
        String action = utilisateurSelectionne.isEstActif() ? "Désactiver" : "Activer";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                action + " le compte '" + utilisateurSelectionne.getLogin() + "' ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            utilisateurSelectionne.setEstActif(!utilisateurSelectionne.isEstActif());
            utilisateurDAO.update(utilisateurSelectionne);
            chargerUtilisateurs();
        }
    }

    /** Sauvegarde la configuration du chantier. */
    @FXML
    private void handleSauvegarderConfig() {
        SessionManager.getInstance().rafraichirActivite();
        AppConfig.getInstance().setNomChantier(nomChantierField.getText().trim());
        AppConfig.getInstance().setAdresseChantier(adresseChantierField.getText().trim());
        configStatusLabel.setText("Configuration sauvegardée (redémarrage requis pour le titre).");
        new Alert(Alert.AlertType.INFORMATION, "Configuration mise à jour.", ButtonType.OK).showAndWait();
    }

    /** Effectue une sauvegarde de la base de données. */
    @FXML
    private void handleSauvegarderBDD() {
        SessionManager.getInstance().rafraichirActivite();
        try {
            Path src = Paths.get(Constants.DB_PATH);
            if (!Files.exists(src)) {
                backupStatusLabel.setText("Base de données introuvable.");
                return;
            }
            String horodatage = DateUtils.formaterPourFichier(LocalDateTime.now());
            Path dest = Paths.get(Constants.BACKUP_DIR, "chantier_backup_" + horodatage + ".db");
            Files.createDirectories(dest.getParent());
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            backupStatusLabel.setText("Sauvegarde créée: " + dest);
            new Alert(Alert.AlertType.INFORMATION,
                    "Sauvegarde créée:\n" + dest, ButtonType.OK).showAndWait();
        } catch (IOException e) {
            backupStatusLabel.setText("Erreur: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Erreur sauvegarde: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /**
     * Lit le journal d'audit depuis la base de données.
     *
     * @return liste des entrées d'audit
     */
    private List<JournalAudit> lireJournalAudit() {
        String sql = """
                SELECT ja.*, u.login FROM journal_audit ja
                LEFT JOIN utilisateurs_systeme u ON ja.utilisateur_id = u.id
                ORDER BY ja.date_heure DESC LIMIT 200
                """;
        List<JournalAudit> liste = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JournalAudit j = new JournalAudit();
                j.setId(rs.getInt("id"));
                j.setUtilisateurId(rs.getInt("utilisateur_id"));
                j.setAction(rs.getString("action"));
                j.setTableCiblee(rs.getString("table_ciblee"));
                String dh = rs.getString("date_heure");
                if (dh != null) j.setDateHeure(DateUtils.parseDateTime(dh));
                j.setAncienneValeur(rs.getString("ancienne_valeur"));
                j.setNouvelleValeur(rs.getString("nouvelle_valeur"));
                j.setIpMachine(rs.getString("ip_machine"));
                j.setUtilisateurLogin(rs.getString("login"));
                liste.add(j);
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lecture audit: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
        return liste;
    }
}
