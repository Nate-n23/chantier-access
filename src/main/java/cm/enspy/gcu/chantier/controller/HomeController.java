package cm.enspy.gcu.chantier.controller;

import cm.enspy.gcu.chantier.model.entities.Acces;
import cm.enspy.gcu.chantier.model.entities.Alerte;
import cm.enspy.gcu.chantier.service.AccesService;
import cm.enspy.gcu.chantier.service.AlerteService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.logging.Logger;

/**
 * Contrôleur de la vue d'accueil (Dashboard Content).
 */
public class HomeController {
    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    @FXML private Label kpiPresents;
    @FXML private Label kpiAlertes;
    @FXML private Label kpiBadgesExpires;
    @FXML private Label kpiIncidents;
    @FXML private BarChart<String, Number> frequentationChart;

    @FXML private TableView<Alerte> alertesTable;
    @FXML private TableColumn<Alerte, String> colAlerteDate;
    @FXML private TableColumn<Alerte, String> colAlerteMessage;
    @FXML private TableColumn<Alerte, String> colAlerteGravite;

    @FXML private TableView<Acces> derniersAccesTable;
    @FXML private TableColumn<Acces, String> colAccesDate;
    @FXML private TableColumn<Acces, String> colAccesIntervenant;
    @FXML private TableColumn<Acces, String> colAccesZone;
    @FXML private TableColumn<Acces, String> colAccesType;
    @FXML private TableColumn<Acces, String> colAccesStatut;

    private final AccesService accesService = new AccesService();
    private final AlerteService alerteService = new AlerteService();

    @FXML
    public void initialize() {
        configureTableColumns();
        chargerDonnees();
    }

    private void configureTableColumns() {
        colAccesDate.setCellValueFactory(c -> c.getValue().dateHeureProperty());
        colAccesIntervenant.setCellValueFactory(c -> c.getValue().intervenantNomProperty());
        colAccesZone.setCellValueFactory(c -> c.getValue().zoneNomProperty());
        colAccesType.setCellValueFactory(c -> c.getValue().typeProperty());
        colAccesStatut.setCellValueFactory(c -> c.getValue().statutProperty());

        colAlerteDate.setCellValueFactory(c -> c.getValue().dateHeureProperty());
        colAlerteMessage.setCellValueFactory(c -> c.getValue().messageProperty());
        colAlerteGravite.setCellValueFactory(c -> c.getValue().niveauGraviteProperty());
    }

    public void chargerDonnees() {
        Platform.runLater(() -> {
            try {
                // KPIs
                kpiPresents.setText(String.valueOf(accesService.countPresentsAujourdhui()));
                kpiAlertes.setText(String.valueOf(alerteService.countAlertesNonLues()));
                kpiBadgesExpires.setText("..."); // Pas encore de méthode directe pour le compte
                kpiIncidents.setText(String.valueOf(accesService.countIncidentsAujourdhui()));

                // Chart
                chargerGraphique();

                // Tables
                derniersAccesTable.setItems(FXCollections.observableArrayList(accesService.getDerniersAcces(10)));
                alertesTable.setItems(FXCollections.observableArrayList(alerteService.getAlertesNonLues()));

            } catch (Exception e) {
                LOGGER.warning("Erreur chargement données home: " + e.getMessage());
            }
        });
    }

    private void chargerGraphique() {
        try {
            frequentationChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Passages");
            
            List<Object[]> stats = accesService.getFrequentationParZone(7);
            for (Object[] row : stats) {
                series.getData().add(new XYChart.Data<>((String) row[0], (Number) row[1]));
            }
            
            frequentationChart.getData().add(series);
        } catch (Exception e) {
            LOGGER.warning("Erreur graphique: " + e.getMessage());
        }
    }

}
