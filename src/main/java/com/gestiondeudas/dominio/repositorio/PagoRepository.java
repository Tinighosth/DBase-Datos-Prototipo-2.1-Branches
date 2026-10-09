package com.gestiondeudas.dominio.repositorio;

import com.gestiondeudas.dominio.modelo.Pago;

import java.util.List;
import java.util.Optional;

/** Libro de pagos: solo se inserta y se consulta. No existe actualizar ni eliminar. */
public interface PagoRepository {
    Pago guardar(Pago pago);
    Optional<Pago> buscarPorClaveIdempotencia(String clave);
    List<Pago> listarPorDeuda(Long idDeuda);
}
