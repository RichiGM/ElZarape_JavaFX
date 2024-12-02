package org.utl.elzarape;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.utl.elzarape.model.Alimento;
import org.utl.elzarape.model.Categoria;
import org.utl.elzarape.model.Producto;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.utl.elzarape.model.Sucursal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class AlimentoController {

    @FXML
    private Button btnGuardar, btnLimpiar, btnCambiarEstatus,btnCargarImagen;

    @FXML
    private TableView<Alimento> tblAlimentos;

    @FXML
    private TableColumn<Alimento, String> colNombre, colDescripcion, colCategoria, colEstatus;

    @FXML
    private TableColumn<Alimento, ImageView> colFoto;
    @FXML
    private TableColumn<Alimento, Double> colPrecio;

    @FXML
    private TextField txtNombre, txtDescripcion, txtPrecio, txtBuscar;

    @FXML
    private ComboBox<Categoria> cbCategoria;

    @FXML
    private ImageView ivFoto;


    @FXML
    private Label lblEstadoImagen;

    private ObservableList<Alimento> alimentos;
    private ObservableList<Categoria> categorias;
    private Alimento alimentoSelected = null;
    private String imagenBase64 = "";

    @FXML
    private void initialize() {
        System.out.println("Inicializando AlimentoController...");
        initColumns();
        loadCategorias();
        loadAlimentos();
        // Escuchar cambios en el campo de búsqueda
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Si el filtro está vacío, recargar todas las sucursales
                loadAlimentos();
            } else {
                buscarAlimento(newValue); // Buscar con el filtro
            }
        });
        tblAlimentos.setOnMouseClicked(event -> showAlimentoSelected());
        Image image = new Image(getClass().getResourceAsStream("/org/utleon/elzarape/img/placeholder.png"));
        ivFoto.setImage(image);
    }
    @FXML
    private void initColumns() {
        colNombre.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getNombre()));
        colDescripcion.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getDescripcion()));
        colPrecio.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getPrecio()));
        colCategoria.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getCategoria().getDescripcion()));

        // Cambiar aquí para usar ImageView
        colFoto.setCellValueFactory(col -> {
            String fotoBase64 = col.getValue().getProducto().getFoto();
            ImageView imageView = new ImageView();

            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                if (fotoBase64.startsWith("data:image")) {
                    try {
                        Image image = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(fotoBase64.split(",")[1])));
                        imageView.setImage(image);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Error al decodificar la imagen: " + e.getMessage());
                    }
                } else {
                    System.err.println("La cadena no tiene el prefijo correcto: " + fotoBase64);
                }
            } else {
                System.err.println("No hay imagen disponible para este alimento.");
            }

            // Establecer propiedades del ImageView
            imageView.setPreserveRatio(true); // Mantener la relación de aspecto
            imageView.setSmooth(true); // Suavizar la imagen

            // Ajustar el tamaño del ImageView
            imageView.setFitHeight(75); // Alto fijo
            imageView.setFitWidth(75); // Ancho inicial (puedes quitar esto si no quieres un ancho fijo)

            return new SimpleObjectProperty<>(imageView);
        });

        colEstatus.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getProducto().getActivo() == 1 ? "Activo" : "Inactivo"));

        tblAlimentos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                        if (categoria.getActivo() == 1 && "A".equals(categoria.getTipo())) { // Solo agregar categorías activas y de tipo "alimento"
                            categorias.add(categoria);
                        }
                    }

                    Platform.runLater(() -> {
                        cbCategoria.setItems(categorias);
                    });
                    System.out.println("Categorías de tipo alimento cargadas correctamente.");
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

    private void loadAlimentos() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "alimento/getall").asString();

                if (response.getStatus() == 200) {
                    Gson gson = new Gson();
                    Alimento[] alimentoArray = gson.fromJson(response.getBody(), Alimento[].class);

                    alimentos = FXCollections.observableArrayList(List.of(alimentoArray));
                    Platform.runLater(() -> tblAlimentos.setItems(alimentos));
                    System.out.println("Alimentos cargados correctamente.");
                } else {
                    System.err.println("Error al cargar alimentos. Código HTTP: " + response.getStatus());
                    showAlert(Alert.AlertType.ERROR, "Error", "Hubo un problema al cargar los alimentos.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar los alimentos: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al intentar cargar los alimentos.");
            }
        }).start();
    }

    private void buscarAlimento(String filtro) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                String url = globals.BASE_URL + "alimento/search/" + filtro; // Usar la API de búsqueda
                HttpResponse<String> response = Unirest.get(url).asString();

                System.out.println("Respuesta de la API: " + response.getBody()); // Imprimir respuesta

                if (response.getStatus() == 200) {
                    Gson gson = new Gson();
                    // Deserializar la respuesta JSON a un arreglo de Map
                    List<Map<String, Object>> alimentoList = gson.fromJson(response.getBody(), List.class);

                    // Limpiar la lista actual de alimentos
                    Platform.runLater(() -> {
                        alimentos.clear(); // Limpiar la lista actual
                        for (Map<String, Object> responseItem : alimentoList) {
                            // Crear un objeto Producto a partir del Map
                            Producto producto = new Producto(
                                    ((Number) responseItem.get("idAlimento")).intValue(), // Cambiar a idAlimento
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
                            Alimento alimento = new Alimento(((Number) responseItem.get("idAlimento")).intValue(), producto);
                            alimentos.add(alimento); // Agregar el nuevo alimento a la lista
                        }
                        tblAlimentos.setItems(alimentos); // Actualizar la tabla
                    });
                } else {
                    System.err.println("Error al buscar alimentos. Código HTTP: " + response.getStatus());
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo buscar los alimentos. Código: " + response.getStatus());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al buscar alimentos: " + e.getMessage());
            }
        }).start();
    }



    private void showAlimentoSelected() {
        alimentoSelected = tblAlimentos.getSelectionModel().getSelectedItem();
        if (alimentoSelected != null) {
            txtNombre.setText(alimentoSelected.getProducto().getNombre());
            txtDescripcion.setText(alimentoSelected.getProducto().getDescripcion());
            txtPrecio.setText(String.valueOf(alimentoSelected.getProducto().getPrecio()));

            // Buscar la categoría en la lista de categorías y establecerla
            Categoria categoriaSeleccionada = alimentoSelected.getProducto().getCategoria();
            cbCategoria.setValue(categorias.stream()
                    .filter(categoria -> categoria.getIdCategoria() == categoriaSeleccionada.getIdCategoria())
                    .findFirst()
                    .orElse(null));

            // Verificar y cargar la imagen00
            if (alimentoSelected.getProducto().getFoto() != null && !alimentoSelected.getProducto().getFoto().isEmpty()) {
                // Si la imagen no tiene el prefijo, agregarlo
                if (!alimentoSelected.getProducto().getFoto().startsWith("data:image")) {
                    imagenBase64 = "data:image/jpeg;base64," + alimentoSelected.getProducto().getFoto();
                } else {
                    imagenBase64 = alimentoSelected.getProducto().getFoto();
                }
                lblEstadoImagen.setText("Imagen cargada desde el registro seleccionado");

                // Cargar la imagen en el ImageView
                byte[] imageBytes = Base64.getDecoder().decode(imagenBase64.split(",")[1]); // Obtener solo la parte de Base64
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                ivFoto.setImage(image);
            } else {
                imagenBase64 = ""; // Vaciar la imagen si no existe
                lblEstadoImagen.setText("No se ha cargado ninguna imagen");
                ivFoto.setImage(null); // Limpiar el ImageView
            }
            btnGuardar.setText("Modificar");
            btnCambiarEstatus.setVisible(true);
        }
    }

    @FXML
    private void handleGuardar() {
        try {
            if (btnGuardar.getText().equals("Agregar")) {
                agregarAlimento();
            } else {
                modificarAlimento();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al guardar el alimento.");
        }
    }

    private void agregarAlimento() {
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
            Alimento nuevoAlimento = new Alimento(0, producto);

            Globals globals = new Globals();
            new Thread(() -> {
                try {
                    Gson gson = new Gson();
                    String json = gson.toJson(nuevoAlimento);
                    HttpResponse<String> response = Unirest.post(globals.BASE_URL + "alimento/insert")
                            .header("Content-Type", "application/json")
                            .body(json)
                            .asString();

                    if (response.getStatus() == 200) {
                        Platform.runLater(() -> {
                            alimentos.add(nuevoAlimento);
                            tblAlimentos.setItems(alimentos);
                            loadAlimentos();
                            limpiarFormulario();
                            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Alimento agregado correctamente.");
                        });
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo agregar el alimento. Código: " + response.getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al agregar el alimento.");
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al agregar el alimento.");
        }
    }

    private void modificarAlimento() {
        try {
            if (alimentoSelected != null) {
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

                alimentoSelected.getProducto().setNombre(txtNombre.getText());
                alimentoSelected.getProducto().setDescripcion(txtDescripcion.getText());
                alimentoSelected.getProducto().setFoto(imagenBase64);
                alimentoSelected.getProducto().setPrecio(precio);
                alimentoSelected.getProducto().setCategoria(cbCategoria.getSelectionModel().getSelectedItem());

                Globals globals = new Globals();
                new Thread(() -> {
                    try {
                        Gson gson = new Gson();
                        String json = gson.toJson(alimentoSelected.getProducto());
                        HttpResponse<String> response = Unirest.post(globals.BASE_URL + "alimento/update")
                                .header("Content-Type", "application/json")
                                .body(json)
                                .asString();

                        if (response.getStatus() == 200) {
                            Platform.runLater(() -> {
                                tblAlimentos.refresh();
                                limpiarFormulario();
                                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Alimento modificado correctamente.");
                            });
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo modificar el alimento. Código: " + response.getStatus());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al modificar el alimento.");
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Hubo un error al modificar el alimento.");
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
        alimentoSelected = null;
        tblAlimentos.getSelectionModel().clearSelection();
        btnGuardar.setText("Agregar");
        btnCambiarEstatus.setVisible(false);
        Image image = new Image(getClass().getResourceAsStream("/org/utleon/elzarape/img/placeholder.png"));
        ivFoto.setImage(image);
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

                byte[] imageBytes = Base64.getDecoder().decode(imagenBase64.split(",")[1]); // Obtener solo la parte de Base64
                Image image = new Image(new ByteArrayInputStream(imageBytes));

                // Asignar la imagen al ImageView
                ivFoto.setImage(image);
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
            if (alimentoSelected != null) {
                alimentoSelected.getProducto().setActivo(alimentoSelected.getProducto().getActivo() == 1 ? 0 : 1);

                Globals globals = new Globals();
                new Thread(() -> {
                    try {
                        HttpResponse<String> response = Unirest.post(globals.BASE_URL + "alimento/delete/" + alimentoSelected.getProducto().getIdProducto())
                                .header("Content-Type", "application/json")
                                .asString();

                        if (response.getStatus() == 200) {
                            Platform.runLater(() -> {
                                tblAlimentos.refresh();
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
