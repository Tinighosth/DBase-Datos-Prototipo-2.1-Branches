package com.gestiondeudas.dominio.repositorio;

import com.gestiondeudas.dominio.modelo.Admin;

import java.util.Optional;

public interface AdminRepository {
    Admin guardar(Admin admin);
    Optional<Admin> buscarPorId(Long id);
    Optional<Admin> buscarActivoPorUsuario(String usuario);
    boolean existeAlguno();
    boolean existePorDocumentoCorreoOUsuario(String documento, String correo, String usuario);
}
