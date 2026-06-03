package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.model.dao.IntervenantDAO;
import cm.enspy.gcu.chantier.model.entities.Intervenant;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Contrôleur du module de gestion des intervenants.
 * CRUD complet avec recherche en temps réel et gestion du statut.
 */
public class IntervenantController {

    private static final Logger LOGGER = Logger.getLogger(IntervenantController.class.getName());

    // Table
    @FXML private TableView<Intervenant> intervenantTable;
    @FXML private TableColumn<Intervenant, String> colMatricule;
    @FXML private TableColumn<Intervenant, String> colNom;
    @FXML private TableColumn<Intervenant, String> colPrenom;
    @FXML private TableColumn<Intervenant, String> colEntreprise;
    @FXML private TableColumn<Intervenant, String> colCategorie;
    @FXML private TableColumn<Intervenant, String> colStatut;

    // Recherche
    @FXML private TextField searchField;

    // Formulaire
    @FXML private TextField matriculeField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField entrepriseField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private Spinner<Integer> niveauHabSpinner;
    @FXML private Label formTitle;

    // Boutons d'action
    @FXML private Button btnSauvegarder;
    @FXML private Button btnSuspendre;
    @FXML private Button btnArchiver;
    @FXML private Button btnNouveau;

    private final IntervenantDAO intervenantDAO = new IntervenantDAO();
    private final ObservableList<Intervenant> intervenants = FXCollections.observableArrayList();
    private Intervenant intervenantSelectione = null;

