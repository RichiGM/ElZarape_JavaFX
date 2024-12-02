package org.utl.elzarape;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class PrincipalController {
    @FXML
    private AnchorPane mainContainer;

    @FXML
    private HBox alimentoItemBar;

    @FXML
    private HBox bebidaItemBar;

    @FXML
    private HBox categoriaItemBar;

    @FXML
    private HBox homeItemBar;

    @FXML
    private HBox sucursalItemBar;

    @FXML
    private HBox salirItemBar;

    @FXML
    private HBox usuarioItemBar;

    @FXML
    public void initialize() {
        // Cargar el módulo "Home" por defecto
        loadFXML("home");

        homeItemBar.setOnMouseClicked(event -> loadFXML("home"));

        sucursalItemBar.setOnMouseClicked(event -> loadFXML("sucursal"));

        usuarioItemBar.setOnMouseClicked(event -> loadFXML("usuario"));

        alimentoItemBar.setOnMouseClicked(event -> loadFXML("alimento"));

        bebidaItemBar.setOnMouseClicked(event -> loadFXML("bebidas"));

        categoriaItemBar.setOnMouseClicked(event -> loadFXML("categoria"));

        salirItemBar.setOnMouseClicked(event -> logout());
    }

    private void loadFXML(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/utleon/elzarape/" + fxml + ".fxml"));
            AnchorPane newLoadedPane = loader.load();

            // Configurar los anclajes del nuevo contenido
            AnchorPane.setTopAnchor(newLoadedPane, 0.0);
            AnchorPane.setRightAnchor(newLoadedPane, 0.0);
            AnchorPane.setBottomAnchor(newLoadedPane, 0.0);
            AnchorPane.setLeftAnchor(newLoadedPane, 0.0);

            // Limpiar y añadir al contenedor principal
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(newLoadedPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        try {
            // Cerrar la ventana actual
            Stage currentStage = (Stage) mainContainer.getScene().getWindow();
            currentStage.close();

            // Cargar el login.fxml en una nueva ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/utleon/elzarape/login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("Login - El Zarape");
            loginStage.setScene(new Scene(loader.load()));
            loginStage.setMinWidth(710); // Ancho mínimo
            loginStage.setMinHeight(510); // Altura mínima
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
