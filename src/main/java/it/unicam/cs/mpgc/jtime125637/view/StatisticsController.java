package it.unicam.cs.mpgc.jtime125637.view;

import it.unicam.cs.mpgc.jtime125637.controller.ProjectController;
import it.unicam.cs.mpgc.jtime125637.controller.ReportController;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
public class StatisticsController {
    private final ReportController reportController = new ReportController();
    private final ProjectController projectController = new ProjectController();
    private final ObservableList<Project> progettiList = FXCollections.observableArrayList();

    // Elementi FXML corretti dall'FXML fornito
    @FXML private DatePicker stat_dadata;      // Da data
    @FXML private DatePicker stat_adata;       // A data
    @FXML private ChoiceBox<Project> stat_perprog;  // Per progetto
    @FXML private PieChart stat_progetti;      // Chart progetti
    @FXML private PieChart stat_att;           // Chart attività
    @FXML private TextField stat_tothh;        // Totale ore stimate
    @FXML private TextField stat_tothheff;     // Totale ore effettive
    @FXML private TextField stat_tothhrimaste; // Totale ore rimaste

    @FXML
    public void initialize() {
        stat_progetti.setLegendVisible(false);
        stat_att.setLegendVisible(false);
        inizializzaChoiceBox();
        impostaDateDefault();
        generaStatistiche(); // Carica statistiche iniziali
    }

    private void inizializzaChoiceBox() {
        try {
            progettiList.clear();
            progettiList.addAll(projectController.getTuttiProgetti());
            // Aggiungi opzione "Tutti i progetti"
            Project tuttiProgetti = new Project();
            tuttiProgetti.setId(0);
            tuttiProgetti.setNome("Tutti i progetti");
            progettiList.add(0, tuttiProgetti);

            stat_perprog.setItems(progettiList);
            stat_perprog.setValue(progettiList.get(0)); // Seleziona "Tutti"

            // Converter per mostrare il nome del progetto
            stat_perprog.setConverter(new javafx.util.StringConverter<Project>() {
                @Override
                public String toString(Project project) {
                    return project.getNome();
                }
                @Override
                public Project fromString(String string) {
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("Errore caricamento progetti: " + e.getMessage());
        }
    }

    private void impostaDateDefault() {
        stat_adata.setValue(LocalDate.now());
        stat_dadata.setValue(LocalDate.now().minusMonths(1));
    }

    @FXML
    private void generaStatistiche() {
        LocalDate dataInizio = stat_dadata.getValue();
        LocalDate dataFine = stat_adata.getValue();
        Project progettoSelezionato = stat_perprog.getValue();

        if (dataInizio.isAfter(dataFine)) {
            mostraMessaggio("Attenzione", "La data iniziale non può essere dopo la data finale",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            Integer projectId = (progettoSelezionato != null && progettoSelezionato.getId() != 0)
                    ? progettoSelezionato.getId() : null;

            aggiornaGraficoProgetti();
            aggiornaGraficoAttivita(dataInizio, dataFine, projectId);
            aggiornaStatisticheOre(dataInizio, dataFine, projectId);

        } catch (Exception e) {
            mostraMessaggio("Errore", "Impossibile generare statistiche: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void aggiornaGraficoProgetti() {
        try {
            // Statistiche sui progetti (tutti i progetti)
            var statsProgetti = reportController.getStatisticheProgetti();

            ObservableList<PieChart.Data> datiProgetti = FXCollections.observableArrayList(
                    new PieChart.Data("Attivi (" + statsProgetti.getAttivi() + ")", statsProgetti.getAttivi()),
                    new PieChart.Data("Completati (" + statsProgetti.getCompletati() + ")", statsProgetti.getCompletati()),
                    new PieChart.Data("Vuoti (" + statsProgetti.getVuoti() + ")", statsProgetti.getVuoti())
            );

            stat_progetti.setData(datiProgetti);
            stat_progetti.setTitle("Distribuzione Progetti");

        } catch (Exception e) {
            System.err.println("Errore grafico progetti: " + e.getMessage());
        }
    }

    private void aggiornaGraficoAttivita(LocalDate inizio, LocalDate fine, Integer projectId) {
        try {
            var statsAttivita = reportController.getStatisticheIntervallo(inizio, fine, projectId);

            int completate = statsAttivita.getCompletate();
            int totali = statsAttivita.getTotaleAttivita();
            int nonCompletate = totali - completate;

            ObservableList<PieChart.Data> datiAttivita = FXCollections.observableArrayList(
                    new PieChart.Data("Completate (" + completate + ")", completate),
                    new PieChart.Data("Non completate (" + nonCompletate + ")", nonCompletate)
            );

            stat_att.setData(datiAttivita);
            stat_att.setTitle("Attività " + inizio + " - " + fine);

        } catch (Exception e) {
            System.err.println("Errore grafico attività: " + e.getMessage());
        }
    }

    private void aggiornaStatisticheOre(LocalDate inizio, LocalDate fine, Integer projectId) {
        try {
            var stats = reportController.getStatisticheIntervallo(inizio, fine, projectId);

            // Formatta ore (assumendo che ReportController abbia formattaMinutiInOre)
            String oreStimate = formattaOre(stats.getTotaleOreStimate());
            String oreEffettive = formattaOre(stats.getTotaleOreEffettive());
            String oreRimanenti = formattaOre(stats.getTotaleOreStimate() - stats.getTotaleOreEffettive());

            stat_tothh.setText(oreStimate);
            stat_tothheff.setText(oreEffettive);
            stat_tothhrimaste.setText(oreRimanenti);

        } catch (Exception e) {
            System.err.println("Errore statistiche ore: " + e.getMessage());
            stat_tothh.setText("0h");
            stat_tothheff.setText("0h");
            stat_tothhrimaste.setText("0h");
        }
    }

    private String formattaOre(int minutiTotali) {
        int ore = minutiTotali / 60;
        int minuti = minutiTotali % 60;
        return String.format("%dh %02dm", ore, minuti);
    }

    // Listener automatici per rigenerare statistiche al cambio
    @FXML
    private void onDateChange() {
        generaStatistiche();
    }

    @FXML
    private void onProjectChange() {
        generaStatistiche();
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