    /**
     * Initialise les composants après chargement du FXML.
     */
    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFormulaire();
        chargerIntervenants();
        configurerRecherche();
        configurerSelection();
        modeNouvel();
    }

    /**
     * Configure les colonnes de la TableView.
     */
    private void configurerColonnes() {
        colMatricule.setCellValueFactory(c -> c.getValue().matriculeProperty());
        colNom.setCellValueFactory(c -> c.getValue().nomProperty());
        colPrenom.setCellValueFactory(c -> c.getValue().prenomProperty());
        colEntreprise.setCellValueFactory(c -> c.getValue().entrepriseProperty());
        colCategorie.setCellValueFactory(c -> c.getValue().categorieProperty());
        colStatut.setCellValueFactory(c -> c.getValue().statutProperty());

        // Colorisation des statuts
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String style = switch (item) {
                    case "ACTIF" -> "-fx-text-fill: #4caf50; -fx-font-weight: bold;";
                    case "SUSPENDU" -> "-fx-text-fill: #ff9800; -fx-font-weight: bold;";
                    case "EXPULSE" -> "-fx-text-fill: #e94560; -fx-font-weight: bold;";
                    case "ARCHIVE" -> "-fx-text-fill: #888; -fx-font-weight: bold;";
                    default -> "";
                };
                setStyle(style);
            }
        });
    }

    /**
     * Configure les éléments du formulaire (ComboBox, Spinner...).
     */
    private void configurerFormulaire() {
        categorieCombo.setItems(FXCollections.observableArrayList(
                "OUVRIER", "TECHNICIEN", "INGENIEUR", "VISITEUR", "SOUS_TRAITANT"));
        SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1);
        niveauHabSpinner.setValueFactory(svf);
    }

    /**
     * Charge tous les intervenants non archivés depuis la base de données.
     */
    private void chargerIntervenants() {
        intervenants.setAll(intervenantDAO.findAll());
        intervenantTable.setItems(intervenants);
    }

    /**
     * Configure la recherche en temps réel sur le champ texte.
     */
    private void configurerRecherche() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                chargerIntervenants();
            } else {
                List<Intervenant> resultats = intervenantDAO.search(val);
                intervenantTable.setItems(FXCollections.observableArrayList(resultats));
            }
        });
    }

    /**
     * Configure la réaction au clic sur une ligne de la table.
     */
    private void configurerSelection() {
        intervenantTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        intervenantSelectione = newVal;
                        remplirFormulaire(newVal);
                        formTitle.setText("Modifier Intervenant");
                        btnSuspendre.setDisable(false);
                        btnArchiver.setDisable(false);
                    }
                });
    }

    /**
     * Remplit le formulaire avec les données d'un intervenant.
     *
     * @param i l'intervenant à afficher
     */
    private void remplirFormulaire(Intervenant i) {
        matriculeField.setText(i.getMatricule());
        nomField.setText(i.getNom());
        prenomField.setText(i.getPrenom());
        dateNaissancePicker.setValue(i.getDateNaissance());
        telephoneField.setText(i.getTelephone());
        emailField.setText(i.getEmail());
        entrepriseField.setText(i.getEntreprise());
        if (i.getCategorie() != null) categorieCombo.setValue(i.getCategorie().name());
        niveauHabSpinner.getValueFactory().setValue(i.getNiveauHabilitation());
    }

    /**
     * Passe en mode "Nouvel intervenant" et vide le formulaire.
     */
    @FXML
    private void modeNouvel() {
        intervenantSelectione = null;
        intervenantTable.getSelectionModel().clearSelection();
        matriculeField.clear(); nomField.clear(); prenomField.clear();
        telephoneField.clear(); emailField.clear(); entrepriseField.clear();
        dateNaissancePicker.setValue(null);
        categorieCombo.setValue(null);
        niveauHabSpinner.getValueFactory().setValue(1);
        formTitle.setText("Nouvel Intervenant");
        btnSuspendre.setDisable(true);
        btnArchiver.setDisable(true);
        matriculeField.requestFocus();
    }

    /**
     * Sauvegarde l'intervenant (création ou modification).
     */
    @FXML
    private void handleSauvegarder() {
        if (!validerFormulaire()) return;
        SessionManager.getInstance().rafraichirActivite();

        try {
            if (intervenantSelectione == null) {
                Intervenant nouveau = new Intervenant();
                remplirDepuisFormulaire(nouveau);
                intervenantDAO.save(nouveau);
                afficherInfo("Intervenant créé avec succès.");
            } else {
                remplirDepuisFormulaire(intervenantSelectione);
                intervenantDAO.update(intervenantSelectione);
                afficherInfo("Intervenant mis à jour avec succès.");
            }
            chargerIntervenants();
            modeNouvel();
        } catch (Exception e) {
            afficherErreur("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    /**
     * Suspend l'intervenant sélectionné (soft statut).
     */
    @FXML
    private void handleSuspendre() {
        if (intervenantSelectione == null) return;
        SessionManager.getInstance().rafraichirActivite();
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION,
                "Suspendre l'intervenant " + intervenantSelectione.getNomComplet() + " ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            intervenantSelectione.setStatut(Intervenant.Statut.SUSPENDU);
            intervenantDAO.update(intervenantSelectione);
            chargerIntervenants();
            modeNouvel();
        }
    }

    /**
     * Archive l'intervenant sélectionné (soft delete).
     */
    @FXML
    private void handleArchiver() {
        if (intervenantSelectione == null) return;
        SessionManager.getInstance().rafraichirActivite();
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION,
                "Archiver l'intervenant " + intervenantSelectione.getNomComplet() + " ?\nCette action est irréversible.",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            intervenantSelectione.setStatut(Intervenant.Statut.ARCHIVE);
            intervenantDAO.update(intervenantSelectione);
            chargerIntervenants();
            modeNouvel();
        }
    }

    /**
     * Remplit un Intervenant depuis les champs du formulaire.
     *
     * @param i l'intervenant à remplir
     */
    private void remplirDepuisFormulaire(Intervenant i) {
        i.setMatricule(matriculeField.getText().trim());
        i.setNom(nomField.getText().trim());
        i.setPrenom(prenomField.getText().trim());
        i.setDateNaissance(dateNaissancePicker.getValue());
        i.setTelephone(telephoneField.getText().trim());
        i.setEmail(emailField.getText().trim());
        i.setEntreprise(entrepriseField.getText().trim());
        String cat = categorieCombo.getValue();
        if (cat != null) i.setCategorie(Intervenant.Categorie.valueOf(cat));
        i.setNiveauHabilitation(niveauHabSpinner.getValue());
        if (i.getStatut() == null) i.setStatut(Intervenant.Statut.ACTIF);
    }

    /**
     * Valide les champs obligatoires du formulaire.
     *
     * @return true si tous les champs obligatoires sont remplis
     */
    private boolean validerFormulaire() {
        if (matriculeField.getText().isBlank()) {
            afficherErreur("Le matricule est obligatoire."); return false;
        }
        if (nomField.getText().isBlank()) {
            afficherErreur("Le nom est obligatoire."); return false;
        }
        if (prenomField.getText().isBlank()) {
            afficherErreur("Le prénom est obligatoire."); return false;
        }
        if (entrepriseField.getText().isBlank()) {
            afficherErreur("L'entreprise est obligatoire."); return false;
        }
        if (categorieCombo.getValue() == null) {
            afficherErreur("La catégorie est obligatoire."); return false;
        }
        return true;
    }

    private void afficherInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }

    private void afficherErreur(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
