package com.gestiondeudas.infraestructura.seguridad;

import com.gestiondeudas.dominio.repositorio.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Solo autentica administradores ACTIVOS. El mensaje de error es genérico a propósito. */
@Service
class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository admins;

    AdminUserDetailsService(AdminRepository admins) {
        this.admins = admins;
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) {
        return admins.buscarActivoPorUsuario(usuario.trim().toLowerCase(Locale.ROOT))
                .map(AdminPrincipal::desde)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}
