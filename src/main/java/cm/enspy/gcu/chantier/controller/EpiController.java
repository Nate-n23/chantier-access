package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.entities.Epi;
import cm.enspy.gcu.chantier.model.entities.Intervenant;
import cm.enspy.gcu.chantier.service.EpiService;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Contrôleur du module de gestion des EPI.
 * Gère l'attribution, la visualisation et le changement d'état des équipements de protection.
 */
public class EpiController {

    @FXML private TableView<Epi> epiTable;
    @FXML private TableColumn<Epi, String> colType;
    @FXML private TableColumn<Epi, String> colTaille;
    @FXML private TableColumn<Epi, String> colEtat;
    @FXML private TableColumn<Epi, String> colDate;
    @FXML private TableColumn<Epi, String> colIntervenant;

    @FXML private ComboBox<Intervenant> intervenantCombo;
    @FXML private ComboBox<String> typeEpiCombo;
    @FXML private TextField tailleField;
    @FXML private Label epiCountLabel;

    private final EpiService epiService = new EpiService();
    private final IntervenantDAO intervenantDAO = new IntervenantDAO();
    private Epi epiSelectionne = null;

    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFormulaire();
        chargerEpis();
        configurerSelection();
    }

    private void configurerColonnes() {
        colType.setCellValueFactory(c -> c.getValue().typeProperty());
        colTaille.setCellValueFactory(c -> c.getValue().tailleProperty());
        colEtat.setCellValueFactory(c -> c.getValue().etatProperty());
        colDate.setCellValueFactory(c -> c.getValue().dateAttributionProperty());
        colIntervenant.setCellValueFactory(c -> c.getValue().intervenantNomProperty());

        // Coloration état
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String style = switch (item) {
                    case "BON" -> "-fx-text-fill: #4caf50; -fx-font-weight: bold;";
                    case "USAGE" -> "-fx-text-fill: #ff9800; -fx-font-weight: bold;";
                    case "A_REMPLACER" -> "-fx-text-fill: #e94560; -fx-font-weight: bold;";
                    default -> "";
                };
                setStyle(style);
            }
        });
    }

    private void configurerFormulaire() {
        intervenantCombo.setItems(FXCollections.observableArrayList(intervenantDAO.findActifs()));
        intervenantCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Intervenant i) { return i != null ? i.getNomComplet() : ""; }
            @Override public Intervenant fromString(String s) { return null; }
        });
        typeEpiCombo.setItems(FXCollections.observableArrayList(
                "CASQUE", "GILET", "CHAUSSURES", "HARNAIS", "LUNETTES", "GANTS"));
    }

    private void chargerEpis() {
        epiTable.setItems(FXCollections.observableArrayList(epiService.getTousEpis()));
        epiCountLabel.setText("Total: " + epiTable.getItems().size());
    }

    private void configurerSelection() {
        epiTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> epiSelectionne = newVal);
    }

    /** Attribue un EPI à l'intervenant sélectionné. */
    @FXML
    private void handleAttribuer() {
        SessionManager.getInstance().rafraichirActivite();
        Intervenant intervenant = intervenantCombo.getValue();
        String type = typeEpiCombo.getValue();
        if (intervenant == null || type == null) {
            new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner un intervenant et un type d'EPI.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            epiService.attribuer(intervenant.getId(), Epi.TypeEpi.valueOf(type),
                    tailleField.getText().trim().isEmpty() ? "Standard" : tailleField.getText().trim());
            chargerEpis();
            new Alert(Alert.AlertType.INFORMATION, "EPI attribué avec succès.", ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /** Change l'état d'un EPI vers BON. */
    @FXML private void handleMarquerBon() { changerEtat(Epi.EtatEpi.BON); }
    /** Change l'état d'un EPI vers USAGE. */
    @FXML private void handleMarquerUsage() { changerEtat(Epi.EtatEpi.USAGE); }
    /** Change l'état d'un EPI vers À REMPLACER. */
    @FXML private void handleMarquerARemplacer() { changerEtat(Epi.EtatEpi.A_REMPLACER); }

    private void changerEtat(Epi.EtatEpi etat) {
        if (epiSelectionne == null) {
            new Alert(Alert.AlertType.ERROR, "Sélectionnez un EPI.", ButtonType.OK).showAndWait();
            return;
        }
        SessionManager.getInstance().rafraichirActivite();
        epiService.changerEtat(epiSelectionne.getId(), etat);
        chargerEpis();
    }
}
