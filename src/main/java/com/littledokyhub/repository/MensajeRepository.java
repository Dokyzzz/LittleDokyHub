package com.littledokyhub.repository;

import com.littledokyhub.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    java.util.List<Mensaje> findAllByOrderByFechaDesc();
    // Puedes agregar consultas personalizadas si lo necesitas en el futuro, por ejemplo:
    // List<Mensaje> findByEmail(String email);
}
