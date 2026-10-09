package com.gestiondeudas.dominio.repositorio;

import com.gestiondeudas.dominio.modelo.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente guardar(Cliente cliente);
    Optional<Cliente> buscarPorId(Long id);
    List<Cliente> listarActivos(int pagina, int tamano);
    boolean existePorDocumentoOCorreo(String documento, String correo);
}
