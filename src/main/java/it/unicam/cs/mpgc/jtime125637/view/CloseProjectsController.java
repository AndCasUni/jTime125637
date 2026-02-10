package it.unicam.cs.mpgc.jtime125637.view;

import it.unicam.cs.mpgc.jtime125637.controller.ActivityController;
import it.unicam.cs.mpgc.jtime125637.controller.ProjectController;
import it.unicam.cs.mpgc.jtime125637.model.Activity;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class CloseProjectsController {
    private final ProjectController projectController = new ProjectController();
    private final ActivityController activityController = new ActivityController();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<Activity> activities = FXCollections.observableArrayList();

    @FXML private TableView<Project> projTable;
    @FXML private TableColumn<Project, Integer> colIdProj;
    @FXML private TableColumn<Project, String> colNomeProj;
    @FXML private TableColumn<Project, String> colStatoProj;

    @FXML private TableView<Activity> taskTable;
    @FXML private TableColumn<Activity, Integer> colIdTask;
    @FXML private TableColumn<Activity, String> colNomeTask;
    @FXML private TableColumn<Activity, String> colStimaTask;
    @FXML private TableColumn<Activity, String> colEffettivoTask;

    @FXML private CheckBox checkAttivo;
    @FXML private CheckBox checkCompleto;
    @FXML private TextField close_id;
    @FXML private TextField close_nome;
    @FXML private Button close_cerca;
    @FXML private Button close_close;

    @FXML
    public void initialize() {
        inizializzaTabelle();
        impostaListeners();
        caricaProgettiAttivi();
    }

    private void inizializzaTabelle() {
        colIdProj.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomeProj.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colStatoProj.setCellValueFactory(new PropertyValueFactory<>("stato"));
        projTable.setItems(projects);

        colIdTask.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomeTask.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colStimaTask.setCellValueFactory(new PropertyValueFactory<>("stimaTempo"));
        colEffettivoTask.setCellValueFactory(new PropertyValueFactory<>("durataEffettiva"));
        taskTable.setItems(activities);
    }

    /**
     * Configura i listener per la selezione del progetto e i checkbox di filtro.
     * Quando un progetto viene selezionato, carica le sue attività nella tabella inferiore.
     */
    private void impostaListeners() {
        projTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        caricaAttivitaProgetto(newSelection.getId());
                    } else {
                        activities.clear();
                    }
                }
        );

        checkAttivo.selectedProperty().addListener((obs, oldVal, newVal) -> aggiornaProgetti());
        checkCompleto.selectedProperty().addListener((obs, oldVal, newVal) -> aggiornaProgetti());
    }

    /**
     * Cerca progetti applicando filtri per ID, nome e stato.
     * I filtri di stato dipendono dalla selezione dei checkbox (attivo/completato).
     * Se entrambi i checkbox sono selezionati o deselezionati, mostra tutti i progetti.
     */
    @FXML
    private void cercaProgetti() {
        projects.clear();
        Integer idnf = null;
        if (close_id != null && !close_id.getText().trim().isEmpty()) {
            try {
                idnf = Integer.parseInt(close_id.getText().trim());
            } catch (NumberFormatException e) {
                mostraMessaggio("Errore", "ID non valido", Alert.AlertType.ERROR);
                return;
            }
        }
        String nome = close_nome != null ? close_nome.getText().trim() : "";

        List<Project> lista;
        if (checkAttivo.isSelected() && !checkCompleto.isSelected()) {
            lista = projectController.getProgettiAttivi();
        } else if (!checkAttivo.isSelected() && checkCompleto.isSelected()) {
            lista = projectController.getProgettiCompletati();
        } else {
            lista = projectController.getTuttiProgetti();
        }

        final Integer id = idnf;
        lista.stream()
                .filter(p -> id == null || p.getId().equals(id))
                .filter(p -> nome.isEmpty() || p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .forEach(projects::add);

        activities.clear();
    }

    /**
     * Chiude il progetto selezionato cambiando il suo stato a "completato".
     * Un progetto può essere chiuso solo se ha tutte le attività completate.
     */
    @FXML
    private void chiudiProgetto() {
        Project selected = projTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("Attenzione", "Seleziona un progetto", Alert.AlertType.WARNING);
            return;
        }
        try {
            projectController.chiudiProgetto(selected.getId());
            mostraMessaggio("Successo", "Progetto chiuso con successo", Alert.AlertType.INFORMATION);
            aggiornaProgetti();
            activities.clear();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Aggiorna la lista dei progetti in base allo stato dei checkbox di filtro.
     * Se solo "Attivo" è selezionato, mostra progetti attivi.
     * Se solo "Completato" è selezionato, mostra progetti completati.
     * Altrimenti, mostra tutti i progetti.
     */
    private void aggiornaProgetti() {
        if (checkAttivo.isSelected() && !checkCompleto.isSelected()) {
            caricaProgettiAttivi();
        } else if (!checkAttivo.isSelected() && checkCompleto.isSelected()) {
            caricaProgettiCompletati();
        } else {
            projects.clear();
            projects.addAll(projectController.getTuttiProgetti());
        }
    }

    /**
     * Carica nella tabella tutti i progetti con stato "attivo".
     */
    private void caricaProgettiAttivi() {
        projects.clear();
        projects.addAll(projectController.getProgettiAttivi());
    }

    /**
     * Carica nella tabella tutti i progetti con stato "completato".
     */
    private void caricaProgettiCompletati() {
        projects.clear();
        projects.addAll(projectController.getProgettiCompletati());
    }

    /**
     * Carica nella tabella delle attività tutte le attività associate al progetto specificato.
     *
     * @param projectId l'ID del progetto di cui caricare le attività
     */
    private void caricaAttivitaProgetto(Integer projectId) {
        activities.clear();
        activities.addAll(activityController.getAttivitaPerProgetto(projectId));
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
