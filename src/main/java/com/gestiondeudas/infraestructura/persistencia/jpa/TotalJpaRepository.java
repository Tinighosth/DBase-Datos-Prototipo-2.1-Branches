package com.gestiondeudas.infraestructura.persistencia.jpa;

import com.gestiondeudas.infraestructura.persistencia.entidad.TotalDineroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TotalJpaRepository extends JpaRepository<TotalDineroEntity, Long> {
    Optional<TotalDineroEntity> findByIdCliente(Long idCliente);
}
