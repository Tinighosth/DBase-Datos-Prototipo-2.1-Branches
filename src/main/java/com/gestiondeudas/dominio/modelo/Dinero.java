package com.gestiondeudas.dominio.modelo;

import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Objeto de valor inmutable para dinero. Siempre BigDecimal con escala 2
 * (equivale a NUMERIC(14,2) en PostgreSQL). Nunca float ni double.
 * No admite negativos ni más de 2 decimales: no redondea en silencio.
 */
public record Dinero(BigDecimal valor) implements Comparable<Dinero> {

    private static final BigDecimal MAXIMO = new BigDecimal("999999999999.99");
    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

    public Dinero {
        Objects.requireNonNull(valor, "valor");
        if (valor.signum() < 0) {
            throw new ReglaNegocioException("El monto no puede ser negativo");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ReglaNegocioException("El monto admite máximo 2 decimales");
        }
        valor = valor.setScale(2, RoundingMode.UNNECESSARY);
        if (valor.compareTo(MAXIMO) > 0) {
            throw new ReglaNegocioException("El monto excede el máximo permitido");
        }
    }

    public static Dinero de(String texto) {
        return new Dinero(new BigDecimal(texto));
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(valor.add(otro.valor));
    }

    /** Lanza ReglaNegocioException si el resultado fuera negativo. */
    public Dinero restar(Dinero otro) {
        return new Dinero(valor.subtract(otro.valor));
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    @Override
    public int compareTo(Dinero otro) {
        return valor.compareTo(otro.valor);
    }
}
