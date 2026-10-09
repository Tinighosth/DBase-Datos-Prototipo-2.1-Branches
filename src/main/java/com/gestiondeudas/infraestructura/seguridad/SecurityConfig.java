package com.gestiondeudas.infraestructura.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                // API sin sesión ni cookies: no hay token CSRF que proteger
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .headers(h -> h
                        .contentSecurityPolicy(c -> c.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .frameOptions(f -> f.deny())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health").permitAll()
                        // Gestión de administradores: solo nivel TOTAL
                        .requestMatchers(HttpMethod.POST, "/api/v1/admins").hasAuthority("NIVEL_3")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/admins/*/estado").hasAuthority("NIVEL_3")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        // Lo que no esté listado se niega
                        .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder(@Value("${seguridad.bcrypt-strength:12}") int fuerza) {
        return new BCryptPasswordEncoder(fuerza);
    }
}
