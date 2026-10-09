package com.gestiondeudas.dominio.repositorio;

import com.gestiondeudas.dominio.modelo.TotalCliente;

import java.util.Optional;

public interface TotalRepository {
    /** Inserta o actualiza el total del cliente. */
    TotalCliente guardar(TotalCliente total);
    Optional<TotalCliente> buscarPorCliente(Long idCliente);
}
