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

    private void impostaListeners() {
        new_tasktable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        caricaAttivitaInForm(newSelection);
                        aggiornaStatoModifica(newSelection.isEliminabile());
                    } else {
                        // Modalità "nuova attività"
                        aggiornaStatoModifica(true);
                    }
                }
        );

        new_solocanc.selectedProperty().addListener((obs, oldVal, newVal) -> caricaAttivita());
    }

    /** ✅ LOGICA CHIAVE: Controlla se è modificabile in base a isEliminabile() */
    private void aggiornaStatoModifica(boolean modificabile) {
        new_nome.setEditable(modificabile);
        new_desc.setEditable(modificabile);
        new_hh.setDisable(!modificabile);
        new_mm.setDisable(!modificabile);
        new_salva.setDisable(!modificabile);
        new_del.setDisable(!modificabile);

        // Stile visivo per indicare read-only
        if (!modificabile) {
            new_nome.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray;");
            new_desc.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: gray;");
        } else {
            new_nome.setStyle("");
            new_desc.setStyle("");
        }
    }

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
                // 🆕 NUOVA attività
                activityController.creaAttivita(nome, descrizione, stima);
                mostraMessaggio("✅ Creata", "Nuova attività salvata!", Alert.AlertType.INFORMATION);
            } else if (selected.isEliminabile()) {
                selected.setNome(nome);
                selected.setDescrizione(descrizione);
                selected.setStimaTempo(stima);

                activityController.aggiornaAttivita(selected);
                mostraMessaggio("✅ Aggiornata", "Attività modificata!",Alert.AlertType.INFORMATION);
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

        // 🔒 Conferma eliminazione
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

    @FXML
    private void pulisciForm() {
        new_nome.clear();
        new_desc.clear();
        new_hh.setValue("00");
        new_mm.setValue("00");
        // Torna in modalità modifica completa (nuova attività)
        aggiornaStatoModifica(true);
        new_tasktable.getSelectionModel().clearSelection();
    }

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
        // Lo stato editable è gestito da aggiornaStatoModifica()
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
