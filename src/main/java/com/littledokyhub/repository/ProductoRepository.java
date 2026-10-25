package com.littledokyhub.repository;
import com.littledokyhub.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.littledokyhub.dto.ProductoSeleccion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoriaId(Integer categoriaId);
    List<Producto> findByCategoriaCodigoIgnoreCase(String codigo);

    boolean existsByIdAndCategoriaId(Integer id, Integer categoriaId);
    boolean existsByIdAndCategoriaIdAndStockGreaterThan(Integer id, Integer categoriaId, Integer stock);

    @Query("select new com.littledokyhub.dto.ProductoSeleccion(p.id, p.nombre, p.precio, p.stock) "
            + "from Producto p join p.categoria c where c.id = :categoriaId "
            + "and (:incluirAgotados = true or p.stock > 0) order by p.nombre, p.id")
    List<ProductoSeleccion> consultarPorCategoria(@Param("categoriaId") Integer categoriaId,
            @Param("incluirAgotados") boolean incluirAgotados);
    boolean existsByCategoriaId(Integer categoriaId);

}
