package com.gestiondeudas.infraestructura.persistencia.entidad;

import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.modelo.EstadoDeuda;
import com.gestiondeudas.infraestructura.persistencia.convertidor.ActivoConverter;
import com.gestiondeudas.infraestructura.persistencia.convertidor.EstadoDeudaConverter;
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
import java.time.LocalDate;

@Entity
@Table(name = "deudas")
public class DeudaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deuda")
    private Long id;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_admin", nullable = false)
    private Long idAdmin;

    @Column(nullable = false, length = 150)
    private String concepto;

    @Column(name = "monto_inicial", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "saldo_pendiente", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Convert(converter = EstadoDeudaConverter.class)
    @Column(nullable = false)
    private EstadoDeuda estado;

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

    protected DeudaEntity() {
    }

    public static DeudaEntity desde(Deuda d) {
        DeudaEntity e = new DeudaEntity();
        e.id = d.id();
        e.idCliente = d.idCliente();
        e.idAdmin = d.idAdmin();
        e.concepto = d.concepto();
        e.montoInicial = d.montoInicial().valor();
        e.saldoPendiente = d.saldoPendiente().valor();
        e.fechaEmision = d.fechaEmision();
        e.fechaVencimiento = d.fechaVencimiento();
        e.estado = d.estado();
        e.activo = d.activo();
        return e;
    }

    public Deuda aDominio() {
        return new Deuda(id, idCliente, idAdmin, concepto, new Dinero(montoInicial),
                new Dinero(saldoPendiente), fechaEmision, fechaVencimiento, estado, activo);
    }
}
