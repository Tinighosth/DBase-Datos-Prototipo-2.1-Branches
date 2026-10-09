package com.gestiondeudas.aplicacion.puerto;

/** Puerto de salida: la capa de aplicación no conoce BCrypt ni Spring Security. */
public interface CodificadorClave {
    String codificar(CharSequence claveEnClaro);
}
