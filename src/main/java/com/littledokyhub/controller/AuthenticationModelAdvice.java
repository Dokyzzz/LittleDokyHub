package com.littledokyhub.controller;

import com.littledokyhub.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class AuthenticationModelAdvice {
    private final UsuarioRepository usuarios;

    public AuthenticationModelAdvice(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @ModelAttribute
    public void addAuthenticationState(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("sesionAutenticada", authenticated);
        model.addAttribute("sesionAdmin", authenticated && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        String nombre = authenticated ? usuarios.findByEmail(authentication.getName())
            .map(usuario -> usuario.getNombre())
            .orElse(authentication.getName()) : "";
        model.addAttribute("sesionNombre", nombre);
    }
}