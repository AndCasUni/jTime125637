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

/**
 * Controller per la gestione delle statistiche e dei report relativi ai progetti e alle attività.
 * Gestisce la visualizzazione di grafici a torta per progetti e attività, oltre alle statistiche
 * sulle ore stimate, effettive e rimanenti.
 */
@NoArgsConstructor
public class StatisticsController {
    private final ReportController reportController = new ReportController();
    private final ProjectController projectController = new ProjectController();
    private final ObservableList<Project> progettiList = FXCollections.observableArrayList();

    @FXML private DatePicker stat_dadata;
    @FXML private DatePicker stat_adata;
    @FXML private ChoiceBox<Project> stat_perprog;
    @FXML private PieChart stat_progetti;
    @FXML private PieChart stat_att;
    @FXML private TextField stat_tothh;
    @FXML private TextField stat_tothheff;
    @FXML private TextField stat_tothhrimaste;

    /**
     * Inizializza il controller impostando i valori di default e caricando le statistiche iniziali.
     */
    @FXML
    public void initialize() {
        stat_progetti.setLegendVisible(false);
        stat_att.setLegendVisible(false);
        inizializzaChoiceBox();
        impostaDateDefault();
        generaStatistiche();
    }

    /**
     * Inizializza il ChoiceBox dei progetti caricando tutti i progetti disponibili
     * e aggiungendo l'opzione "Tutti i progetti".
     */
    private void inizializzaChoiceBox() {
        try {
            progettiList.clear();
            progettiList.addAll(projectController.getTuttiProgetti());

            Project tuttiProgetti = new Project();
            tuttiProgetti.setId(0);
            tuttiProgetti.setNome("Tutti i progetti");
            progettiList.add(0, tuttiProgetti);

            stat_perprog.setItems(progettiList);
            stat_perprog.setValue(progettiList.get(0));

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

    /**
     * Imposta le date di default per i filtri: data finale a oggi e data iniziale a un mese fa.
     */
    private void impostaDateDefault() {
        stat_adata.setValue(LocalDate.now().plusMonths(1));
        stat_dadata.setValue(LocalDate.now());
    }

    /**
     * Genera le statistiche in base ai filtri selezionati (intervallo di date e progetto).
     * Aggiorna i grafici e le statistiche delle ore.
     */
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

    /**
     * Aggiorna il grafico a torta con la distribuzione dei progetti
     * (attivi, completati, vuoti).
     */
    private void aggiornaGraficoProgetti() {
        try {
            var statsProgetti = reportController.getStatisticheProgetti();

            ObservableList<PieChart.Data> datiProgetti = FXCollections.observableArrayList(
                    new PieChart.Data("Attivi (" + statsProgetti.getAttivi() + ")", statsProgetti.getAttivi() - statsProgetti.getVuoti()),
                    new PieChart.Data("Completati (" + statsProgetti.getCompletati() + ")", statsProgetti.getCompletati()),
                    new PieChart.Data("Vuoti (" + statsProgetti.getVuoti() + ")", statsProgetti.getVuoti())
            );

            stat_progetti.setData(datiProgetti);
            stat_progetti.setTitle("Distribuzione Progetti");

        } catch (Exception e) {
            System.err.println("Errore grafico progetti: " + e.getMessage());
        }
    }

    /**
     * Aggiorna il grafico a torta delle attività per l'intervallo specificato,
     * mostrando attività completate e non completate.
     *
     * @param inizio data di inizio del periodo
     * @param fine data di fine del periodo
     * @param projectId ID del progetto selezionato, null per tutti i progetti
     */
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

    /**
     * Aggiorna i campi di testo con le statistiche delle ore (stimate, effettive, rimanenti)
     * per l'intervallo specificato.
     *
     * Il calcolo delle ore rimanenti è fatto solo per le attività non completate:
     * - Se un'attività è completata, non contribuisce alle ore rimanenti
     * - Se un'attività non è completata, la sua stima intera contribuisce alle ore rimanenti
     *
     * Esempio: 3 attività da 30min ciascuna:
     * - Attività 1: completata in 10min → ore rimanenti: 0min
     * - Attività 2: non completata (stima 30min) → ore rimanenti: 30min
     * - Attività 3: non completata (stima 30min) → ore rimanenti: 30min
     * Totale ore rimanenti: 60min
     *
     * @param inizio data di inizio del periodo
     * @param fine data di fine del periodo
     * @param projectId ID del progetto selezionato, null per tutti i progetti
     */
    private void aggiornaStatisticheOre(LocalDate inizio, LocalDate fine, Integer projectId) {
        try {
            var stats = reportController.getStatisticheIntervallo(inizio, fine, projectId);

            String oreStimate = formattaOre(stats.getTotaleOreStimate());
            String oreEffettive = formattaOre(stats.getTotaleOreEffettive());

            int oreRimanentiMinuti = stats.getTotaleOreStimateDaNonCompletate();
            String oreRimanenti = formattaOre(oreRimanentiMinuti);

            stat_tothh.setText(oreStimate);
            stat_tothheff.setText(oreEffettive);
            stat_tothhrimaste.setText(oreRimanenti);

        } catch (Exception e) {
            System.err.println("Errore statistiche ore: " + e.getMessage());
            stat_tothh.setText("0h 00m");
            stat_tothheff.setText("0h 00m");
            stat_tothhrimaste.setText("0h 00m");
        }
    }

    /**
     * Formatta i minuti in una stringa leggibile nel formato "Xh YYm".
     *
     * @param minutiTotali numero totale di minuti da formattare
     * @return stringa formattata nel formato "ore" e "minuti"
     */
    private String formattaOre(int minutiTotali) {
        int ore = minutiTotali / 60;
        int minuti = minutiTotali % 60;
        return String.format("%dh %02dm", ore, minuti);
    }

    /**
     * Listener per il cambio delle date che rigenera automaticamente le statistiche.
     */
    @FXML
    private void onDateChange() {
        generaStatistiche();
    }

    /**
     * Listener per il cambio del progetto selezionato che rigenera automaticamente le statistiche.
     */
    @FXML
    private void onProjectChange() {
        generaStatistiche();
    }

    /**
     * Mostra un messaggio di dialogo all'utente.
     *
     * @param titolo titolo della finestra di dialogo
     * @param messaggio contenuto del messaggio
     * @param tipo tipo di alert (ERROR, WARNING, INFORMATION, etc.)
     */
    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
