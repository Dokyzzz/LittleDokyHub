package com.littledokyhub.controller;

import com.littledokyhub.dto.ProductoSeleccion;
import com.littledokyhub.dto.OpcionListado;
import com.littledokyhub.service.ConsultaCatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** SecurityConfig restringe /administracion/** al rol ADMIN. */
@RestController
@RequestMapping("/administracion/consultas")
public class ConsultaGestionController {
    private final ConsultaCatalogoService consultas;

    public ConsultaGestionController(ConsultaCatalogoService consultas) {
        this.consultas = consultas;
    }

    @GetMapping("/categorias/{categoriaId}/productos")
    public List<ProductoSeleccion> productos(@PathVariable Integer categoriaId) {
        return consultas.productos(categoriaId, true);
    }

    @GetMapping("/categorias/{categoriaId}/productos/{productoId}/opciones")
    public List<OpcionListado> opciones(@PathVariable Integer categoriaId, @PathVariable Integer productoId) {
        return consultas.opciones(categoriaId, productoId, true);
    }
}
