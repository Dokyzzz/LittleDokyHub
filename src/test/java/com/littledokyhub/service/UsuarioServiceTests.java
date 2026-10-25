package com.littledokyhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.littledokyhub.model.Usuario;
import com.littledokyhub.repository.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTests {
    @Mock
    private UsuarioRepository usuarioRepository;

    private BCryptPasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void guardaLasNuevasContrasenasComoBCrypt() {
        String email = "cliente@example.com";
        String password = "Cliente123!";
        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = new Usuario();
        usuario.setNombre("Cliente de prueba");
        usuario.setEmail(email);
        usuario.setPassword(password);

        assertTrue(usuarioService.guardar(usuario));
        assertTrue(passwordEncoder.matches(password, usuario.getPassword()));
    }

    @Test
    void migraPasswordsLegadasYConservaHashesExistentes() {
        String passwordLegacy = "LegacyPass123!";
        String hashExisting = passwordEncoder.encode("AdminPass123!");
        Usuario cliente = new Usuario();
        cliente.setPassword(passwordLegacy);
        Usuario admin = new Usuario();
        admin.setPassword(hashExisting);
        when(usuarioRepository.findAll()).thenReturn(List.of(cliente, admin));

        new LegacyPasswordMigration(usuarioRepository, passwordEncoder)
                .run(new DefaultApplicationArguments(new String[0]));

        assertTrue(passwordEncoder.matches(passwordLegacy, cliente.getPassword()));
        assertEquals(hashExisting, admin.getPassword());
        verify(usuarioRepository).saveAll(anyList());
    }
}