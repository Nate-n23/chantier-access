package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant un badge d'accès au chantier.
 * Un badge est associé à un intervenant et définit ses zones d'accès autorisées.
 */
public class Badge {

    /** Types de badge possibles. */
    public enum TypeBadge {
        PERMANENT, TEMPORAIRE
    }

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty code = new SimpleStringProperty();
    private TypeBadge typeBadge;
    private LocalDateTime dateCreation;
    private LocalDate dateExpiration;
    private final BooleanProperty estActif = new SimpleBooleanProperty(true);
    private final IntegerProperty intervenantId = new SimpleIntegerProperty();
    private Intervenant intervenant;

    /** Constructeur par défaut. */
    public Badge() {}

    /**
     * Vérifie si le badge est valide (actif et non expiré).
     *
     * @return true si le badge est actuellement utilisable, false sinon
     */
    public boolean isValide() {
        if (!estActif.get()) return false;
        if (dateExpiration == null) return true;
        return !LocalDate.now().isAfter(dateExpiration);
    }

    /**
     * Vérifie si le badge expire dans les prochains jours.
     *
     * @param jours nombre de jours à vérifier
     * @return true si le badge expire dans les {@code jours} prochains jours
     */
    public boolean expireDans(int jours) {
        if (dateExpiration == null) return false;
        LocalDate limite = LocalDate.now().plusDays(jours);
        return !dateExpiration.isAfter(limite) && !LocalDate.now().isAfter(dateExpiration);
    }

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getCode() { return code.get(); }
    public void setCode(String code) { this.code.set(code); }
    public StringProperty codeProperty() { return code; }

    public TypeBadge getTypeBadge() { return typeBadge; }
    public void setTypeBadge(TypeBadge typeBadge) { this.typeBadge = typeBadge; }
    public StringProperty typeBadgeProperty() {
        return new SimpleStringProperty(typeBadge != null ? typeBadge.name() : "");
    }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public StringProperty dateExpirationProperty() {
        return new SimpleStringProperty(dateExpiration != null ? dateExpiration.toString() : "");
    }

    public boolean isEstActif() { return estActif.get(); }
    public void setEstActif(boolean estActif) { this.estActif.set(estActif); }
    public BooleanProperty estActifProperty() { return estActif; }
    public StringProperty statutProperty() {
        return new SimpleStringProperty(estActif.get() ? "ACTIF" : "INACTIF");
    }

    public int getIntervenantId() { return intervenantId.get(); }
    public void setIntervenantId(int intervenantId) { this.intervenantId.set(intervenantId); }
    public IntegerProperty intervenantIdProperty() { return intervenantId; }

    public Intervenant getIntervenant() { return intervenant; }
    public void setIntervenant(Intervenant intervenant) {
        this.intervenant = intervenant;
        if (intervenant != null) this.intervenantId.set(intervenant.getId());
    }

    public StringProperty intervenantNomProperty() {
        return new SimpleStringProperty(intervenant != null ? intervenant.getNomComplet() : "");
    }

    @Override
    public String toString() {
        return code.get();
    }
}
