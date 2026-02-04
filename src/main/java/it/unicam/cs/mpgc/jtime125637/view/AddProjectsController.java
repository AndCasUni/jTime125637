package it.unicam.cs.mpgc.jtime125637.view;

import it.unicam.cs.mpgc.jtime125637.controller.ProjectController;
import it.unicam.cs.mpgc.jtime125637.model.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AddProjectsController {
    private final ProjectController projectController = new ProjectController();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();

    @FXML private TextField addName;
    @FXML private TextArea addDesc;
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, Integer> colId;
    @FXML private TableColumn<Project, String> colNome;
    @FXML private TableColumn<Project, String> colDescrizione;
    @FXML private TableColumn<Project, Boolean> colEliminabile;
    @FXML private CheckBox add_solocanc;
    @FXML private Button add_salva;
    @FXML private Button add_delete;
    @FXML private Button add_clean;

    @FXML
    public void initialize() {
        inizializzaTabella();
        caricaProgetti();
        impostaListeners();
    }

    private void inizializzaTabella() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colEliminabile.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isEmpty())
        );
        projectsTable.setItems(projects);
    }

    private void impostaListeners() {
        projectsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    caricaProgettoInForm(newSelection);
                }
            }
        );

        add_solocanc.selectedProperty().addListener((obs, oldVal, newVal) -> caricaProgetti());
    }

    @FXML
    private void salvaNuovoProgetto() {
        try {
            String nome = addName.getText();
            String descrizione = addDesc.getText();

            projectController.creaProgetto(nome, descrizione);
            mostraMessaggio("Successo", "Progetto creato con successo", Alert.AlertType.INFORMATION);
            pulisciForm();
            caricaProgetti();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminaProgetto() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("Attenzione", "Seleziona un progetto da eliminare", Alert.AlertType.WARNING);
            return;
        }

        try {
            projectController.eliminaProgetto(selected.getId());
            mostraMessaggio("Successo", "Progetto eliminato con successo", Alert.AlertType.INFORMATION);
            pulisciForm();
            caricaProgetti();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void pulisciForm() {
        addName.clear();
        addDesc.clear();
        addName.setEditable(true);
        addDesc.setEditable(true);
        projectsTable.getSelectionModel().clearSelection();
    }

    private void caricaProgetti() {
        projects.clear();
        if (add_solocanc.isSelected()) {
            projects.addAll(projectController.getProgettiEliminabili());
        } else {
            projects.addAll(projectController.getTuttiProgetti());
        }
    }

    private void caricaProgettoInForm(Project project) {
        addName.setText(project.getNome());
        addDesc.setText(project.getDescrizione());
        addName.setEditable(false);
        addDesc.setEditable(false);
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
