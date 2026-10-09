package com.gestiondeudas.presentacion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Activar o desactivar: 1 = activo, 0 = inactivo. */
public record EstadoRequest(@NotNull @Min(0) @Max(1) Integer activo) {

    public boolean esActivo() {
        return activo == 1;
    }
}
