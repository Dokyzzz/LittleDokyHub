// Paquete: com.littledokyhub.repository
package com.littledokyhub.repository;

import com.littledokyhub.model.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
}