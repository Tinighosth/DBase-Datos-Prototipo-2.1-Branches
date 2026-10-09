package com.gestiondeudas.presentacion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gestiondeudas.dominio.modelo.Deuda;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class DeudaDto {

    private DeudaDto() {
    }

    public record CrearRequest(
            @NotNull @Positive Long idCliente,
            @NotBlank @Size(max = 150) @Pattern(regexp = ClienteDto.SIN_CONTROL) String concepto,
            @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal monto,
            @FutureOrPresent LocalDate fechaVencimiento) {
    }

    /** Los montos salen como texto ("500000.00") para que ningún cliente los convierta a float. */
    public record Respuesta(Long id, Long idCliente, String concepto,
                            @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal montoInicial,
                            @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal saldoPendiente,
                            LocalDate fechaEmision, LocalDate fechaVencimiento,
                            int estado, int activo) {

        public Respuesta {
            SalidaValidator.id(id, "id");
            SalidaValidator.id(idCliente, "idCliente");
            SalidaValidator.dinero(montoInicial, "montoInicial");
            SalidaValidator.dinero(saldoPendiente, "saldoPendiente");
            SalidaValidator.bit(activo, "activo");
            if (saldoPendiente.compareTo(montoInicial) > 0) {
                throw new IllegalStateException("Saldo de salida inconsistente");
            }
        }

        public static Respuesta desde(Deuda d) {
            return new Respuesta(d.id(), d.idCliente(), d.concepto(),
                    d.montoInicial().valor(), d.saldoPendiente().valor(),
                    d.fechaEmision(), d.fechaVencimiento(),
                    d.estado().codigo(), d.activo() ? 1 : 0);
        }
    }
}
