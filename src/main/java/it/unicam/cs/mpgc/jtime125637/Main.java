package it.unicam.cs.mpgc.jtime125637;

import it.unicam.cs.mpgc.jtime125637.model.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/jTime_home.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        URL cssUrl = getClass().getResource("/css/application.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("jTime - Gestione Attività e Progetti");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
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
