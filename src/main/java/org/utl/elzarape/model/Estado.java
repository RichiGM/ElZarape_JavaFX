package org.utl.elzarape.model;

public class Estado {
    private int idEstado;
    private String nombre;

    // Constructor
    public Estado(int idEstado, String nombre) {
        this.idEstado = idEstado;
        this.nombre = nombre;
    }

    public Estado() {
    }

    // Getters y Setters
    public int getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre; // Muestra solo el nombre del estado
    }

}
