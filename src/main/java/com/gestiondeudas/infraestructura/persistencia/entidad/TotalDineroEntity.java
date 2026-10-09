package com.gestiondeudas.infraestructura.persistencia.entidad;

import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.modelo.TotalCliente;
import com.gestiondeudas.infraestructura.persistencia.convertidor.ActivoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "total_dinero")
public class TotalDineroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_total")
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "total_deuda", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDeuda;

    @Column(name = "total_pagado", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPagado;

    @Convert(converter = ActivoConverter.class)
    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @PrePersist
    @PreUpdate
    void marcarFecha() {
        this.fechaActualizacion = Instant.now();
    }

    protected TotalDineroEntity() {
    }

    /** Aplica el dominio sobre esta fila (conserva el id de una fila existente). */
    public static TotalDineroEntity desde(TotalCliente t, Long idExistente) {
        TotalDineroEntity e = new TotalDineroEntity();
        e.id = idExistente;
        e.idCliente = t.idCliente();
        e.totalDeuda = t.totalDeuda().valor();
        e.totalPagado = t.totalPagado().valor();
        e.activo = t.activo();
        return e;
    }

    public Long getId() {
        return id;
    }

    public TotalCliente aDominio() {
        return new TotalCliente(idCliente, new Dinero(totalDeuda), new Dinero(totalPagado), activo);
    }
}
