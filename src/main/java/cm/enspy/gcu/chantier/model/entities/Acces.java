package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un enregistrement d'accès à une zone du chantier.
 * Chaque passage badge (entrée ou sortie) génère un enregistrement Acces.
 */
public class Acces {

    /** Types d'accès possibles. */
    public enum TypeAcces {
        ENTREE, SORTIE
    }

    /** Statuts possibles d'un accès. */
    public enum StatutAcces {
        AUTORISE, REFUSE
    }

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty intervenantId = new SimpleIntegerProperty();
    private final IntegerProperty badgeId = new SimpleIntegerProperty();
    private final IntegerProperty zoneId = new SimpleIntegerProperty();
    private LocalDateTime dateHeure;
    private TypeAcces type;
    private StatutAcces statut;
    private String motifRefus;
    private Integer agentId;

    // Champs dénormalisés pour affichage
    private String intervenantNom;
    private String zoneNom;
    private String badgeCode;

    /** Constructeur par défaut. */
    public Acces() {}

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getIntervenantId() { return intervenantId.get(); }
    public void setIntervenantId(int intervenantId) { this.intervenantId.set(intervenantId); }

    public int getBadgeId() { return badgeId.get(); }
    public void setBadgeId(int badgeId) { this.badgeId.set(badgeId); }

    public int getZoneId() { return zoneId.get(); }
    public void setZoneId(int zoneId) { this.zoneId.set(zoneId); }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public StringProperty dateHeureProperty() {
        return new SimpleStringProperty(dateHeure != null ? dateHeure.toString().replace("T", " ") : "");
    }

    public TypeAcces getType() { return type; }
    public void setType(TypeAcces type) { this.type = type; }
    public StringProperty typeProperty() {
        return new SimpleStringProperty(type != null ? type.name() : "");
    }

    public StatutAcces getStatut() { return statut; }
    public void setStatut(StatutAcces statut) { this.statut = statut; }
    public StringProperty statutProperty() {
        return new SimpleStringProperty(statut != null ? statut.name() : "");
    }

    public String getMotifRefus() { return motifRefus; }
    public void setMotifRefus(String motifRefus) { this.motifRefus = motifRefus; }
    public StringProperty motifRefusProperty() {
        return new SimpleStringProperty(motifRefus != null ? motifRefus : "");
    }

    public Integer getAgentId() { return agentId; }
    public void setAgentId(Integer agentId) { this.agentId = agentId; }

    public String getIntervenantNom() { return intervenantNom; }
    public void setIntervenantNom(String intervenantNom) { this.intervenantNom = intervenantNom; }
    public StringProperty intervenantNomProperty() {
        return new SimpleStringProperty(intervenantNom != null ? intervenantNom : "");
    }

    public String getZoneNom() { return zoneNom; }
    public void setZoneNom(String zoneNom) { this.zoneNom = zoneNom; }
    public StringProperty zoneNomProperty() {
        return new SimpleStringProperty(zoneNom != null ? zoneNom : "");
    }

    public String getBadgeCode() { return badgeCode; }
    public void setBadgeCode(String badgeCode) { this.badgeCode = badgeCode; }
    public StringProperty badgeCodeProperty() {
        return new SimpleStringProperty(badgeCode != null ? badgeCode : "");
    }
}
