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

    @FXML private ListView<Project> projectsList;
    @FXML private TableView<Activity> activitiesTable;
    @FXML private TableColumn<Activity, Integer> colId;
    @FXML private TableColumn<Activity, String> colNome;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, String> colStima;
    @FXML private TableColumn<Activity, String> colEffettivo;
    @FXML private RadioButton filterAttivi;
    @FXML private RadioButton filterCompletati;
    @FXML private RadioButton filterVuoti;
    @FXML private Button closeButton;


    @FXML
    public void initialize() {
        inizializzaTabella();
        impostaListeners();
        caricaProgettiAttivi();
    }

    private void inizializzaTabella() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colStima.setCellValueFactory(new PropertyValueFactory<>("stimaTempo"));
        colEffettivo.setCellValueFactory(new PropertyValueFactory<>("durataEffettiva"));
        activitiesTable.setItems(activities);
    }

    private void impostaListeners() {
        projectsList.setItems(projects);
        projectsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                } else {
                    setText(project.getNome() + " - " + project.getStato());
                }
            }
        });

        projectsList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    caricaAttivitaProgetto(newSelection.getId());
                }
            }
        );

        ToggleGroup group = new ToggleGroup();
        filterAttivi.setToggleGroup(group);
        filterCompletati.setToggleGroup(group);
        filterVuoti.setToggleGroup(group);
        filterAttivi.setSelected(true);

        filterAttivi.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) caricaProgettiAttivi();
        });
        filterCompletati.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) caricaProgettiCompletati();
        });
        filterVuoti.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) caricaProgettiVuoti();
        });
    }

    /*@FXML
    private void cercaProgetti() {
        projects.clear();

        Integer id = null;
        if (close_id != null && !close_id.getText().isBlank()) {
            try {
                id = Integer.parseInt(close_id.getText().trim());
            } catch (NumberFormatException e) {
                mostraMessaggio("Errore", "ID non valido", Alert.AlertType.ERROR);
                return;
            }
        }

        String nome = (close_nome != null) ? close_nome.getText().trim() : "";

        // recupero tutti i progetti secondo filtro stato
        List<Project> lista;
        if (checkAttivo.isSelected() && !checkCompleto.isSelected()) {
            lista = projectController.getProgettiAttivi();
        } else if (!checkAttivo.isSelected() && checkCompleto.isSelected()) {
            lista = projectController.getProgettiCompletati();
        } else {
            // entrambi selezionati o nessuno → tutti
            lista = projectController.getTuttiProgetti();
        }

        // filtra per id e nome se presenti
        lista.stream()
                .filter(p -> id == null || p.getId().equals(id))
                .filter(p -> nome.isEmpty() || p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .forEach(projects::add);

        // aggiorna eventuale selezione e tabella attività
        activities.clear();
        Project selected = projectsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            caricaAttivitaProgetto(selected.getId());
        }
    }
*/
    @FXML
    private void chiudiProgetto() {
        Project selected = projectsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("Attenzione", "Seleziona un progetto", Alert.AlertType.WARNING);
            return;
        }

        try {
            projectController.chiudiProgetto(selected.getId());
            mostraMessaggio("Successo", "Progetto chiuso con successo", Alert.AlertType.INFORMATION);
            caricaProgettiAttivi();
            activities.clear();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void caricaProgettiAttivi() {
        projects.clear();
        projects.addAll(projectController.getProgettiAttivi());
    }

    private void caricaProgettiCompletati() {
        projects.clear();
        projects.addAll(projectController.getProgettiCompletati());
    }

    private void caricaProgettiVuoti() {
        projects.clear();
        projects.addAll(projectController.getProgettiVuoti());
    }

    private void caricaAttivitaProgetto(Integer projectId) {
        activities.clear();
        activities.addAll(activityController.getAttivitaPerProgetto(projectId));
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
