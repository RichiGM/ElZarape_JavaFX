package org.utl.elzarape;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.utl.elzarape.model.Alimento;
import org.utl.elzarape.model.Bebida;
import org.utl.elzarape.model.Categoria;
import org.utl.elzarape.model.Producto;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class BebidasController {

    @FXML
    private Button btnGuardar, btnLimpiar, btnCambiarEstatus, btnCargarImagen;

    @FXML
    private TableView<Bebida> tblBebidas;

    @FXML
    private TableColumn<Bebida, String> colNombre, colDescripcion, colCategoria, colFoto, colEstatus;

    @FXML
    private TableColumn<Bebida, Double> colPrecio;

    @FXML
    private TextField txtNombre, txtDescripcion, txtPrecio, txtBuscar;

    @FXML
    private ComboBox<Categoria> cbCategoria;


    @FXML
    private Label lblEstadoImagen;

    private ObservableList<Bebida> bebidas;
    private ObservableList<Categoria> categorias;
    private Bebida bebidaSelected = null;
    private String imagenBase64 = "";

    @FXML
    private void initialize() {
        System.out.println("Inicializando BebidasController...");
        initColumns();
        loadCategorias();
        loadBebidas();
        // Escuchar cambios en el campo de búsqueda
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Si el filtro está vacío, recargar todas las sucursales
                loadBebidas();
            } else {
                buscarBebida(newValue); // Buscar con el filtro
            }
        });
        tblBebidas.setOnMouseClicked(event -> showBebidasSelected());
    }

    @FXML
    private void initColumns() {
        colNombre.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getNombre()));
        colDescripcion.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getDescripcion()));
        colPrecio.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getPrecio()));
        colCategoria.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getCategoria().getDescripcion()));
        colFoto.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getFoto()));
        colEstatus.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getActivo() == 1 ? "Activo" : "Inactivo"));

        tblBebidas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }



    private void loadCategorias() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "categoria/getall").asString();

                if (response.getStatus() == 200) {
                    Gson gson = new Gson();
                    Categoria[] categoriaArray = gson.fromJson(response.getBody(), Categoria[].class);

                    // Filtrar categorías activas y de tipo "alimento"
                    categorias = FXCollections.observableArrayList();
                    for (Categoria categoria : categoriaArray) {
                        if (categoria.getActivo() == 1 && "B".equals(categoria.getTipo())) { // Solo agregar categorías activas y de tipo "alimento"
                            categorias.add(categoria);
                        }
                    }

                    Platform.runLater(() -> {
                        cbCategoria.setItems(categorias);
                    });
                    System.out.println("Categorías de tipo bebida cargadas correctamente.");
                } else {
                    System.err.println("Error al cargar categorías. Código HTTP: " + response.getStatus());
                    showAlert(Alert.AlertType.ERROR, "Error", "Hubo un problema al cargar las categorías.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar las categorías: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al intentar cargar las categorías.");
            }
        }).start();
    }

    private void loadBebidas() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "bebidas/getall").asString();

                if (response.getStatus() == 200) {
                    Gson gson = new Gson();
                    Bebida[] bebidaArray = gson.fromJson(response.getBody(), Bebida[].class);

                    bebidas = FXCollections.observableArrayList(List.of(bebidaArray));
                    Platform.runLater(() -> tblBebidas.setItems(bebidas));
                    System.out.println("Bebidas cargados correctamente.");
                } else {
                    System.err.println("Error al cargar bebidas. Código HTTP: " + response.getStatus());
                    showAlert(Alert.AlertType.ERROR, "Error", "Hubo un problema al cargar las bebidas.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar los alimentos: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al intentar cargar las bebidas.");
            }
        }).start();
    }

    private void buscarBebida(String filtro) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                String url = globals.BASE_URL + "bebidas/search/" + filtro; // Usar la API de búsqueda
                HttpResponse<String> response = Unirest.get(url).asString();

                System.out.println("Respuesta de la API: " + response.getBody()); // Imprimir respuesta

                if (response.getStatus() == 200) {
                    Gson gson = new Gson();
                    // Deserializar la respuesta JSON a un arreglo de Map
                    List<Map<String, Object>> bebidaList = gson.fromJson(response.getBody(), List.class);

                    // Limpiar la lista actual de alimentos
                    Platform.runLater(() -> {
                        bebidas.clear(); // Limpiar la lista actual
                        for (Map<String, Object> responseItem : bebidaList) {
                            // Crear un objeto Producto a partir del Map
                            Producto producto = new Producto(
                                    ((Number) responseItem.get("idBebida")).intValue(), // Cambiar a idAlimento
                                    (String) responseItem.get("nombreProducto"),
                                    (String) responseItem.get("descripcionProducto"),
                                    (String) responseItem.get("foto"),
                                    ((Number) responseItem.get("precio")).doubleValue(),
                                    // Deserializar la categoría
                                    new Categoria(
                                            ((Number) ((Map<String, Object>) responseItem.get("categoria")).get("idCategoria")).intValue(),
                                            (String) ((Map<String, Object>) responseItem.get("categoria")).get("descripcion"),
                                            (String) ((Map<String, Object>) responseItem.get("categoria")).get("tipo"),
                                            ((Number) ((Map<String, Object>) responseItem.get("categoria")).get("activo")).intValue()
                                    ),
                                    ((Number) responseItem.get("activo")).intValue()
                            );

                            // Crear un objeto Alimento a partir de Producto
                             Bebida bebida = new Bebida(((Number) responseItem.get("idBebida")).intValue(), producto);
                            bebidas.add(bebida); // Agregar el nuevo alimento a la lista
                        }
                        tblBebidas.setItems(bebidas); // Actualizar la tabla
                    });
                } else {
                    System.err.println("Error al buscar bebidas. Código HTTP: " + response.getStatus());
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo buscar las bebidas. Código: " + response.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al buscar bebidas: " + e.getMessage());
            }
        }).start();
    }



    private void showBebidasSelected() {
        bebidaSelected = tblBebidas.getSelectionModel().getSelectedItem();
        if (bebidaSelected != null) {
            txtNombre.setText(bebidaSelected.getProducto().getNombre());
            txtDescripcion.setText(bebidaSelected.getProducto().getDescripcion());
            txtPrecio.setText(String.valueOf(bebidaSelected.getProducto().getPrecio()));

            // Buscar la categoría en la lista de categorías y establecerla
            Categoria categoriaSeleccionada = bebidaSelected.getProducto().getCategoria();
            cbCategoria.setValue(categorias.stream()
                    .filter(categoria -> categoria.getIdCategoria() == categoriaSeleccionada.getIdCategoria())
                    .findFirst()
                    .orElse(null));

            // Verificar y cargar la imagen
            if (bebidaSelected.getProducto().getFoto() != null && !bebidaSelected.getProducto().getFoto().isEmpty()) {
                // Si la imagen no tiene el prefijo, agregarlo
                if (!bebidaSelected.getProducto().getFoto().startsWith("data:image")) {
                    imagenBase64 = "data:image/jpeg;base64," + bebidaSelected.getProducto().getFoto();
                } else {
                    imagenBase64 = bebidaSelected.getProducto().getFoto();
                }
                lblEstadoImagen.setText("Imagen cargada desde el registro seleccionado");
            } else {
                imagenBase64 = ""; // Vaciar la imagen si no existe
                lblEstadoImagen.setText("No se ha cargado ninguna imagen");
            }
            btnGuardar.setText("Modificar");
            btnCambiarEstatus.setVisible(true);
        }
    }

    @FXML
    private void handleGuardar() {
        try {
            if (btnGuardar.getText().equals("Agregar")) {
                agregarBebida();
            } else {
                modificarBebida();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al guardar la bebida.");
        }
    }

    private void agregarBebida() {
        try {
            // Validaciones
            if (txtNombre.getText().isEmpty() || txtDescripcion.getText().isEmpty() ||
                    txtPrecio.getText().isEmpty() || cbCategoria.getSelectionModel().isEmpty() ||
                    imagenBase64.isEmpty()) { // Validación de imagen
                showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos y cargue una imagen.");
                return;
            }

            double precio;
            try {
                precio = Double.parseDouble(txtPrecio.getText());
                if (precio < 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "El precio no puede ser negativo.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "El precio debe ser un número válido.");
                return;
            }

            Producto producto = new Producto(
                    0,
                    txtNombre.getText(),
                    txtDescripcion.getText(),
                    imagenBase64,
                    precio,
                    cbCategoria.getSelectionModel().getSelectedItem(),
                    1
            );
            Bebida nuevaBebida = new Bebida(0, producto);

            Globals globals = new Globals();
            new Thread(() -> {
                try {
                    Gson gson = new Gson();
                    String json = gson.toJson(nuevaBebida);
                    HttpResponse<String> response = Unirest.post(globals.BASE_URL + "bebidas/insert")
                            .header("Content-Type", "application/json")
                            .body(json)
                            .asString();

                    if (response.getStatus() == 200) {
                        Platform.runLater(() -> {
                            bebidas.add(nuevaBebida);
                            tblBebidas.setItems(bebidas);
                            loadBebidas();
                            limpiarFormulario();
                            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Bebida agregada correctamente.");
                        });
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo agregar la bebida. Código: " + response.getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al agregar la bebida.");
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al agregar la bebida.");
        }
    }

    private void modificarBebida() {
        try {
            if (bebidaSelected != null) {
                // Validaciones
                if (txtNombre.getText().isEmpty() || txtDescripcion.getText().isEmpty() ||
                        txtPrecio.getText().isEmpty() || cbCategoria.getSelectionModel().isEmpty() ||
                        imagenBase64.isEmpty()) { // Validación de imagen
                    showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos y cargue una imagen.");
                    return;
                }

                double precio;
                try {
                    precio = Double.parseDouble(txtPrecio.getText());
                    if (precio < 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "El precio no puede ser negativo.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "El precio debe ser un número válido.");
                    return;
                }

                bebidaSelected.getProducto().setNombre(txtNombre.getText());
                bebidaSelected.getProducto().setDescripcion(txtDescripcion.getText());
                bebidaSelected.getProducto().setFoto(imagenBase64);
                bebidaSelected.getProducto().setPrecio(precio);
                bebidaSelected.getProducto().setCategoria(cbCategoria.getSelectionModel().getSelectedItem());

                Globals globals = new Globals();
                new Thread(() -> {
                    try {
                        Gson gson = new Gson();
                        String json = gson.toJson(bebidaSelected.getProducto());
                        HttpResponse<String> response = Unirest.put(globals.BASE_URL + "bebidas/update")
                                .header("Content-Type", "application/json")
                                .body(json)
                                .asString();

                        if (response.getStatus() == 200) {
                            Platform.runLater(() -> {
                                tblBebidas.refresh();
                                limpiarFormulario();
                                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Bebida modificada correctamente.");
                            });
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo modificar la bebida. Código: " + response.getStatus());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al modificar la bebida.");
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al modificar la bebida.");
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        cbCategoria.getSelectionModel().clearSelection();

        lblEstadoImagen.setText("No se ha cargado ninguna imagen");
        imagenBase64 = "";
        bebidaSelected = null;
        tblBebidas.getSelectionModel().clearSelection();
        btnGuardar.setText("Agregar");
        btnCambiarEstatus.setVisible(false);
    }

    @FXML
    private void cargarImagen() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Imagen");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg"));

            File archivoSeleccionado = fileChooser.showOpenDialog(null);

            if (archivoSeleccionado != null) {
                FileInputStream fis = new FileInputStream(archivoSeleccionado);
                byte[] datos = fis.readAllBytes();
                fis.close();

                imagenBase64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(datos);
                lblEstadoImagen.setText("Imagen cargada correctamente");
            } else {
                lblEstadoImagen.setText("No se seleccionó ninguna imagen");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblEstadoImagen.setText("Error al cargar la imagen");
        }
    }

    @FXML
    private void cambiarEstatus() {
        try {
            if (bebidaSelected != null) {
                // Cambiar el estado activo de la bebida
                bebidaSelected.getProducto().setActivo(bebidaSelected.getProducto().getActivo() == 1 ? 0 : 1);

                Globals globals = new Globals();
                new Thread(() -> {
                    try {
                        // Usar PUT en lugar de POST y la nueva ruta
                        HttpResponse<String> response = Unirest.put(globals.BASE_URL + "bebidas/cambiarEstatus/" + bebidaSelected.getProducto().getIdProducto())
                                .header("Content-Type", "application/json")
                                .asString();

                        if (response.getStatus() == 200) {
                            Platform.runLater(() -> {
                                tblBebidas.refresh();
                                limpiarFormulario();
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
            } else {
                showAlert(Alert.AlertType.WARNING, "Advertencia", "Seleccione una bebida para cambiar el estatus.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al cambiar el estatus.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
