package com.littledokyhub.controller;

import com.littledokyhub.dto.ProductoSeleccion;
import com.littledokyhub.dto.OpcionListado;
import com.littledokyhub.service.ConsultaCatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogo")
public class ConsultaCatalogoController {
    private final ConsultaCatalogoService consultas;

    public ConsultaCatalogoController(ConsultaCatalogoService consultas) {
        this.consultas = consultas;
    }

    @GetMapping("/categorias/{categoriaId}/productos")
    public List<ProductoSeleccion> productos(@PathVariable Integer categoriaId) {
        return consultas.productos(categoriaId, false);
    }

    @GetMapping("/categorias/{categoriaId}/productos/{productoId}/opciones")
    public List<OpcionListado> opciones(@PathVariable Integer categoriaId, @PathVariable Integer productoId) {
        return consultas.opciones(categoriaId, productoId, false);
    }
}
