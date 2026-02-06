package it.unicam.cs.mpgc.jtime125637.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URL;

@NoArgsConstructor
public class HomeViewController {

	@FXML
	private void openAddTask() {
	    openView("/fxml/jTime_addTask.fxml", "Crea Attività");
	}

    @FXML
    private void openAddProjects() {
        openView("/fxml/jTime_addProjects.fxml", "Gestione Progetti");
    }

    @FXML
    private void openManageTask() {
        openView("/fxml/jTime_manageTask.fxml", "Gestione Attività");
    }

    @FXML
    private void openCloseProjects() {
        openView("/fxml/jTime_closeProjects.fxml", "Chiudi Progetti");
    }

    @FXML
    private void openPlanning() {
        openView("/fxml/jTime_planning.fxml", "Pianificazione");
    }

    @FXML
    private void openStatistics() {
        openView("/fxml/jTime_statistics.fxml", "Analisi e Statistiche");
    }

    private void openView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
