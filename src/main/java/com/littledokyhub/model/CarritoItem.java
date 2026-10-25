package com.littledokyhub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carrito_items", uniqueConstraints = @UniqueConstraint(columnNames = {"carrito_id", "producto_id", "configuracion"}))
public class CarritoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 500)
    private String configuracion;

    @Column(nullable = false)
    private Integer cantidad;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Carrito getCarrito() { return carrito; }
    public void setCarrito(Carrito carrito) { this.carrito = carrito; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public String getConfiguracion() { return configuracion; }
    public void setConfiguracion(String configuracion) { this.configuracion = configuracion; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}