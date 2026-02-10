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

import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
public class ManageTaskController {
    private final ActivityController activityController = new ActivityController();
    private final ProjectController projectController = new ProjectController();
    private final ObservableList<Project> progettiList = FXCollections.observableArrayList();
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


    @FXML
    public void initialize() {
        inizializzaTabella();
        inizializzaChoiceBox();
        caricaProgetti();
        impostaListenerSelezioneTabella();
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

    /**
     * Carica i progetti attivi nei ChoiceBox per filtro e assegnazione.
     * Configura anche il converter per visualizzare il nome del progetto.
     */
    private void caricaProgetti() {
        try {
            progettiList.clear();
            progettiList.addAll(projectController.getProgettiAttivi());
            manage_task.setItems(progettiList);
            manage_assegna.setItems(progettiList);
            setupProjectConverter(manage_task);
            setupProjectConverter(manage_assegna);
        } catch (Exception e) {
            System.err.println("Errore caricamento progetti: " + e.getMessage());
        }
    }

    /**
     * Configura il converter per visualizzare il nome del progetto nel ChoiceBox.
     *
     * @param choiceBox il ChoiceBox da configurare
     */
    private void setupProjectConverter(ChoiceBox<Project> choiceBox) {
        choiceBox.setConverter(new javafx.util.StringConverter<Project>() {
            @Override
            public String toString(Project project) {
                return project == null ? "Nessun progetto" : project.getNome();
            }
            @Override
            public Project fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Configura il listener per la selezione della tabella.
     * Quando un'attività viene selezionata, carica i suoi dati nel form.
     * Le attività completate vengono rese read-only.
     */
    private void impostaListenerSelezioneTabella() {
        manage_table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                caricaDatiAttivita(newSelection);
                aggiornaStatoModifica(!newSelection.isCompletata());
            } else {
                aggiornaStatoModifica(true);
            }
        });
    }

    /**
     * Aggiorna lo stato di modifica del form in base allo stato di completamento dell'attività.
     * Le attività completate non possono essere modificate e i campi vengono disabilitati.
     *
     * @param modificabile true se l'attività può essere modificata, false se è completata
     */
    private void aggiornaStatoModifica(boolean modificabile) {
        manage_add_data.setDisable(!modificabile);
        manage_assegna.setDisable(!modificabile);
        manage_hh.setDisable(!modificabile);
        manage_mm.setDisable(!modificabile);

        if (!modificabile) {
            manage_add_data.setStyle("-fx-opacity: 0.6;");
            manage_assegna.setStyle("-fx-opacity: 0.6;");
            manage_hh.setStyle("-fx-opacity: 0.6;");
            manage_mm.setStyle("-fx-opacity: 0.6;");
        } else {
            manage_add_data.setStyle("");
            manage_assegna.setStyle("");
            manage_hh.setStyle("");
            manage_mm.setStyle("");
        }
    }

    /**
     * Carica i dati di un'attività nel form per visualizzazione o modifica.
     * Include data di pianificazione, progetto assegnato e durata effettiva.
     *
     * @param attivita l'attività di cui caricare i dati
     */
    private void caricaDatiAttivita(Activity attivita) {
        try {
            Activity attivitaAggiornata = activityController.getAttivitaById(attivita.getId());

            if (attivitaAggiornata.getDataPianificazione() != null) {
                LocalDate dataLocal = convertToLocalDate(attivitaAggiornata.getDataPianificazione());
                manage_add_data.setValue(dataLocal);
            } else {
                manage_add_data.setValue(null);
            }

            if (attivitaAggiornata.getProject() != null) {
                for (Project proj : progettiList) {
                    if (proj.getId().equals(attivitaAggiornata.getProject().getId())) {
                        manage_assegna.setValue(proj);
                        break;
                    }
                }
            } else {
                manage_assegna.setValue(null);
            }

            if (attivitaAggiornata.getDurataEffettiva() != null &&
                    !attivitaAggiornata.getDurataEffettiva().equals("00:00")) {
                String[] parti = attivitaAggiornata.getDurataEffettiva().split(":");
                if (parti.length == 2) {
                    manage_hh.setValue(parti[0]);
                    manage_mm.setValue(parti[1]);
                }
            } else {
                manage_hh.setValue("00");
                manage_mm.setValue("00");
            }

        } catch (Exception e) {
            System.err.println("Errore caricamento dati attività: " + e.getMessage());
            pulisciForm();
        }
    }

    /**
     * Cerca attività applicando filtri multipli: ID, nome, progetto e stati
     * (attive, completate, pianificate, assegnate).
     */
    @FXML
    private void cercaAttivita() {
        try {
            Integer id = null;
            if (!manage_cerca_id.getText().trim().isEmpty()) {
                id = Integer.parseInt(manage_cerca_id.getText().trim());
            }
            String nome = manage_cerca_nome.getText().trim();
            Integer projectId = manage_task.getValue() != null ? manage_task.getValue().getId() : null;

            activities.clear();
            activities.addAll(activityController.cercaAttivita(
                    id, nome.isEmpty() ? null : nome, projectId,
                    manage_completate.isSelected(),
                    manage_pianificate.isSelected(),
                    manage_assegnate.isSelected(),
                    manage_attive.isSelected()
            ));

            manage_table.getSelectionModel().clearSelection();
            pulisciForm();

        } catch (NumberFormatException e) {
            mostraMessaggio("Errore", "ID attività non valido", Alert.AlertType.ERROR);
        }
    }

    /**
     * Salva le modifiche all'attività selezionata.
     * Può pianificare, assegnare a progetto e/o completare l'attività.
     * Le attività già completate non possono essere modificate.
     */
    @FXML
    private void salvaModifiche() {
        Activity selected = manage_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("⚠️ Attenzione", "Seleziona un'attività", Alert.AlertType.WARNING);
            return;
        }

        if (selected.isCompletata()) {
            mostraMessaggio("❌ Impossibile",
                    "Attività completata non modificabile!\n" +
                            "Durata effettiva: " + selected.getDurataEffettiva(),
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            boolean haModifiche = false;

            if (manage_add_data.getValue() != null) {
                activityController.pianificaAttivita(selected.getId(), manage_add_data.getValue());
                haModifiche = true;
            }

            Project progettoSelezionato = manage_assegna.getValue();
            if (progettoSelezionato != null) {
                activityController.associaProgetto(selected.getId(), progettoSelezionato.getId());
                haModifiche = true;
            }

            String durataEffettiva = manage_hh.getValue() + ":" + manage_mm.getValue();
            if (!durataEffettiva.equals("00:00")) {
                activityController.completaAttivita(selected.getId(), durataEffettiva);
                haModifiche = true;
            }

            if (haModifiche) {
                mostraMessaggio("✅ Successo", "Attività aggiornata!", Alert.AlertType.INFORMATION);
                cercaAttivita();
            } else {
                mostraMessaggio("ℹ️ Info", "Nessuna modifica", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            mostraMessaggio("❌ Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Pulisce tutti i campi del form e ripristina lo stato di modifica.
     */
    @FXML
    private void pulisciForm() {
        manage_add_data.setValue(null);
        manage_assegna.setValue(null);
        manage_hh.setValue("00");
        manage_mm.setValue("00");
        aggiornaStatoModifica(true);
    }

    /**
     * Converte un oggetto Date in LocalDate.
     *
     * @param date la data da convertire
     * @return la data convertita in LocalDate
     */
    private LocalDate convertToLocalDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
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
