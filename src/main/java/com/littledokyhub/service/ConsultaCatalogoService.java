package com.littledokyhub.service;

import com.littledokyhub.dto.ProductoSeleccion;
import com.littledokyhub.dto.OpcionListado;
import com.littledokyhub.repository.CategoriaRepository;
import com.littledokyhub.repository.ProductoRepository;
import com.littledokyhub.repository.OpcionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ConsultaCatalogoService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final OpcionRepository opciones;

    public ConsultaCatalogoService(CategoriaRepository categorias, ProductoRepository productos,
            OpcionRepository opciones) {
        this.categorias = categorias;
        this.productos = productos;
        this.opciones = opciones;
    }

    public List<ProductoSeleccion> productos(Integer categoriaId, boolean administracion) {
        if (!categorias.existsById(categoriaId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría inexistente");
        return productos.consultarPorCategoria(categoriaId, administracion);
    }

    public List<OpcionListado> opciones(Integer categoriaId, Integer productoId, boolean administracion) {
        boolean valido = administracion ? productos.existsByIdAndCategoriaId(productoId, categoriaId)
                : productos.existsByIdAndCategoriaIdAndStockGreaterThan(productoId, categoriaId, 0);
        if (!valido)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no disponible en esta categoría");
        return opciones.consultarPorProducto(categoriaId, productoId, administracion);
    }
}
