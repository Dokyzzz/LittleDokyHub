package com.littledokyhub.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CheckoutForm {
    @NotBlank
    @Pattern(regexp = "DELIVERY|RECOJO", message = "Selecciona un tipo de entrega válido")
    private String entrega = "RECOJO";

    @Size(max = 255, message = "La dirección admite hasta 255 caracteres")
    private String direccion = "";

    @NotBlank
    @Pattern(regexp = "EFECTIVO|TARJETA_AL_RECIBIR", message = "Selecciona un método de pago válido")
    private String pago = "EFECTIVO";

    @AssertTrue(message = "Ingresa una dirección de al menos 5 caracteres para el delivery")
    public boolean isDireccionValida() {
        return !"DELIVERY".equals(entrega) || direccion != null && direccion.trim().length() >= 5;
    }

    public BigDecimal costoEnvio() {
        return "DELIVERY".equals(entrega) ? new BigDecimal("5.00") : new BigDecimal("0.00");
    }

    public String getEntrega() { return entrega; }
    public void setEntrega(String entrega) { this.entrega = entrega; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getPago() { return pago; }
    public void setPago(String pago) { this.pago = pago; }
}
