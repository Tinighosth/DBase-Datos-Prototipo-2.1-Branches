package com.gestiondeudas.presentacion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gestiondeudas.dominio.modelo.TotalCliente;

import java.math.BigDecimal;

public final class TotalDto {

    private TotalDto() {
    }

    public record Respuesta(Long idCliente,
                            @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal totalDeuda,
                            @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal totalPagado) {

        public Respuesta {
            SalidaValidator.id(idCliente, "idCliente");
            SalidaValidator.dinero(totalDeuda, "totalDeuda");
            SalidaValidator.dinero(totalPagado, "totalPagado");
        }

        public static Respuesta desde(TotalCliente t) {
            return new Respuesta(t.idCliente(), t.totalDeuda().valor(), t.totalPagado().valor());
        }
    }
}
