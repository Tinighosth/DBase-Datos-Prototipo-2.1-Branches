package com.gestiondeudas.infraestructura.persistencia.adaptador;

import com.gestiondeudas.dominio.modelo.Admin;
import com.gestiondeudas.dominio.repositorio.AdminRepository;
import com.gestiondeudas.infraestructura.persistencia.entidad.AdminEntity;
import com.gestiondeudas.infraestructura.persistencia.jpa.AdminJpaRepository;
import com.gestiondeudas.infraestructura.persistencia.jpa.PersonaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class AdminPersistenceAdapter implements AdminRepository {

    private final AdminJpaRepository jpa;
    private final PersonaJpaRepository personas;

    AdminPersistenceAdapter(AdminJpaRepository jpa, PersonaJpaRepository personas) {
        this.jpa = jpa;
        this.personas = personas;
    }

    @Override
    public Admin guardar(Admin admin) {
        return jpa.save(AdminEntity.desde(admin)).aDominio();
    }

    @Override
    public Optional<Admin> buscarPorId(Long id) {
        return jpa.findById(id).map(AdminEntity::aDominio);
    }

    @Override
    public Optional<Admin> buscarActivoPorUsuario(String usuario) {
        return jpa.findByUsuarioAndActivo(usuario, true).map(AdminEntity::aDominio);
    }

    @Override
    public boolean existeAlguno() {
        return jpa.count() > 0;
    }

    @Override
    public boolean existePorDocumentoCorreoOUsuario(String documento, String correo, String usuario) {
        return personas.existsByDocumentoOrCorreo(documento, correo) || jpa.existsByUsuario(usuario);
    }
}
