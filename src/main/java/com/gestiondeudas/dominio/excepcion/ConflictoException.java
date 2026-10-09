package com.gestiondeudas.dominio.excepcion;

/** Conflicto con el estado actual, por ejemplo un duplicado (HTTP 409). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
