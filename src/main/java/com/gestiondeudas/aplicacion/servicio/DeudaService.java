package com.gestiondeudas.aplicacion.servicio;

import com.gestiondeudas.dominio.excepcion.RecursoNoEncontradoException;
import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;
import com.gestiondeudas.dominio.modelo.Cliente;
import com.gestiondeudas.dominio.modelo.Deuda;
import com.gestiondeudas.dominio.modelo.Dinero;
import com.gestiondeudas.dominio.repositorio.ClienteRepository;
import com.gestiondeudas.dominio.repositorio.DeudaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class DeudaService {

    public record Crear(Long idCliente, Long idAdmin, String concepto,
                        Dinero monto, LocalDate fechaVencimiento) {
    }

    private final DeudaRepository deudas;
    private final ClienteRepository clientes;
    private final TotalService totalService;
    private final Clock reloj;

    public DeudaService(DeudaRepository deudas, ClienteRepository clientes,
                        TotalService totalService, Clock reloj) {
        this.deudas = deudas;
        this.clientes = clientes;
        this.totalService = totalService;
        this.reloj = reloj;
    }

    @Transactional
    public Deuda crear(Crear datos) {
        Cliente cliente = clienteExistente(datos.idCliente());
        if (!cliente.activo()) {
            throw new ReglaNegocioException("No se puede crear una deuda para un cliente inactivo");
        }
        Deuda nueva = Deuda.nueva(datos.idCliente(), datos.idAdmin(), datos.concepto().trim(),
                datos.monto(), LocalDate.now(reloj), datos.fechaVencimiento());
        Deuda guardada = deudas.guardar(nueva);
        totalService.recalcular(datos.idCliente());
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Deuda> listarActivasPorCliente(Long idCliente) {
        clienteExistente(idCliente);
        return deudas.listarActivasPorCliente(idCliente);
    }

    /** Borrado lógico de una sola deuda. Bloquea la fila para no pisar un abono en curso. */
    @Transactional
    public Deuda cambiarEstado(Long idDeuda, boolean activo) {
        Deuda deuda = deudas.buscarPorIdConBloqueo(idDeuda)
                .orElseThrow(() -> new RecursoNoEncontradoException("Deuda no encontrada"));
        if (activo && !clienteExistente(deuda.idCliente()).activo()) {
            throw new ReglaNegocioException("No se puede reactivar la deuda de un cliente inactivo");
        }
        Deuda actualizada = deudas.guardar(deuda.conActivo(activo));
        totalService.recalcular(deuda.idCliente());
        return actualizada;
    }

    private Cliente clienteExistente(Long idCliente) {
        return clientes.buscarPorId(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
    }
}
