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
import javafx.scene.Node;
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

    // Navigation buttons
    @FXML private Button navDashboard;
    @FXML private Button navIntervenants;
    @FXML private Button navBadges;
    @FXML private Button navAcces;
    @FXML private Button navZones;
    @FXML private Button navEpi;
    @FXML private Button navRapports;
    @FXML private Button navAdmin;

    private Timeline refreshTimeline;
    private Timeline sessionCheckTimeline;
    private HomeController currentHomeController; // Pour rafraichir les données à distance

    /**
     * Initialise le tableau de bord après le chargement du FXML.
     */
    @FXML
    public void initialize() {
        try {
            LOGGER.info("Initialisation du contrôleur du tableau de bord...");
            
            // 1. Configurer les infos de session (Sidebar)
            configureHeader();
            configureNavigationRoles();
            
            // 2. Charger l'accueil par défaut
            navDashboard();

            // 3. Démarrer les tâches de fond
            demarrerRefraichissement();
            demarrerVerificationSession();
            
            LOGGER.info("Tableau de bord initialisé avec succès.");
        } catch (Exception e) {
            LOGGER.severe("Erreur critique lors de l'initialisation du tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche la vue d'accueil (KPIs et Graphiques).
     */
    @FXML
    private void navDashboard() {
        if (navDashboard != null) activerBouton(navDashboard);
        chargerHome();
    }

    private void chargerHome() {
        try {
            LOGGER.info("Chargement de la vue Accueil...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cm/enspy/gcu/chantier/fxml/home.fxml"));
            Node node = loader.load();
            currentHomeController = loader.getController();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            LOGGER.severe("Erreur chargement home.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure le header avec le nom du chantier et l'utilisateur connecté.
     */
    private void configureHeader() {
        String nomChantier = AppConfig.getInstance().getNomChantier();
        if (headerTitle != null) headerTitle.setText("Chantier Accès — " + nomChantier);
        
        UtilisateurSysteme user = SessionManager.getInstance().getUtilisateurCourant();
        if (user != null) {
            if (userLabel != null) userLabel.setText(user.getLogin());
            if (roleLabel != null) roleLabel.setText(user.getRole() != null ? user.getRole().name() : "");
            LOGGER.info("Header configuré pour utilisateur: " + user.getLogin());
        }
    }

    private void demarrerRefraichissement() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (currentHomeController != null) {
                currentHomeController.chargerDonnees();
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
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

    @FXML private void navIntervenants() { activerBouton(navIntervenants); chargerModule("/cm/enspy/gcu/chantier/fxml/intervenants.fxml"); }
    @FXML private void navBadges() { activerBouton(navBadges); chargerModule("/cm/enspy/gcu/chantier/fxml/badges.fxml"); }
    @FXML private void navAcces() { activerBouton(navAcces); chargerModule("/cm/enspy/gcu/chantier/fxml/controle_acces.fxml"); }

    @FXML private void navZones() { activerBouton(navZones); chargerModule("/cm/enspy/gcu/chantier/fxml/zones.fxml"); }
    @FXML private void navEpi() { activerBouton(navEpi); chargerModule("/cm/enspy/gcu/chantier/fxml/epi.fxml"); }
    @FXML private void navRapports() { activerBouton(navRapports); chargerModule("/cm/enspy/gcu/chantier/fxml/rapports.fxml"); }
    @FXML private void navAdmin() { activerBouton(navAdmin); chargerModule("/cm/enspy/gcu/chantier/fxml/administration.fxml"); }

    /**
     * Gère l'apparence des boutons de navigation pour indiquer la page active.
     */
    private void activerBouton(Button btnActif) {
        Button[] boutons = {navDashboard, navIntervenants, navBadges, navAcces, navZones, navEpi, navRapports, navAdmin};
        for (Button b : boutons) {
            if (b == null) continue;
            if (b == btnActif) {
                b.getStyleClass().add("nav-active");
                b.setStyle(b.getStyle() + "-fx-text-fill: #e94560; -fx-background-color: rgba(233,69,96,0.1);");
            } else {
                b.getStyleClass().remove("nav-active");
                b.setStyle(b.getStyle().replace("-fx-text-fill: #e94560;", "-fx-text-fill: #c0c0d8;")
                                       .replace("-fx-background-color: rgba(233,69,96,0.1);", "-fx-background-color: transparent;"));
            }
        }
    }

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
