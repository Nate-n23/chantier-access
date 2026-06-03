package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.config.Constants;
import cm.enspy.gcu.chantier.model.dao.ZoneDAO;
import cm.enspy.gcu.chantier.model.entities.Acces;
import cm.enspy.gcu.chantier.model.entities.Zone;
import cm.enspy.gcu.chantier.service.AccesService;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.awt.Toolkit;
import java.util.List;

/**
 * Contrôleur du module de contrôle d'accès.
 * Gère la saisie badge, la vérification d'accès en temps réel et l'affichage des présents.
 */
public class AccesController {

    @FXML private TextField codeBadgeField;
    @FXML private ComboBox<Zone> zoneCombo;
    @FXML private VBox resultPanel;
    @FXML private Label resultLabel;
    @FXML private Label resultDetail;
    @FXML private HBox resultBox;

    @FXML private TableView<Acces> presentsTable;
    @FXML private TableColumn<Acces, String> colPresentsNom;
    @FXML private TableColumn<Acces, String> colPresentsZone;
    @FXML private TableColumn<Acces, String> colPresentsHeure;

    @FXML private TableView<Acces> historiqueTable;
    @FXML private TableColumn<Acces, String> colHistDate;
    @FXML private TableColumn<Acces, String> colHistNom;
    @FXML private TableColumn<Acces, String> colHistZone;
    @FXML private TableColumn<Acces, String> colHistType;
    @FXML private TableColumn<Acces, String> colHistStatut;
    @FXML private TableColumn<Acces, String> colHistMotif;

    @FXML private Label presentsCountLabel;

    private final AccesService accesService = new AccesService();
    private final ZoneDAO zoneDAO = new ZoneDAO();
    private Timeline presentsTimeline;

    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize() {
        configurerColonnes();
        chargerZones();
        chargerDonnees();
        demarrerRefraichissementPresents();
        resultPanel.setVisible(false);
    }

    private void configurerColonnes() {
        colPresentsNom.setCellValueFactory(c -> c.getValue().intervenantNomProperty());
        colPresentsZone.setCellValueFactory(c -> c.getValue().zoneNomProperty());
        colPresentsHeure.setCellValueFactory(c -> c.getValue().dateHeureProperty());

        colHistDate.setCellValueFactory(c -> c.getValue().dateHeureProperty());
        colHistNom.setCellValueFactory(c -> c.getValue().intervenantNomProperty());
        colHistZone.setCellValueFactory(c -> c.getValue().zoneNomProperty());
        colHistType.setCellValueFactory(c -> c.getValue().typeProperty());
        colHistStatut.setCellValueFactory(c -> c.getValue().statutProperty());
        colHistMotif.setCellValueFactory(c -> c.getValue().motifRefusProperty());

        // Coloration statut historique
        colHistStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("AUTORISE".equals(item)
                        ? "-fx-text-fill: #4caf50; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e94560; -fx-font-weight: bold;");
            }
        });
    }

    private void chargerZones() {
        zoneCombo.setItems(FXCollections.observableArrayList(zoneDAO.findActives()));
        zoneCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Zone z) { return z != null ? z.getNom() : ""; }
            @Override public Zone fromString(String s) { return null; }
        });
    }

    private void chargerDonnees() {
        List<Acces> presents = accesService.getPresents();
        presentsTable.setItems(FXCollections.observableArrayList(presents));
        presentsCountLabel.setText(String.valueOf(presents.size()));

        List<Acces> historique = accesService.getDerniersAcces(Constants.MAX_HISTORIQUE_PASSAGES);
        historiqueTable.setItems(FXCollections.observableArrayList(historique));
    }

    /**
     * Gère une tentative d'accès de type ENTRÉE.
     */
    @FXML
    private void handleEntree() {
        traiterAcces(Acces.TypeAcces.ENTREE);
    }

    /**
     * Gère une tentative d'accès de type SORTIE.
     */
    @FXML
    private void handleSortie() {
        traiterAcces(Acces.TypeAcces.SORTIE);
    }

    /**
     * Traite une tentative d'accès.
     *
     * @param type type d'accès (ENTREE ou SORTIE)
     */
    private void traiterAcces(Acces.TypeAcces type) {
        SessionManager.getInstance().rafraichirActivite();
        String codeBadge = codeBadgeField.getText().trim().toUpperCase();
        Zone zone = zoneCombo.getValue();

        if (codeBadge.isBlank()) {
            afficherResultat(false, "Veuillez saisir un code badge.", "");
            return;
        }
        if (zone == null) {
            afficherResultat(false, "Veuillez sélectionner une zone.", "");
            return;
        }

        AccesService.ResultatAcces resultat = accesService.enregistrer(codeBadge, zone.getId(), type);
        afficherResultat(resultat.autorise(), resultat.message(),
                resultat.intervenant() != null ? resultat.intervenant().getNomComplet() : "");

        // Son
        jouerSon(resultat.autorise());

        // Rafraîchir les données
        chargerDonnees();
        codeBadgeField.clear();
        codeBadgeField.requestFocus();
    }

    /**
     * Affiche le panneau de résultat (vert ou rouge) avec message.
     *
     * @param autorise true pour affichage vert, false pour rouge
     * @param message  message principal
     * @param detail   détail additionnel
     */
    private void afficherResultat(boolean autorise, String message, String detail) {
        resultPanel.setVisible(true);
        resultLabel.setText(message);
        resultDetail.setText(detail);
        String style = autorise
                ? "-fx-background-color: #1a4a1a; -fx-border-color: #4caf50; -fx-border-width: 2;"
                : "-fx-background-color: #4a1a1a; -fx-border-color: #e94560; -fx-border-width: 2;";
        resultBox.setStyle(style + " -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8;");
        resultLabel.setStyle(autorise ? "-fx-text-fill: #4caf50; -fx-font-size: 16; -fx-font-weight: bold;"
                : "-fx-text-fill: #e94560; -fx-font-size: 16; -fx-font-weight: bold;");
    }

    /**
     * Joue un son de succès ou d'erreur.
     *
     * @param succes true pour bip succès, false pour double bip d'erreur
     */
    private void jouerSon(boolean succes) {
        new Thread(() -> {
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                toolkit.beep();
                if (!succes) {
                    Thread.sleep(300);
                    toolkit.beep();
                }
            } catch (Exception ignored) {}
        }).start();
    }

    /**
     * Démarre la mise à jour automatique des présents toutes les 5 secondes.
     */
    private void demarrerRefraichissementPresents() {
        presentsTimeline = new Timeline(new KeyFrame(
                Duration.seconds(Constants.PRESENTS_REFRESH_SECONDS),
                e -> Platform.runLater(this::chargerDonnees)));
        presentsTimeline.setCycleCount(Timeline.INDEFINITE);
        presentsTimeline.play();
    }
}
