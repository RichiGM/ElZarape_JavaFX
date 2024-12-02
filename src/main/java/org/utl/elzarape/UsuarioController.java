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
import javafx.scene.layout.HBox;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.utl.elzarape.model.Cliente;
import org.utl.elzarape.model.Empleado;
import org.utl.elzarape.model.Persona;
import org.utl.elzarape.model.Usuario;
import org.utl.elzarape.model.Estado;
import org.utl.elzarape.model.Ciudad;
import org.utl.elzarape.model.Sucursal;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsuarioController {

    @FXML
    private Button btnCambiarEstatus, btnGuardar;

    @FXML
    private ToggleButton btnVerContrasenia;

    @FXML
    private TableView<Object> tblUsuarios;

    @FXML
    private TableColumn<Object, String> colNombre, colNombreUsuario, colEstatus, colApellidos, colTelefono, colContrasenia, colCiudad, colTipoEntidad, colSucursal;

    @FXML
    private TextField txtNombre, txtNombreUsuario, txtApellidos, txtTelefono, txtContraseniaVisible, txtBuscar;

    @FXML
    private PasswordField txtContrasenia;
    @FXML
    private ComboBox<Ciudad> cbCiudad;
    @FXML
    private ComboBox<Sucursal> cbSucursal;
    @FXML
    private ComboBox<String> cbTipoEntidad;
    @FXML
    private ComboBox<Estado> cbEstado;

    @FXML
    private HBox hBoxSucursal;

    private ObservableList<Object> usuarios;
    private Object usuarioSelected = null;

    @FXML
    public void initialize() {
        initColumns();
        loadUsuarios();
        loadEstados();
        loadTiposEntidad();
        loadSucursales();
        hBoxSucursal.setVisible(false);
        // Al inicio, ocultar el TextField que muestra la contraseña
        txtContraseniaVisible.setVisible(false);
        // Listener para la selección de usuarios en la tabla
        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            showUsuarioSelected();
        });

        // Sincronizar el texto del TextField con el PasswordField
        txtContraseniaVisible.textProperty().addListener((observable, oldValue, newValue) -> {
            txtContrasenia.setText(newValue); // Actualiza el PasswordField cuando se edita el TextField
        });

        // Sincronizar el PasswordField con el TextField cuando se cambia la visibilidad
        txtContrasenia.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!btnVerContrasenia.isSelected()) {
                txtContraseniaVisible.setText(newValue); // Actualiza el TextField cuando se edita el PasswordField
            }
        });

        cbTipoEntidad.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("Empleado".equalsIgnoreCase(newValue)) {
                loadSucursales(); // Cargar sucursales si se selecciona "Empleado"
                hBoxSucursal.setVisible(true);
            } else {
                cbSucursal.getItems().clear(); // Limpiar sucursales si no es empleado
                hBoxSucursal.setVisible(false); // Ocultar el ComboBox de sucursales
            }
        });

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarUsuario(newValue);
        });
    }

    private void initColumns() {
        colNombre.setCellValueFactory(col -> new SimpleObjectProperty<>(getNombre(col.getValue())));
        colNombreUsuario.setCellValueFactory(col -> new SimpleObjectProperty<>(getNombreUsuario(col.getValue())));
        colEstatus.setCellValueFactory(col -> new SimpleObjectProperty<>(getEstatus(col.getValue())));
        colApellidos.setCellValueFactory(col -> new SimpleObjectProperty<>(getApellidos(col.getValue())));
        colTelefono.setCellValueFactory(col -> new SimpleObjectProperty<>(getTelefono(col.getValue())));
        colContrasenia.setCellValueFactory(col -> new SimpleObjectProperty<>(getContrasenia(col.getValue())));
        colCiudad.setCellValueFactory(col -> new SimpleObjectProperty<>(getCiudad(col.getValue())));
        colTipoEntidad.setCellValueFactory(col -> new SimpleObjectProperty<>(getTipoEntidad(col.getValue())));
        colSucursal.setCellValueFactory(col -> new SimpleObjectProperty<>(getSucursal(col.getValue())));
    }

    private String getNombre(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getPersona().getNombre();
        } else if (usuario instanceof Cliente) {
            return ((Cliente) usuario).getPersona().getNombre();
        }
        return null;
    }

    private String getNombreUsuario(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getUsuario().getNombre();
        } else if (usuario instanceof Cliente) {
            return ((Cliente) usuario).getUsuario().getNombre();
        }
        return null;
    }

    private String getEstatus(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getUsuario().getActivo() == 1 ? "Activo" : "Inactivo";
        } else if (usuario instanceof Cliente) {
            return ((Cliente) usuario).getUsuario().getActivo() == 1 ? "Activo" : "Inactivo";
        }
        return null;
    }

    private String getApellidos(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getPersona().getApellidos();
        } else if (usuario instanceof Cliente) {
            return ((Cliente) usuario).getPersona().getApellidos();
        }
        return null;
    }

    private String getTelefono(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getPersona().getTelefono();
        } else if (usuario instanceof Cliente) {
            return ((Cliente) usuario).getPersona().getTelefono();
        }
        return null;
    }

    private String getContrasenia(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getUsuario().getContrasenia();
        } else if (usuario instanceof Cliente) {
            return ((Cliente) usuario).getUsuario().getContrasenia();
        }
        return null;
    }

    private String getCiudad(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getPersona().getNombreCiudad();
        } else if (usuario instanceof Cliente) {
            return ((Cliente) usuario).getPersona().getNombreCiudad();
        }
        return null;
    }

    private String getTipoEntidad(Object usuario) {
        return usuario instanceof Empleado ? "Empleado" : "Cliente";
    }

    private String getSucursal(Object usuario) {
        if (usuario instanceof Empleado) {
            return ((Empleado) usuario).getNombreSucursal();
        } else if (usuario instanceof Cliente) {
            return "N/A";
        }
        return null;
    }

    private void loadUsuarios() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "usuario/getall").asString();
                System.out.println("JSON recibido: " + response.getBody());
                Gson gson = new Gson();
                JsonArray jsonArray = gson.fromJson(response.getBody(), JsonArray.class);
                List<Object> usuariosList = new ArrayList<>();

                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Usuario usuario = gson.fromJson(jsonObject.get("usuario"), Usuario.class);
                    Persona persona = gson.fromJson(jsonObject.get("persona"), Persona.class);

                    if (jsonObject.has("idEmpleado")) {
                        int idEmpleado = jsonObject.get("idEmpleado").getAsInt();
                        Empleado empleado = new Empleado(idEmpleado, usuario, persona, jsonObject.get("idSucursal").getAsInt(), jsonObject.get("nombreSucursal").getAsString());
                        usuariosList.add(empleado);
                    } else if (jsonObject.has("idCliente")) {
                        int idCliente = jsonObject.get("idCliente").getAsInt();
                        Cliente cliente = new Cliente(idCliente, usuario, persona);
                        usuariosList.add(cliente);
                    }
                }

                usuarios = FXCollections.observableArrayList(usuariosList);
                Platform.runLater(() -> tblUsuarios.setItems(usuarios));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onEstadoSelected() {
        Estado selectedEstado = cbEstado.getSelectionModel().getSelectedItem();

        // Si no se seleccionó ningún estado, no cargues las ciudades
        if (selectedEstado != null) {
            // Limpiar el ComboBox de ciudades antes de cargar nuevas ciudades
            cbCiudad.getSelectionModel().clearSelection();

            // Cargar las ciudades basadas en el estado seleccionado
            loadCiudadesByEstado(selectedEstado.getIdEstado(), null);
        } else {
            // Si no hay estado seleccionado, asegúrate de limpiar el ComboBox de ciudades
            cbCiudad.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    private void showUsuarioSelected() {
        // Verificar y deseleccionar el ToggleButton si está seleccionado
        if (btnVerContrasenia.isSelected()) {
            btnVerContrasenia.setSelected(false);  // Desmarcar el ToggleButton
            txtContrasenia.setText(txtContraseniaVisible.getText()); // Copiar el texto plano de vuelta al PasswordField
            txtContraseniaVisible.setVisible(false); // Ocultar el TextField
            txtContrasenia.setVisible(true); // Mostrar el PasswordField
        }

        usuarioSelected = tblUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSelected != null) {
            if (usuarioSelected instanceof Empleado) {
                Empleado empleado = (Empleado) usuarioSelected;

                // Cargar datos de Empleado
                txtNombre.setText(empleado.getPersona().getNombre());
                txtApellidos.setText(empleado.getPersona().getApellidos());
                txtTelefono.setText(empleado.getPersona().getTelefono());
                txtNombreUsuario.setText(empleado.getUsuario().getNombre());
                txtContrasenia.setText(empleado.getUsuario().getContrasenia());  // Esto se maneja de forma oculta

                // Cargar Estado y Ciudad
                cargarEstadoYCiudad(empleado.getPersona().getIdCiudad());

                // Cargar Sucursal para Empleado
                cargarSucursal(empleado.getIdSucursal());

                cbTipoEntidad.setValue("Empleado");  // Asignar tipo de entidad
                btnGuardar.setText("Modificar");
                btnCambiarEstatus.setVisible(true);
                hBoxSucursal.setVisible(true); // Mostrar la sección de sucursal
            } else if (usuarioSelected instanceof Cliente) {
                Cliente cliente = (Cliente) usuarioSelected;

                // Cargar datos de Cliente
                txtNombre.setText(cliente.getPersona().getNombre());
                txtApellidos.setText(cliente.getPersona().getApellidos());
                txtTelefono.setText(cliente.getPersona().getTelefono());
                txtNombreUsuario.setText(cliente.getUsuario().getNombre());
                txtContrasenia.setText(cliente.getUsuario().getContrasenia());  // Ocultando contraseña

                // Cargar Estado y Ciudad
                cargarEstadoYCiudad(cliente.getPersona().getIdCiudad());

                cbTipoEntidad.setValue("Cliente");  // Asignar tipo de entidad
                btnGuardar.setText("Modificar");
                btnCambiarEstatus.setVisible(true);
                hBoxSucursal.setVisible(false); // Ocultar la sección de sucursal
            }
        }
    }

    private void cargarEstadoYCiudad(int idCiudad) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                // Obtener las ciudades disponibles
                HttpResponse<String> responseCiudades = Unirest.get(globals.BASE_URL + "ciudad/getall").asString();
                Gson gson = new Gson();
                Ciudad[] ciudades = gson.fromJson(responseCiudades.getBody(), Ciudad[].class);

                // Buscar la ciudad seleccionada
                Ciudad ciudadSeleccionada = Arrays.stream(ciudades)
                        .filter(ciudad -> ciudad.getIdCiudad() == idCiudad)
                        .findFirst()
                        .orElse(null);

                // Si encontramos la ciudad seleccionada, obtenemos su estado
                if (ciudadSeleccionada != null) {
                    Platform.runLater(() -> {
                        // Limpiar el ComboBox de ciudad
                        cbCiudad.getSelectionModel().clearSelection();

                        // Actualizamos el ComboBox de Ciudad con la ciudad seleccionada
                        cbCiudad.setValue(ciudadSeleccionada);
                        HttpResponse<String> responseEstados = Unirest.get(globals.BASE_URL + "estado/getall").asString();
                        Gson gsonEstado = new Gson();
                        Estado[] estados = gsonEstado.fromJson(responseEstados.getBody(), Estado[].class);

                        // Buscar la ciudad seleccionada
                        Estado estadoSeleccionado = Arrays.stream(estados)
                                .filter(ciudad -> ciudad.getIdEstado() == ciudadSeleccionada.getIdEstado())
                                .findFirst()
                                .orElse(null);
                        // Obtener el estado relacionado con la ciudad

                        // Si encontramos el estado, lo asignamos al ComboBox de Estado
                        if (estadoSeleccionado != null) {
                            cbEstado.setValue(estadoSeleccionado);
                        } else {
                            System.err.println("No se encontró un estado para la ciudad.");
                        }
                    });
                } else {
                    System.err.println("Ciudad seleccionada es null.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar Ciudad y Estado: " + e.getMessage());
            }
        }).start();
    }



    private void cargarSucursal(int idSucursal) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                // Obtener las sucursales disponibles
                HttpResponse<String> responseSucursales = Unirest.get(globals.BASE_URL + "sucursal/getall").asString();
                Gson gson = new Gson();
                Sucursal[] sucursales = gson.fromJson(responseSucursales.getBody(), Sucursal[].class);

                // Buscar la sucursal correspondiente por ID
                Sucursal sucursalSeleccionada = Arrays.stream(sucursales)
                        .filter(sucursal -> sucursal.getIdSucursal() == idSucursal)
                        .findFirst()
                        .orElse(null);

                Platform.runLater(() -> {
                    // Limpiar el ComboBox antes de agregar las sucursales
                    cbSucursal.getItems().clear();

                    // Agregar todas las sucursales al ComboBox
                    cbSucursal.getItems().addAll(sucursales);

                    // Solo mostrar el nombre de la sucursal en el ComboBox
                    cbSucursal.setButtonCell(new ListCell<Sucursal>() {
                        @Override
                        protected void updateItem(Sucursal item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getNombre()); // Mostrar solo el nombre de la sucursal
                            }
                        }
                    });

                    // Mostrar solo el nombre también en la lista de elementos del ComboBox
                    cbSucursal.setCellFactory(param -> new ListCell<Sucursal>() {
                        @Override
                        protected void updateItem(Sucursal item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getNombre()); // Solo mostramos el nombre de la sucursal
                            }
                        }
                    });

                    // Seleccionar la sucursal en el ComboBox
                    if (sucursalSeleccionada != null) {
                        cbSucursal.setValue(sucursalSeleccionada);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar Sucursal: " + e.getMessage());
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

    private void loadTiposEntidad() {
        List<String> tiposEntidad = List.of("Cliente", "Empleado");
        ObservableList<String> listaTiposEntidad = FXCollections.observableArrayList(tiposEntidad);
        cbTipoEntidad.setItems(listaTiposEntidad);
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


    private void loadSucursales() {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                // Hacer la solicitud GET para obtener todas las sucursales
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "sucursal/getall").asString();
                Gson gson = new Gson();
                JsonArray jsonArray = gson.fromJson(response.getBody(), JsonArray.class);
                List<Sucursal> sucursalesList = new ArrayList<>();

                // Iterar sobre el JSON y crear objetos Sucursal, solo las activas
                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Sucursal sucursal = gson.fromJson(jsonObject, Sucursal.class);

                    // Filtrar solo las sucursales activas (activo == 1)
                    if (sucursal.getSucursalActivo() == 1) {
                        sucursalesList.add(sucursal);
                    }
                }

                // Convertir la lista de sucursales activas a ObservableList
                ObservableList<Sucursal> sucursalesObservableList = FXCollections.observableArrayList(sucursalesList);

                // Actualizar el ComboBox de Sucursales en la interfaz de usuario
                Platform.runLater(() -> {
                    cbSucursal.setItems(sucursalesObservableList);

                    // Establecer qué se va a mostrar en el ComboBox (solo el nombre de la sucursal)
                    cbSucursal.setCellFactory(param -> new ListCell<Sucursal>() {
                        @Override
                        protected void updateItem(Sucursal item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getNombre());  // Solo mostramos el nombre de la sucursal
                            }
                        }
                    });

                    // También se puede establecer el texto cuando seleccionas un item
                    cbSucursal.setButtonCell(new ListCell<Sucursal>() {
                        @Override
                        protected void updateItem(Sucursal item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getNombre());  // Mostrar solo el nombre en el botón del ComboBox
                            }
                        }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error al cargar las sucursales: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void onLimpiar() {
        // Limpiar los campos de texto
        txtNombre.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtNombreUsuario.clear();
        txtContrasenia.clear();

        // Limpiar los ComboBoxes
        cbCiudad.setValue(null);  // Limpiar la ciudad
        cbSucursal.setValue(null);  // Limpiar la sucursal
        cbEstado.setValue(null);  // Limpiar el estado
        cbTipoEntidad.setValue(null);  // Limpiar el tipo de entidad

        // Restablecer la visibilidad de la sección de sucursal
        hBoxSucursal.setVisible(false);  // Ocultar la sección de sucursal si no es empleado

        // Restablecer el botón de "Cambiar Estatus"
        btnCambiarEstatus.setVisible(false);

        // Restablecer el texto de "Guardar" a "Agregar"
        btnGuardar.setText("Agregar");
    }

    private String generarContrasenia() {
        int longitud = 12;
        String caracteres = "abcdefghijklmnopqrstuvwxyz";
        String caracteresMayus = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numeros = "0123456789";
        String caracteresEspeciales = "!@#$%^&*()-_=+[]{}|;:,.<>?";

        SecureRandom random = new SecureRandom();  // Generador de números aleatorios seguro
        StringBuilder contrasenia = new StringBuilder();

        // Añadir al menos un carácter de cada tipo
        contrasenia.append(caracteresMayus.charAt(random.nextInt(caracteresMayus.length())));
        contrasenia.append(numeros.charAt(random.nextInt(numeros.length())));
        contrasenia.append(caracteresEspeciales.charAt(random.nextInt(caracteresEspeciales.length())));

        // Rellenar con caracteres aleatorios
        String todosCaracteres = caracteres + caracteresMayus + numeros + caracteresEspeciales;
        for (int i = contrasenia.length(); i < longitud; i++) {
            contrasenia.append(todosCaracteres.charAt(random.nextInt(todosCaracteres.length())));
        }

        // Desordenar la contraseña
        for (int i = 0; i < contrasenia.length(); i++) {
            int j = random.nextInt(contrasenia.length());
            char temp = contrasenia.charAt(i);
            contrasenia.setCharAt(i, contrasenia.charAt(j));
            contrasenia.setCharAt(j, temp);
        }

        return contrasenia.toString();  // Retorna la contraseña generada
    }

    @FXML
    private void onGenerarContrasenia() {
        String nuevaContrasenia = generarContrasenia();
        txtContrasenia.setText(nuevaContrasenia);  // Asigna la nueva contraseña al PasswordField
    }

    @FXML
    private void onMostrarContrasenia() {
        if (btnVerContrasenia.isSelected()) {
            // Si el botón está seleccionado, mostrar la contraseña en texto plano
            txtContraseniaVisible.setText(txtContrasenia.getText()); // Copiar la contraseña al TextField
            txtContraseniaVisible.setVisible(true); // Mostrar el TextField
            txtContrasenia.setVisible(false); // Ocultar el PasswordField
        } else {
            // Si el botón no está seleccionado, ocultar la contraseña (volver a puntos)
            txtContrasenia.setText(txtContraseniaVisible.getText()); // Copiar el texto plano de vuelta al PasswordField
            txtContraseniaVisible.setVisible(false); // Ocultar el TextField
            txtContrasenia.setVisible(true); // Mostrar el PasswordField
        }
    }

    @FXML
    private void onGuardar() {
        // Validar campos
        if (txtNombreUsuario.getText().isEmpty() || txtContrasenia.getText().isEmpty() ||
                txtNombre.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                txtTelefono.getText().isEmpty() || cbCiudad.getValue() == null ||
                cbTipoEntidad.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            return;
        }

        // Validar la contraseña
        if (!isValidPassword(txtContrasenia.getText())) {
            showAlert(Alert.AlertType.ERROR, "Error", "La contraseña debe tener al menos 12 caracteres, incluyendo una mayúscula, un número y un carácter especial.");
            return;
        }

        // Validar el número de teléfono
        if (!isValidPhoneNumber(txtTelefono.getText())) {
            showAlert(Alert.AlertType.ERROR, "Error", "El número de teléfono debe tener 10 dígitos.");
            return;
        }

        // Crear el objeto Usuario
        Usuario nuevoUsuario = new Usuario(0, txtNombreUsuario.getText(), txtContrasenia.getText(), 1);

        // Crear el objeto Persona
        Persona nuevaPersona = new Persona(0, txtNombre.getText(), txtApellidos.getText(),
                txtTelefono.getText(), cbCiudad.getValue().getIdCiudad(), "");

        // Obtener el tipo de entidad
        String tipoEntidad = cbTipoEntidad.getValue().toString().toLowerCase(); // "empleado" o "cliente"
        Integer idSucursal;

        // Si es un empleado, obtener el ID de la sucursal (si aplica)
        if ("empleado".equals(tipoEntidad)) {
            if (cbSucursal.getValue() != null) {
                idSucursal = cbSucursal.getValue().getIdSucursal(); // Asignar el ID de la sucursal seleccionada
            } else {
                idSucursal = null;
                showAlert(Alert.AlertType.ERROR, "Error", "Debe seleccionar una sucursal para el empleado.");
                return; // Salir si no hay sucursal seleccionada
            }
        } else {
            idSucursal = null;
        }

        // Enviar los datos a la API
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response;
                Gson gson = new Gson();

                // Crear el objeto JSON para la actualización
                JsonObject jsonObject = new JsonObject();
                if ("Modificar".equals(btnGuardar.getText())) {
                    // Modificar el usuario existente
                    int idUsuario = 0; // Inicializar el ID del usuario
                    int idPersona = 0; // Inicializar el ID de la persona

                    // Verificar el tipo de usuario seleccionado
                    if (usuarioSelected instanceof Empleado) {
                        Empleado empleado = (Empleado) usuarioSelected;
                        idUsuario = empleado.getUsuario().getIdUsuario(); // Obtener el ID del usuario
                        idPersona = empleado.getPersona().getIdPersona(); // Obtener el ID de la persona
                    } else if (usuarioSelected instanceof Cliente) {
                        Cliente cliente = (Cliente) usuarioSelected;
                        idUsuario = cliente.getUsuario().getIdUsuario(); // Obtener el ID del usuario
                        idPersona = cliente.getPersona().getIdPersona(); // Obtener el ID de la persona
                    }

                    jsonObject.addProperty("idUsuario", idUsuario);
                    jsonObject.addProperty("nombreUsuario", nuevoUsuario.getNombre());
                    jsonObject.addProperty("contrasenia", nuevoUsuario.getContrasenia());
                    jsonObject.addProperty("idPersona", idPersona); // Usar el ID de la persona
                    jsonObject.addProperty("nombrePersona", nuevaPersona.getNombre());
                    jsonObject.addProperty("apellidosPersona", nuevaPersona.getApellidos());
                    jsonObject.addProperty("telefono", nuevaPersona.getTelefono());
                    jsonObject.addProperty("idCiudad", nuevaPersona.getIdCiudad());
                    jsonObject.addProperty("tipoEntidad", tipoEntidad);
                    jsonObject.addProperty("idSucursal", idSucursal != null ? idSucursal : 0); // Enviar 0 si no hay sucursal

                    // Realizar la solicitud POST para actualizar
                    response = Unirest.post(globals.BASE_URL + "usuario/update")
                            .header("Content-Type", "application/json")
                            .body(gson .toJson(jsonObject))
                            .asString();
                } else {
                    // Agregar un nuevo usuario
                    response = Unirest.post(globals.BASE_URL + "usuario/insert")
                            .field("nombreUsuario", nuevoUsuario.getNombre())
                            .field("contrasenia", nuevoUsuario.getContrasenia())
                            .field("nombrePersona", nuevaPersona.getNombre())
                            .field("apellidosPersona", nuevaPersona.getApellidos())
                            .field("telefono", nuevaPersona.getTelefono())
                            .field("idCiudad", String.valueOf(nuevaPersona.getIdCiudad())) // Convertir a String
                            .field("tipoEntidad", tipoEntidad)
                            .field("idSucursal", idSucursal != null ? String.valueOf(idSucursal) : "0") // Convertir a String
                            .asString();
                }

                if (response.getStatus() == 200 || response.getStatus() == 201) {
                    // Si la respuesta es exitosa, actualizar la tabla de usuarios
                    loadUsuarios(); // Recargar la lista de usuarios
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Usuario guardado exitosamente.");
                        onLimpiar(); // Limpiar los campos después de agregar/modificar
                    });
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el usuario: " + response.getStatusText()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el usuario: " + e.getMessage()));
            }
        }).start();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 12 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}"); // Verifica que tenga exactamente 10 dígitos
    }

    private void buscarUsuario(String query) {
        Globals globals = new Globals();
        new Thread(() -> {
            try {
                // Realiza la solicitud al endpoint de búsqueda
                HttpResponse<String> response = Unirest.get(globals.BASE_URL + "usuario/search")
                        .queryString("nombreUsuario", query) // Envía el nombre de usuario como parámetro
                        .asString();

                System.out.println("JSON recibido: " + response.getBody()); // Imprime la respuesta JSON

                Gson gson = new Gson();
                JsonArray jsonArray = gson.fromJson(response.getBody(), JsonArray.class);
                List<Object> usuariosList = new ArrayList<>();

                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Usuario usuario = gson.fromJson(jsonObject.get("usuario"), Usuario.class);
                    Persona persona = gson.fromJson(jsonObject.get("persona"), Persona.class);

                    if (jsonObject.has("idEmpleado")) {
                        int idEmpleado = jsonObject.get("idEmpleado").getAsInt();
                        Empleado empleado = new Empleado(idEmpleado, usuario, persona, jsonObject.get("idSucursal").getAsInt(), jsonObject.get("nombreSucursal").getAsString());
                        usuariosList.add(empleado);
                    } else if (jsonObject.has("idCliente")) {
                        int idCliente = jsonObject.get("idCliente").getAsInt();
                        Cliente cliente = new Cliente(idCliente, usuario, persona);
                        usuariosList.add(cliente);
                    }
                }

                ObservableList<Object> usuarios = FXCollections.observableArrayList(usuariosList);
                Platform.runLater(() -> tblUsuarios.setItems(usuarios)); // Actualiza la tabla en el hilo de la interfaz de usuario
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void onCambiarEstatus() {
        if (usuarioSelected == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No hay usuario seleccionado.");
            return;
        }

        // Obtener el ID del usuario seleccionado
        final int idUsuario;

        if (usuarioSelected instanceof Empleado) {
            Empleado empleado = (Empleado) usuarioSelected;
            idUsuario = empleado.getUsuario().getIdUsuario();
        } else if (usuarioSelected instanceof Cliente) {
            Cliente cliente = (Cliente) usuarioSelected;
            idUsuario = cliente.getUsuario().getIdUsuario();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Tipo de usuario no reconocido.");
            return;
        }

        Globals globals = new Globals();
        new Thread(() -> {
            try {
                HttpResponse<String> response = Unirest.post(globals.BASE_URL + "usuario/cambiarEstatus")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .field("idUsuario", idUsuario)
                        .asString();

                if (response.getStatus() == 200) {
                    loadUsuarios(); // Recargar la lista de usuarios
                    Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Éxito", "Estatus cambiado exitosamente."));
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Error al cambiar el estatus: " + response.getStatusText()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Error al cambiar el estatus: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}