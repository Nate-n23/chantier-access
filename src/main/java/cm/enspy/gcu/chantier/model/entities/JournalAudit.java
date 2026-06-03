package cm.enspy.gcu.chantier.model.entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une entrée dans le journal d'audit du système.
 * Chaque modification importante est tracée avec l'utilisateur responsable.
 */
public class JournalAudit {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty utilisateurId = new SimpleIntegerProperty();
    private final StringProperty action = new SimpleStringProperty();
    private final StringProperty tableCiblee = new SimpleStringProperty();
    private LocalDateTime dateHeure;
    private String ancienneValeur;
    private String nouvelleValeur;
    private String ipMachine;

    // Champ dénormalisé pour affichage
    private String utilisateurLogin;

    /** Constructeur par défaut. */
    public JournalAudit() {}

    /**
     * Constructeur principal.
     *
     * @param utilisateurId  identifiant de l'utilisateur ayant effectué l'action
     * @param action         description de l'action réalisée
     * @param tableCiblee    table de base de données concernée
     * @param ancienneValeur ancienne valeur (JSON), peut être null
     * @param nouvelleValeur nouvelle valeur (JSON), peut être null
     * @param ipMachine      adresse IP de la machine
     */
    public JournalAudit(int utilisateurId, String action, String tableCiblee,
                        String ancienneValeur, String nouvelleValeur, String ipMachine) {
        this.utilisateurId.set(utilisateurId);
        this.action.set(action);
        this.tableCiblee.set(tableCiblee);
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.ipMachine = ipMachine;
        this.dateHeure = LocalDateTime.now();
    }

    // --- Propriétés JavaFX ---

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getUtilisateurId() { return utilisateurId.get(); }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId.set(utilisateurId); }
    public IntegerProperty utilisateurIdProperty() { return utilisateurId; }

    public String getAction() { return action.get(); }
    public void setAction(String action) { this.action.set(action); }
    public StringProperty actionProperty() { return action; }

    public String getTableCiblee() { return tableCiblee.get(); }
    public void setTableCiblee(String tableCiblee) { this.tableCiblee.set(tableCiblee); }
    public StringProperty tableCibleeProperty() { return tableCiblee; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public StringProperty dateHeureProperty() {
        return new SimpleStringProperty(dateHeure != null
                ? dateHeure.toString().replace("T", " ") : "");
    }

    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }
    public StringProperty ancienneValeurProperty() {
        return new SimpleStringProperty(ancienneValeur != null ? ancienneValeur : "");
    }

    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }
    public StringProperty nouvelleValeurProperty() {
        return new SimpleStringProperty(nouvelleValeur != null ? nouvelleValeur : "");
    }

    public String getIpMachine() { return ipMachine; }
    public void setIpMachine(String ipMachine) { this.ipMachine = ipMachine; }
    public StringProperty ipMachineProperty() {
        return new SimpleStringProperty(ipMachine != null ? ipMachine : "");
    }

    public String getUtilisateurLogin() { return utilisateurLogin; }
    public void setUtilisateurLogin(String utilisateurLogin) { this.utilisateurLogin = utilisateurLogin; }
    public StringProperty utilisateurLoginProperty() {
        return new SimpleStringProperty(utilisateurLogin != null ? utilisateurLogin : "");
    }
}
