package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.config.Constants;
import cm.enspy.gcu.chantier.model.dao.BadgeDAO;
import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.dao.ZoneDAO;
import cm.enspy.gcu.chantier.model.entities.Badge;
import cm.enspy.gcu.chantier.model.entities.Intervenant;
import cm.enspy.gcu.chantier.model.entities.Zone;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrôleur du module de gestion des badges.
 * Génère les codes de badge, les attribue aux intervenants et gère les zones autorisées.
 */
public class BadgeController {

    @FXML private TableView<Badge> badgeTable;
    @FXML private TableColumn<Badge, String> colCode;
    @FXML private TableColumn<Badge, String> colType;
    @FXML private TableColumn<Badge, String> colExpiration;
    @FXML private TableColumn<Badge, String> colStatut;
    @FXML private TableColumn<Badge, String> colIntervenant;

    @FXML private ComboBox<Intervenant> intervenantCombo;
    @FXML private ComboBox<String> typeBadgeCombo;
    @FXML private DatePicker dateExpirationPicker;
    @FXML private Label codeGenereLabel;
    @FXML private VBox zonesContainer;
    @FXML private Label formTitle;
    @FXML private TextArea motifRevocationArea;

    private final BadgeDAO badgeDAO = new BadgeDAO();
    private final IntervenantDAO intervenantDAO = new IntervenantDAO();
    private final ZoneDAO zoneDAO = new ZoneDAO();
    private final List<CheckBox> zonesCheckboxes = new ArrayList<>();
    private Badge badgeSelectionne = null;
    private String codeTemp;

    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFormulaire();
        chargerBadges();
        configurerSelection();
        genererCode();
    }

    private void configurerColonnes() {
        colCode.setCellValueFactory(c -> c.getValue().codeProperty());
        colType.setCellValueFactory(c -> c.getValue().typeBadgeProperty());
        colExpiration.setCellValueFactory(c -> c.getValue().dateExpirationProperty());
        colStatut.setCellValueFactory(c -> c.getValue().statutProperty());
        colIntervenant.setCellValueFactory(c -> c.getValue().intervenantNomProperty());

        // Coloration statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("ACTIF".equals(item)
                        ? "-fx-text-fill: #4caf50; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e94560; -fx-font-weight: bold;");
            }
        });

        // Coloration badge expirant
        badgeTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Badge b, boolean empty) {
                super.updateItem(b, empty);
                if (!empty && b != null && b.expireDans(Constants.ALERTE_BADGE_JOURS)) {
                    setStyle("-fx-background-color: rgba(255,152,0,0.15);");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configurerFormulaire() {
        intervenantCombo.setItems(FXCollections.observableArrayList(intervenantDAO.findActifs()));
        typeBadgeCombo.setItems(FXCollections.observableArrayList("PERMANENT", "TEMPORAIRE"));
        typeBadgeCombo.valueProperty().addListener((obs, old, val) -> {
            if ("PERMANENT".equals(val)) {
                dateExpirationPicker.setValue(LocalDate.now().plusDays(Constants.BADGE_PERMANENT_DUREE_JOURS));
                dateExpirationPicker.setDisable(true);
            } else {
                dateExpirationPicker.setDisable(false);
            }
        });
        typeBadgeCombo.setValue("PERMANENT");

        // Charger les zones avec CheckBoxes
        zonesContainer.getChildren().clear();
        zonesCheckboxes.clear();
        List<Zone> zones = zoneDAO.findActives();
        for (Zone z : zones) {
            CheckBox cb = new CheckBox(z.getNom() + " (Niveau " + z.getNiveauSecurite() + ")");
            cb.setUserData(z.getId());
            zonesContainer.getChildren().add(cb);
            zonesCheckboxes.add(cb);
        }
    }

    private void chargerBadges() {
        badgeTable.setItems(FXCollections.observableArrayList(badgeDAO.findAll()));
    }

    private void configurerSelection() {
        badgeTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        badgeSelectionne = newVal;
                        formTitle.setText("Badge: " + newVal.getCode());
                    }
                });
    }

    /**
     * Génère un code badge aléatoire de 8 caractères.
     */
    @FXML
    private void genererCode() {
        codeTemp = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, Constants.BADGE_CODE_LENGTH)
                .toUpperCase();
        codeGenereLabel.setText(codeTemp);
    }

    /**
     * Sauvegarde un nouveau badge en base de données.
     */
    @FXML
    private void handleCreer() {
        SessionManager.getInstance().rafraichirActivite();
        Intervenant intervenant = intervenantCombo.getValue();
        if (intervenant == null) {
            afficherErreur("Veuillez sélectionner un intervenant."); return;
        }
        String type = typeBadgeCombo.getValue();
        if (type == null) {
            afficherErreur("Veuillez sélectionner un type de badge."); return;
        }
        LocalDate expiration = dateExpirationPicker.getValue();

        Badge badge = new Badge();
        badge.setCode(codeTemp);
        badge.setTypeBadge(Badge.TypeBadge.valueOf(type));
        badge.setDateCreation(LocalDateTime.now());
        badge.setDateExpiration(expiration);
        badge.setEstActif(true);
        badge.setIntervenantId(intervenant.getId());

        try {
            badgeDAO.save(badge);
            // Associer les zones sélectionnées
            for (CheckBox cb : zonesCheckboxes) {
                if (cb.isSelected()) {
                    badgeDAO.ajouterZone(badge.getId(), (int) cb.getUserData());
                }
            }
            chargerBadges();
            genererCode();
            // Décocher les cases
            zonesCheckboxes.forEach(cb -> cb.setSelected(false));
            afficherInfo("Badge " + badge.getCode() + " créé avec succès.");
        } catch (Exception e) {
            afficherErreur("Erreur lors de la création du badge: " + e.getMessage());
        }
    }

    /**
     * Révoque le badge sélectionné (désactivation immédiate).
     */
    @FXML
    private void handleRevoquer() {
        if (badgeSelectionne == null) { afficherErreur("Sélectionnez un badge."); return; }
        SessionManager.getInstance().rafraichirActivite();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Révoquer le badge " + badgeSelectionne.getCode() + " ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            badgeDAO.revoquerBadge(badgeSelectionne.getId());
            chargerBadges();
            afficherInfo("Badge révoqué.");
        }
    }

    private void afficherInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void afficherErreur(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
