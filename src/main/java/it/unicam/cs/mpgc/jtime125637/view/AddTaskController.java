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
                }
            }
        );

        new_solocanc.selectedProperty().addListener((obs, oldVal, newVal) -> caricaAttivita());
    }

    @FXML
    private void salvaNuovaAttivita() {
        try {
            String nome = new_nome.getText();
            String descrizione = new_desc.getText();
            String stima = new_hh.getValue() + ":" + new_mm.getValue();

            activityController.creaAttivita(nome, descrizione, stima);
            mostraMessaggio("Successo", "Attività creata con successo", Alert.AlertType.INFORMATION);
            pulisciForm();
            caricaAttivita();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminaAttivita() {
        Activity selected = new_tasktable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraMessaggio("Attenzione", "Seleziona un'attività da eliminare", Alert.AlertType.WARNING);
            return;
        }

        try {
            activityController.eliminaAttivita(selected.getId());
            mostraMessaggio("Successo", "Attività eliminata con successo", Alert.AlertType.INFORMATION);
            pulisciForm();
            caricaAttivita();
        } catch (Exception e) {
            mostraMessaggio("Errore", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void pulisciForm() {
        new_nome.clear();
        new_desc.clear();
        new_hh.setValue("00");
        new_mm.setValue("00");
        new_nome.setEditable(true);
        new_desc.setEditable(true);
        new_hh.setDisable(false);
        new_mm.setDisable(false);
        new_tasktable.getSelectionModel().clearSelection();
    }

    private void caricaAttivita() {
        activities.clear();
        if (new_solocanc.isSelected()) {
            activities.addAll(activityController.getAttivitaEliminabili());
        } else {
            activities.addAll(activityController.getTutteAttivita());
        }
    }

    private void caricaAttivitaInForm(Activity activity) {
        new_nome.setText(activity.getNome());
        new_desc.setText(activity.getDescrizione());
        if (activity.getStimaTempo() != null) {
            String[] parts = activity.getStimaTempo().split(":");
            new_hh.setValue(parts[0]);
            new_mm.setValue(parts[1]);
        }
        new_nome.setEditable(false);
        new_desc.setEditable(false);
        new_hh.setDisable(true);
        new_mm.setDisable(true);
    }

    private void mostraMessaggio(String titolo, String messaggio, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
