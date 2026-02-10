package it.unicam.cs.mpgc.jtime125637.view;

import it.unicam.cs.mpgc.jtime125637.controller.ActivityController;
import it.unicam.cs.mpgc.jtime125637.model.Activity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AddTaskController {
    private final ActivityController activityController = new ActivityController();
    private final ObservableList<Activity> activities = FXCollections.observableArrayList();

    @FXML private TextField new_nome;
    @FXML private TextField new_desc;
    @FXML private ChoiceBox<String> new_hh;
    @FXML private ChoiceBox<String> new_mm;
    @FXML private TableView<Activity> new_tasktable;
    @FXML private TableColumn<Activity, Integer> colId;
    @FXML private TableColumn<Activity, String> colNome;
    @FXML private TableColumn<Activity, String> colDescrizione;
    @FXML private TableColumn<Activity, Boolean> colEliminabile;
    @FXML private CheckBox new_solocanc;
    @FXML private Button new_salva;
    @FXML private Button new_del;
    @FXML private Button new_clean;

    @FXML
    public void initialize() {
        inizializzaChoiceBox();
        inizializzaTabella();
        caricaAttivita();
        impostaListeners();
    }

    private void inizializzaChoiceBox() {
        ObservableList<String> ore = FXCollections.observableArrayList();
        for (int i = 0; i <= 24; i++) {
            ore.add(String.format("%02d", i));
        }
        new_hh.setItems(ore);
        new_hh.setValue("00");

        ObservableList<String> minuti = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) {
            minuti.add(String.format("%02d", i));
        }
        new_mm.setItems(minuti);
        new_mm.setValue("00");
    }

    private void inizializzaTabella() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colEliminabile.setCellValueFactory(new PropertyValueFactory<>("eliminabile"));
        new_tasktable.setItems(activities);
    }

    /**
     * Configura i listener per la selezione della tabella e il checkbox di filtro.
     * Quando un'attività viene selezionata, i suoi dati vengono caricati nel form
     * e lo stato di modifica viene aggiornato in base alla sua eliminabilità.
     */
    private void impostaListeners() {
        new_tasktable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        caricaAttivitaInForm(newSelection);
                        aggiornaStatoModifica(newSelection.isEliminabile());
                    } else {
                        aggiornaStatoModifica(true);
                    }
                }
        );

        new_solocanc.selectedProperty().addListener((obs, oldVal, newVal) -> caricaAttivita());
    }

    /**
     * Aggiorna lo stato di modifica del form in base all'eliminabilità dell'attività.
     * Se l'attività non è eliminabile (assegnata, pianificata o completata), il form diventa read-only.
     *
     * @param modificabile true se l'attività può essere modificata, false altrimenti
     */
    private void aggiornaStatoModifica(boolean modificabile) {
        new_nome.setEditable(modificabile);
        new_desc.setEditable(modificabile);
        new_hh.setDisable(!modificabile);
        new_mm.setDisable(!modificabile);
        new_salva.setDisable(!modificabile);
        new_del.setDisable(!modificabile);

        if (!modificabile) {
            new_nome.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray;");
            new_desc.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray;");
        } else {
            new_nome.setStyle("");
            new_desc.setStyle("");
        }
    }

    /**
     * Salva una nuova attività o aggiorna un'attività esistente.
     * Se nessuna attività è selezionata, crea una nuova attività.
     * Se un'attività eliminabile è selezionata, la aggiorna.
     * Le attività non eliminabili non possono essere modificate.
     */
    @FXML
    private void salvaNuovaAttivita() {
        Activity selected = new_tasktable.getSelectionModel().getSelectedItem();

        try {
            String nome = new_nome.getText().trim();
            String descrizione = new_desc.getText().trim();
            String stima = new_hh.getValue() + ":" + new_mm.getValue();

            if (nome.isEmpty()) {
                mostraMessaggio("❌ Errore", "Il nome è obbligatorio", Alert.AlertType.ERROR);
                return;
            }

            if (selected == null) {
                activityController.creaAttivita(nome, descrizione, stima);
                mostraMessaggio("✅ Creata", "Nuova attività salvata!", Alert.AlertType.INFORMATION);
            } else if (selected.isEliminabile()) {
                selected.setNome(nome);
                selected.setDescrizione(descrizione);
                selected.setStimaTempo(stima);

                activityController.aggiornaAttivita(selected);
                mostraMessaggio("✅ Aggiornata", "Attività modificata!", Alert.AlertType.INFORMATION);
            } else {
                mostraMessaggio("⚠️ Read-only",
                        "Attività non modificabile:\n" +
                                "• Assegnata a progetto\n• Pianificata\n• Completata",
                        Alert.AlertType.WARNING);
                return;
            }

            pulisciForm();
            caricaAttivita();

        } catch (Exception e) {
            mostraMessaggio("❌ Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Elimina l'attività selezionata dopo conferma dell'utente.
     * Solo le attività eliminabili (non assegnate, non pianificate, non completate) possono essere eliminate.
     */
    @FXML
    private void eliminaAttivita() {
        Activity selected = new_tasktable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("⚠️ Attenzione", "Seleziona un'attività", Alert.AlertType.WARNING);
            return;
        }

        if (!selected.isEliminabile()) {
            mostraMessaggio("❌ Non eliminabile",
                    "Impossibile eliminare:\n" +
                            "• Assegnata a progetto\n" +
                            "• Pianificata\n" +
                            "• Completata", Alert.AlertType.WARNING);
            return;
        }

        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
        conferma.setTitle("🗑️ Conferma eliminazione");
        conferma.setHeaderText("Eliminare definitivamente?");
        conferma.setContentText(selected.getNome());

        if (conferma.showAndWait().get() == ButtonType.OK) {
            try {
                activityController.eliminaAttivita(selected.getId());
                mostraMessaggio("✅ Eliminata", "Attività rimossa!", Alert.AlertType.INFORMATION);
                pulisciForm();
                caricaAttivita();
            } catch (Exception e) {
                mostraMessaggio("❌ Errore", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Pulisce il form e deseleziona l'attività dalla tabella,
     * riportando l'interfaccia in modalità "nuova attività".
     */
    @FXML
    private void pulisciForm() {
        new_nome.clear();
        new_desc.clear();
        new_hh.setValue("00");
        new_mm.setValue("00");
        aggiornaStatoModifica(true);
        new_tasktable.getSelectionModel().clearSelection();
    }

    /**
     * Carica le attività nella tabella.
     * Se il checkbox "solo eliminabili" è selezionato, carica solo le attività eliminabili,
     * altrimenti carica tutte le attività.
     */
    private void caricaAttivita() {
        activities.clear();
        try {
            if (new_solocanc.isSelected()) {
                activities.addAll(activityController.getAttivitaEliminabili());
            } else {
                activities.addAll(activityController.getTutteAttivita());
            }
        } catch (Exception e) {
            mostraMessaggio("Errore", "Impossibile caricare attività", Alert.AlertType.ERROR);
        }
    }

    /**
     * Carica i dati di un'attività nel form per la visualizzazione o modifica.
     * Estrae ore e minuti dalla stima tempo e li imposta nei rispettivi ChoiceBox.
     *
     * @param activity l'attività da caricare nel form
     */
    private void caricaAttivitaInForm(Activity activity) {
        new_nome.setText(activity.getNome());
        new_desc.setText(activity.getDescrizione());

        if (activity.getStimaTempo() != null && !activity.getStimaTempo().isEmpty()) {
            String[] parts = activity.getStimaTempo().split(":");
            if (parts.length == 2) {
                new_hh.setValue(parts[0]);
                new_mm.setValue(parts[1]);
            }
        }
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
