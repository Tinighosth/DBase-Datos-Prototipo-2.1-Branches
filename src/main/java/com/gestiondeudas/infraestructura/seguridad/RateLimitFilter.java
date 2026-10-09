package com.gestiondeudas.infraestructura.seguridad;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Límite de solicitudes por IP (token bucket). Corre ANTES de Spring Security,
 * así también frena los intentos de adivinar credenciales.
 * Escrituras (POST/PUT/PATCH/DELETE) tienen un cupo más estricto que las lecturas.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final Cache<String, Bucket> lecturas = nuevoCache();
    private final Cache<String, Bucket> escrituras = nuevoCache();

    public RateLimitFilter(RateLimitProperties props) {
        this.props = props;
    }

    private static Cache<String, Bucket> nuevoCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(100_000)          // evita crecer sin límite ante muchas IP
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        boolean escritura = switch (request.getMethod()) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
        int cupo = escritura ? props.escrituraPorMinuto() : props.lecturaPorMinuto();
        Cache<String, Bucket> cache = escritura ? escrituras : lecturas;
        Bucket bucket = cache.get(request.getRemoteAddr(), ip -> nuevoBucket(cupo));

        ConsumptionProbe sonda = bucket.tryConsumeAndReturnRemaining(1);
        if (sonda.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(sonda.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }
        long segundos = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(sonda.getNanosToWaitForRefill()));
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(segundos));
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"title\":\"Demasiadas solicitudes\",\"status\":429}");
    }

    private static Bucket nuevoBucket(int porMinuto) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(porMinuto)
                        .refillGreedy(porMinuto, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}
