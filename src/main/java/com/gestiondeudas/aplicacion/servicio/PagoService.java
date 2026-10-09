package com.gestiondeudas.aplicacion.servicio;

import com.gestiondeudas.dominio.excepcion.ConflictoException;
import com.gestiondeudas.dominio.excepcion.RecursoNoEncontradoException;
import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.modelo.Pago;
import com.gestiondeudas.dominio.repositorio.DeudaRepository;
import com.gestiondeudas.dominio.repositorio.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    public record Registrar(Long idDeuda, Long idAdmin, Dinero monto, String claveIdempotencia) {
    }

    /** reutilizado = true cuando la clave de idempotencia ya existía (repetición segura). */
    public record Resultado(Pago pago, boolean reutilizado) {
    }

    private final DeudaRepository deudas;
    private final PagoRepository pagos;
    private final TotalService totalService;
    private final Clock reloj;

    public PagoService(DeudaRepository deudas, PagoRepository pagos,
                       TotalService totalService, Clock reloj) {
        this.deudas = deudas;
        this.pagos = pagos;
        this.totalService = totalService;
        this.reloj = reloj;
    }

    /**
     * Una sola transacción atómica: o se guarda todo (deuda, pago y total) o nada.
     * 1. Idempotencia: la misma clave devuelve el pago original sin cobrar de nuevo.
     * 2. Bloqueo de fila (FOR UPDATE): dos abonos simultáneos se ejecutan en serie.
     * 3. La deuda es inmutable: se crea una nueva versión y el pago se agrega al libro.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Resultado registrar(Registrar cmd) {
        Optional<Pago> previo = pagos.buscarPorClaveIdempotencia(cmd.claveIdempotencia());
        if (previo.isPresent()) {
            Pago p = previo.get();
            if (!p.idDeuda().equals(cmd.idDeuda()) || p.monto().compareTo(cmd.monto()) != 0) {
                throw new ConflictoException("La clave de idempotencia ya se usó con otra solicitud");
            }
            return new Resultado(p, true);
        }

        Deuda deuda = deudas.buscarPorIdConBloqueo(cmd.idDeuda())
                .orElseThrow(() -> new RecursoNoEncontradoException("Deuda no encontrada"));

        Deuda actualizada = deuda.aplicarAbono(cmd.monto());
        deudas.guardar(actualizada);

        Pago pago = pagos.guardar(Pago.registrar(cmd.idDeuda(), cmd.idAdmin(), cmd.monto(),
                actualizada.saldoPendiente(), cmd.claveIdempotencia(), Instant.now(reloj)));

        totalService.recalcular(deuda.idCliente());

        // Auditoría: solo identificadores, sin datos personales
        log.info("Abono registrado idPago={} idDeuda={} idAdmin={}",
                pago.id(), pago.idDeuda(), pago.idAdmin());
        return new Resultado(pago, false);
    }

    @Transactional(readOnly = true)
    public List<Pago> listarPorDeuda(Long idDeuda) {
        return pagos.listarPorDeuda(idDeuda);
    }
}
