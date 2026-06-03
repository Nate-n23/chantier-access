package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un équipement de protection individuelle (EPI)
 * attribué à un intervenant.
 */
public class Epi {

    /** État possible d'un EPI. */
    public enum EtatEpi {
        BON, USAGE, A_REMPLACER
    }

    /** Types d'EPI gérés par l'application. */
    public enum TypeEpi {
        CASQUE, GILET, CHAUSSURES, HARNAIS, LUNETTES, GANTS
    }

    private final IntegerProperty id = new SimpleIntegerProperty();
    private TypeEpi type;
    private final StringProperty taille = new SimpleStringProperty();
    private EtatEpi etat = EtatEpi.BON;
    private LocalDateTime dateAttribution;
    private final IntegerProperty intervenantId = new SimpleIntegerProperty();
    private Intervenant intervenant;

    /** Constructeur par défaut. */
    public Epi() {}

    /**
     * Constructeur principal.
     *
     * @param type          type d'EPI
     * @param taille        taille de l'EPI
     * @param intervenantId identifiant de l'intervenant attributaire
     */
    public Epi(TypeEpi type, String taille, int intervenantId) {
        this.type = type;
        this.taille.set(taille);
        this.intervenantId.set(intervenantId);
        this.dateAttribution = LocalDateTime.now();
    }

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public TypeEpi getType() { return type; }
    public void setType(TypeEpi type) { this.type = type; }
    public StringProperty typeProperty() {
        return new SimpleStringProperty(type != null ? type.name() : "");
    }

    public String getTaille() { return taille.get(); }
    public void setTaille(String taille) { this.taille.set(taille); }
    public StringProperty tailleProperty() { return taille; }

    public EtatEpi getEtat() { return etat; }
    public void setEtat(EtatEpi etat) { this.etat = etat; }
    public StringProperty etatProperty() {
        return new SimpleStringProperty(etat != null ? etat.name() : "");
    }

    public LocalDateTime getDateAttribution() { return dateAttribution; }
    public void setDateAttribution(LocalDateTime dateAttribution) { this.dateAttribution = dateAttribution; }
    public StringProperty dateAttributionProperty() {
        return new SimpleStringProperty(dateAttribution != null
                ? dateAttribution.toString().replace("T", " ") : "");
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
}
