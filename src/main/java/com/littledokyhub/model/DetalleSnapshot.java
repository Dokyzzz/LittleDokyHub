// Paquete: com.littledokyhub.model
package com.littledokyhub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_snapshot")
public class DetalleSnapshot {
    @Id
    private Integer detalleId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "detalle_id")
    private DetallePedido detallePedido;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String personalizacion;

    // Getters y Setters
    public Integer getDetalleId() { return detalleId; }
    public void setDetalleId(Integer detalleId) { this.detalleId = detalleId; }

    public DetallePedido getDetallePedido() { return detallePedido; }
    public void setDetallePedido(DetallePedido detallePedido) { this.detallePedido = detallePedido; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPersonalizacion() { return personalizacion; }
    public void setPersonalizacion(String personalizacion) { this.personalizacion = personalizacion; }
}