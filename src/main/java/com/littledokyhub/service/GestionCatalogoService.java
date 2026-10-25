package com.littledokyhub.service;

import com.littledokyhub.model.Opcion;
import com.littledokyhub.repository.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GestionCatalogoService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final OpcionRepository opciones;

    public GestionCatalogoService(CategoriaRepository categorias, ProductoRepository productos, OpcionRepository opciones) {
        this.categorias = categorias; this.productos = productos; this.opciones = opciones;
    }

    public void eliminarCategoria(int id) {
        if (productos.existsByCategoriaId(id))
            throw new IllegalArgumentException("La categoría tiene productos; reasígnalos antes de eliminarla");
        categorias.deleteById(id);
        categorias.flush();
    }

    public void guardarOpcion(Integer id, int productoId, String grupo, String nombre, BigDecimal precio, boolean activo) {
        var producto = productos.findById(productoId).orElseThrow(() -> new IllegalArgumentException("Producto inexistente"));
        Opcion opcion = id == null ? new Opcion() : opciones.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La opción que intentas editar no existe"));
        opcion.setProducto(producto); opcion.setGrupo(grupo); opcion.setNombre(nombre);
        opcion.setPrecio(precio); opcion.setActivo(activo);
        opciones.saveAndFlush(opcion);
    }
}
