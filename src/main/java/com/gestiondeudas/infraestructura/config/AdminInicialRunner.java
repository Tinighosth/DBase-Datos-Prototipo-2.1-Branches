package com.gestiondeudas.infraestructura.config;

import com.gestiondeudas.aplicacion.servicio.AdminService;
import com.gestiondeudas.dominio.modelo.NivelAcceso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el primer administrador (nivel TOTAL) si la base está vacía.
 * Las credenciales vienen de variables de entorno: nunca van en el código.
 */
@Component
class AdminInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialRunner.class);

    private final AdminService admins;
    private final String usuario;
    private final String clave;

    AdminInicialRunner(AdminService admins,
                       @Value("${seguridad.admin-inicial.usuario:}") String usuario,
                       @Value("${seguridad.admin-inicial.clave:}") String clave) {
        this.admins = admins;
        this.usuario = usuario;
        this.clave = clave;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (admins.existeAlguno()) {
            return;
        }
        if (usuario.isBlank() || clave.isBlank()) {
            log.warn("No hay administradores. Defina ADMIN_BOOTSTRAP_USER y ADMIN_BOOTSTRAP_PASSWORD.");
            return;
        }
        admins.crear(new AdminService.Crear("Administrador", "Inicial", "ADMIN-0001",
                "admin@local.invalid", null, usuario, clave, NivelAcceso.TOTAL));
        log.info("Administrador inicial creado: {}", usuario);
    }
}
