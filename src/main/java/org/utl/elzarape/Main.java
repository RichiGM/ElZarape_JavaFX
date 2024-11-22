package org.utl.elzarape;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(Main.class.getResource("/org/utleon/elzarape/login.fxml"));
        primaryStage.setTitle("Elzarape - Sistema de Control");
        primaryStage.setScene(new Scene(parent));
        primaryStage.setMinWidth(710);  // Ancho mínimo
        primaryStage.setMinHeight(510); // Altura mínima
        primaryStage.show();
    }
}
