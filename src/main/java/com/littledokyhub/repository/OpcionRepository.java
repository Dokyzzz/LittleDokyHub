package com.littledokyhub.repository;

import com.littledokyhub.model.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import com.littledokyhub.dto.OpcionListado;
import java.util.List;
import org.springframework.data.repository.query.Param;

@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Integer> {
    @Query("select new com.littledokyhub.dto.OpcionListado(o.id, p.id, p.nombre, o.grupo, o.nombre, o.precio, o.activo) "
            + "from Opcion o join o.producto p order by p.nombre, o.grupo, o.id")
    List<OpcionListado> listarConProducto();

    @Query("select new com.littledokyhub.dto.OpcionListado(o.id, p.id, p.nombre, o.grupo, o.nombre, o.precio, o.activo) "
            + "from Opcion o join o.producto p join p.categoria c "
            + "where c.id = :categoriaId and p.id = :productoId "
            + "and (:incluirInactivas = true or o.activo = true) order by o.grupo, o.nombre, o.id")
    List<OpcionListado> consultarPorProducto(@Param("categoriaId") Integer categoriaId,
            @Param("productoId") Integer productoId, @Param("incluirInactivas") boolean incluirInactivas);
    java.util.List<Opcion> findByProductoIdAndActivoTrueOrderByGrupoAscIdAsc(Integer productoId);

}
