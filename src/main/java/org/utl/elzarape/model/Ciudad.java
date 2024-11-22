package org.utl.elzarape.model;

public class Ciudad {
    private int idCiudad;
    private String nombre;
    private int idEstado;

    // Constructor

    public Ciudad(int idCiudad, String nombre, int idEstado) {
        this.idCiudad = idCiudad;
        this.nombre = nombre;
        this.idEstado = idEstado;
    }

    public Ciudad() {

    }


    public int getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(int idCiudad) {
        this.idCiudad = idCiudad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
