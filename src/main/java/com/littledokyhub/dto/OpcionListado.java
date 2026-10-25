package com.littledokyhub.dto;

import java.math.BigDecimal;

/** Proyección de opciones y productos; no expone entidades ni credenciales. */
public record OpcionListado(Integer id, Integer productoId, String productoNombre,
        String grupo, String nombre, BigDecimal precio, Boolean activo) {
}
