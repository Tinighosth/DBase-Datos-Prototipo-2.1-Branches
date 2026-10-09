package com.gestiondeudas.infraestructura.persistencia.jpa;

import com.gestiondeudas.infraestructura.persistencia.entidad.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoJpaRepository extends JpaRepository<PagoEntity, Long> {
    Optional<PagoEntity> findByClaveIdempotencia(String claveIdempotencia);
    List<PagoEntity> findByIdDeudaOrderByFechaPagoAsc(Long idDeuda);
}
