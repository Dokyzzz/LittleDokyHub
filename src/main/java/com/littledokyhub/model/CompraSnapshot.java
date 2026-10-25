package com.littledokyhub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "compra_snapshot")
public class CompraSnapshot {
    @Id
    private Integer pedidoId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false, length = 30)
    private String entrega;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false, length = 30)
    private String pago;

    // Getters y Setters
    public Integer getPedidoId() { return pedidoId; }
    public void setPedidoId(Integer pedidoId) { this.pedidoId = pedidoId; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public String getEntrega() { return entrega; }
    public void setEntrega(String entrega) { this.entrega = entrega; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPago() { return pago; }
    public void setPago(String pago) { this.pago = pago; }
}