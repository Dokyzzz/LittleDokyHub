package com.littledokyhub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactoController {
    private final JdbcTemplate db;

    public ContactoController(JdbcTemplate db) {
        this.db = db;
    }

    @PostMapping("/contacto")
    String send(@RequestParam String nombre, @RequestParam String email, @RequestParam String telefono,
            @RequestParam String asunto, @RequestParam String mensaje, RedirectAttributes f) {
        if (nombre.isBlank() || nombre.length() > 100 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
                || email.length() > 100 || telefono.length() > 15 || asunto.isBlank() || asunto.length() > 100
                || mensaje.isBlank() || mensaje.length() > 5000) {
            f.addFlashAttribute("error", "Revisa los campos. El mensaje admite hasta 5000 caracteres.");
            return "redirect:/contacto";
        }
        db.update("INSERT INTO mensajes(nombre,email,telefono,asunto,mensaje,fecha) VALUES(?,?,?,?,?,NOW())", nombre,
            email, telefono, asunto, mensaje);
        f.addFlashAttribute("exito", "Tu mensaje quedó registrado.");
        return "redirect:/contacto";
    }
}
