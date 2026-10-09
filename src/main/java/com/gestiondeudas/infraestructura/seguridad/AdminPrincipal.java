package com.gestiondeudas.infraestructura.seguridad;

import com.gestiondeudas.dominio.modelo.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuario autenticado: lleva el id del admin para auditar quién registra cada operación. */
public class AdminPrincipal implements UserDetails {

    private final Long id;
    private final String usuario;
    private final String claveHash;
    private final List<GrantedAuthority> autoridades;

    private AdminPrincipal(Long id, String usuario, String claveHash, List<GrantedAuthority> autoridades) {
        this.id = id;
        this.usuario = usuario;
        this.claveHash = claveHash;
        this.autoridades = autoridades;
    }

    public static AdminPrincipal desde(Admin admin) {
        return new AdminPrincipal(admin.id(), admin.usuario(), admin.claveHash(), List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("NIVEL_" + admin.nivel().codigo())));
    }

    public Long id() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return autoridades;
    }

    @Override
    public String getPassword() {
        return claveHash;
    }

    @Override
    public String getUsername() {
        return usuario;
    }
}
