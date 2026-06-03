package cm.enspy.gcu.chantier;

import cm.enspy.gcu.chantier.utils.DatabaseManager;
import cm.enspy.gcu.chantier.utils.SessionManager;
import cm.enspy.gcu.chantier.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Point d'entrée principal de l'application JavaFX Chantier Accès.
 * Initialise la base de données, charge l'écran de connexion et démarre l'application.
 */
public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    /** Stage principal partagé entre les contrôleurs. */
    private static Stage primaryStage;

    /**
     * Initialise la base de données avant le démarrage de l'interface.
     *
     * @throws Exception en cas d'erreur d'initialisation
     */
    @Override
    public void init() throws Exception {
        super.init();
        DatabaseManager.getInstance().initialize();
        LOGGER.info("Base de données initialisée avec succès.");
    }

    /**
     * Démarre l'interface graphique en affichant l'écran de connexion.
     *
     * @param stage le stage principal JavaFX
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            showLoginScreen();
        } catch (IOException e) {
            LOGGER.severe("Impossible de charger l'écran de connexion: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Affiche l'écran de connexion.
     *
     * @throws IOException si le fichier FXML est introuvable
     */
    public static void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/cm/enspy/gcu/chantier/fxml/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(
                MainApp.class.getResource("/cm/enspy/gcu/chantier/css/style.css").toExternalForm());
        primaryStage.setTitle("Chantier Accès — Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Affiche le tableau de bord principal après une connexion réussie.
     *
     * @throws IOException si le fichier FXML est introuvable
     */
    public static void showDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/cm/enspy/gcu/chantier/fxml/dashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(
                MainApp.class.getResource("/cm/enspy/gcu/chantier/css/style.css").toExternalForm());
        String chantierNom = AppConfig.getInstance().getNomChantier();
        primaryStage.setTitle("Chantier Accès v1.0 — " + chantierNom);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Retourne le stage principal.
     *
     * @return le stage principal de l'application
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Nettoyage des ressources à la fermeture.
     */
    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
        SessionManager.getInstance().deconnect();
        LOGGER.info("Application fermée proprement.");
    }

    /**
     * Point d'entrée Java standard.
     *
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}
