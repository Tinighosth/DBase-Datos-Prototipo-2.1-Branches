package com.gestiondeudas.infraestructura.persistencia.entidad;

import com.gestiondeudas.dominio.modelo.Admin;
import com.gestiondeudas.dominio.modelo.NivelAcceso;
import com.gestiondeudas.infraestructura.persistencia.convertidor.NivelAccesoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin")
@DiscriminatorValue("2")
@PrimaryKeyJoinColumn(name = "id_persona")
public class AdminEntity extends PersonaEntity {

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Convert(converter = NivelAccesoConverter.class)
    @Column(name = "nivel_acceso", nullable = false)
    private NivelAcceso nivelAcceso;

    protected AdminEntity() {
    }

    public static AdminEntity desde(Admin a) {
        AdminEntity e = new AdminEntity();
        e.id = a.id();
        e.nombre = a.nombre();
        e.apellido = a.apellido();
        e.documento = a.documento();
        e.correo = a.correo();
        e.telefono = a.telefono();
        e.activo = a.activo();
        e.usuario = a.usuario();
        e.claveHash = a.claveHash();
        e.nivelAcceso = a.nivel();
        return e;
    }

    public Admin aDominio() {
        return new Admin(id, nombre, apellido, documento, correo, telefono,
                usuario, claveHash, nivelAcceso, activo);
    }
}
