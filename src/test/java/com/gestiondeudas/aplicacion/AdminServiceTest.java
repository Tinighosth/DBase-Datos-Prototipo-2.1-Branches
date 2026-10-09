package com.gestiondeudas.aplicacion;

import com.gestiondeudas.aplicacion.puerto.CodificadorClave;
import com.gestiondeudas.aplicacion.servicio.AdminService;
import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;
import com.gestiondeudas.dominio.modelo.Admin;
import com.gestiondeudas.dominio.modelo.NivelAcceso;
import com.gestiondeudas.dominio.repositorio.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock AdminRepository admins;
    @Mock CodificadorClave codificador;

    private AdminService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AdminService(admins, codificador);
    }

    private AdminService.Crear datos(String clave) {
        return new AdminService.Crear("Mateo", "Silva", "2002001", "Mateo@Empresa.com",
                null, "Mateo.Admin", clave, NivelAcceso.TOTAL);
    }

    @Test
    @DisplayName("Guarda solo el hash de la clave, nunca el texto plano")
    void guardaHash() {
        when(admins.existePorDocumentoCorreoOUsuario("2002001", "mateo@empresa.com", "mateo.admin")).thenReturn(false);
        when(codificador.codificar("Clave-Muy-Segura-1")).thenReturn("$2a$HASH");
        when(admins.guardar(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.crear(datos("Clave-Muy-Segura-1"));

        ArgumentCaptor<Admin> captura = ArgumentCaptor.forClass(Admin.class);
        verify(admins).guardar(captura.capture());
        assertEquals("$2a$HASH", captura.getValue().claveHash());
        assertEquals("mateo.admin", captura.getValue().usuario());
        assertFalse(captura.getValue().toString().contains("HASH"));
    }

    @Test
    void claveCortaSeRechazaSinTocarNada() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(datos("corta")));
        verifyNoInteractions(admins, codificador);
    }

    @Test
    void noPuedeDesactivarseASiMismo() {
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado(1L, false, 1L));
        verifyNoInteractions(admins);
    }
}
