package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;

/**
 * Entité représentant une zone du chantier avec son niveau de sécurité.
 * Les zones définissent les périmètres d'accès contrôlés.
 */
public class Zone {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty niveauSecurite = new SimpleIntegerProperty(1);
    private final IntegerProperty capaciteMax = new SimpleIntegerProperty(0);
    private final BooleanProperty estActive = new SimpleBooleanProperty(true);

    /** Constructeur par défaut. */
    public Zone() {}

    /**
     * Constructeur avec les champs principaux.
     *
     * @param nom            nom de la zone
     * @param description    description de la zone
     * @param niveauSecurite niveau de sécurité (1-4)
     * @param capaciteMax    capacité maximale d'occupants
     */
    public Zone(String nom, String description, int niveauSecurite, int capaciteMax) {
        this.nom.set(nom);
        this.description.set(description);
        this.niveauSecurite.set(niveauSecurite);
        this.capaciteMax.set(capaciteMax);
    }

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public int getNiveauSecurite() { return niveauSecurite.get(); }
    public void setNiveauSecurite(int niveauSecurite) { this.niveauSecurite.set(niveauSecurite); }
    public IntegerProperty niveauSecuriteProperty() { return niveauSecurite; }

    public int getCapaciteMax() { return capaciteMax.get(); }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax.set(capaciteMax); }
    public IntegerProperty capaciteMaxProperty() { return capaciteMax; }

    public boolean isEstActive() { return estActive.get(); }
    public void setEstActive(boolean estActive) { this.estActive.set(estActive); }
    public BooleanProperty estActiveProperty() { return estActive; }
    public StringProperty statutProperty() {
        return new SimpleStringProperty(estActive.get() ? "ACTIVE" : "INACTIVE");
    }

    @Override
    public String toString() {
        return nom.get();
    }
}
