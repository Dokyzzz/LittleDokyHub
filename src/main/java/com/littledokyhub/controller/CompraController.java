package com.littledokyhub.controller;

import com.littledokyhub.service.CompraService;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.*;
import com.littledokyhub.form.CheckoutForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

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
    String producto(@PathVariable int id) {
        return "redirect:/personalizar/" + id;
    }

    @PostMapping("/carrito/agregar")
    String add(@RequestParam int productoId, @RequestParam(defaultValue = "1") int cantidad,
            @RequestParam(required = false) List<Integer> opciones, @RequestParam(required = false) Integer TAMANO,
            @RequestParam(required = false) Integer MASA, @RequestParam(required = false) Integer SALSA,
            @RequestParam(required = false) Integer TEMPERATURA, HttpServletRequest req, HttpServletResponse res,
            Principal user, RedirectAttributes flash) {
        List<Integer> selected = new ArrayList<>(opciones == null ? List.of() : opciones);
        if (TAMANO != null)
            selected.add(TAMANO);
        if (MASA != null) selected.add(MASA);
        if (SALSA != null) selected.add(SALSA);
        if (TEMPERATURA != null) selected.add(TEMPERATURA);
        try {
            service.agregar(cart(req, res, user), productoId, selected, cantidad);
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/personalizar/" + productoId;
        }
        return "redirect:/carrito";
    }

    @GetMapping({ "/carrito", "/resumen" })
    String resumenCarrito() {
        return "redirect:/pedido/resumen";
    }

    @GetMapping("/pedido/resumen")
    String resumen(HttpServletRequest req, HttpServletResponse res, Principal user, Model model) {
        cargarCarrito(req, res, user, model);
        return "shop/resumen-pedido";
    }

    private void cargarCarrito(HttpServletRequest req, HttpServletResponse res, Principal user, Model model) {
        var lineas = service.lineas(cart(req, res, user));
        model.addAttribute("lineas", lineas);
        model.addAttribute("subtotal", service.total(lineas));
        model.addAttribute("puedeConfirmar", !lineas.isEmpty() && lineas.stream().allMatch(CompraService.Linea::disponible));
    }

    @GetMapping("/carrito/datos")
    @ResponseBody
    Map<String, Object> datos(HttpServletRequest req, HttpServletResponse res, Principal user) {
        var lineas = service.lineas(cart(req, res, user));
        return Map.of("lineas", lineas, "total", service.total(lineas));
    }

    @GetMapping("/pedido/entrega")
    String entrega(HttpServletRequest req, HttpServletResponse res, Principal user, Model model) {
        model.addAttribute("checkout", new CheckoutForm());
        cargarCarrito(req, res, user, model);
        return "shop/entrega-pedido";
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

    @PostMapping("/carrito/eliminar")
    String remove(@RequestParam long id, HttpServletRequest req, HttpServletResponse res, Principal user) {
        service.cantidad(cart(req, res, user), id, 0);
        return "redirect:/carrito";
    }

    @PostMapping("/checkout/confirmar")
    String confirm(@Valid @ModelAttribute("checkout") CheckoutForm checkout, BindingResult errors,
            HttpServletRequest req, HttpServletResponse res, Principal user, Model model) {
        if (errors.hasErrors()) {
            cargarCarrito(req, res, user, model);
            return "shop/entrega-pedido";
        }
        return "redirect:/pedidos/" + service.confirmar(cart(req, res, user), user.getName(), checkout);
    }

    @GetMapping("/pedidos/{id}")
    String pedido(@PathVariable int id, Principal user, Model model) {
        var vista = service.consultarPedido(id, user.getName());
        model.addAttribute("pedido", vista.pedido());
        model.addAttribute("detalles", vista.detalles());
        model.addAttribute("entrega", vista.entrega());
        return "shop/pedido";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    String error(IllegalArgumentException ex, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getMessage());
        return "redirect:/carrito";
    }
}
