package com.littledokyhub.controller;

import java.security.Principal;
import com.littledokyhub.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CuentaController {
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public CuentaController(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping("/cuenta")
    String page() {
        return "account/cuenta";
    }

    @PostMapping("/cuenta/password")
    String change(Principal principal, @RequestParam String actual, @RequestParam String nueva,
            RedirectAttributes flash) {
        var user = repo.findByEmail(principal.getName()).orElseThrow();
        if (nueva.length() < 8 || nueva.length() > 72 || !encoder.matches(actual, user.getPassword())) {
            flash.addFlashAttribute("error",
                    "Contraseña actual incorrecta o nueva contraseña fuera de 8 a 72 caracteres.");
            return "redirect:/cuenta";
        }
        user.setPassword(encoder.encode(nueva));
        repo.save(user);
        flash.addFlashAttribute("exito", "Contraseña actualizada.");
        return "redirect:/cuenta";
    }
}
