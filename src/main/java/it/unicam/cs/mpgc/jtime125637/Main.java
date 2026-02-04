package it.unicam.cs.mpgc.jtime125637;

import it.unicam.cs.mpgc.jtime125637.model.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/jTime_home.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            
            primaryStage.setTitle("jTime - Gestione Attività e Progetti");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dell'interfaccia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
        System.out.println("Applicazione chiusa correttamente");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
