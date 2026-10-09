package com.gestiondeudas.infraestructura.persistencia.jpa;

import com.gestiondeudas.infraestructura.persistencia.entidad.DeudaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DeudaJpaRepository extends JpaRepository<DeudaEntity, Long> {

    List<DeudaEntity> findByIdClienteAndActivoOrderByFechaVencimientoAsc(Long idCliente, boolean activo);

    /** SELECT ... FOR UPDATE: serializa los abonos concurrentes sobre la misma deuda. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DeudaEntity d where d.id = :id")
    Optional<DeudaEntity> buscarConBloqueo(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update DeudaEntity d set d.activo = :activo where d.idCliente = :idCliente")
    int cambiarEstadoPorCliente(@Param("idCliente") Long idCliente, @Param("activo") boolean activo);

    /** Devuelve null si no hay filas. */
    @Query("select sum(d.saldoPendiente) from DeudaEntity d where d.idCliente = :idCliente and d.activo = :activo")
    BigDecimal sumarSaldo(@Param("idCliente") Long idCliente, @Param("activo") boolean activo);

    @Query("select sum(d.montoInicial - d.saldoPendiente) from DeudaEntity d "
            + "where d.idCliente = :idCliente and d.activo = :activo")
    BigDecimal sumarPagado(@Param("idCliente") Long idCliente, @Param("activo") boolean activo);
}
