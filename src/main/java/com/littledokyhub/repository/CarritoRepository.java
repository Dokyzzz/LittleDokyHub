// Paquete: com.littledokyhub.repository
package com.littledokyhub.repository;

import com.littledokyhub.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, String> {
    Optional<Carrito> findByUsuarioId(Integer usuarioId);
}