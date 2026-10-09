package com.gestiondeudas.dominio.modelo;

/** Superclase del dominio: solo existen dos subclases, Cliente y Admin. */
public sealed interface Persona permits Cliente, Admin {
    Long id();
    String nombre();
    String apellido();
    String documento();
    String correo();
    String telefono();
    boolean activo();
}
