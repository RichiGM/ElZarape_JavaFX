package org.utl.elzarape.model;

public class Persona {
    private int idPersona;
    private String nombre;
    private String apellidos;
    private String telefono;
    private int idCiudad;
    private String nombreCiudad; // Nombre de la ciudad asociada

    // Constructor
    public Persona(int idPersona, String nombre, String apellidos, String telefono, int idCiudad, String nombreCiudad) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.idCiudad = idCiudad;
        this.nombreCiudad = nombreCiudad;
    }

    // Getters y setters
    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(int idCiudad) {
        this.idCiudad = idCiudad;
    }

    public String getNombreCiudad() {
        return nombreCiudad;
    }

    public void setNombreCiudad(String nombreCiudad) {
        this.nombreCiudad = nombreCiudad;
    }
}
