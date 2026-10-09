package com.gestiondeudas.presentacion.controlador;

import com.gestiondeudas.aplicacion.servicio.PagoService;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.infraestructura.seguridad.AdminPrincipal;
import com.gestiondeudas.presentacion.dto.PagoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    /**
     * Requiere el encabezado Idempotency-Key. Repetir la misma petición (por ejemplo, tras un
     * corte de red) devuelve el pago original con 200 y NO cobra dos veces.
     */
    @PostMapping("/api/v1/deudas/{idDeuda}/pagos")
    public ResponseEntity<PagoDto.Respuesta> abonar(
            @AuthenticationPrincipal AdminPrincipal admin,
            @PathVariable @Positive Long idDeuda,
            @RequestHeader("Idempotency-Key") @Pattern(regexp = "^[A-Za-z0-9_-]{16,64}$") String clave,
            @Valid @RequestBody PagoDto.CrearRequest req) {
        PagoService.Resultado r = service.registrar(
                new PagoService.Registrar(idDeuda, admin.id(), new Dinero(req.monto()), clave));
        HttpStatus estado = r.reutilizado() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(estado).body(PagoDto.Respuesta.desde(r.pago()));
    }

    @GetMapping("/api/v1/deudas/{idDeuda}/pagos")
    public List<PagoDto.Respuesta> listar(@PathVariable @Positive Long idDeuda) {
        return service.listarPorDeuda(idDeuda).stream().map(PagoDto.Respuesta::desde).toList();
    }
}
