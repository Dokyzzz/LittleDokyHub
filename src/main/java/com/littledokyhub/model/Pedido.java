package com.littledokyhub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.00", message = "El total no puede ser negativo")
    @Column(nullable = false)
    private Double total;

    @NotBlank(message = "El estado del pedido es obligatorio")
    @Column(nullable = false, length = 30)
    private String estado;

    @NotNull(message = "El pedido debe estar asociado a un usuario")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = "Pendiente";
        }
    }

    public Pedido() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}