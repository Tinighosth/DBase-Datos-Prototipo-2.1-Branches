package com.gestiondeudas.presentacion.controlador;

import com.gestiondeudas.aplicacion.servicio.DeudaService;
import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.infraestructura.seguridad.AdminPrincipal;
import com.gestiondeudas.presentacion.dto.DeudaDto;
import com.gestiondeudas.presentacion.dto.EstadoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
public class DeudaController {

    private final DeudaService service;

    public DeudaController(DeudaService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/deudas")
    public ResponseEntity<DeudaDto.Respuesta> crear(@AuthenticationPrincipal AdminPrincipal admin,
                                                    @Valid @RequestBody DeudaDto.CrearRequest req) {
        Deuda d = service.crear(new DeudaService.Crear(req.idCliente(), admin.id(),
                req.concepto(), new Dinero(req.monto()), req.fechaVencimiento()));
        return ResponseEntity.created(URI.create("/api/v1/deudas/" + d.id()))
                .body(DeudaDto.Respuesta.desde(d));
    }

    @GetMapping("/api/v1/clientes/{idCliente}/deudas")
    public List<DeudaDto.Respuesta> listarPorCliente(@PathVariable @Positive Long idCliente) {
        return service.listarActivasPorCliente(idCliente).stream().map(DeudaDto.Respuesta::desde).toList();
    }

    @PatchMapping("/api/v1/deudas/{id}/estado")
    public DeudaDto.Respuesta cambiarEstado(@PathVariable @Positive Long id,
                                            @Valid @RequestBody EstadoRequest req) {
        return DeudaDto.Respuesta.desde(service.cambiarEstado(id, req.esActivo()));
    }
}
