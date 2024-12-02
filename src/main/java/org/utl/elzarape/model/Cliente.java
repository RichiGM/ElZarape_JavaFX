package org.utl.elzarape.model;

public class Cliente {
    private int idCliente;
    private Usuario usuario;
    private Persona persona;

    // Constructor
    public Cliente(int idCliente, Usuario usuario, Persona persona) {
        this.idCliente = idCliente;
        this.usuario = usuario;
        this.persona = persona;
    }

    // Getters y setters
    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
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


}
