package it.unicam.cs.mpgc.jtime125637.view;

import it.unicam.cs.mpgc.jtime125637.controller.ProjectController;
import it.unicam.cs.mpgc.jtime125637.controller.ReportController;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
public class StatisticsController {
    private final ReportController reportController = new ReportController();
    private final ProjectController projectController = new ProjectController();

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private ChoiceBox<Project> projectFilter;
    @FXML private PieChart chartProgetti;
    @FXML private PieChart chartAttivita;
    @FXML private Label lblTotaleOreStimate;
    @FXML private Label lblTotaleOreEffettive;
    @FXML private Label lblOreMancanti;
    @FXML private Button btnGenera;

    @FXML
    public void initialize() {
        caricaProgetti();
        impostaDateDefault();
        generaReport();
    }

    private void caricaProgetti() {
        ObservableList<Project> progetti = FXCollections.observableArrayList(
            projectController.getTuttiProgetti()
        );
        projectFilter.setItems(progetti);
        
        // Rimuovi setCellFactory (non esiste per ChoiceBox)
        // Usa solo setConverter
        projectFilter.setConverter(new javafx.util.StringConverter<Project>() {
            @Override
            public String toString(Project project) {
                return project == null ? "Tutti" : project.getNome();
            }

            @Override
            public Project fromString(String string) {
                return null;
            }
        });
    }

    private void impostaDateDefault() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusMonths(1));
    }

    @FXML
    private void generaReport() {
        try {
            LocalDate inizio = dateFrom.getValue();
            LocalDate fine = dateTo.getValue();
            Integer projectId = projectFilter.getValue() != null ? 
                projectFilter.getValue().getId() : null;

            if (inizio == null || fine == null) {
                mostraMessaggio("Attenzione", "Seleziona un intervallo di date valido", 
                    Alert.AlertType.WARNING);
                return;
            }

            aggiornaChartProgetti();
            aggiornaChartAttivita(inizio, fine, projectId);
            aggiornaStatisticheOre(inizio, fine, projectId);

        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void aggiornaChartProgetti() {
        ReportController.StatisticheProgetti stats = reportController.getStatisticheProgetti();
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Attivi (" + stats.getAttivi() + ")", stats.getAttivi()),
            new PieChart.Data("Completati (" + stats.getCompletati() + ")", stats.getCompletati()),
            new PieChart.Data("Vuoti (" + stats.getVuoti() + ")", stats.getVuoti())
        );
        
        chartProgetti.setData(pieChartData);
        chartProgetti.setTitle("Distribuzione Progetti");
        chartProgetti.setLegendVisible(true);
    }

    private void aggiornaChartAttivita(LocalDate inizio, LocalDate fine, Integer projectId) {
        ReportController.StatisticheIntervallo stats = 
            reportController.getStatisticheIntervallo(inizio, fine, projectId);
        
        int nonCompletate = stats.getTotaleAttivita() - stats.getCompletate();
        
        ReportController.StatisticheAttivita statsGenerali = reportController.getStatisticheAttivita();
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Completate (" + stats.getCompletate() + ")", stats.getCompletate()),
            new PieChart.Data("Non Completate (" + nonCompletate + ")", nonCompletate),
            new PieChart.Data("Non Schedulate (" + statsGenerali.getNonPianificate() + ")", 
                statsGenerali.getNonPianificate())
        );
        
        chartAttivita.setData(pieChartData);
        chartAttivita.setTitle("Distribuzione Attività");
        chartAttivita.setLegendVisible(true);
    }

    private void aggiornaStatisticheOre(LocalDate inizio, LocalDate fine, Integer projectId) {
        ReportController.StatisticheIntervallo stats = 
            reportController.getStatisticheIntervallo(inizio, fine, projectId);
        
        String oreStimate = reportController.formattaMinutiInOre(stats.getTotaleOreStimate());
        String oreEffettive = reportController.formattaMinutiInOre(stats.getTotaleOreEffettive());
        String oreMancanti = reportController.formattaMinutiInOre(stats.getOreMancanti());
        
        lblTotaleOreStimate.setText("Totale ore stimate: " + oreStimate);
        lblTotaleOreEffettive.setText("Totale ore effettive: " + oreEffettive);
        lblOreMancanti.setText("Ore mancanti dalla stima: " + oreMancanti);
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
