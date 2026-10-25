package com.littledokyhub.controller;

import com.littledokyhub.model.Categoria;
import com.littledokyhub.repository.CategoriaRepository;
import jakarta.validation.Valid;
import com.littledokyhub.service.GestionCatalogoService;
import com.littledokyhub.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import com.littledokyhub.repository.OpcionRepository;
import com.littledokyhub.repository.MensajeRepository;
import com.littledokyhub.service.CategoriaService;

@Controller
@RequestMapping("/administracion/gestion")
public class GestionController {
    private final CategoriaRepository categorias;
    private final GestionCatalogoService gestion;
    private final ProductoService productos;
    private final OpcionRepository opciones;
    private final MensajeRepository mensajes;
    private final CategoriaService categoriaService;

    public GestionController(CategoriaRepository categorias, GestionCatalogoService gestion, ProductoService productos, OpcionRepository opciones,
            MensajeRepository mensajes, CategoriaService categoriaService) {
        this.categorias = categorias;
        this.gestion = gestion;
        this.productos = productos;
        this.opciones = opciones;
        this.mensajes = mensajes;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    String page(Model m) {
        m.addAttribute("mensajes", mensajes.findAllByOrderByFechaDesc());
        m.addAttribute("categorias", categorias.findAll());
        m.addAttribute("productos", productos.listar());
        m.addAttribute("opciones", opciones.listarConProducto());
        return "admin/gestion";
    }

    @PostMapping("/categoria")
    String save(@Valid @ModelAttribute Categoria c, BindingResult errors, RedirectAttributes f) {
        if (errors.hasErrors())
            throw new IllegalArgumentException(errors.getAllErrors().get(0).getDefaultMessage());
        if (c.getId() != null && !categorias.existsById(c.getId()))
            throw new IllegalArgumentException("Categoría inexistente");
        categoriaService.guardar(c);
        f.addFlashAttribute("exito", "Categoría guardada");
        return "redirect:/administracion/gestion";
    }

    @PostMapping("/categoria/{id}/eliminar")
    String delete(@PathVariable int id) {
        gestion.eliminarCategoria(id);
        return "redirect:/administracion/gestion";
    }

    @PostMapping("/opcion")
    String option(@RequestParam(required = false) Integer id, @RequestParam int productoId, @RequestParam String grupo,
            @RequestParam String nombre, @RequestParam BigDecimal precio,
            @RequestParam(defaultValue = "false") boolean activo) {
        if (!java.util.List.of("TAMANO", "MASA", "EXTRA").contains(grupo) || nombre.isBlank() || nombre.length() > 100
                || precio.signum() < 0 || precio.compareTo(new BigDecimal("99999")) > 0 || precio.scale() > 2)
            throw new IllegalArgumentException("Revisa el nombre, grupo y precio de la opción");
        gestion.guardarOpcion(id, productoId, grupo, nombre, precio, activo);
        return "redirect:/administracion/gestion";
    }

    @ExceptionHandler({ IllegalArgumentException.class, org.springframework.dao.DataIntegrityViolationException.class })
    String error(Exception e, RedirectAttributes f) {
        f.addFlashAttribute("error", e instanceof IllegalArgumentException ? e.getMessage()
                : "No se pudo guardar: nombre o código duplicado, o referencia inválida");
        return "redirect:/administracion/gestion";
    }
}
