package com.littledokyhub.service;

import com.littledokyhub.model.Producto;
import com.littledokyhub.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public boolean guardar(Producto datos) {
        if (datos.getId() == null) {
            productoRepository.save(datos);
            return true;
        }

        Optional<Producto> existente = productoRepository.findById(datos.getId());
        if (existente.isEmpty()) {
            return false;
        }

        Producto p = existente.get();
        p.setNombre(datos.getNombre());
        p.setDescripcion(datos.getDescripcion());
        p.setPrecio(datos.getPrecio());
        p.setStock(datos.getStock());
        p.setImagenUrl(datos.getImagenUrl());
        p.setCategoria(datos.getCategoria());
        productoRepository.save(p);
        return true;
    }

    public boolean eliminar(Integer id) {
        if (productoRepository.existsById(id)) {
            Producto producto = productoRepository.findById(id).orElseThrow();
            producto.setStock(0);
            productoRepository.saveAndFlush(producto);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Producto> porCategoria(String codigoCategoria) {
        return productoRepository.findByCategoriaCodigoIgnoreCase(codigoCategoria);
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerClasicas() {
        return porCategoria("clasicas");
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerEspeciales() {
        return porCategoria("especiales");
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerBebidas() {
        return porCategoria("bebidas");
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerFavoritas() {
        return obtenerClasicas().stream().limit(3).toList();
    }
}
