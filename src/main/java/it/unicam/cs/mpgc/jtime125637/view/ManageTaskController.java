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

@NoArgsConstructor
public class ManageTaskController {
    private final ActivityController activityController = new ActivityController();
    private final ProjectController projectController = new ProjectController();
    private final ObservableList<Activity> activities = FXCollections.observableArrayList();

    @FXML private TableView<Activity> manage_table;
    @FXML private TableColumn<Activity, Integer> colId;
    @FXML private TableColumn<Activity, String> colNome;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, String> colStima;
    @FXML private TextField manage_cerca_id;
    @FXML private TextField manage_cerca_nome;
    @FXML private ChoiceBox<Project> manage_task;
    @FXML private CheckBox manage_attive;
    @FXML private CheckBox manage_completate;
    @FXML private CheckBox manage_pianificate;
    @FXML private CheckBox manage_assegnate;
    @FXML private DatePicker manage_add_data;
    @FXML private ChoiceBox<Project> manage_assegna;
    @FXML private ChoiceBox<String> manage_hh;
    @FXML private ChoiceBox<String> manage_mm;
    @FXML private Button manage_save;
    @FXML private Button manage_clean;

    @FXML
    public void initialize() {
        inizializzaTabella();
        inizializzaChoiceBox();
        caricaProgetti();
        cercaAttivita();
    }

    private void inizializzaTabella() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colStima.setCellValueFactory(new PropertyValueFactory<>("stimaTempo"));
        manage_table.setItems(activities);
    }

    private void inizializzaChoiceBox() {
        ObservableList<String> ore = FXCollections.observableArrayList();
        for (int i = 0; i <= 24; i++) {
            ore.add(String.format("%02d", i));
        }
        manage_hh.setItems(ore);
        manage_hh.setValue("00");

        ObservableList<String> minuti = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) {
            minuti.add(String.format("%02d", i));
        }
        manage_mm.setItems(minuti);
        manage_mm.setValue("00");
    }

    private void caricaProgetti() {
        ObservableList<Project> progetti = FXCollections.observableArrayList();
        progetti.add(null); 
        progetti.addAll(projectController.getProgettiAttivi());
        manage_task.setItems(progetti);
        manage_assegna.setItems(progetti);
    }


    @FXML
    private void cercaAttivita() {
        try {
            Integer id = null;
            if (!manage_cerca_id.getText().isEmpty()) {
                id = Integer.parseInt(manage_cerca_id.getText());
            }
            String nome = manage_cerca_nome.getText();
            Integer projectId = manage_task.getValue() != null ?
                manage_task.getValue().getId() : null;

            activities.clear();
            activities.addAll(activityController.cercaAttivita(
                id, nome, projectId,
                manage_completate.isSelected(),
                manage_pianificate.isSelected(),
                manage_assegnate.isSelected(),
                manage_attive.isSelected()
            ));
        } catch (NumberFormatException e) {
            mostraMessaggio("Errore", "ID non valido", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void salvaModifiche() {
        Activity selected = manage_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("Attenzione", "Seleziona un'attività", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (manage_add_data.getValue() != null) {
                activityController.pianificaAttivita(selected.getId(), manage_add_data.getValue());
            }

            if (manage_assegna.getValue() != null) {
                activityController.associaProgetto(selected.getId(), manage_assegna.getValue().getId());
            }

            String durataEffettiva = manage_hh.getValue() + ":" + manage_mm.getValue();
            if (!durataEffettiva.equals("00:00")) {
                activityController.completaAttivita(selected.getId(), durataEffettiva);
            }

            mostraMessaggio("Successo", "Attività aggiornata con successo", Alert.AlertType.INFORMATION);
            pulisciForm();
            cercaAttivita();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void pulisciForm() {
        manage_add_data.setValue(null);
        manage_assegna.setValue(null);
        manage_hh.setValue("00");
        manage_mm.setValue("00");
        manage_table.getSelectionModel().clearSelection();
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
