package com.gestiondeudas.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "seguridad.rate-limit.lectura-por-minuto=3")
class RateLimitIT extends IntegracionBase {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("Al superar el cupo por minuto responde 429 con Retry-After, incluso sin autenticar")
    void superarElCupo() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/api/v1/clientes")).andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
