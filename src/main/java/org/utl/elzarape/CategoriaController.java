package org.utl.elzarape;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.utl.elzarape.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoriaController {

    @FXML
    private Button btnCambiarEstatus;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Categoria> tblCategorias;

    @FXML
    private TableColumn<Categoria, String> colDescripcion, colTipo, colEstatus;

    @FXML
    private TextField txtDescripcion, txtBuscar;

    @FXML
    private ComboBox<String> cbTipo;

    private ObservableList<Categoria> categorias;
    private Categoria categoriaSelected = null;

    @FXML
    private void initialize() {
        System.out.println("Inicializando CategoriaController...");
        initColumns();
        cargarCategorias();
        // Agregar un listener al campo de búsqueda
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                cargarCategorias(); // Si el campo está vacío, cargar todas las categorías
            } else {
                buscarCategoria(newValue); // Buscar categorías que coincidan con el texto ingresado
            }
        });
        tblCategorias.setOnMouseClicked(event -> onCategoriaSeleccionada());
        btnGuardar.setText("Agregar"); // Asegurarse de que el botón muestre "Agregar" al inicio
    }

    @FXML
    private void initColumns() {
        colDescripcion.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getDescripcion()));
        colTipo.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getTipo().equals("A") ? "Alimento" : "Bebida"));
        colEstatus.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getActivo() == 1 ? "Activo" : "Inactivo"));

        tblCategorias.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void cargarCategorias() {
        Globals globals = new Globals(); // Clase con la URL base
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "categoria/getall").asString();
                Gson gson = new Gson();
                Categoria[] categoriasArray = gson.fromJson(response.getBody(), Categoria[].class);
                categorias = FXCollections.observableArrayList(categoriasArray);
                btnCambiarEstatus.setVisible(false); // Mostrar botón de cambiar estatus
                Platform.runLater(() -> tblCategorias.setItems(categorias));
            } catch (Exception e) {
                System.err.println("No se pudo conectar a la API. Usando datos predeterminados.");
            }
        }).start();
    }

    private void buscarCategoria(String query) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                // Realiza la solicitud al endpoint de búsqueda
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "categoria/search")
                        .queryString("descripcion", query) // Envía la descripcion como parámetro
                        .asString();

                System.out.println("JSON recibido: " + response.getBody()); // Imprime la respuesta JSON

                Gson gson = new Gson();
                // Deserializa la respuesta JSON a un arreglo de Categoria
                Categoria[] categoriasArray = gson.fromJson(response.getBody(), Categoria[].class);
                List<Categoria> categoriasList = Arrays.asList(categoriasArray); // Convierte el arreglo a lista

                // Crea una ObservableList para la tabla
                ObservableList<Categoria> categoriasObservableList = FXCollections.observableArrayList(categoriasList);
                Platform.runLater(() -> {
                    tblCategorias.setItems(categoriasObservableList); // Actualiza la tabla en el hilo de la interfaz de usuario
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void onCategoriaSeleccionada() {
        categoriaSelected = tblCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSelected != null) {
            txtDescripcion.setText(categoriaSelected.getDescripcion());
            cbTipo.setValue(categoriaSelected.getTipo().equals("A") ? "Alimento" : "Bebida");
            btnGuardar.setText("Modificar"); // Cambiar texto del botón a "Modificar"
            btnGuardar.setVisible(true); // Mostrar botón de modificar
            btnCambiarEstatus.setVisible(true); // Mostrar botón de cambiar estatus
            System.out.println("Categoría seleccionada: " + categoriaSelected);
        } else {
            limpiarFormulario(); // Limpiar formulario si no hay selección
        }
    }

    @FXML
    private void handleGuardar() {
        try {
            if (btnGuardar.getText().equals("Agregar")) {
                agregarCategoria();
            } else {
                modificarCategoria();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al agregar la categoria.");
        }
    }

    @FXML
    private void agregarCategoria() {
        if (txtDescripcion.getText().isEmpty() || cbTipo .getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            return;
        }

        String tipo = cbTipo.getSelectionModel().getSelectedItem().equals("Alimento") ? "A" : "B";
        Categoria nuevaCategoria = new Categoria(0, txtDescripcion.getText(), tipo, 1);

        // Enviar la nueva categoría a la API
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(nuevaCategoria);
                HttpResponse<String> response = Unirest.post(globals.BASE_URL + "categoria/insert")
                        .header("Content-Type", "application/json")
                        .body(json)
                        .asString();

                if (response.getStatus() == 200) {
                    Platform.runLater(() -> {
                        categorias.add(nuevaCategoria);
                        tblCategorias.setItems(categorias);
                        cargarCategorias();
                        limpiarFormulario();
                        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Categoria agregada con exito.");

                    });
                } else {
                    System.err.println("Error al guardar categoría en la API: " + response.getBody());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al guardar categoría: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void modificarCategoria() {
        if (categoriaSelected == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No hay categoría seleccionada para modificar.");
            return;
        }

        categoriaSelected.setDescripcion(txtDescripcion.getText());
        categoriaSelected.setTipo(cbTipo.getSelectionModel().getSelectedItem().equals("Alimento") ? "A" : "B");

        // Enviar actualización a la API
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(categoriaSelected);
                HttpResponse<String> response = Unirest.post(globals.BASE_URL + "categoria/update")
                        .header("Content-Type", "application/json")
                        .body(json)
                        .asString();

                if (response.getStatus() == 200) {
                    System.out.println("Categoría modificada correctamente en la API.");
                    Platform.runLater(() -> {
                        tblCategorias.refresh();
                        limpiarFormulario();
                        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Categoria modificada con exito.");
                    });
                } else {
                    System.err.println("Error al modificar categoría en la API: " + response.getBody());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al modificar categoría: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void cambiarEstatus() {
        try {
            if (categoriaSelected != null) {
                // Cambiar el estatus de la categoría
                int nuevoEstatus = categoriaSelected.getActivo() == 1 ? 0 : 1; // Cambiar el estatus
                categoriaSelected.setActivo(nuevoEstatus); // Actualizar el objeto local

                // Crear un objeto para enviar a la API
                Categoria categoriaActualizada = new Categoria(categoriaSelected.getIdCategoria(), categoriaSelected.getDescripcion(), categoriaSelected.getTipo(), nuevoEstatus);

                Globals globals = new Globals();
                new Thread(() -> {
                    try {
                        Gson gson = new Gson();
                        String json = gson.toJson(categoriaActualizada);
                        HttpResponse<String> response = Unirest.post(globals.BASE_URL + "categoria/delete") // Cambia esto al endpoint correcto
                                .header("Content-Type", "application/json")
                                .body(json)
                                .asString();

                        if (response.getStatus() == 200) {
                            Platform.runLater(() -> {
                                tblCategorias.refresh(); // Refrescar la tabla
                                limpiarFormulario(); // Limpiar el formulario
                                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Estatus cambiado correctamente.");
                            });
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cambiar el estatus. Código: " + response.getStatus());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al cambiar el estatus.");
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al cambiar el estatus.");
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtDescripcion.clear();
        cbTipo.getSelectionModel().clearSelection();
        tblCategorias.getSelectionModel().clearSelection();
        categoriaSelected = null;
        btnGuardar.setText("Agregar"); // Resetear el texto del botón a "Agregar"

        btnCambiarEstatus.setVisible(false); // Ocultar botón de cambiar estatus
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}