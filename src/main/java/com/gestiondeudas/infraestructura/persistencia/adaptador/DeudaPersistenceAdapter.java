package com.gestiondeudas.infraestructura.persistencia.adaptador;

import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.repositorio.DeudaRepository;
import com.gestiondeudas.infraestructura.persistencia.entidad.DeudaEntity;
import com.gestiondeudas.infraestructura.persistencia.jpa.DeudaJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
class DeudaPersistenceAdapter implements DeudaRepository {

    private final DeudaJpaRepository jpa;

    DeudaPersistenceAdapter(DeudaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Deuda guardar(Deuda deuda) {
        return jpa.save(DeudaEntity.desde(deuda)).aDominio();
    }

    @Override
    public Optional<Deuda> buscarPorIdConBloqueo(Long id) {
        return jpa.buscarConBloqueo(id).map(DeudaEntity::aDominio);
    }

    @Override
    public List<Deuda> listarActivasPorCliente(Long idCliente) {
        return jpa.findByIdClienteAndActivoOrderByFechaVencimientoAsc(idCliente, true).stream()
                .map(DeudaEntity::aDominio)
                .toList();
    }

    @Override
    public int cambiarEstadoPorCliente(Long idCliente, boolean activo) {
        return jpa.cambiarEstadoPorCliente(idCliente, activo);
    }

    @Override
    public Dinero sumarSaldoActivo(Long idCliente) {
        return aDinero(jpa.sumarSaldo(idCliente, true));
    }

    @Override
    public Dinero sumarPagadoActivo(Long idCliente) {
        return aDinero(jpa.sumarPagado(idCliente, true));
    }

    private static Dinero aDinero(BigDecimal valor) {
        return valor == null ? Dinero.CERO : new Dinero(valor);
    }
}
