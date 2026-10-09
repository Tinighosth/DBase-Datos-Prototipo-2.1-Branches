package com.gestiondeudas.infraestructura.persistencia.entidad;

import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.modelo.Pago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

/** @Immutable: Hibernate nunca emite UPDATE. En la base lo refuerza un trigger (V2). */
@Entity
@Immutable
@Table(name = "pagos")
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @Column(name = "id_deuda", nullable = false, updatable = false)
    private Long idDeuda;

    @Column(name = "id_admin", nullable = false, updatable = false)
    private Long idAdmin;

    @Column(nullable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_resultante", nullable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal saldoResultante;

    @Column(name = "clave_idempotencia", nullable = false, updatable = false, length = 64)
    private String claveIdempotencia;

    @Column(name = "fecha_pago", nullable = false, updatable = false)
    private Instant fechaPago;

    protected PagoEntity() {
    }

    public static PagoEntity desde(Pago p) {
        PagoEntity e = new PagoEntity();
        e.id = p.id();
        e.idDeuda = p.idDeuda();
        e.idAdmin = p.idAdmin();
        e.monto = p.monto().valor();
        e.saldoResultante = p.saldoResultante().valor();
        e.claveIdempotencia = p.claveIdempotencia();
        e.fechaPago = p.fechaPago();
        return e;
    }

    public Pago aDominio() {
        return new Pago(id, idDeuda, idAdmin, new Dinero(monto), new Dinero(saldoResultante),
                claveIdempotencia, fechaPago);
    }
}
