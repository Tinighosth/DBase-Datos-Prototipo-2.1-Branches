package com.gestiondeudas.integracion;

import com.gestiondeudas.aplicacion.servicio.AdminService;
import com.gestiondeudas.dominio.modelo.Admin;
import com.gestiondeudas.dominio.modelo.NivelAcceso;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Utilidades comunes: crea un admin TOTAL y ofrece atajos para clientes y deudas. */
abstract class ApiBase extends IntegracionBase {

    static final String USUARIO = "admin.tester";
    static final String CLAVE = "Clave-Segura-1234";

    @Autowired protected MockMvc mvc;
    @Autowired protected AdminService admins;

    protected Admin adminTotal;
    protected RequestPostProcessor auth;

    @BeforeEach
    void crearAdmin() {
        adminTotal = admins.crear(new AdminService.Crear("Test", "Admin", "ADM-001",
                "admin@test.com", null, USUARIO, CLAVE, NivelAcceso.TOTAL));
        auth = httpBasic(USUARIO, CLAVE);
    }

    protected long crearCliente(String documento, String correo) throws Exception {
        String cuerpo = """
                {"nombre":"Laura","apellido":"Gómez","documento":"%s","correo":"%s","direccion":"Calle 1"}
                """.formatted(documento, correo);
        String json = mvc.perform(post("/api/v1/clientes").with(auth)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    protected long crearDeuda(long idCliente, String monto) throws Exception {
        String cuerpo = """
                {"idCliente":%d,"concepto":"Préstamo","monto":%s}
                """.formatted(idCliente, monto);
        String json = mvc.perform(post("/api/v1/deudas").with(auth)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    protected String total(long idCliente) throws Exception {
        return mvc.perform(get("/api/v1/clientes/" + idCliente + "/total").with(auth))
                .andReturn().getResponse().getContentAsString();
    }
}
