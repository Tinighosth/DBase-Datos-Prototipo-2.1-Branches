package com.gestiondeudas.dominio;

import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;
import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.modelo.EstadoDeuda;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeudaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 5);

    private Deuda deuda(String monto) {
        return Deuda.nueva(1L, 2L, "Préstamo", Dinero.de(monto), HOY, HOY.plusMonths(3));
    }

    @Test
    @DisplayName("Abono parcial: crea una NUEVA deuda y la original no cambia (inmutabilidad)")
    void abonoParcialEsInmutable() {
        Deuda original = deuda("500.00");

        Deuda nueva = original.aplicarAbono(Dinero.de("200.00"));

        assertNotSame(original, nueva);
        assertEquals(Dinero.de("500.00"), original.saldoPendiente());
        assertEquals(Dinero.de("300.00"), nueva.saldoPendiente());
        assertEquals(EstadoDeuda.PENDIENTE, nueva.estado());
    }

    @Test
    void abonoTotalMarcaPagada() {
        Deuda pagada = deuda("500.00").aplicarAbono(Dinero.de("500.00"));

        assertEquals(EstadoDeuda.PAGADA, pagada.estado());
        assertEquals(Dinero.CERO, pagada.saldoPendiente());
    }

    @Test
    void abonoMayorAlSaldoSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> deuda("100.00").aplicarAbono(Dinero.de("100.01")));
    }

    @Test
    void abonoEnCeroSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> deuda("100.00").aplicarAbono(Dinero.CERO));
    }

    @Test
    void deudaPagadaNoAceptaMasAbonos() {
        Deuda pagada = deuda("100.00").aplicarAbono(Dinero.de("100.00"));
        assertThrows(ReglaNegocioException.class, () -> pagada.aplicarAbono(Dinero.de("1.00")));
    }

    @Test
    void deudaInactivaNoAceptaAbonos() {
        Deuda inactiva = deuda("100.00").conActivo(false);
        assertFalse(inactiva.activo());
        assertThrows(ReglaNegocioException.class, () -> inactiva.aplicarAbono(Dinero.de("1.00")));
    }

    @Test
    void montoCeroSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> deuda("0.00"));
    }

    @Test
    void vencimientoAnteriorALaEmisionSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> Deuda.nueva(1L, 2L, "X", Dinero.de("10.00"), HOY, HOY.minusDays(1)));
    }
}
