package com.gestiondeudas.infraestructura.persistencia.adaptador;

import com.gestiondeudas.dominio.modelo.TotalCliente;
import com.gestiondeudas.dominio.repositorio.TotalRepository;
import com.gestiondeudas.infraestructura.persistencia.entidad.TotalDineroEntity;
import com.gestiondeudas.infraestructura.persistencia.jpa.TotalJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class TotalPersistenceAdapter implements TotalRepository {

    private final TotalJpaRepository jpa;

    TotalPersistenceAdapter(TotalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TotalCliente guardar(TotalCliente total) {
        Long idExistente = jpa.findByIdCliente(total.idCliente())
                .map(TotalDineroEntity::getId)
                .orElse(null);
        return jpa.save(TotalDineroEntity.desde(total, idExistente)).aDominio();
    }

    @Override
    public Optional<TotalCliente> buscarPorCliente(Long idCliente) {
        return jpa.findByIdCliente(idCliente).map(TotalDineroEntity::aDominio);
    }
}
