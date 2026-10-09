package com.gestiondeudas.infraestructura.persistencia.jpa;

import com.gestiondeudas.infraestructura.persistencia.entidad.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminJpaRepository extends JpaRepository<AdminEntity, Long> {
    Optional<AdminEntity> findByUsuarioAndActivo(String usuario, boolean activo);
    boolean existsByUsuario(String usuario);
}
