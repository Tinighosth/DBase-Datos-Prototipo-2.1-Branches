package com.gestiondeudas.presentacion.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Validación rigurosa de SALIDAS: ningún dato inconsistente sale de la API.
 * Si algo viola un invariante, falla (500 genérico) en lugar de responder datos erróneos.
 */
final class SalidaValidator {

    private SalidaValidator() {
    }

    static BigDecimal dinero(BigDecimal valor, String campo) {
        Objects.requireNonNull(valor, campo);
        if (valor.signum() < 0 || valor.scale() != 2) {
            throw new IllegalStateException("Monto de salida inválido en " + campo);
        }
        return valor;
    }

    static int bit(int valor, String campo) {
        if (valor != 0 && valor != 1) {
            throw new IllegalStateException("Valor de salida inválido en " + campo);
        }
        return valor;
    }

    static Long id(Long valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new IllegalStateException("Identificador de salida inválido en " + campo);
        }
        return valor;
    }

    /** Minimización de datos: muestra solo los últimos 3 caracteres del documento. */
    static String enmascarar(String documento) {
        Objects.requireNonNull(documento, "documento");
        int visibles = Math.min(3, documento.length());
        return "*".repeat(documento.length() - visibles) + documento.substring(documento.length() - visibles);
    }
}
