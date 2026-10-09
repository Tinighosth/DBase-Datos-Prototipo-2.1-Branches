package com.gestiondeudas.aplicacion;

import com.gestiondeudas.aplicacion.servicio.ClienteService;
import com.gestiondeudas.aplicacion.servicio.TotalService;
import com.gestiondeudas.dominio.excepcion.ConflictoException;
import com.gestiondeudas.dominio.excepcion.RecursoNoEncontradoException;
import com.gestiondeudas.dominio.modelo.Cliente;
import com.gestiondeudas.dominio.repositorio.ClienteRepository;
import com.gestiondeudas.dominio.repositorio.DeudaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock ClienteRepository clientes;
    @Mock DeudaRepository deudas;
    @Mock TotalService totales;

    private ClienteService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ClienteService(clientes, deudas, totales);
    }

    private ClienteService.Crear datos() {
        return new ClienteService.Crear("Laura", "Gómez", "1001001", "  Laura@Correo.COM ", null, "Calle 1");
    }

    @Test
    @DisplayName("Crea el cliente normalizando el correo y deja su total en cero")
    void creaCliente() {
        when(clientes.existePorDocumentoOCorreo("1001001", "laura@correo.com")).thenReturn(false);
        when(clientes.guardar(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            return new Cliente(5L, c.nombre(), c.apellido(), c.documento(), c.correo(),
                    c.telefono(), c.direccion(), c.activo());
        });

        Cliente creado = servicio.crear(datos());

        assertEquals(5L, creado.id());
        assertEquals("laura@correo.com", creado.correo());
        verify(totales).sincronizar(5L, true);
    }

    @Test
    void duplicadoSeRechazaSinGuardar() {
        when(clientes.existePorDocumentoOCorreo("1001001", "laura@correo.com")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.crear(datos()));

        verify(clientes, never()).guardar(any());
        verifyNoInteractions(totales);
    }

    @Test
    @DisplayName("Desactivar es borrado lógico en cascada: cliente, deudas y total (nunca DELETE)")
    void desactivaEnCascada() {
        Cliente activo = new Cliente(5L, "Laura", "Gómez", "1001001", "laura@correo.com", null, null, true);
        when(clientes.buscarPorId(5L)).thenReturn(Optional.of(activo));
        when(clientes.guardar(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = servicio.cambiarEstado(5L, false);

        assertFalse(resultado.activo());
        ArgumentCaptor<Cliente> guardado = ArgumentCaptor.forClass(Cliente.class);
        InOrder orden = inOrder(clientes, deudas, totales);
        orden.verify(clientes).guardar(guardado.capture());
        orden.verify(deudas).cambiarEstadoPorCliente(5L, false);
        orden.verify(totales).sincronizar(5L, false);
        assertFalse(guardado.getValue().activo());
    }

    @Test
    void clienteInexistente() {
        when(clientes.buscarPorId(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(9L));
    }
}
