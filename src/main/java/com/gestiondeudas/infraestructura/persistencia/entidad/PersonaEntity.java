package com.gestiondeudas.infraestructura.persistencia.entidad;

import com.gestiondeudas.infraestructura.persistencia.convertidor.ActivoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/** Superclase: tabla personas. Herencia JOINED (una tabla por clase). tipo: 1 = cliente, 2 = admin. */
@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.INTEGER)
public abstract class PersonaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    Long id;

    @Column(nullable = false, length = 80)
    String nombre;

    @Column(nullable = false, length = 80)
    String apellido;

    @Column(nullable = false, length = 20)
    String documento;

    @Column(nullable = false, length = 120)
    String correo;

    @Column(length = 20)
    String telefono;

    @Convert(converter = ActivoConverter.class)
    @Column(name = "activo", nullable = false)
    boolean activo;

    @Column(name = "fecha_actualizacion")
    Instant fechaActualizacion;

    @PrePersist
    @PreUpdate
    void marcarFecha() {
        this.fechaActualizacion = Instant.now();
    }

    protected PersonaEntity() {
    }
}
