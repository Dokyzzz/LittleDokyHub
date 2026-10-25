// Paquete: com.littledokyhub.repository
package com.littledokyhub.repository;

import com.littledokyhub.model.DetalleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleSnapshotRepository extends JpaRepository<DetalleSnapshot, Integer> {
}