package com.gestiondeudas.integracion;

import com.gestiondeudas.aplicacion.servicio.AdminService;
import com.gestiondeudas.dominio.modelo.NivelAcceso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SeguridadIT extends ApiBase {

    @Autowired JdbcTemplate jdbc;

    private static final String ADMIN_NUEVO = """
            {"nombre":"Sofia","apellido":"Torres","documento":"ADM-002","correo":"sofia@test.com",
             "usuario":"sofia.admin","clave":"Otra-Clave-Segura-9","nivel":2}
            """;

    @Test
    void sinCredencialesDa401() throws Exception {
        mvc.perform(get("/api/v1/clientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void claveIncorrectaDa401() throws Exception {
        mvc.perform(get("/api/v1/clientes").with(httpBasic(USUARIO, "incorrecta-123456")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un admin que no es nivel TOTAL no puede crear administradores")
    void adminBasicoNoCreaAdmins() throws Exception {
        admins.crear(new AdminService.Crear("Basico", "Admin", "ADM-003", "basico@test.com",
                null, "basico.admin", "Clave-Basica-12345", NivelAcceso.BASICO));

        mvc.perform(post("/api/v1/admins").with(httpBasic("basico.admin", "Clave-Basica-12345"))
                        .contentType(MediaType.APPLICATION_JSON).content(ADMIN_NUEVO))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/admins").with(auth)
                        .contentType(MediaType.APPLICATION_JSON).content(ADMIN_NUEVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clave").doesNotExist())
                .andExpect(jsonPath("$.claveHash").doesNotExist());
    }

    @Test
    @DisplayName("Un admin desactivado ya no puede autenticarse")
    void adminInactivoNoEntra() throws Exception {
        var otro = admins.crear(new AdminService.Crear("Otro", "Admin", "ADM-004", "otro@test.com",
                null, "otro.admin", "Clave-Otro-123456", NivelAcceso.INTERMEDIO));
        admins.cambiarEstado(otro.id(), false, adminTotal.id());

        mvc.perform(get("/api/v1/clientes").with(httpBasic("otro.admin", "Clave-Otro-123456")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("No existe DELETE en la API")
    void deleteNoExiste() throws Exception {
        mvc.perform(delete("/api/v1/clientes/1").with(auth)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Validación de entrada: campos inválidos y propiedades desconocidas dan 400")
    void validacionDeEntrada() throws Exception {
        mvc.perform(post("/api/v1/clientes").with(auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"apellido\":\"X\",\"documento\":\"1\",\"correo\":\"no-es-correo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.correo").exists());

        mvc.perform(post("/api/v1/clientes").with(auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"apellido\":\"Ruiz\",\"documento\":\"1234567\","
                                + "\"correo\":\"a@b.co\",\"activo\":0}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/v1/clientes?tamano=1000").with(auth)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Monto con más de 2 decimales o como texto se rechaza")
    void montosEstrictos() throws Exception {
        long cliente = crearCliente("1001010", "estricto@correo.com");

        mvc.perform(post("/api/v1/deudas").with(auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCliente\":" + cliente + ",\"concepto\":\"X\",\"monto\":10.005}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/deudas").with(auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCliente\":" + cliente + ",\"concepto\":\"X\",\"monto\":\"10.00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Los errores no filtran detalles internos")
    void erroresGenericos() throws Exception {
        mvc.perform(get("/api/v1/clientes/999999").with(auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Cliente no encontrado"));
    }

    @Test
    @DisplayName("Respuestas con cabeceras de seguridad")
    void cabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/api/v1/clientes").with(auth))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    @DisplayName("La base de datos bloquea DELETE y la modificación del libro de pagos")
    void triggersDeProteccion() throws Exception {
        long cliente = crearCliente("1001011", "trigger@correo.com");
        long deuda = crearDeuda(cliente, "100.00");
        mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                .header("Idempotency-Key", "trigger-00000000001")
                .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":10.00}"));

        assertThrows(DataAccessException.class, () -> jdbc.update("DELETE FROM deudas"));
    }

    @Test
    void libroDePagosEsInmutable() throws Exception {
        long cliente = crearCliente("1001012", "inmutable@correo.com");
        long deuda = crearDeuda(cliente, "100.00");
        mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                .header("Idempotency-Key", "inmutable-0000000001")
                .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":10.00}"));

        assertThrows(DataAccessException.class, () -> jdbc.update("UPDATE pagos SET monto = 1.00"));
    }
}
