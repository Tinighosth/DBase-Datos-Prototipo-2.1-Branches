package com.gestiondeudas.infraestructura.persistencia.entidad;

import com.gestiondeudas.dominio.modelo.Cliente;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
@DiscriminatorValue("1")
@PrimaryKeyJoinColumn(name = "id_persona")
public class ClienteEntity extends PersonaEntity {

    @Column(length = 150)
    private String direccion;

    protected ClienteEntity() {
    }

    public static ClienteEntity desde(Cliente c) {
        ClienteEntity e = new ClienteEntity();
        e.id = c.id();
        e.nombre = c.nombre();
        e.apellido = c.apellido();
        e.documento = c.documento();
        e.correo = c.correo();
        e.telefono = c.telefono();
        e.activo = c.activo();
        e.direccion = c.direccion();
        return e;
    }

    public Cliente aDominio() {
        return new Cliente(id, nombre, apellido, documento, correo, telefono, direccion, activo);
    }
}
