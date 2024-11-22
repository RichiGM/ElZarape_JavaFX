package org.utl.elzarape;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnEntrar;

    @FXML
    public void initialize() {
        btnEntrar.setOnAction(event -> {
            validarCredenciales();
        });
    }

    private void validarCredenciales() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if ("admin".equals(usuario) && "1234".equals(password)) {
            try {
                // Cargar la pantalla principal
                Parent principalView = FXMLLoader.load(getClass().getResource("/org/utleon/elzarape/principal.fxml"));
                Stage stage = (Stage) btnEntrar.getScene().getWindow();
                stage.setScene(new Scene(principalView));
                stage.setTitle("Sistema El Zarape");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Mostrar error (puedes mejorarlo con una alerta)
            txtUsuario.clear();
            txtPassword.clear();
            txtUsuario.setPromptText("Usuario incorrecto");
            txtPassword.setPromptText("Contraseña incorrecta");
        }
    }
}
