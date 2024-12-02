package org.utl.elzarape.model;

public class Producto {
    private int idProducto;
    private String nombre;
    private String descripcion;
    private String foto;
    private double precio;
    private Categoria categoria;
    private int activo;

    // Constructor
    public Producto(int idProducto, String nombre, String descripcion, String foto, double precio, Categoria categoria, int activo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.precio = precio;
        this.categoria = categoria;
        this.activo = activo;
    }

    public Producto() {

    }

    // Getters y Setters
    // (Implementación de todos los getters y setters)

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }

}
