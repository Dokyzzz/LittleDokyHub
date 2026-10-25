// Paquete: com.littledokyhub.repository
package com.littledokyhub.repository;

import com.littledokyhub.model.CompraSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompraSnapshotRepository extends JpaRepository<CompraSnapshot, Integer> {
}