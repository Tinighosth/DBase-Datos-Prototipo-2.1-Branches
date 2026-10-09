package com.gestiondeudas.presentacion.controlador;

import com.gestiondeudas.aplicacion.servicio.TotalService;
import com.gestiondeudas.presentacion.dto.TotalDto;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TotalController {

    private final TotalService service;

    public TotalController(TotalService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/clientes/{idCliente}/total")
    public TotalDto.Respuesta obtener(@PathVariable @Positive Long idCliente) {
        return TotalDto.Respuesta.desde(service.obtener(idCliente));
    }
}
