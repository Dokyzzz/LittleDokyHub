package com.littledokyhub.controller;

import com.littledokyhub.service.CompraService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.*;

@Controller
public class CompraController {
    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    private String cart(HttpServletRequest req, HttpServletResponse res, Principal user) {
        String token = null;
        if (req.getCookies() != null)
            for (var c : req.getCookies())
                if (c.getName().equals("DOKY_CART"))
                    token = c.getValue();
        token = service.carrito(token, user == null ? null : user.getName());
        var cookie = new Cookie("DOKY_CART", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(req.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(2592000);
        cookie.setAttribute("SameSite", "Lax");
        res.addCookie(cookie);
        return token;
    }

    @GetMapping("/producto/{id}")
    String producto(@PathVariable int id, Model model) {
        model.addAttribute("producto", service.producto(id));
        model.addAttribute("opciones", service.opciones(id));
        return "shop/producto";
    }

    @PostMapping("/carrito/agregar")
    String add(@RequestParam int productoId, @RequestParam(defaultValue = "1") int cantidad,
            @RequestParam(required = false) List<Integer> opciones, @RequestParam(required = false) Integer TAMANO,
            @RequestParam(required = false) Integer MASA, HttpServletRequest req, HttpServletResponse res,
            Principal user) {
        List<Integer> selected = new ArrayList<>(opciones == null ? List.of() : opciones);
        if (TAMANO != null)
            selected.add(TAMANO);
        if (MASA != null)
            selected.add(MASA);
        service.agregar(cart(req, res, user), productoId, selected, cantidad);
        return "redirect:/carrito";
    }

    @GetMapping({ "/carrito", "/resumen" })
    String resumenCarrito() {
        return "redirect:/pedido/resumen";
    }

    @GetMapping("/checkout")
    String checkout() {
        return "redirect:/pedido/entrega";
    }

    @PostMapping("/carrito/cantidad")
    String quantity(@RequestParam long id, @RequestParam int cantidad, HttpServletRequest req, HttpServletResponse res,
            Principal user) {
        service.cantidad(cart(req, res, user), id, cantidad);
        return "redirect:/carrito";
    }

    @PostMapping("/checkout/confirmar")
    String confirm(HttpServletRequest req, HttpServletResponse res, Principal user) {
        return "redirect:/pedidos/" + service.confirmar(cart(req, res, user), user.getName());
    }

    @GetMapping("/pedidos/{id}")
    String pedido(@PathVariable int id, Principal user, Model model) {
        var orders = service.query(
                "SELECT p.* FROM pedidos p JOIN usuarios u ON u.id=p.usuario_id WHERE p.id=? AND u.email=?", id,
                user.getName());
        if (orders.isEmpty())
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND);
        model.addAttribute("pedido", orders.get(0));
        model.addAttribute("detalles", service.query(
                "SELECT d.*,s.nombre,s.personalizacion FROM detalles_pedido d JOIN detalle_snapshot s ON s.detalle_id=d.id WHERE d.pedido_id=?",
                id));
        return "shop/pedido";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    String error(IllegalArgumentException ex, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getMessage());
        return "redirect:/carrito";
    }
}
