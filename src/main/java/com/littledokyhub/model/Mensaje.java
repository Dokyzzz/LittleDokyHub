package com.littledokyhub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido")
    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 15)
    private String telefono;

    @NotBlank(message = "El asunto es obligatorio")
    @Column(nullable = false, length = 100)
    private String asunto;

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    // Se asigna la fecha actual automáticamente antes de guardarse en la BD
    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
    }

    // Constructor vacío requerido por JPA
    public Mensaje() {
    }

    // Constructor con campos principales
    public Mensaje(String nombre, String email, String telefono, String asunto, String mensaje) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    // Getters y Setters manuales
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}