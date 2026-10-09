package com.gestiondeudas.presentacion.controlador;

import com.gestiondeudas.aplicacion.servicio.ClienteService;
import com.gestiondeudas.dominio.modelo.Cliente;
import com.gestiondeudas.presentacion.dto.ClienteDto;
import com.gestiondeudas.presentacion.dto.EstadoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClienteDto.Respuesta> crear(@Valid @RequestBody ClienteDto.CrearRequest req) {
        Cliente c = service.crear(new ClienteService.Crear(
                req.nombre(), req.apellido(), req.documento(), req.correo(), req.telefono(), req.direccion()));
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + c.id()))
                .body(ClienteDto.Respuesta.desde(c));
    }

    @GetMapping
    public List<ClienteDto.Respuesta> listar(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamano) {
        return service.listarActivos(pagina, tamano).stream().map(ClienteDto.Respuesta::desde).toList();
    }

    @GetMapping("/{id}")
    public ClienteDto.Respuesta obtener(@PathVariable @Positive Long id) {
        return ClienteDto.Respuesta.desde(service.obtener(id));
    }

    /** "Eliminar" = activo 0. Reactivar = activo 1. */
    @PatchMapping("/{id}/estado")
    public ClienteDto.Respuesta cambiarEstado(@PathVariable @Positive Long id,
                                              @Valid @RequestBody EstadoRequest req) {
        return ClienteDto.Respuesta.desde(service.cambiarEstado(id, req.esActivo()));
    }
}
