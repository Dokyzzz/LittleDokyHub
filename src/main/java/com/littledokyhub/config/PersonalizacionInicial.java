package com.littledokyhub.config;

import com.littledokyhub.service.PersonalizacionService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersonalizacionInicial {
    @Bean
    @ConditionalOnProperty(name = "app.personalizacion.completar-opciones", havingValue = "true", matchIfMissing = true)
    ApplicationRunner completarOpciones(PersonalizacionService service) {
        return args -> service.completarFaltantes();
    }
}
