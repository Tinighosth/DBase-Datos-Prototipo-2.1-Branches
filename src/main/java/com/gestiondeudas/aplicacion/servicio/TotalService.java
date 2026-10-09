package com.gestiondeudas.aplicacion.servicio;

import com.gestiondeudas.dominio.excepcion.RecursoNoEncontradoException;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.modelo.TotalCliente;
import com.gestiondeudas.dominio.repositorio.DeudaRepository;
import com.gestiondeudas.dominio.repositorio.TotalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Mantiene el acumulado de dinero por cliente (tabla total_dinero). */
@Service
public class TotalService {

    private final DeudaRepository deudas;
    private final TotalRepository totales;

    public TotalService(DeudaRepository deudas, TotalRepository totales) {
        this.deudas = deudas;
        this.totales = totales;
    }

    /** Recalcula conservando el estado activo que ya tenga el total. */
    @Transactional(propagation = Propagation.MANDATORY)
    public TotalCliente recalcular(Long idCliente) {
        boolean activo = totales.buscarPorCliente(idCliente).map(TotalCliente::activo).orElse(true);
        return sincronizar(idCliente, activo);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public TotalCliente sincronizar(Long idCliente, boolean activo) {
        Dinero totalDeuda = deudas.sumarSaldoActivo(idCliente);
        Dinero totalPagado = deudas.sumarPagadoActivo(idCliente);
        return totales.guardar(new TotalCliente(idCliente, totalDeuda, totalPagado, activo));
    }

    @Transactional(readOnly = true)
    public TotalCliente obtener(Long idCliente) {
        return totales.buscarPorCliente(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Total del cliente no encontrado"));
    }
}
