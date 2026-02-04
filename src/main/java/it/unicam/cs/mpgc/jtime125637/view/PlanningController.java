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

@NoArgsConstructor
public class PlanningController {
    private final ActivityController activityController = new ActivityController();
    private final ReportController reportController = new ReportController();
    private final ObservableList<Activity> activities = FXCollections.observableArrayList();

    @FXML private DatePicker datePicker;
    @FXML private TableView<Activity> planningTable;
    @FXML private TableColumn<Activity, Integer> colId;
    @FXML private TableColumn<Activity, String> colNome;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, String> colStima;
    @FXML private TableColumn<Activity, String> colEffettivo;
    @FXML private RadioButton filterAttive;
    @FXML private RadioButton filterCompletate;
    @FXML private Label lblTotaleProgrammate;
    @FXML private Label lblPercentuale;
    @FXML private Label lblCompletate;
    @FXML private Label lblAttive;

    @FXML
    public void initialize() {
        inizializzaTabella();
        impostaListeners();
        datePicker.setValue(LocalDate.now());
        caricaAttivitaGiorno();
    }

    private void inizializzaTabella() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colStima.setCellValueFactory(new PropertyValueFactory<>("stimaTempo"));
        colEffettivo.setCellValueFactory(new PropertyValueFactory<>("durataEffettiva"));
        
        planningTable.setItems(activities);
        
        planningTable.setRowFactory(tv -> new TableRow<Activity>() {
            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);
                if (empty || activity == null) {
                    setStyle("");
                    getStyleClass().removeAll("attiva", "completata");
                } else {
                    if (activity.isCompletata()) {
                        getStyleClass().removeAll("attiva");
                        getStyleClass().add("completata");
                    } else {
                        getStyleClass().removeAll("completata");
                        getStyleClass().add("attiva");
                    }
                }
            }
        });
    }

    private void impostaListeners() {
        ToggleGroup group = new ToggleGroup();
        filterAttive.setToggleGroup(group);
        filterCompletate.setToggleGroup(group);

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                caricaAttivitaGiorno();
            }
        });

        filterAttive.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filtraAttivita();
        });

        filterCompletate.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) filtraAttivita();
        });
    }

    @FXML
    private void caricaAttivitaGiorno() {
        LocalDate data = datePicker.getValue();
        if (data == null) {
            return;
        }

        try {
            activities.clear();
            activities.addAll(activityController.getAttivitaPerData(data));
            aggiornaStatistiche(data);
            filtraAttivita();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filtraAttivita() {
        LocalDate data = datePicker.getValue();
        if (data == null) {
            return;
        }

        activities.clear();
        var tutteAttivita = activityController.getAttivitaPerData(data);

        if (filterAttive.isSelected()) {
            activities.addAll(tutteAttivita.stream()
                .filter(Activity::isAttiva)
                .toList());
        } else if (filterCompletate.isSelected()) {
            activities.addAll(tutteAttivita.stream()
                .filter(Activity::isCompletata)
                .toList());
        } else {
            activities.addAll(tutteAttivita);
        }
    }

    private void aggiornaStatistiche(LocalDate data) {
        try {
            ReportController.StatisticheGiorno stats = reportController.getStatistichePerGiorno(data);
            
            lblTotaleProgrammate.setText("Totale programmate: " + stats.getTotaleProgrammate());
            lblCompletate.setText("Completate: " + stats.getCompletate());
            lblAttive.setText("Attive: " + stats.getAttive());
            lblPercentuale.setText(String.format("Percentuale completate: %.1f%%", 
                stats.getPercentualeCompletate()));
        } catch (Exception e) {
            System.err.println("Errore nel calcolo statistiche: " + e.getMessage());
        }
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
