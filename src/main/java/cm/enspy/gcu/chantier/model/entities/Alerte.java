package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une alerte de sécurité générée automatiquement
 * ou manuellement par le système.
 */
public class Alerte {

    /** Types d'alertes possibles. */
    public enum TypeAlerte {
        BADGE_EXPIRE, BADGE_EXPIRANT, EPI_MANQUANT, ACCES_REFUSE_MULTIPLE,
        INTERVENANT_SUSPENDU, CAPACITE_DEPASSEE, AUTRE
    }

    /** Niveaux de gravité. */
    public enum NiveauGravite {
        INFO, AVERTISSEMENT, CRITIQUE
    }

    private final IntegerProperty id = new SimpleIntegerProperty();
    private String type;
    private final StringProperty message = new SimpleStringProperty();
    private LocalDateTime dateHeure;
    private final BooleanProperty estLue = new SimpleBooleanProperty(false);
    private NiveauGravite niveauGravite = NiveauGravite.INFO;
    private Integer sourceId;

    /** Constructeur par défaut. */
    public Alerte() {}

    /**
     * Constructeur principal.
     *
     * @param type          type de l'alerte
     * @param message       message descriptif
     * @param niveauGravite niveau de gravité
     * @param sourceId      identifiant de l'entité source (optionnel)
     */
    public Alerte(String type, String message, NiveauGravite niveauGravite, Integer sourceId) {
        this.type = type;
        this.message.set(message);
        this.niveauGravite = niveauGravite;
        this.sourceId = sourceId;
        this.dateHeure = LocalDateTime.now();
    }

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public StringProperty typeProperty() { return new SimpleStringProperty(type != null ? type : ""); }

    public String getMessage() { return message.get(); }
    public void setMessage(String message) { this.message.set(message); }
    public StringProperty messageProperty() { return message; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public StringProperty dateHeureProperty() {
        return new SimpleStringProperty(dateHeure != null
                ? dateHeure.toString().replace("T", " ") : "");
    }

    public boolean isEstLue() { return estLue.get(); }
    public void setEstLue(boolean estLue) { this.estLue.set(estLue); }
    public BooleanProperty estLueProperty() { return estLue; }

    public NiveauGravite getNiveauGravite() { return niveauGravite; }
    public void setNiveauGravite(NiveauGravite niveauGravite) { this.niveauGravite = niveauGravite; }
    public StringProperty niveauGraviteProperty() {
        return new SimpleStringProperty(niveauGravite != null ? niveauGravite.name() : "");
    }

    public Integer getSourceId() { return sourceId; }
    public void setSourceId(Integer sourceId) { this.sourceId = sourceId; }
}
