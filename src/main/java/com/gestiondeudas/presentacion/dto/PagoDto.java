package com.gestiondeudas.presentacion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gestiondeudas.dominio.modelo.Pago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public final class PagoDto {

    private PagoDto() {
    }

    public record CrearRequest(
            @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal monto) {
    }

    public record Respuesta(Long id, Long idDeuda,
                            @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal monto,
                            @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal saldoResultante,
                            Instant fechaPago) {

        public Respuesta {
            SalidaValidator.id(id, "id");
            SalidaValidator.id(idDeuda, "idDeuda");
            SalidaValidator.dinero(monto, "monto");
            SalidaValidator.dinero(saldoResultante, "saldoResultante");
        }

        public static Respuesta desde(Pago p) {
            return new Respuesta(p.id(), p.idDeuda(), p.monto().valor(),
                    p.saldoResultante().valor(), p.fechaPago());
        }
    }
}
