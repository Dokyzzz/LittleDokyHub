package com.littledokyhub.service;

import com.littledokyhub.model.Usuario;
import com.littledokyhub.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean guardar(Usuario datos) {
        if (datos.getId() == null) {
            if (usuarioRepository.existsByEmail(datos.getEmail())) {
                return false;
            }
            datos.setPassword(passwordEncoder.encode(datos.getPassword()));
            if (datos.getActivo() == null) {
                datos.setActivo(true);
            }
            usuarioRepository.save(datos);
            return true;
        }

        Optional<Usuario> existente = usuarioRepository.findById(datos.getId());
        if (existente.isEmpty()) {
            return false;
        }

        Usuario u = existente.get();
        u.setNombre(datos.getNombre());
        u.setEmail(datos.getEmail());
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        u.setTelefono(datos.getTelefono());
        u.setRol(datos.getRol());
        u.setActivo(datos.getActivo());
        usuarioRepository.save(u);
        return true;
    }

    public boolean eliminar(Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public long contarClientes() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && "CLIENTE".equalsIgnoreCase(u.getRol().getNombre()))
                .count();
    }
}