package org.utl.elzarape;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.application.Platform;

import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtPasswordVisible;

    @FXML
    private Button btnEntrar;

    @FXML
    private Button btnTogglePassword;

    private boolean isPasswordVisible = false;
    @FXML
    public void initialize() {
        btnEntrar.setOnAction(event -> validarCredenciales());
    }

    @FXML
    public void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            // Cambiar a texto visible
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPassword.setVisible(false);
        } else {
            // Cambiar a texto oculto
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPasswordVisible.setVisible(false);
        }
    }


    private void validarCredenciales() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        // Validar campos vacíos
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta("Error de validación", "Por favor, completa todos los campos.");
            return;
        }

        // Llamada al servidor en un hilo separado
        new Thread(() -> {
            try {
                Globals globals = new Globals();
                URL url = new URL(globals.BASE_URL + "login/validate");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Construir el JSON con los datos
                String jsonInputString = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", usuario, password);

                // Enviar datos al servidor
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInputString.getBytes("utf-8"));
                }

                // Leer respuesta del servidor
                int status = conn.getResponseCode();
                if (status == 200) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }

                        // Verificar éxito en la respuesta JSON
                        if (response.toString().contains("\"success\":true")) {
                            Platform.runLater(this::cargarPantallaPrincipal);
                        } else {
                            Platform.runLater(() -> mostrarAlerta("Inicio de sesión fallido", "Credenciales incorrectas."));
                        }
                    }
                } else {
                    Platform.runLater(() -> mostrarAlerta("Error de conexión", "Código de respuesta del servidor: " + status));
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarAlerta("Error", "Hubo un problema al conectar con el servidor."));
            }
        }).start();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cargarPantallaPrincipal() {
        try {
            Parent principalView = FXMLLoader.load(getClass().getResource("/org/utleon/elzarape/principal.fxml"));
            Stage stage = (Stage) btnEntrar.getScene().getWindow();
            stage.setScene(new Scene(principalView));
            stage.setTitle("Sistema El Zarape");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Hubo un problema al cargar la pantalla principal.");
        }
    }
}
