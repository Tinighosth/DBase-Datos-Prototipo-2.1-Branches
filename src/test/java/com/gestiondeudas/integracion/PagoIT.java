package com.gestiondeudas.integracion;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagoIT extends ApiBase {

    private static final String JSON_MONTO_200 = "{\"monto\":200000.00}";

    @Test
    @DisplayName("Flujo completo: cliente -> deuda -> abonos -> totales, con montos exactos")
    void flujoCompleto() throws Exception {
        long cliente = crearCliente("1001001", "laura@correo.com");
        long deuda = crearDeuda(cliente, "500000.00");

        mvc.perform(get("/api/v1/clientes/" + cliente + "/total").with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeuda").value("500000.00"))
                .andExpect(jsonPath("$.totalPagado").value("0.00"));

        mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                        .header("Idempotency-Key", "abono-0000000000001")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_MONTO_200))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoResultante").value("300000.00"));

        mvc.perform(get("/api/v1/clientes/" + cliente + "/total").with(auth))
                .andExpect(jsonPath("$.totalDeuda").value("300000.00"))
                .andExpect(jsonPath("$.totalPagado").value("200000.00"));

        // Abono que supera el saldo: 422 y nada cambia
        mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                        .header("Idempotency-Key", "abono-0000000000002")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":300000.01}"))
                .andExpect(status().isUnprocessableEntity());

        // Pago final: la deuda queda pagada (estado 2)
        mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                        .header("Idempotency-Key", "abono-0000000000003")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":300000.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoResultante").value("0.00"));

        mvc.perform(get("/api/v1/clientes/" + cliente + "/deudas").with(auth))
                .andExpect(jsonPath("$[0].estado").value(2))
                .andExpect(jsonPath("$[0].saldoPendiente").value("0.00"));

        mvc.perform(get("/api/v1/deudas/" + deuda + "/pagos").with(auth))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Idempotencia: repetir la misma petición devuelve el mismo pago y no cobra dos veces")
    void idempotencia() throws Exception {
        long cliente = crearCliente("1001002", "carlos@correo.com");
        long deuda = crearDeuda(cliente, "500000.00");

        String primero = mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                        .header("Idempotency-Key", "repetido-000000001")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_MONTO_200))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String segundo = mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                        .header("Idempotency-Key", "repetido-000000001")
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_MONTO_200))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals((Number) JsonPath.read(primero, "$.id"), (Number) JsonPath.read(segundo, "$.id"));

        mvc.perform(get("/api/v1/clientes/" + cliente + "/total").with(auth))
                .andExpect(jsonPath("$.totalDeuda").value("300000.00"));

        // Misma clave con otro monto: conflicto
        mvc.perform(post("/api/v1/deudas/" + deuda + "/pagos").with(auth)
                        .header("Idempotency-Key", "repetido-000000001")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":1.00}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Borrado lógico en cascada: al desactivar el cliente su total vuelve a 0 y reactivar lo restaura")
    void desactivarYReactivarCliente() throws Exception {
        long cliente = crearCliente("1001003", "ana@correo.com");
        crearDeuda(cliente, "100.00");

        mvc.perform(patch("/api/v1/clientes/" + cliente + "/estado").with(auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(0));

        mvc.perform(get("/api/v1/clientes/" + cliente + "/total").with(auth))
                .andExpect(jsonPath("$.totalDeuda").value("0.00"));

        mvc.perform(patch("/api/v1/clientes/" + cliente + "/estado").with(auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":1}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/clientes/" + cliente + "/total").with(auth))
                .andExpect(jsonPath("$.totalDeuda").value("100.00"));
    }

    @Test
    @DisplayName("Salida: el documento sale enmascarado y nunca aparece clave ni hash")
    void salidaSinDatosSensibles() throws Exception {
        long cliente = crearCliente("1234567890", "mask@correo.com");

        mvc.perform(get("/api/v1/clientes/" + cliente).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentoEnmascarado").value("*******890"))
                .andExpect(jsonPath("$.documento").doesNotExist());
    }
}
