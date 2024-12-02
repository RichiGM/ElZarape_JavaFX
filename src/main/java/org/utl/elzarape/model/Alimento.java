package org.utl.elzarape.model;

import org.utl.elzarape.model.Producto;

public class Alimento {
    private int idAlimento;
    private Producto producto;

    // Constructor
    public Alimento(int idAlimento, Producto producto) {
        this.idAlimento = idAlimento;
        this.producto = producto;
    }

    // Getters y Setters
    // (Implementación de todos los getters y setters)

    public int getIdAlimento() {
        return idAlimento;
    }

    public void setIdAlimento(int idAlimento) {
        this.idAlimento = idAlimento;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

}

