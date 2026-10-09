package com.gestiondeudas.dominio.modelo;

import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;

import java.time.Instant;
import java.util.Objects;

/** Abono registrado. Es un hecho histórico: no tiene métodos que lo modifiquen. */
public record Pago(Long id, Long idDeuda, Long idAdmin, Dinero monto,
                   Dinero saldoResultante, String claveIdempotencia, Instant fechaPago) {

    public Pago {
        Objects.requireNonNull(idDeuda, "idDeuda");
        Objects.requireNonNull(idAdmin, "idAdmin");
        Objects.requireNonNull(monto, "monto");
        Objects.requireNonNull(saldoResultante, "saldoResultante");
        Objects.requireNonNull(claveIdempotencia, "claveIdempotencia");
        Objects.requireNonNull(fechaPago, "fechaPago");
        if (monto.esCero()) {
            throw new ReglaNegocioException("El pago debe ser mayor que cero");
        }
    }

    public static Pago registrar(Long idDeuda, Long idAdmin, Dinero monto, Dinero saldoResultante,
                                 String claveIdempotencia, Instant ahora) {
        return new Pago(null, idDeuda, idAdmin, monto, saldoResultante, claveIdempotencia, ahora);
    }
}
