package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.model.dao.ZoneDAO;
import cm.enspy.gcu.chantier.model.entities.Acces;
import cm.enspy.gcu.chantier.model.entities.Zone;
import cm.enspy.gcu.chantier.service.RapportService;
import cm.enspy.gcu.chantier.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur du module de génération des rapports.
 * Prend en charge les rapports journaliers, hebdomadaires, mensuels, et d'incidents.
 */
public class RapportController {

    @FXML private RadioButton radioJournalier;
    @FXML private RadioButton radioHebdomadaire;
    @FXML private RadioButton radioMensuel;
    @FXML private RadioButton radioIncidents;
    @FXML private ToggleGroup typeRapportGroup;

    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<Zone> zoneFiltreCombo;
    @FXML private ComboBox<String> statutFiltreCombo;
    @FXML private TextArea aperçuArea;
    @FXML private Label statusLabel;

    private final RapportService rapportService = new RapportService();
    private final ZoneDAO zoneDAO = new ZoneDAO();
    private List<Acces> accesCourants = null;

    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize() {
        typeRapportGroup = new ToggleGroup();
        radioJournalier.setToggleGroup(typeRapportGroup);
        radioHebdomadaire.setToggleGroup(typeRapportGroup);
        radioMensuel.setToggleGroup(typeRapportGroup);
        radioIncidents.setToggleGroup(typeRapportGroup);
        radioJournalier.setSelected(true);

        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now());

        List<Zone> zones = zoneDAO.findActives();
        zoneFiltreCombo.setItems(FXCollections.observableArrayList(zones));
        zoneFiltreCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Zone z) { return z != null ? z.getNom() : "Toutes"; }
            @Override public Zone fromString(String s) { return null; }
        });

        statutFiltreCombo.setItems(FXCollections.observableArrayList("Tous", "AUTORISE", "REFUSE"));
        statutFiltreCombo.setValue("Tous");

        aperçuArea.setEditable(false);
        statusLabel.setText("");
    }

    /**
     * Génère l'aperçu du rapport dans la TextArea.
     */
    @FXML
    private void handleGenererAperçu() {
        SessionManager.getInstance().rafraichirActivite();
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();
        if (debut == null) debut = LocalDate.now();
        if (fin == null) fin = LocalDate.now();

        String titre;
        if (radioJournalier.isSelected()) {
            titre = "Rapport Journalier — " + debut;
            accesCourants = rapportService.genererRapportJournalier(debut);
        } else if (radioHebdomadaire.isSelected()) {
            titre = "Rapport Hebdomadaire — Semaine de " + debut;
            accesCourants = rapportService.genererRapportHebdomadaire(debut);
        } else if (radioMensuel.isSelected()) {
            titre = "Rapport Mensuel — " + debut.getMonth() + " " + debut.getYear();
            accesCourants = rapportService.genererRapportMensuel(debut);
        } else {
            titre = "Rapport d'Incidents — " + debut + " au " + fin;
            accesCourants = rapportService.genererRapportIncidents(debut, fin);
        }

        aperçuArea.setText(rapportService.formaterResume(accesCourants, titre));
        statusLabel.setText(accesCourants.size() + " enregistrement(s) trouvé(s).");
    }

    /**
     * Exporte le rapport courant en PDF.
     */
    @FXML
    private void handleExporterPDF() {
        if (accesCourants == null) {
            new Alert(Alert.AlertType.WARNING, "Générez d'abord un aperçu.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            String chemin = rapportService.exporterPDF(accesCourants,
                    "Rapport Chantier Accès", "rapport_acces");
            statusLabel.setText("PDF exporté: " + chemin);
            new Alert(Alert.AlertType.INFORMATION, "PDF exporté:\n" + chemin, ButtonType.OK).showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur export PDF: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /**
     * Exporte le rapport courant en CSV.
     */
    @FXML
    private void handleExporterCSV() {
        if (accesCourants == null) {
            new Alert(Alert.AlertType.WARNING, "Générez d'abord un aperçu.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            String chemin = rapportService.exporterCSV(accesCourants, "rapport_acces");
            statusLabel.setText("CSV exporté: " + chemin);
            new Alert(Alert.AlertType.INFORMATION, "CSV exporté:\n" + chemin, ButtonType.OK).showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur export CSV: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}
