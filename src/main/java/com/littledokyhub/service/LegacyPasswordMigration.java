package com.littledokyhub.service;

import com.littledokyhub.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LegacyPasswordMigration implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(LegacyPasswordMigration.class);
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public LegacyPasswordMigration(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var registros = usuarios.findAll();
        int migrados = 0;
        for (var usuario : registros) {
            String password = usuario.getPassword();
            if (password == null || password.isBlank() || password.matches(BCRYPT_PATTERN))
                continue;
            usuario.setPassword(passwordEncoder.encode(password));
            migrados++;
        }
        if (migrados > 0)
            usuarios.saveAll(registros);
        log.info("Migrated {} legacy user passwords to BCrypt", migrados);
    }
}