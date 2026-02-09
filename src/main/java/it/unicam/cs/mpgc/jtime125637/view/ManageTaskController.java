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
import java.time.format.DateTimeFormatter;
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
        // Ore (00-24)
        ObservableList<String> ore = FXCollections.observableArrayList();
        for (int i = 0; i <= 24; i++) {
            ore.add(String.format("%02d", i));
        }
        manage_hh.setItems(ore);
        manage_hh.setValue("00");

        // Minuti (incrementi di 5)
        ObservableList<String> minuti = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) {
            minuti.add(String.format("%02d", i));
        }
        manage_mm.setItems(minuti);
        manage_mm.setValue("00");
    }

    private void caricaProgetti() {
        try {
            progettiList.clear();
            progettiList.addAll(projectController.getProgettiAttivi());

            // Popola entrambi i ChoiceBox
            manage_task.setItems(progettiList);
            manage_assegna.setItems(progettiList);

            // Converter per mostrare nome progetto
            setupProjectConverter(manage_task);
            setupProjectConverter(manage_assegna);

        } catch (Exception e) {
            System.err.println("Errore caricamento progetti: " + e.getMessage());
        }
    }

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

    /** CARICAMENTO AUTOMATICO DATI DAL DB */
    private void impostaListenerSelezioneTabella() {
        manage_table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                caricaDatiAttivita(newSelection);
            } else {
                pulisciForm();
            }
        });
    }

    private void caricaDatiAttivita(Activity attivita) {
        try {
            // Ricarica l'attività dal DB per dati aggiornati
            Activity attivitaAggiornata = activityController.getAttivitaById(attivita.getId());

            // Data pianificazione
            if (attivitaAggiornata.getDataPianificazione() != null) {
                LocalDate dataLocal = convertToLocalDate(attivitaAggiornata.getDataPianificazione());
                manage_add_data.setValue(dataLocal);
            } else {
                manage_add_data.setValue(null);
            }

            // Progetto assegnato
            if (attivitaAggiornata.getProject() != null) {
                // Cerca il progetto nella lista
                for (Project proj : progettiList) {
                    if (proj.getId().equals(attivitaAggiornata.getProject().getId())) {
                        manage_assegna.setValue(proj);
                        break;
                    }
                }
            } else {
                manage_assegna.setValue(null);
            }

            // Durata effettiva (tempo di chiusura)
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

            // Pulisci selezione e form quando si ricercano nuove attività
            manage_table.getSelectionModel().clearSelection();
            pulisciForm();

        } catch (NumberFormatException e) {
            mostraMessaggio("Errore", "ID attività non valido", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void salvaModifiche() {
        Activity selected = manage_table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("Attenzione", "Seleziona un'attività dalla tabella", Alert.AlertType.WARNING);
            return;
        }

        try {
            boolean haModifiche = false;

            // Pianificazione data
            if (manage_add_data.getValue() != null) {
                activityController.pianificaAttivita(selected.getId(), manage_add_data.getValue());
                haModifiche = true;
            }

            // Assegnazione progetto
            Project progettoSelezionato = manage_assegna.getValue();
            if (progettoSelezionato != null) {
                activityController.associaProgetto(selected.getId(), progettoSelezionato.getId());
                haModifiche = true;
            }

            // Tempo di chiusura
            String durataEffettiva = manage_hh.getValue() + ":" + manage_mm.getValue();
            if (!durataEffettiva.equals("00:00")) {
                activityController.completaAttivita(selected.getId(), durataEffettiva);
                haModifiche = true;
            }

            if (haModifiche) {
                mostraMessaggio("Successo", "Attività aggiornata con successo!", Alert.AlertType.INFORMATION);
                cercaAttivita(); // Ricarica la tabella
            } else {
                mostraMessaggio("Info", "Nessuna modifica da salvare", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            mostraMessaggio("Errore", "Impossibile salvare: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void pulisciForm() {
        manage_add_data.setValue(null);
        manage_assegna.setValue(null);
        manage_hh.setValue("00");
        manage_mm.setValue("00");
    }

    // Utility per convertire Date -> LocalDate
    private LocalDate convertToLocalDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
