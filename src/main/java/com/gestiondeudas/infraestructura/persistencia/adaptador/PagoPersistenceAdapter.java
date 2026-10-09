package com.gestiondeudas.infraestructura.persistencia.adaptador;

import com.gestiondeudas.dominio.modelo.Pago;
import com.gestiondeudas.dominio.repositorio.PagoRepository;
import com.gestiondeudas.infraestructura.persistencia.entidad.PagoEntity;
import com.gestiondeudas.infraestructura.persistencia.jpa.PagoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class PagoPersistenceAdapter implements PagoRepository {

    private final PagoJpaRepository jpa;

    PagoPersistenceAdapter(PagoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Pago guardar(Pago pago) {
        return jpa.save(PagoEntity.desde(pago)).aDominio();
    }

    @Override
    public Optional<Pago> buscarPorClaveIdempotencia(String clave) {
        return jpa.findByClaveIdempotencia(clave).map(PagoEntity::aDominio);
    }

    @Override
    public List<Pago> listarPorDeuda(Long idDeuda) {
        return jpa.findByIdDeudaOrderByFechaPagoAsc(idDeuda).stream()
                .map(PagoEntity::aDominio)
                .toList();
    }
}
