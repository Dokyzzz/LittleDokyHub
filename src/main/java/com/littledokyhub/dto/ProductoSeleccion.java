package com.littledokyhub.dto;

import java.math.BigDecimal;

public record ProductoSeleccion(Integer id, String nombre, BigDecimal precio, Integer stock) {
}
