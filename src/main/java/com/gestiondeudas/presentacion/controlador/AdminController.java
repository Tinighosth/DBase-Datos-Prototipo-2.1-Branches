package com.gestiondeudas.presentacion.controlador;

import com.gestiondeudas.aplicacion.servicio.AdminService;
import com.gestiondeudas.dominio.modelo.Admin;
import com.gestiondeudas.dominio.modelo.NivelAcceso;
import com.gestiondeudas.infraestructura.seguridad.AdminPrincipal;
import com.gestiondeudas.presentacion.dto.AdminDto;
import com.gestiondeudas.presentacion.dto.EstadoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Acceso restringido a nivel TOTAL (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admins")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AdminDto.Respuesta> crear(@Valid @RequestBody AdminDto.CrearRequest req) {
        Admin a = service.crear(new AdminService.Crear(req.nombre(), req.apellido(), req.documento(),
                req.correo(), req.telefono(), req.usuario(), req.clave(),
                NivelAcceso.desde(req.nivel().shortValue())));
        return ResponseEntity.created(URI.create("/api/v1/admins/" + a.id()))
                .body(AdminDto.Respuesta.desde(a));
    }

    @PatchMapping("/{id}/estado")
    public AdminDto.Respuesta cambiarEstado(@AuthenticationPrincipal AdminPrincipal solicitante,
                                            @PathVariable @Positive Long id,
                                            @Valid @RequestBody EstadoRequest req) {
        return AdminDto.Respuesta.desde(service.cambiarEstado(id, req.esActivo(), solicitante.id()));
    }
}
