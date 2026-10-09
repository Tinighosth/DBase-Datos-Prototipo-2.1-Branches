package com.gestiondeudas.infraestructura.persistencia.adaptador;

import com.gestiondeudas.dominio.modelo.Cliente;
import com.gestiondeudas.dominio.repositorio.ClienteRepository;
import com.gestiondeudas.infraestructura.persistencia.entidad.ClienteEntity;
import com.gestiondeudas.infraestructura.persistencia.jpa.ClienteJpaRepository;
import com.gestiondeudas.infraestructura.persistencia.jpa.PersonaJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class ClientePersistenceAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpa;
    private final PersonaJpaRepository personas;

    ClientePersistenceAdapter(ClienteJpaRepository jpa, PersonaJpaRepository personas) {
        this.jpa = jpa;
        this.personas = personas;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        return jpa.save(ClienteEntity.desde(cliente)).aDominio();
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(ClienteEntity::aDominio);
    }

    @Override
    public List<Cliente> listarActivos(int pagina, int tamano) {
        return jpa.findByActivoOrderByIdAsc(true, PageRequest.of(pagina, tamano)).stream()
                .map(ClienteEntity::aDominio)
                .toList();
    }

    @Override
    public boolean existePorDocumentoOCorreo(String documento, String correo) {
        return personas.existsByDocumentoOrCorreo(documento, correo);
    }
}
