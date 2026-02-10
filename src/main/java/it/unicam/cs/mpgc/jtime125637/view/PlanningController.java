package it.unicam.cs.mpgc.jtime125637.view;

import it.unicam.cs.mpgc.jtime125637.controller.ActivityController;
import it.unicam.cs.mpgc.jtime125637.controller.ReportController;
import it.unicam.cs.mpgc.jtime125637.model.Activity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Controller per la gestione della pianificazione delle attività.
 * Permette di visualizzare le attività pianificate per una data specifica,
 * filtrarle per stato di completamento e visualizzare statistiche aggregate.
 */
@NoArgsConstructor
public class PlanningController {
    private final ActivityController activityController = new ActivityController();
    private final ObservableList<Activity> tutteAttivita = FXCollections.observableArrayList();
    private final ObservableList<Activity> attivitaFiltrate = FXCollections.observableArrayList();

    @FXML private DatePicker date;
    @FXML private TableView<Activity> scheduleTable;
    @FXML private TableColumn<Activity, String> colTask;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, String> colStima;
    @FXML private TableColumn<Activity, String> colEffettivo;
    @FXML private TableColumn<Activity, String> colProgetto;
    @FXML private RadioButton planning_complete;
    @FXML private RadioButton planning_noncomplete;
    @FXML private RadioButton planning_scheduled;
    @FXML private TextField scheduled;
    @FXML private TextField completed;
    @FXML private TextField notCompleted;

    @FXML
    public void initialize() {
        inizializzaTabella();
        impostaListeners();
        date.setValue(LocalDate.now());
        caricaAttivitaGiorno();
    }

    /**
     * Configura le colonne della tabella con i rispettivi data binding.
     * Include nome, descrizione, stima tempo, durata effettiva e progetto associato.
     */
    private void inizializzaTabella() {
        colTask.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colStima.setCellValueFactory(new PropertyValueFactory<>("stimaTempo"));
        colEffettivo.setCellValueFactory(new PropertyValueFactory<>("durataEffettiva"));
        colProgetto.setCellValueFactory(new PropertyValueFactory<>("project"));

        scheduleTable.setItems(attivitaFiltrate);
    }

    /**
     * Configura i listener per i RadioButton di filtro.
     * Quando un filtro viene selezionato, la lista delle attività viene filtrata di conseguenza.
     */
    private void impostaListeners() {
        planning_complete.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filtraAttivita();
        });

        planning_noncomplete.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filtraAttivita();
        });

        planning_scheduled.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                filtraAttivita();
            }
        });
    }

    /**
     * Carica tutte le attività pianificate per la data selezionata.
     * Aggiorna le statistiche e applica i filtri correnti.
     */
    @FXML
    private void caricaAttivitaGiorno() {
        LocalDate dataSelezionata = date.getValue();
        if (dataSelezionata == null) {
            return;
        }

        try {
            tutteAttivita.clear();
            tutteAttivita.addAll(activityController.getAttivitaPerData(dataSelezionata));
            aggiornaStatistiche();
            filtraAttivita();
        } catch (Exception e) {
            mostraMessaggioErrore("Errore caricamento attività", e.getMessage());
        }
    }

    /**
     * Filtra le attività visualizzate in base al RadioButton selezionato:
     * - planning_complete: mostra solo attività completate
     * - planning_noncomplete: mostra solo attività non completate
     * - planning_scheduled: mostra tutte le attività pianificate
     */
    private void filtraAttivita() {
        attivitaFiltrate.clear();

        if (planning_complete.isSelected()) {
            attivitaFiltrate.addAll(tutteAttivita.stream()
                    .filter(Activity::isCompletata)
                    .toList());
        } else if (planning_noncomplete.isSelected()) {
            attivitaFiltrate.addAll(tutteAttivita.stream()
                    .filter(activity -> !activity.isCompletata())
                    .toList());
        } else {
            attivitaFiltrate.addAll(tutteAttivita);
        }

        scheduleTable.refresh();
    }

    /**
     * Aggiorna i campi delle statistiche con i contatori delle attività:
     * totali pianificate, completate e non completate.
     */
    private void aggiornaStatistiche() {
        try {
            int totali = tutteAttivita.size();
            long completateCount = tutteAttivita.stream()
                    .filter(Activity::isCompletata)
                    .count();
            int nonCompletateCount = totali - (int)completateCount;

            scheduled.setText(String.valueOf(totali));
            completed.setText(String.valueOf(completateCount));
            notCompleted.setText(String.valueOf(nonCompletateCount));

        } catch (Exception e) {
            System.err.println("Errore statistiche: " + e.getMessage());
            scheduled.setText("0");
            completed.setText("0");
            notCompleted.setText("0");
        }
    }

    /**
     * Mostra un messaggio di errore all'utente.
     *
     * @param titolo titolo della finestra di dialogo
     * @param messaggio contenuto del messaggio di errore
     */
    private void mostraMessaggioErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
