package org.utl.elzarape;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.stage.FileChooser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.utl.elzarape.model.Ciudad;
import org.utl.elzarape.model.Estado;
import org.utl.elzarape.model.Sucursal;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.List;

public class SucursalController {

    @FXML
    private Button btnCambiarEstatus, btnLimpiar, btnGuardar;

    @FXML
    private TableView<Sucursal> tblSucursales;

    @FXML
    private TableColumn<Sucursal, String> colNombre, colDireccion;

    @FXML
    private TableColumn<Sucursal, String> colEstatus;

    @FXML
    private ComboBox<Ciudad> cbCiudad;

    @FXML
    private ComboBox<Estado> cbEstado;

    @FXML
    private TextField txtNombre, txtCalle, txtNumCalle, txtColonia, txtLatitud, txtLongitud, txtFoto, txtUrl, txtHorarios;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnCargarImagen;

    @FXML
    private Label lblEstadoImagen;

    private String imagenBase64 = ""; // Variable para almacenar la imagen en Base64


    private ObservableList<Sucursal> sucursales;
    private ObservableList<Ciudad> ciudades;
    private Sucursal sucursalSelected = null;

    @FXML
    private void initialize() {
        System.out.println("Inicializando SucursalController...");
        initColumns();
        loadSucursales();
        loadEstados();

        // Escuchar cambios en el campo de búsqueda
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Si el filtro está vacío, recargar todas las sucursales
                loadSucursales();
            } else {
                buscarSucursales(newValue); // Buscar con el filtro
            }
        });

        tblSucursales.setOnMouseClicked(event -> showSucursalSelected());
    }


    @FXML
    private void initColumns() {
        colNombre.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getNombre())); // Cambiado a "getNombre"
        colDireccion.setCellValueFactory(col -> new SimpleObjectProperty<>(
                col.getValue().getCalle() + " #" +
                        col.getValue().getNumCalle() + ", " +
                        col.getValue().getColonia() + ", " +
                        col.getValue().getCiudad().getNombre() + ", " +
                        col.getValue().getEstado().getNombre()));

        colEstatus.setCellValueFactory(col -> new SimpleObjectProperty<>(
                col.getValue().getSucursalActivo() == 1 ? "Activo" : "Inactivo"));

        tblSucursales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void buscarSucursales(String filtro) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                String url = filtro.isEmpty()
                        ? globals.BASE_URL + "sucursal/getall" // Si no hay filtro, cargar todas las sucursales
                        : globals.BASE_URL + "sucursal/search/" + filtro; // Si hay filtro, usar la API de búsqueda

                HttpResponse<String> response = Unirest.get(url).asString();
                Gson gson = new Gson();
                Sucursal[] sucursalArray = gson.fromJson(response.getBody(), Sucursal[].class);

                // Actualizar la tabla con los resultados
                ObservableList<Sucursal> resultados = FXCollections.observableArrayList(List.of(sucursalArray));
                Platform.runLater(() -> tblSucursales.setItems(resultados));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al buscar sucursales: " + e.getMessage());
            }
        }).start();
    }

    private void loadSucursales() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "sucursal/getall").asString();
                System.out.println("JSON recibido: " + response.getBody());
                Gson gson = new Gson();
                Sucursal[] sucursalArray = gson.fromJson(response.getBody(), Sucursal[].class);

                // Actualizar el estado de las sucursales
                for (Sucursal sucursal : sucursalArray) {
                    if (sucursal.getSucursalActivo() == 1) {
                        sucursal.setSucursalActivo(1); // Activo
                    } else {
                        sucursal.setSucursalActivo(0); // Inactivo
                    }
                }

                sucursales = FXCollections.observableArrayList(List.of(sucursalArray));
                Platform.runLater(() -> tblSucursales.setItems(sucursales));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar sucursales: " + e.getMessage());
            }
        }).start();
    }


    private void loadEstados() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "sucursal/getestados").asString();
                Gson gson = new Gson();
                Estado[] estadoArray = gson.fromJson(response.getBody(), Estado[].class);
                ObservableList<Estado> listaEstados = FXCollections.observableArrayList(List.of(estadoArray));
                Platform.runLater(() -> cbEstado.setItems(listaEstados));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar estados: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void onEstadoSelected() {
        Estado selectedEstado = cbEstado.getSelectionModel().getSelectedItem();
        if (selectedEstado != null) {
            loadCiudadesByEstado(selectedEstado.getIdEstado(), null);
        }
    }

    @FXML
    private void showSucursalSelected() {
        sucursalSelected = tblSucursales.getSelectionModel().getSelectedItem();
        if (sucursalSelected != null) {
            txtNombre.setText(sucursalSelected.getNombre());
            txtLatitud.setText(sucursalSelected.getLatitud());
            txtLongitud.setText(sucursalSelected.getLongitud());
            txtUrl.setText(sucursalSelected.getUrlWeb());
            txtHorarios.setText(sucursalSelected.getHorarios());
            txtCalle.setText(sucursalSelected.getCalle());
            txtNumCalle.setText(sucursalSelected.getNumCalle());
            txtColonia.setText(sucursalSelected.getColonia());

            // Seleccionar el estado
            cbEstado.getSelectionModel().select(
                    cbEstado.getItems()
                            .stream()
                            .filter(estado -> estado.getIdEstado() == sucursalSelected.getEstado().getIdEstado())
                            .findFirst()
                            .orElse(null)
            );

            // Cargar las ciudades del estado seleccionado y luego seleccionar la ciudad
            Estado selectedEstado = sucursalSelected.getEstado();
            if (selectedEstado != null) {
                loadCiudadesByEstado(selectedEstado.getIdEstado(), () -> {
                    Platform.runLater(() -> {
                        cbCiudad.getSelectionModel().select(
                                cbCiudad.getItems()
                                        .stream()
                                        .filter(ciudad -> ciudad.getIdCiudad() == sucursalSelected.getCiudad().getIdCiudad())
                                        .findFirst()
                                        .orElse(null)
                        );
                    });
                });
            }

            // Verificar y cargar la imagen
            if (sucursalSelected.getFoto() != null && !sucursalSelected.getFoto().isEmpty()) {
                // Si la imagen no tiene el prefijo, agregarlo
                if (!sucursalSelected.getFoto().startsWith("data:image")) {
                    imagenBase64 = "data:image/jpeg;base64," + sucursalSelected.getFoto();
                } else {
                    imagenBase64 = sucursalSelected.getFoto();
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





    private void loadCiudadesByEstado(int idEstado, Runnable onComplete) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "sucursal/getciudades/" + idEstado).asString();
                Gson gson = new Gson();
                Ciudad[] ciudadArray = gson.fromJson(response.getBody(), Ciudad[].class);
                ObservableList<Ciudad> ciudadList = FXCollections.observableArrayList(List.of(ciudadArray));
                Platform.runLater(() -> {
                    cbCiudad.setItems(ciudadList);
                    if (onComplete != null) {
                        onComplete.run(); // Ejecuta el Runnable cuando las ciudades se hayan cargado
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar ciudades: " + e.getMessage());
            }
        }).start();
    }




    @FXML
    private void cleanForm() {
        tblSucursales.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtLatitud.clear();
        txtLongitud.clear();
        lblEstadoImagen.setText("No se ha cargado ninguna imagen");
        imagenBase64 = "";
        txtUrl.clear();
        txtHorarios.clear();
        txtCalle.clear();
        txtNumCalle.clear();
        txtColonia.clear();
        cbCiudad.getSelectionModel().clearSelection();
        cbEstado.getSelectionModel().clearSelection();
        btnGuardar.setText("Agregar");
        btnCambiarEstatus.setVisible(false);
    }


    @FXML
    private void cambiarEstatus() {
        if (sucursalSelected != null) {
            int nuevoEstatus = sucursalSelected.getSucursalActivo() == 1 ? 0 : 1;
            sucursalSelected.setSucursalActivo(nuevoEstatus);
            Globals globals = new Globals();
            new Thread(() -> {
                try {
                    Unirest.delete(globals.BASE_URL + "sucursal/delete/" + sucursalSelected.getIdSucursal()).asString();
                    Platform.runLater(() -> {
                        loadSucursales();
                        cleanForm();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error al cambiar estatus: " + e.getMessage());
                }
            }).start();
        }
    }


    @FXML
    private void agregarSucursal() {
        if (imagenBase64.isEmpty()) {
            lblEstadoImagen.setText("Debe cargar una imagen válida antes de continuar.");
            return;
        }
        if (!validarCampos()) {
            System.err.println("Por favor, completa todos los campos.");
            return;
        }
        try {
            // Crear una nueva instancia de Sucursal
            Sucursal nuevaSucursal = new Sucursal();
            nuevaSucursal.setNombre(txtNombre.getText()); // Cambiado a "nombre"
            nuevaSucursal.setLatitud(txtLatitud.getText());
            nuevaSucursal.setLongitud(txtLongitud.getText());
            nuevaSucursal.setFoto(imagenBase64);
            nuevaSucursal.setUrlWeb(txtUrl.getText());
            nuevaSucursal.setHorarios(txtHorarios.getText());
            nuevaSucursal.setCalle(txtCalle.getText());
            nuevaSucursal.setNumCalle(txtNumCalle.getText());
            nuevaSucursal.setColonia(txtColonia.getText());
            nuevaSucursal.setSucursalActivo(1);

            // Asignar la ciudad seleccionada
            Ciudad ciudadSeleccionada = cbCiudad.getSelectionModel().getSelectedItem();
            if (ciudadSeleccionada != null) {
                nuevaSucursal.setCiudad(ciudadSeleccionada);
            } else {
                System.err.println("Por favor, selecciona una ciudad válida.");
                return;
            }

            // Asignar el estado relacionado con la ciudad seleccionada
            Estado estadoSeleccionado = cbEstado.getSelectionModel().getSelectedItem();
            if (estadoSeleccionado != null) {
                nuevaSucursal.setEstado(estadoSeleccionado);
            } else {
                System.err.println("Por favor, selecciona un estado válido.");
                return;
            }

            // Convertir la sucursal a JSON y enviarla al API
            Gson gson = new Gson();
            String json = gson.toJson(nuevaSucursal);
            HttpResponse<String> response = Unirest.post(new Globals().BASE_URL + "sucursal/add")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();

            // Verificar respuesta del API
            if (response.getStatus() == 200) {
                System.out.println("Sucursal agregada correctamente.");
                loadSucursales();
                cleanForm();
            } else {
                System.err.println("Error al agregar sucursal: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void modificarSucursal() {
        if (imagenBase64.isEmpty()) {
            lblEstadoImagen.setText("Debe cargar una imagen válida antes de continuar.");
            return;
        }
        if (sucursalSelected == null) {
            System.err.println("No hay sucursal seleccionada para modificar.");
            return;
        }
        if (!validarCampos()) {
            System.err.println("Por favor, completa todos los campos.");
            return;
        }

        try {
            // Actualizar los campos de la sucursal seleccionada
            sucursalSelected.setNombre(txtNombre.getText()); // Cambiado a "nombre"
            sucursalSelected.setLatitud(txtLatitud.getText());
            sucursalSelected.setLongitud(txtLongitud.getText());
            sucursalSelected.setFoto(imagenBase64);
            sucursalSelected.setUrlWeb(txtUrl.getText());
            sucursalSelected.setHorarios(txtHorarios.getText());
            sucursalSelected.setCalle(txtCalle.getText());
            sucursalSelected.setNumCalle(txtNumCalle.getText());
            sucursalSelected.setColonia(txtColonia.getText());

            // Asignar la ciudad seleccionada
            Ciudad ciudadSeleccionada = cbCiudad.getSelectionModel().getSelectedItem();
            if (ciudadSeleccionada != null) {
                sucursalSelected.setCiudad(ciudadSeleccionada);
            } else {
                System.err.println("Por favor, selecciona una ciudad válida.");
                return;
            }

            // Asignar el estado relacionado con la ciudad seleccionada
            Estado estadoSeleccionado = cbEstado.getSelectionModel().getSelectedItem();
            if (estadoSeleccionado != null) {
                sucursalSelected.setEstado(estadoSeleccionado);
            } else {
                System.err.println("Por favor, selecciona un estado válido.");
                return;
            }

            // Convertir la sucursal modificada a JSON y enviarla al API
            Gson gson = new Gson();
            String json = gson.toJson(sucursalSelected);
            HttpResponse<String> response = Unirest.put(new Globals().BASE_URL + "sucursal/update")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString();

            // Verificar respuesta del API
            if (response.getStatus() == 200) {
                System.out.println("Sucursal modificada correctamente.");
                loadSucursales();
                cleanForm();
            } else {
                System.err.println("Error al modificar sucursal: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleGuardar() {
        if (btnGuardar.getText().equals("Agregar")) {
            agregarSucursal();
        } else if (btnGuardar.getText().equals("Modificar")) {
            modificarSucursal();
        }
    }

    private boolean validarCampos() {
        return !(txtNombre.getText().isEmpty() || txtLatitud.getText().isEmpty() || txtLongitud.getText().isEmpty()
                 || txtUrl.getText().isEmpty() || txtHorarios.getText().isEmpty()
                || txtCalle.getText().isEmpty() || txtNumCalle.getText().isEmpty() || txtColonia.getText().isEmpty()
                || cbEstado.getSelectionModel().isEmpty() || cbCiudad.getSelectionModel().isEmpty());
    }

    @FXML
    private void cargarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(btnCargarImagen.getScene().getWindow());
        if (archivoSeleccionado != null) {
            try {
                // Convertir la imagen a Base64
                FileInputStream fis = new FileInputStream(archivoSeleccionado);
                byte[] datos = fis.readAllBytes();

                // Determinar el tipo de archivo para el prefijo
                String extension = archivoSeleccionado.getName().substring(archivoSeleccionado.getName().lastIndexOf('.') + 1).toLowerCase();
                String mimeType;
                switch (extension) {
                    case "png":
                        mimeType = "data:image/png;base64,";
                        break;
                    case "gif":
                        mimeType = "data:image/gif;base64,";
                        break;
                    case "bmp":
                        mimeType = "data:image/bmp;base64,";
                        break;
                    default:
                        mimeType = "data:image/jpeg;base64,"; // Asumimos que JPEG es el tipo por defecto
                }

                imagenBase64 = mimeType + Base64.getEncoder().encodeToString(datos);

                // Actualizar el Label para indicar que la imagen se cargó
                lblEstadoImagen.setText("Imagen cargada correctamente");

                System.out.println("Imagen convertida a Base64: " + imagenBase64.substring(0, 50) + "..."); // Depuración
            } catch (Exception e) {
                e.printStackTrace();
                lblEstadoImagen.setText("Error al cargar la imagen");
            }
        } else {
            lblEstadoImagen.setText("No se seleccionó ninguna imagen");
        }
    }


}
