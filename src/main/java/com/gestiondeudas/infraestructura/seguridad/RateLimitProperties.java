package com.gestiondeudas.infraestructura.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("seguridad.rate-limit")
public record RateLimitProperties(int lecturaPorMinuto, int escrituraPorMinuto) {
}
