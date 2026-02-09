package it.unicam.cs.mpgc.jtime125637.view;

import it.unicam.cs.mpgc.jtime125637.controller.ActivityController;
import it.unicam.cs.mpgc.jtime125637.controller.ReportController;
import it.unicam.cs.mpgc.jtime125637.model.Activity;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
public class PlanningController {
    private final ActivityController activityController = new ActivityController();
    private final ReportController reportController = new ReportController();
    private final ObservableList<Activity> tutteAttivita = FXCollections.observableArrayList();
    private final ObservableList<Activity> attivitaFiltrate = FXCollections.observableArrayList();

    // Elementi FXML aggiornati
    @FXML private DatePicker date;
    @FXML private TableView<Activity> scheduleTable;
    @FXML private TableColumn<Activity, String> colTask;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, String> colStima;
    @FXML private TableColumn<Activity, String> colEffettivo;
    @FXML private TableColumn<Activity, String> colProgetto;
    @FXML private RadioButton planning_complete;
    @FXML private RadioButton planning_noncomplete;
    @FXML private RadioButton planning_scheduled; // Nuovo radio button
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

    private void inizializzaTabella() {
        // Mappatura corretta ai getter della classe Activity
        colTask.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colStima.setCellValueFactory(new PropertyValueFactory<>("stimaTempo"));
        colEffettivo.setCellValueFactory(new PropertyValueFactory<>("durataEffettiva"));
        colProgetto.setCellValueFactory(new PropertyValueFactory<>("project"));

        scheduleTable.setItems(attivitaFiltrate);
    }

    private void impostaListeners() {
        // Listener per tutti i radio button
        planning_complete.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filtraAttivita();
        });

        planning_noncomplete.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filtraAttivita();
        });

        planning_scheduled.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Reset filtri - mostra tutte
                filtraAttivita();
            }
        });
    }

    @FXML
    private void caricaAttivitaGiorno() {
        LocalDate dataSelezionata = date.getValue();
        if (dataSelezionata == null) {
            return;
        }

        try {
            tutteAttivita.clear();
            // Usa il metodo findByDate della repository
            tutteAttivita.addAll(activityController.getAttivitaPerData(dataSelezionata));
            aggiornaStatistiche();
            filtraAttivita();
        } catch (Exception e) {
            mostraMessaggioErrore("Errore caricamento attività", e.getMessage());
        }
    }

    private void filtraAttivita() {
        attivitaFiltrate.clear();

        if (planning_complete.isSelected()) {
            // Solo completate
            attivitaFiltrate.addAll(tutteAttivita.stream()
                    .filter(Activity::isCompletata)
                    .toList());
        } else if (planning_noncomplete.isSelected()) {
            // Solo non completate
            attivitaFiltrate.addAll(tutteAttivita.stream()
                    .filter(activity -> !activity.isCompletata())
                    .toList());
        } else {
            // planning_scheduled selezionato o nessun filtro -> mostra tutte
            attivitaFiltrate.addAll(tutteAttivita);
        }

        scheduleTable.refresh();
    }

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

    private void mostraMessaggioErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
