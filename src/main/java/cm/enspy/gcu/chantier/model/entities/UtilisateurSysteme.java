package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un compte utilisateur du système de gestion.
 * Distinct des intervenants, un utilisateur système gère l'application.
 */
public class UtilisateurSysteme {

    /** Rôles disponibles dans le système. */
    public enum Role {
        ADMIN, RESPONSABLE, AGENT, CONSULTANT
    }

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty login = new SimpleStringProperty();
    private String motDePasseHash;
    private Role role;
    private final BooleanProperty estActif = new SimpleBooleanProperty(true);
    private LocalDateTime derniereConnexion;

    /** Constructeur par défaut. */
    public UtilisateurSysteme() {}

    /**
     * Constructeur avec les champs principaux.
     *
     * @param login          identifiant de connexion unique
     * @param motDePasseHash empreinte SHA-256 du mot de passe
     * @param role           rôle de l'utilisateur dans le système
     */
    public UtilisateurSysteme(String login, String motDePasseHash, Role role) {
        this.login.set(login);
        this.motDePasseHash = motDePasseHash;
        this.role = role;
    }

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getLogin() { return login.get(); }
    public void setLogin(String login) { this.login.set(login); }
    public StringProperty loginProperty() { return login; }

    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public StringProperty roleProperty() {
        return new SimpleStringProperty(role != null ? role.name() : "");
    }

    public boolean isEstActif() { return estActif.get(); }
    public void setEstActif(boolean estActif) { this.estActif.set(estActif); }
    public BooleanProperty estActifProperty() { return estActif; }
    public StringProperty statutProperty() {
        return new SimpleStringProperty(estActif.get() ? "ACTIF" : "INACTIF");
    }

    public LocalDateTime getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }
    public StringProperty derniereConnexionProperty() {
        return new SimpleStringProperty(derniereConnexion != null
                ? derniereConnexion.toString().replace("T", " ") : "Jamais");
    }

    @Override
    public String toString() {
        return login.get();
    }
}
