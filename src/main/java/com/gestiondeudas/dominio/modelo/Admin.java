package com.gestiondeudas.dominio.modelo;

import java.util.Objects;

public record Admin(Long id, String nombre, String apellido, String documento,
                    String correo, String telefono, String usuario, String claveHash,
                    NivelAcceso nivel, boolean activo) implements Persona {

    public Admin {
        Objects.requireNonNull(nombre, "nombre");
        Objects.requireNonNull(apellido, "apellido");
        Objects.requireNonNull(documento, "documento");
        Objects.requireNonNull(correo, "correo");
        Objects.requireNonNull(usuario, "usuario");
        Objects.requireNonNull(claveHash, "claveHash");
        Objects.requireNonNull(nivel, "nivel");
    }

    public static Admin nuevo(String nombre, String apellido, String documento, String correo,
                              String telefono, String usuario, String claveHash, NivelAcceso nivel) {
        return new Admin(null, nombre, apellido, documento, correo, telefono, usuario, claveHash, nivel, true);
    }

    public Admin conActivo(boolean nuevoEstado) {
        return new Admin(id, nombre, apellido, documento, correo, telefono, usuario, claveHash, nivel, nuevoEstado);
    }

    /** El hash de la clave nunca debe llegar a los logs. */
    @Override
    public String toString() {
        return "Admin[id=" + id + ", usuario=" + usuario + ", nivel=" + nivel + ", activo=" + activo + "]";
    }
}
