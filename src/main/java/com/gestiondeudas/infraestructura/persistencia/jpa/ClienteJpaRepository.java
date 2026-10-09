package com.gestiondeudas.infraestructura.persistencia.jpa;

import com.gestiondeudas.infraestructura.persistencia.entidad.ClienteEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {
    List<ClienteEntity> findByActivoOrderByIdAsc(boolean activo, Pageable pageable);
}
