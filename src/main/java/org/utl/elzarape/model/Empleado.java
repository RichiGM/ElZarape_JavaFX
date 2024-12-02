package org.utl.elzarape.model;

public class Empleado {
    private int idEmpleado;
    private Usuario usuario;
    private Persona persona;
    private int idSucursal;
    private String nombreSucursal; // Nombre de la sucursal asociada

    // Constructor
    public Empleado(int idEmpleado, Usuario usuario, Persona persona, int idSucursal, String nombreSucursal) {
        this.idEmpleado = idEmpleado;
        this.usuario = usuario;
        this.persona = persona;
        this.idSucursal = idSucursal;
        this.nombreSucursal = nombreSucursal;
    }

    // Getters y setters
    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getNombreSucursal() {
        return nombreSucursal;
    }

    public void setNombreSucursal(String nombreSucursal) {
        this.nombreSucursal = nombreSucursal;
    }
}
