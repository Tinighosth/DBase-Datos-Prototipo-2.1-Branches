package com.gestiondeudas.aplicacion;

import com.gestiondeudas.aplicacion.servicio.PagoService;
import com.gestiondeudas.aplicacion.servicio.TotalService;
import com.gestiondeudas.dominio.excepcion.ConflictoException;
import com.gestiondeudas.dominio.excepcion.RecursoNoEncontradoException;
import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;
import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.modelo.EstadoDeuda;
import com.gestiondeudas.dominio.modelo.Pago;
import com.gestiondeudas.dominio.repositorio.DeudaRepository;
import com.gestiondeudas.dominio.repositorio.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    private static final String CLAVE = "abono-0000000000001";
    private static final Instant AHORA = Instant.parse("2026-10-05T12:00:00Z");

    @Mock DeudaRepository deudas;
    @Mock PagoRepository pagos;
    @Mock TotalService totales;

    private PagoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PagoService(deudas, pagos, totales, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private Deuda deuda(String saldo) {
        return new Deuda(1L, 7L, 9L, "Préstamo", Dinero.de("500.00"), Dinero.de(saldo),
                LocalDate.of(2026, 10, 1), null, EstadoDeuda.PENDIENTE, true);
    }

    private PagoService.Registrar cmd(String monto) {
        return new PagoService.Registrar(1L, 9L, Dinero.de(monto), CLAVE);
    }

    @Test
    @DisplayName("Abono feliz: guarda deuda nueva, agrega el pago al libro y recalcula el total, en ese orden")
    void abonoFeliz() {
        when(pagos.buscarPorClaveIdempotencia(CLAVE)).thenReturn(Optional.empty());
        when(deudas.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(deuda("500.00")));
        when(pagos.guardar(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoService.Resultado r = servicio.registrar(cmd("200.00"));

        assertFalse(r.reutilizado());
        ArgumentCaptor<Deuda> deudaGuardada = ArgumentCaptor.forClass(Deuda.class);
        ArgumentCaptor<Pago> pagoGuardado = ArgumentCaptor.forClass(Pago.class);
        InOrder orden = inOrder(deudas, pagos, totales);
        orden.verify(deudas).guardar(deudaGuardada.capture());
        orden.verify(pagos).guardar(pagoGuardado.capture());
        orden.verify(totales).recalcular(7L);

        assertEquals(Dinero.de("300.00"), deudaGuardada.getValue().saldoPendiente());
        assertEquals(Dinero.de("200.00"), pagoGuardado.getValue().monto());
        assertEquals(Dinero.de("300.00"), pagoGuardado.getValue().saldoResultante());
        assertEquals(AHORA, pagoGuardado.getValue().fechaPago());
    }

    @Test
    @DisplayName("Misma clave y misma solicitud: devuelve el pago original y NO cobra otra vez")
    void repeticionIdempotente() {
        Pago original = new Pago(5L, 1L, 9L, Dinero.de("200.00"), Dinero.de("300.00"), CLAVE, AHORA);
        when(pagos.buscarPorClaveIdempotencia(CLAVE)).thenReturn(Optional.of(original));

        PagoService.Resultado r = servicio.registrar(cmd("200.00"));

        assertTrue(r.reutilizado());
        assertEquals(5L, r.pago().id());
        verifyNoInteractions(deudas, totales);
        verify(pagos, never()).guardar(any());
    }

    @Test
    @DisplayName("Misma clave con OTRO monto: conflicto")
    void claveReutilizadaConOtroMonto() {
        Pago original = new Pago(5L, 1L, 9L, Dinero.de("200.00"), Dinero.de("300.00"), CLAVE, AHORA);
        when(pagos.buscarPorClaveIdempotencia(CLAVE)).thenReturn(Optional.of(original));

        assertThrows(ConflictoException.class, () -> servicio.registrar(cmd("100.00")));
        verifyNoInteractions(deudas, totales);
    }

    @Test
    @DisplayName("Abono mayor al saldo: se rechaza y no se guarda nada")
    void abonoMayorAlSaldo() {
        when(pagos.buscarPorClaveIdempotencia(CLAVE)).thenReturn(Optional.empty());
        when(deudas.buscarPorIdConBloqueo(1L)).thenReturn(Optional.of(deuda("100.00")));

        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(cmd("100.01")));

        verify(deudas, never()).guardar(any());
        verify(pagos, never()).guardar(any());
        verifyNoInteractions(totales);
    }

    @Test
    void deudaInexistente() {
        when(pagos.buscarPorClaveIdempotencia(CLAVE)).thenReturn(Optional.empty());
        when(deudas.buscarPorIdConBloqueo(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrar(cmd("10.00")));
        verify(pagos, never()).guardar(any());
    }
}
