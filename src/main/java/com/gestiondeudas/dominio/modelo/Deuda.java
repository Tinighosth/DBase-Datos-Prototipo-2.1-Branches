package com.gestiondeudas.dominio.modelo;

import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Deuda inmutable: cada operación devuelve una NUEVA instancia.
 * Invariantes: saldo <= monto inicial y (estado PAGADA <=> saldo = 0).
 */
public record Deuda(Long id, Long idCliente, Long idAdmin, String concepto,
                    Dinero montoInicial, Dinero saldoPendiente,
                    LocalDate fechaEmision, LocalDate fechaVencimiento,
                    EstadoDeuda estado, boolean activo) {

    public Deuda {
        Objects.requireNonNull(idCliente, "idCliente");
        Objects.requireNonNull(idAdmin, "idAdmin");
        Objects.requireNonNull(concepto, "concepto");
        Objects.requireNonNull(montoInicial, "montoInicial");
        Objects.requireNonNull(saldoPendiente, "saldoPendiente");
        Objects.requireNonNull(fechaEmision, "fechaEmision");
        Objects.requireNonNull(estado, "estado");
        if (montoInicial.esCero()) {
            throw new ReglaNegocioException("El monto de la deuda debe ser mayor que cero");
        }
        if (saldoPendiente.compareTo(montoInicial) > 0) {
            throw new ReglaNegocioException("El saldo no puede superar el monto inicial");
        }
        if ((estado == EstadoDeuda.PAGADA) != saldoPendiente.esCero()) {
            throw new ReglaNegocioException("El estado no es coherente con el saldo");
        }
        if (fechaVencimiento != null && fechaVencimiento.isBefore(fechaEmision)) {
            throw new ReglaNegocioException("El vencimiento no puede ser anterior a la emisión");
        }
    }

    public static Deuda nueva(Long idCliente, Long idAdmin, String concepto, Dinero monto,
                              LocalDate hoy, LocalDate fechaVencimiento) {
        return new Deuda(null, idCliente, idAdmin, concepto, monto, monto,
                hoy, fechaVencimiento, EstadoDeuda.PENDIENTE, true);
    }

    public Deuda aplicarAbono(Dinero abono) {
        if (!activo) {
            throw new ReglaNegocioException("La deuda está inactiva");
        }
        if (estado == EstadoDeuda.PAGADA) {
            throw new ReglaNegocioException("La deuda ya está pagada");
        }
        if (abono.esCero()) {
            throw new ReglaNegocioException("El abono debe ser mayor que cero");
        }
        if (abono.compareTo(saldoPendiente) > 0) {
            throw new ReglaNegocioException("El abono supera el saldo pendiente");
        }
        Dinero nuevoSaldo = saldoPendiente.restar(abono);
        return new Deuda(id, idCliente, idAdmin, concepto, montoInicial, nuevoSaldo,
                fechaEmision, fechaVencimiento,
                nuevoSaldo.esCero() ? EstadoDeuda.PAGADA : EstadoDeuda.PENDIENTE, activo);
    }

    public Deuda conActivo(boolean nuevoEstado) {
        return new Deuda(id, idCliente, idAdmin, concepto, montoInicial, saldoPendiente,
                fechaEmision, fechaVencimiento, estado, nuevoEstado);
    }
}
