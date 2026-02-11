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

    /**
     * Configura le colonne della tabella e il rendering della colonna "eliminabile".
     * La colonna eliminabile mostra un indicatore visivo (✅/❌) con colori appropriati.
     */
    private void inizializzaTabella() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colEliminabile.setCellValueFactory(new PropertyValueFactory<>("eliminabile"));

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

    /**
     * Configura i listener per la selezione della tabella e il checkbox di filtro.
     * Quando un progetto viene selezionato, i suoi dati vengono caricati nel form
     * e lo stato di modifica viene aggiornato in base alla sua eliminabilità.
     */
    private void impostaListeners() {
        projectsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        caricaProgettoInForm(newSelection);
                        aggiornaStatoModifica(newSelection.isEliminabile());
                    } else {
                        aggiornaStatoModifica(true);
                    }
                }
        );

        add_solocanc.selectedProperty().addListener((obs, oldVal, newVal) -> {
            caricaProgetti();
            add_delete.setDisable(true);
        });
    }

    /**
     * Aggiorna lo stato di modifica del form in base all'eliminabilità del progetto.
     * Se il progetto non è eliminabile (ha attività assegnate), il form diventa read-only.
     *
     * @param modificabile true se il progetto può essere modificato, false altrimenti
     */
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

    /**
     * Salva un nuovo progetto o aggiorna un progetto esistente.
     * Se nessun progetto è selezionato, crea un nuovo progetto.
     * Se un progetto eliminabile è selezionato, lo aggiorna.
     * I progetti non eliminabili non possono essere modificati.
     */
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
                projectController.creaProgetto(nome, descrizione);
                mostraMessaggio("✅ Creato", "Nuovo progetto salvato!", Alert.AlertType.INFORMATION);
            } else if (selected.isEliminabile()) {
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

    /**
     * Elimina il progetto selezionato dopo conferma dell'utente.
     * Solo i progetti eliminabili (senza attività) possono essere eliminati.
     */
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

    /**
     * Pulisce il form e deseleziona il progetto dalla tabella,
     * riportando l'interfaccia in modalità "nuovo progetto".
     */
    @FXML
    private void pulisciForm() {
        addName.clear();
        addDesc.clear();
        aggiornaStatoModifica(true);
        projectsTable.getSelectionModel().clearSelection();
    }

    /**
     * Carica i progetti nella tabella.
     * Se il checkbox "solo eliminabili" è selezionato, carica solo i progetti eliminabili,
     * altrimenti carica tutti i progetti.
     */
    @FXML
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

    /**
     * Carica i dati di un progetto nel form per la visualizzazione o modifica.
     *
     * @param project il progetto da caricare nel form
     */
    private void caricaProgettoInForm(Project project) {
        addName.setText(project.getNome());
        addDesc.setText(project.getDescrizione());
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
