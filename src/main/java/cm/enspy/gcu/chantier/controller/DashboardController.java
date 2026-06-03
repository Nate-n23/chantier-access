package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.MainApp;
import cm.enspy.gcu.chantier.config.AppConfig;
import cm.enspy.gcu.chantier.config.Constants;
import cm.enspy.gcu.chantier.model.entities.Acces;
import cm.enspy.gcu.chantier.model.entities.Alerte;
import cm.enspy.gcu.chantier.model.entities.UtilisateurSysteme;
import cm.enspy.gcu.chantier.service.AccesService;
import cm.enspy.gcu.chantier.service.AlerteService;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Contrôleur du tableau de bord principal.
 * Affiche les KPIs, le graphique de fréquentation et les alertes. Se rafraîchit toutes les 30 secondes.
 */
public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private BorderPane mainPane;
    @FXML private StackPane contentArea;
    @FXML private Label headerTitle;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;

    // KPI Cards
    @FXML private Label kpiPresents;
    @FXML private Label kpiAlertes;
    @FXML private Label kpiBadgesExpires;
    @FXML private Label kpiIncidents;

    // Chart
    @FXML private BarChart<String, Number> frequentationChart;

    // Tables
    @FXML private TableView<Acces> derniersAccesTable;
    @FXML private TableColumn<Acces, String> colAccesDate;
    @FXML private TableColumn<Acces, String> colAccesIntervenant;
    @FXML private TableColumn<Acces, String> colAccesZone;
    @FXML private TableColumn<Acces, String> colAccesType;
    @FXML private TableColumn<Acces, String> colAccesStatut;

    @FXML private TableView<Alerte> alertesTable;
    @FXML private TableColumn<Alerte, String> colAlerteDate;
    @FXML private TableColumn<Alerte, String> colAlerteMessage;
    @FXML private TableColumn<Alerte, String> colAlerteGravite;

    // Navigation buttons
    @FXML private Button navIntervenants;
    @FXML private Button navBadges;
    @FXML private Button navAcces;
    @FXML private Button navZones;
    @FXML private Button navEpi;
    @FXML private Button navRapports;
    @FXML private Button navAdmin;

    private final AccesService accesService = new AccesService();
    private final AlerteService alerteService = new AlerteService();
    private Timeline refreshTimeline;
    private Timeline sessionCheckTimeline;

    /**
     * Initialise le tableau de bord après le chargement du FXML.
     */
    @FXML
    public void initialize() {
        try {
            LOGGER.info("Initialisation du contrôleur du tableau de bord...");
            configureHeader();
            configureNavigationRoles();
            configureTableColumns();
            chargerDonnees();
            demarrerRefraichissement();
            demarrerVerificationSession();
            LOGGER.info("Tableau de bord initialisé avec succès.");
        } catch (Exception e) {
            LOGGER.severe("Erreur critique lors de l'initialisation du tableau de bord: " + e.getMessage());
            e.printStackTrace();
            // On affiche quand même une partie de l'interface si possible
        }
    }


    /**
     * Configure le header avec le nom du chantier et l'utilisateur connecté.
     */
    private void configureHeader() {
        String nomChantier = AppConfig.getInstance().getNomChantier();
        headerTitle.setText("Chantier Accès — " + nomChantier);
        UtilisateurSysteme user = SessionManager.getInstance().getUtilisateurCourant();
        if (user != null) {
            userLabel.setText(user.getLogin());
            roleLabel.setText(user.getRole() != null ? user.getRole().name() : "");
        }
    }

    /**
     * Adapte la navigation selon le rôle de l'utilisateur.
     * Les CONSULTANT ne voient que le Dashboard et les Rapports.
     */
    private void configureNavigationRoles() {
        if (SessionManager.getInstance().isConsultantOnly()) {
            navIntervenants.setVisible(false);
            navBadges.setVisible(false);
            navAcces.setVisible(false);
            navZones.setVisible(false);
            navEpi.setVisible(false);
            navAdmin.setVisible(false);
        }
        if (!SessionManager.getInstance().isAdmin()) {
            navAdmin.setVisible(false);
        }
    }

    /**
     * Configure les colonnes des tables du tableau de bord.
     */
    private void configureTableColumns() {
        colAccesDate.setCellValueFactory(c -> c.getValue().dateHeureProperty());
        colAccesIntervenant.setCellValueFactory(c -> c.getValue().intervenantNomProperty());
        colAccesZone.setCellValueFactory(c -> c.getValue().zoneNomProperty());
        colAccesType.setCellValueFactory(c -> c.getValue().typeProperty());
        colAccesStatut.setCellValueFactory(c -> c.getValue().statutProperty());

        colAlerteDate.setCellValueFactory(c -> c.getValue().dateHeureProperty());
        colAlerteMessage.setCellValueFactory(c -> c.getValue().messageProperty());
        colAlerteGravite.setCellValueFactory(c -> c.getValue().niveauGraviteProperty());

        // Clic sur alerte pour la marquer comme lue
        alertesTable.setRowFactory(tv -> {
            TableRow<Alerte> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Alerte alerte = row.getItem();
                    alerteService.marquerCommeLue(alerte.getId());
                    chargerDonnees();
                }
            });
            return row;
        });
    }

    /**
     * Charge toutes les données affichées sur le tableau de bord.
     */
    private void chargerDonnees() {
        try {
            // KPIs
            kpiPresents.setText(String.valueOf(accesService.countPresentsAujourdhui()));
            kpiAlertes.setText(String.valueOf(alerteService.countAlertesNonLues()));
            kpiBadgesExpires.setText("...");
            kpiIncidents.setText(String.valueOf(accesService.countIncidentsAujourdhui()));
        } catch (Exception e) {
            LOGGER.warning("Erreur chargement KPIs: " + e.getMessage());
        }

        try {
            // Derniers accès
            List<Acces> derniers = accesService.getDerniersAcces(Constants.MAX_DERNIERS_ACCES);
            derniersAccesTable.setItems(FXCollections.observableArrayList(derniers));
        } catch (Exception e) {
            LOGGER.warning("Erreur chargement derniers accès: " + e.getMessage());
        }

        try {
            // Alertes non lues
            List<Alerte> alertes = alerteService.getAlertesNonLues();
            alertesTable.setItems(FXCollections.observableArrayList(alertes));
        } catch (Exception e) {
            LOGGER.warning("Erreur chargement alertes: " + e.getMessage());
        }

        try {
            // Graphique de fréquentation
            chargerGraphique();
        } catch (Exception e) {
            LOGGER.warning("Erreur chargement graphique: " + e.getMessage());
        }
    }


    /**
     * Charge le graphique de fréquentation des 7 derniers jours.
     */
    private void chargerGraphique() {
        frequentationChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Passages");
        List<Object[]> freq = accesService.getFrequentationParZone(7);
        for (Object[] row : freq) {
            series.getData().add(new XYChart.Data<>((String) row[0], (Integer) row[1]));
        }
        frequentationChart.getData().add(series);
    }

    /**
     * Démarre le rafraîchissement automatique toutes les 30 secondes.
     */
    private void demarrerRefraichissement() {
        refreshTimeline = new Timeline(new KeyFrame(
                Duration.seconds(Constants.DASHBOARD_REFRESH_SECONDS),
                e -> Platform.runLater(this::chargerDonnees)));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    /**
     * Démarre la vérification périodique du timeout de session.
     */
    private void demarrerVerificationSession() {
        sessionCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> {
            if (SessionManager.getInstance().isSessionExpiree()) {
                Platform.runLater(() -> {
                    stopTimelines();
                    try {
                        MainApp.showLoginScreen();
                    } catch (IOException ex) {
                        LOGGER.severe("Erreur retour login: " + ex.getMessage());
                    }
                });
            } else {
                SessionManager.getInstance().rafraichirActivite();
            }
        }));
        sessionCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        sessionCheckTimeline.play();
    }

    /** Arrête toutes les timelines actives. */
    private void stopTimelines() {
        if (refreshTimeline != null) refreshTimeline.stop();
        if (sessionCheckTimeline != null) sessionCheckTimeline.stop();
    }

    // ==================== Navigation ====================

    /** Charge le module Intervenants. */
    @FXML private void navIntervenants() { chargerModule("/cm/enspy/gcu/chantier/fxml/intervenants.fxml"); }
    /** Charge le module Badges. */
    @FXML private void navBadges() { chargerModule("/cm/enspy/gcu/chantier/fxml/badges.fxml"); }
    /** Charge le module Contrôle d'accès. */
    @FXML private void navAcces() { chargerModule("/cm/enspy/gcu/chantier/fxml/controle_acces.fxml"); }
    /** Charge le module Zones. */
    @FXML private void navZones() { chargerModule("/cm/enspy/gcu/chantier/fxml/zones.fxml"); }
    /** Charge le module EPI. */
    @FXML private void navEpi() { chargerModule("/cm/enspy/gcu/chantier/fxml/epi.fxml"); }
    /** Charge le module Rapports. */
    @FXML private void navRapports() { chargerModule("/cm/enspy/gcu/chantier/fxml/rapports.fxml"); }
    /** Charge le module Administration. */
    @FXML private void navAdmin() { chargerModule("/cm/enspy/gcu/chantier/fxml/administration.fxml"); }

    /**
     * Déconnecte l'utilisateur et retourne à l'écran de connexion.
     */
    @FXML
    private void handleLogout() {
        stopTimelines();
        SessionManager.getInstance().deconnect();
        try {
            MainApp.showLoginScreen();
        } catch (IOException e) {
            LOGGER.severe("Erreur déconnexion: " + e.getMessage());
        }
    }

    /**
     * Charge un module FXML dans la zone de contenu centrale.
     *
     * @param fxmlPath chemin vers le fichier FXML du module
     */
    private void chargerModule(String fxmlPath) {
        SessionManager.getInstance().rafraichirActivite();
        try {
            LOGGER.info("Chargement du module: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vue = loader.load();
            contentArea.getChildren().setAll(vue);
            LOGGER.info("Module " + fxmlPath + " chargé avec succès.");
        } catch (Exception e) {
            LOGGER.severe("ERREUR CRITIQUE lors du chargement du module " + fxmlPath);
            LOGGER.severe("Message: " + e.getMessage());
            if (e.getCause() != null) {
                LOGGER.severe("Cause réelle: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Impossible de charger le module " + fxmlPath + ":\n" + 
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage())).showAndWait();
        }
    }

}
