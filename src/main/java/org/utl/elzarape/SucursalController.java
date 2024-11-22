package org.utl.elzarape;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.utl.elzarape.model.Ciudad;
import org.utl.elzarape.model.Estado;
import org.utl.elzarape.model.Sucursal;

import java.util.List;
import java.util.stream.Collectors;

public class SucursalController {

    @FXML
    private Button btnCancelar, btnGuardar;

    @FXML
    private TableView<Sucursal> tblSucursales;

    @FXML
    private TableColumn<Sucursal, String> colNombre, colDireccion;

    @FXML
    private TableColumn<Sucursal, Integer> colIdSucursal, colEstatus;

    @FXML
    private ComboBox<String> cbCiudad;

    @FXML
    private ComboBox<Estado> cbEstado;

    @FXML
    private TextField txtIdSucursal, txtNombre, txtCalle, txtNumCalle, txtColonia, txtLatitud, txtLongitud, txtFoto, txtUrl, txtHorarios;

    @FXML
    private CheckBox txtEstatus;

    private ObservableList<Sucursal> sucursales;
    private ObservableList<String> ciudades;
    private Sucursal sucursalSelected = null;

    @FXML
    public void initialize() {
        System.out.println("Inicializando SucursalController...");
        initColumns();
        loadSucursales();
        loadEstados();
        tblSucursales.setOnMouseClicked(event -> showSucursalSelected());
    }

    private void initColumns() {
        colIdSucursal.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getIdSucursal()));
        colNombre.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getNombreSucursal()));
        colDireccion.setCellValueFactory(col -> new SimpleObjectProperty<>(
                col.getValue().getCalle() + " #" +
                        col.getValue().getNumCalle() + ", " +
                        col.getValue().getColonia() + "."+", "+
                         col.getValue().getCiudad() +", "+
                col.getValue().getEstado()));

        colEstatus.setCellValueFactory(col -> new SimpleObjectProperty<>(col.getValue().getSucursalActivo()));
    }

    private void loadSucursales() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "sucursal/getall").asString();
                Gson gson = new Gson();
                Sucursal[] sucursalArray = gson.fromJson(response.getBody(), Sucursal[].class);
                sucursales = FXCollections.observableArrayList(List.of(sucursalArray));
                Platform.runLater(() -> tblSucursales.setItems(sucursales));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadEstados() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                System.out.println("Cargando estados desde la API...");
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "sucursal/getestados").asString();
                System.out.println("Respuesta cruda de estados: " + response.getBody());

                Gson gson = new Gson();
                Estado[] estadoArray = gson.fromJson(response.getBody(), Estado[].class);
                ObservableList<Estado> listaEstados = FXCollections.observableArrayList(List.of(estadoArray));

                Platform.runLater(() -> {
                    cbEstado.setItems(listaEstados);
                    System.out.println("Estados cargados: " + listaEstados);
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar estados: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void onEstadoSelected() {
        System.out.println("onEstadoSelected llamado...");
        Estado selectedEstado = cbEstado.getSelectionModel().getSelectedItem();
        if (selectedEstado != null) {
            System.out.println("Estado seleccionado: " + selectedEstado.getNombre());
            loadCiudadesByEstado(selectedEstado.getIdEstado());
        } else {
            System.out.println("No se seleccionó ningún estado.");
        }
    }

    private void loadCiudadesByEstado(int idEstado) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                String url = globals.BASE_URL + "sucursal/getciudades/" + idEstado;
                System.out.println("URL Generada: " + url);

                HttpResponse<String> response = Unirest.get(url).asString();
                System.out.println("Respuesta Cruda de Ciudades: " + response.getBody());

                if (!response.getBody().trim().startsWith("[")) {
                    throw new IllegalStateException("La respuesta no es un arreglo JSON: " + response.getBody());
                }

                Gson gson = new Gson();
                Ciudad[] ciudadArray = gson.fromJson(response.getBody(), Ciudad[].class);

                ciudades = FXCollections.observableArrayList(
                        List.of(ciudadArray).stream().map(Ciudad::getNombre).collect(Collectors.toList())
                );

                Platform.runLater(() -> {
                    cbCiudad.setItems(ciudades);
                    System.out.println("Ciudades cargadas: " + ciudades);
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar ciudades: " + e.getMessage());
            }
        }).start();
    }

    private void showSucursalSelected() {
        sucursalSelected = tblSucursales.getSelectionModel().getSelectedItem();
        if (sucursalSelected != null) {
            txtIdSucursal.setText(String.valueOf(sucursalSelected.getIdSucursal()));
            txtNombre.setText(sucursalSelected.getNombreSucursal());
            txtLatitud.setText(sucursalSelected.getLatitud());
            txtLongitud.setText(sucursalSelected.getLongitud());
            txtFoto.setText(sucursalSelected.getFoto());
            txtUrl.setText(sucursalSelected.getUrlWeb());
            txtHorarios.setText(sucursalSelected.getHorarios());
            txtCalle.setText(sucursalSelected.getCalle());
            txtNumCalle.setText(sucursalSelected.getNumCalle());
            txtColonia.setText(sucursalSelected.getColonia());
            txtEstatus.setSelected(sucursalSelected.getSucursalActivo() == 1);

            // Selección del Estado basado en el nombre
            cbEstado.getSelectionModel().select(
                    cbEstado.getItems()
                            .stream()
                            .filter(estado -> estado.getNombre().equals(sucursalSelected.getEstado()))
                            .findFirst()
                            .orElse(null)
            );

            // Selección de la Ciudad basada en el nombre
            cbCiudad.getSelectionModel().select(sucursalSelected.getCiudad());
            btnGuardar.setText("Modificar");
        }
    }

    private void cleanForm() {
        txtCalle.setText("");
        txtColonia.setText("");
        txtFoto.setText("");
        txtHorarios.setText("");
        txtIdSucursal.setText("");
        txtLatitud.setText("");
        txtLongitud.setText("");
        txtNombre.setText("");
        txtNumCalle.setText("");
        txtUrl.setText("");
        cbCiudad.getSelectionModel().clearSelection();
        cbEstado.getSelectionModel().clearSelection();
        txtEstatus.setSelected(false);
        btnGuardar.setDisable(false);
        btnGuardar.setText("Guardar");
    }
}
