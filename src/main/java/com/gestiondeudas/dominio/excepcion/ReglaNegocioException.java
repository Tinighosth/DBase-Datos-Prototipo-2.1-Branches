package com.gestiondeudas.dominio.excepcion;

/** Se viola una regla de negocio (HTTP 422). El mensaje es seguro de mostrar. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
