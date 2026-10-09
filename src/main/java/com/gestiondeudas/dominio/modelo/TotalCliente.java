package com.gestiondeudas.dominio.modelo;

import java.util.Objects;

/** Acumulado de dinero de un cliente (tabla total_dinero). */
public record TotalCliente(Long idCliente, Dinero totalDeuda, Dinero totalPagado, boolean activo) {

    public TotalCliente {
        Objects.requireNonNull(idCliente, "idCliente");
        Objects.requireNonNull(totalDeuda, "totalDeuda");
        Objects.requireNonNull(totalPagado, "totalPagado");
    }
}
