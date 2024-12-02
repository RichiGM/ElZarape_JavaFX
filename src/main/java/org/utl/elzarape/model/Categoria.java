package org.utl.elzarape.model;

public class Categoria {
    private int idCategoria;
    private String descripcion;
    private String tipo;
    private int activo;

    // Constructor completo
    public Categoria(int idCategoria, String descripcion, String tipo, int activo) {
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.activo = activo;
    }

    // Constructor sin ID (para crear nuevas categorías)
    public Categoria(String descripcion, String tipo, int activo) {
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.activo = activo;
    }

    // Constructor vacío (si es necesario)
    public Categoria() {
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

    // Método toString para representar el objeto en el ComboBox
    @Override
    public String toString() {
        return descripcion + " (" + tipo + ")";
    }
}
