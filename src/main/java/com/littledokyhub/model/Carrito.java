package com.littledokyhub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carritos")
public class Carrito {
    @Id
    @Column(length = 36)
    private String id;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}