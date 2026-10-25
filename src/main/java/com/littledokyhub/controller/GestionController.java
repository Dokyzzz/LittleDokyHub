package com.littledokyhub.controller;

import com.littledokyhub.model.Categoria;
import com.littledokyhub.repository.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/administracion/gestion")
public class GestionController {
    private final CategoriaRepository categorias;
    private final JdbcTemplate db;

    public GestionController(CategoriaRepository categorias, JdbcTemplate db) {
        this.categorias = categorias;
        this.db = db;
    }

    @GetMapping
    String page(Model m) {
        m.addAttribute("mensajes", db.queryForList("SELECT * FROM mensajes ORDER BY fecha DESC"));
        m.addAttribute("categorias", categorias.findAll());
        m.addAttribute("productos", db.queryForList("SELECT id,nombre FROM productos"));
        m.addAttribute("opciones", db.queryForList("SELECT * FROM opciones ORDER BY producto_id,id"));
        return "admin/gestion";
    }

    @PostMapping("/categoria")
    String save(@Valid @ModelAttribute Categoria c, BindingResult errors, RedirectAttributes f) {
        if (errors.hasErrors())
            throw new IllegalArgumentException(errors.getAllErrors().get(0).getDefaultMessage());
        if (c.getId() != null && !categorias.existsById(c.getId()))
            throw new IllegalArgumentException("Categoría inexistente");
        categorias.save(c);
        f.addFlashAttribute("exito", "Categoría guardada");
        return "redirect:/administracion/gestion";
    }

    @PostMapping("/categoria/{id}/eliminar")
    String delete(@PathVariable int id) {
        if (db.queryForObject("SELECT COUNT(*) FROM productos WHERE categoria_id=?", Integer.class, id) > 0)
            throw new IllegalArgumentException("La categoría tiene productos; reasígnalos antes de eliminarla");
        categorias.deleteById(id);
        return "redirect:/administracion/gestion";
    }

    @PostMapping("/opcion")
    String option(@RequestParam(required = false) Integer id, @RequestParam int productoId, @RequestParam String grupo,
            @RequestParam String nombre, @RequestParam BigDecimal precio,
            @RequestParam(defaultValue = "false") boolean activo) {
        if (!java.util.List.of("TAMANO", "MASA", "EXTRA").contains(grupo) || nombre.isBlank() || nombre.length() > 100
                || precio.signum() < 0 || precio.compareTo(new BigDecimal("99999")) > 0 || precio.scale() > 2)
            throw new IllegalArgumentException("Revisa el nombre, grupo y precio de la opción");
        if (id == null)
            db.update("INSERT INTO opciones(producto_id,grupo,nombre,precio,activo) VALUES(?,?,?,?,?)", productoId,
                    grupo, nombre, precio, activo);
        else
            db.update("UPDATE opciones SET producto_id=?,grupo=?,nombre=?,precio=?,activo=? WHERE id=?", productoId,
                    grupo, nombre, precio, activo, id);
        return "redirect:/administracion/gestion";
    }

    @ExceptionHandler({ IllegalArgumentException.class, org.springframework.dao.DataIntegrityViolationException.class })
    String error(Exception e, RedirectAttributes f) {
        f.addFlashAttribute("error", e instanceof IllegalArgumentException ? e.getMessage()
                : "No se pudo guardar: nombre o código duplicado, o referencia inválida");
        return "redirect:/administracion/gestion";
    }
}
