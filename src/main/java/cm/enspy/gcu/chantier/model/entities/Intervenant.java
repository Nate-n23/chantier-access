package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant un intervenant sur le chantier.
 * Un intervenant est toute personne ayant accès au chantier (ouvrier, visiteur, etc.).
 */
public class Intervenant {

    /** Catégories possibles d'un intervenant. */
    public enum Categorie {
        OUVRIER, TECHNICIEN, INGENIEUR, VISITEUR, SOUS_TRAITANT
    }

    /** Statuts possibles d'un intervenant. */
    public enum Statut {
        ACTIF, SUSPENDU, EXPULSE, ARCHIVE
    }

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty matricule = new SimpleStringProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty prenom = new SimpleStringProperty();
    private LocalDate dateNaissance;
    private final StringProperty telephone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty entreprise = new SimpleStringProperty();
    private Categorie categorie;
    private Statut statut = Statut.ACTIF;
    private String photoPath;
    private final IntegerProperty niveauHabilitation = new SimpleIntegerProperty(1);
    private LocalDateTime dateCreation;

    /** Constructeur par défaut. */
    public Intervenant() {}

    /**
     * Constructeur complet.
     *
     * @param matricule            matricule unique
     * @param nom                  nom de famille
     * @param prenom               prénom
     * @param entreprise           entreprise d'appartenance
     * @param categorie            catégorie de l'intervenant
     * @param niveauHabilitation   niveau d'habilitation (1-4)
     */
    public Intervenant(String matricule, String nom, String prenom,
                       String entreprise, Categorie categorie, int niveauHabilitation) {
        this.matricule.set(matricule);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.entreprise.set(entreprise);
        this.categorie = categorie;
        this.niveauHabilitation.set(niveauHabilitation);
    }

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getMatricule() { return matricule.get(); }
    public void setMatricule(String matricule) { this.matricule.set(matricule); }
    public StringProperty matriculeProperty() { return matricule; }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public StringProperty prenomProperty() { return prenom; }

    public String getNomComplet() { return prenom.get() + " " + nom.get(); }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public StringProperty telephoneProperty() { return telephone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public String getEntreprise() { return entreprise.get(); }
    public void setEntreprise(String entreprise) { this.entreprise.set(entreprise); }
    public StringProperty entrepriseProperty() { return entreprise; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public StringProperty categorieProperty() {
        return new SimpleStringProperty(categorie != null ? categorie.name() : "");
    }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
    public StringProperty statutProperty() {
        return new SimpleStringProperty(statut != null ? statut.name() : "");
    }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public int getNiveauHabilitation() { return niveauHabilitation.get(); }
    public void setNiveauHabilitation(int niveau) { this.niveauHabilitation.set(niveau); }
    public IntegerProperty niveauHabilitationProperty() { return niveauHabilitation; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return getNomComplet() + " (" + getMatricule() + ")";
    }
}
