package com.littledokyhub.repository;

import com.littledokyhub.model.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import com.littledokyhub.dto.OpcionListado;
import java.util.List;

@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Integer> {
    @Query("select new com.littledokyhub.dto.OpcionListado(o.id, p.id, p.nombre, o.grupo, o.nombre, o.precio, o.activo) "
            + "from Opcion o join o.producto p order by p.nombre, o.grupo, o.id")
    List<OpcionListado> listarConProducto();
}
