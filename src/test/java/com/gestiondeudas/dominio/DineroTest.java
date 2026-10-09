package com.gestiondeudas.dominio;

import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;
import com.gestiondeudas.dominio.modelo.Dinero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DineroTest {

    @Test
    @DisplayName("Normaliza siempre a escala 2")
    void normalizaEscala() {
        assertEquals(new BigDecimal("100.00"), Dinero.de("100").valor());
        assertEquals(new BigDecimal("10.50"), Dinero.de("10.5").valor());
    }

    @Test
    @DisplayName("0.10 + 0.20 es exactamente 0.30 (con double daría 0.30000000000000004)")
    void sumaExacta() {
        assertEquals(Dinero.de("0.30"), Dinero.de("0.10").sumar(Dinero.de("0.20")));
    }

    @Test
    void rechazaNegativos() {
        assertThrows(ReglaNegocioException.class, () -> Dinero.de("-0.01"));
    }

    @Test
    @DisplayName("No redondea en silencio: más de 2 decimales se rechaza")
    void rechazaMasDeDosDecimales() {
        assertThrows(ReglaNegocioException.class, () -> Dinero.de("1.005"));
    }

    @Test
    void aceptaCerosSobrantes() {
        assertEquals(Dinero.de("1.00"), Dinero.de("1.000"));
    }

    @Test
    void rechazaMontoQueExcedeNumeric14_2() {
        assertThrows(ReglaNegocioException.class, () -> Dinero.de("1000000000000.00"));
    }

    @Test
    void restaQueDaNegativoSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> Dinero.de("1.00").restar(Dinero.de("1.01")));
    }

    @Test
    void esCeroYComparacion() {
        assertTrue(Dinero.CERO.esCero());
        assertTrue(Dinero.de("2.00").compareTo(Dinero.de("1.99")) > 0);
    }
}
