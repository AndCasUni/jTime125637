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

        // Usa isEliminabile() della classe Project
        colEliminabile.setCellValueFactory(new PropertyValueFactory<>("eliminabile"));

        // Stile visivo ✅/❌
        colEliminabile.setCellFactory(col -> new TableCell<Project, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✅ Sì" : "❌ No");
                    setStyle(item ?
                            "-fx-background-color: #d4edda; -fx-text-fill: green;" :
                            "-fx-background-color: #f8d7da; -fx-text-fill: red;");
                }
            }
        });

        projectsTable.setItems(projects);
    }

    private void impostaListeners() {
        projectsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        caricaProgettoInForm(newSelection);
                        aggiornaStatoModifica(newSelection.isEliminabile());
                    } else {
                        aggiornaStatoModifica(true); // Modalità "nuovo"
                    }
                }
        );

        add_solocanc.selectedProperty().addListener((obs, oldVal, newVal) -> {
            caricaProgetti();
            add_delete.setDisable(true);
        });
    }

    /** 🎯 Controlla isEliminabile() per abilitare modifica */
    private void aggiornaStatoModifica(boolean modificabile) {
        addName.setEditable(modificabile);
        addDesc.setEditable(modificabile);
        add_salva.setDisable(!modificabile);
        add_delete.setDisable(!modificabile);

        if (!modificabile) {
            addName.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray;");
            addDesc.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray;");
        } else {
            addName.setStyle("");
            addDesc.setStyle("");
        }
    }

    @FXML
    private void salvaNuovoProgetto() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();

        try {
            String nome = addName.getText().trim();
            String descrizione = addDesc.getText().trim();

            if (nome.isEmpty()) {
                mostraMessaggio("❌ Errore", "Il nome è obbligatorio", Alert.AlertType.ERROR);
                return;
            }

            if (selected == null) {
                // 🆕 NUOVO progetto
                projectController.creaProgetto(nome, descrizione);
                mostraMessaggio("✅ Creato", "Nuovo progetto salvato!", Alert.AlertType.INFORMATION);
            } else if (selected.isEliminabile()) {
                // ✏️ AGGIORNA esistente
                selected.setNome(nome);
                selected.setDescrizione(descrizione);
                projectController.aggiornaProgetto(selected);
                mostraMessaggio("✅ Aggiornato", "Progetto modificato!", Alert.AlertType.INFORMATION);
            } else {
                mostraMessaggio("⚠️ Read-only",
                        "Progetto non modificabile:\n• Ha attività assegnate",
                        Alert.AlertType.WARNING);
                return;
            }

            pulisciForm();
            caricaProgetti();

        } catch (Exception e) {
            mostraMessaggio("❌ Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminaProgetto() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("⚠️ Attenzione", "Seleziona un progetto", Alert.AlertType.WARNING);
            return;
        }

        if (!selected.isEliminabile()) {
            mostraMessaggio("❌ Non eliminabile",
                    "Impossibile eliminare:\n• Progetto ha attività assegnate",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("🗑️ Conferma");
        conferma.setHeaderText("Eliminare definitivamente?");
        conferma.setContentText(selected.getNome());

        if (conferma.showAndWait().get() == ButtonType.OK) {
            try {
                projectController.eliminaProgetto(selected.getId());
                mostraMessaggio("✅ Eliminato", "Progetto rimosso!", Alert.AlertType.INFORMATION);
                pulisciForm();
                caricaProgetti();
            } catch (Exception e) {
                mostraMessaggio("❌ Errore", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void pulisciForm() {
        addName.clear();
        addDesc.clear();
        aggiornaStatoModifica(true);
        projectsTable.getSelectionModel().clearSelection();
    }

    private void caricaProgetti() {
        projects.clear();
        try {
            if (add_solocanc.isSelected()) {
                projects.addAll(projectController.getProgettiEliminabili());
            } else {
                projects.addAll(projectController.getTuttiProgetti());
            }
        } catch (Exception e) {
            mostraMessaggio("Errore", "Impossibile caricare progetti", Alert.AlertType.ERROR);
        }
    }

    private void caricaProgettoInForm(Project project) {
        addName.setText(project.getNome());
        addDesc.setText(project.getDescrizione());
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
