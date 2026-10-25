package com.littledokyhub.service;

import com.littledokyhub.model.Mensaje;
import com.littledokyhub.repository.MensajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MensajeService {
    private final MensajeRepository mensajes;
    public MensajeService(MensajeRepository mensajes) { this.mensajes = mensajes; }

    @Transactional
    public void enviar(String nombre, String email, String telefono, String asunto, String texto) {
        mensajes.saveAndFlush(new Mensaje(nombre, email, telefono, asunto, texto));
    }
}
