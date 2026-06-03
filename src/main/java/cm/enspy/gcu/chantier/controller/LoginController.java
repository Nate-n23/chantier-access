package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.MainApp;
import cm.enspy.gcu.chantier.model.entities.UtilisateurSysteme;
import cm.enspy.gcu.chantier.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Contrôleur de l'écran de connexion.
 * Gère l'authentification des utilisateurs système.
 */
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label titleLabel;

    private final AuthService authService = new AuthService();

    /**
     * Initialise le contrôleur après chargement du FXML.
     * Configure le message d'erreur comme invisible par défaut.
     */
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loginField.requestFocus();
    }

    /**
     * Gère la touche Entrée dans les champs de saisie.
     *
     * @param event événement clavier
     */
    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(null);
        }
    }

    /**
     * Traite la tentative de connexion.
     *
     * @param event événement d'action du bouton
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login == null || login.isBlank()) {
            afficherErreur("Veuillez saisir votre identifiant.");
            return;
        }
        if (password == null || password.isBlank()) {
            afficherErreur("Veuillez saisir votre mot de passe.");
            return;
        }

        loginButton.setDisable(true);
        UtilisateurSysteme user = authService.login(login, password);

        if (user != null) {
            errorLabel.setVisible(false);
            LOGGER.info("Connexion réussie: " + login);
            try {
                MainApp.showDashboard();
            } catch (IOException e) {
                afficherErreur("Erreur lors du chargement du tableau de bord: " + e.getMessage());
                loginButton.setDisable(false);
            }
        } else {
            afficherErreur("Identifiant ou mot de passe incorrect.");
            passwordField.clear();
            loginButton.setDisable(false);
        }
    }

    /**
     * Affiche un message d'erreur sous le formulaire.
     *
     * @param message texte de l'erreur
     */
    private void afficherErreur(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
