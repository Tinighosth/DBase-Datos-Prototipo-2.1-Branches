package com.gestiondeudas.dominio.modelo;

import java.util.Objects;

public record Cliente(Long id, String nombre, String apellido, String documento,
                      String correo, String telefono, String direccion,
                      boolean activo) implements Persona {

    public Cliente {
        Objects.requireNonNull(nombre, "nombre");
        Objects.requireNonNull(apellido, "apellido");
        Objects.requireNonNull(documento, "documento");
        Objects.requireNonNull(correo, "correo");
    }

    public static Cliente nuevo(String nombre, String apellido, String documento,
                                String correo, String telefono, String direccion) {
        return new Cliente(null, nombre, apellido, documento, correo, telefono, direccion, true);
    }

    /** Devuelve una copia: el objeto original no cambia. */
    public Cliente conActivo(boolean nuevoEstado) {
        return new Cliente(id, nombre, apellido, documento, correo, telefono, direccion, nuevoEstado);
    }
}
