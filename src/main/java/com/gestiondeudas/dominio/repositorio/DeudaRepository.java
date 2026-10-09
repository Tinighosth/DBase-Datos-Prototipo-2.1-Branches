package com.gestiondeudas.dominio.repositorio;

import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;

import java.util.List;
import java.util.Optional;

public interface DeudaRepository {
    Deuda guardar(Deuda deuda);

    /** Lee la deuda con bloqueo de fila (SELECT ... FOR UPDATE). Requiere transacción. */
    Optional<Deuda> buscarPorIdConBloqueo(Long id);

    List<Deuda> listarActivasPorCliente(Long idCliente);

    /** Activa o desactiva todas las deudas del cliente (borrado lógico en cascada). */
    int cambiarEstadoPorCliente(Long idCliente, boolean activo);

    Dinero sumarSaldoActivo(Long idCliente);

    Dinero sumarPagadoActivo(Long idCliente);
}
