package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.model.dao.ZoneDAO;
import cm.enspy.gcu.chantier.model.entities.Zone;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

/**
 * Contrôleur du module de gestion des zones du chantier.
 */
public class ZoneController {

    @FXML private TableView<Zone> zoneTable;
    @FXML private TableColumn<Zone, String> colNom;
    @FXML private TableColumn<Zone, String> colDescription;
    @FXML private TableColumn<Zone, Number> colNiveau;
    @FXML private TableColumn<Zone, Number> colCapacite;
    @FXML private TableColumn<Zone, String> colStatut;

    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    @FXML private Spinner<Integer> niveauSpinner;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private Label formTitle;

    private final ZoneDAO zoneDAO = new ZoneDAO();
    private Zone zoneSelectionnee = null;

    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFormulaire();
        chargerZones();
        configurerSelection();
        modeNouvel();
    }

    private void configurerColonnes() {
        colNom.setCellValueFactory(c -> c.getValue().nomProperty());
        colDescription.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colNiveau.setCellValueFactory(c -> c.getValue().niveauSecuriteProperty());
        colCapacite.setCellValueFactory(c -> c.getValue().capaciteMaxProperty());
        colStatut.setCellValueFactory(c -> c.getValue().statutProperty());
    }

    private void configurerFormulaire() {
        niveauSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1));
        capaciteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 50));
    }

    private void chargerZones() {
        zoneTable.setItems(FXCollections.observableArrayList(zoneDAO.findAll()));
    }

    private void configurerSelection() {
        zoneTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        zoneSelectionnee = newVal;
                        nomField.setText(newVal.getNom());
                        descriptionArea.setText(newVal.getDescription());
                        niveauSpinner.getValueFactory().setValue(newVal.getNiveauSecurite());
                        capaciteSpinner.getValueFactory().setValue(newVal.getCapaciteMax());
                        formTitle.setText("Modifier Zone");
                    }
                });
    }

    /** Passe en mode Nouveau. */
    @FXML
    private void modeNouvel() {
        zoneSelectionnee = null;
        zoneTable.getSelectionModel().clearSelection();
        nomField.clear();
        descriptionArea.clear();
        niveauSpinner.getValueFactory().setValue(1);
        capaciteSpinner.getValueFactory().setValue(50);
        formTitle.setText("Nouvelle Zone");
    }

    /** Sauvegarde ou met à jour la zone. */
    @FXML
    private void handleSauvegarder() {
        SessionManager.getInstance().rafraichirActivite();
        if (nomField.getText().isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Le nom de la zone est obligatoire.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            if (zoneSelectionnee == null) {
                Zone z = new Zone(nomField.getText().trim(), descriptionArea.getText().trim(),
                        niveauSpinner.getValue(), capaciteSpinner.getValue());
                zoneDAO.save(z);
            } else {
                zoneSelectionnee.setNom(nomField.getText().trim());
                zoneSelectionnee.setDescription(descriptionArea.getText().trim());
                zoneSelectionnee.setNiveauSecurite(niveauSpinner.getValue());
                zoneSelectionnee.setCapaciteMax(capaciteSpinner.getValue());
                zoneDAO.update(zoneSelectionnee);
            }
            chargerZones();
            modeNouvel();
            new Alert(Alert.AlertType.INFORMATION, "Zone sauvegardée.", ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /** Désactive la zone sélectionnée. */
    @FXML
    private void handleDesactiver() {
        if (zoneSelectionnee == null) return;
        SessionManager.getInstance().rafraichirActivite();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Désactiver la zone '" + zoneSelectionnee.getNom() + "' ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            zoneSelectionnee.setEstActive(!zoneSelectionnee.isEstActive());
            zoneDAO.update(zoneSelectionnee);
            chargerZones();
            modeNouvel();
        }
    }
}
