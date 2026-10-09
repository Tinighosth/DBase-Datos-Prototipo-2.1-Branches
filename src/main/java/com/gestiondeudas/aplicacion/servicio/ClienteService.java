package com.gestiondeudas.aplicacion.servicio;

import com.gestiondeudas.dominio.excepcion.ConflictoException;
import com.gestiondeudas.dominio.excepcion.RecursoNoEncontradoException;
import com.gestiondeudas.dominio.modelo.Cliente;
import com.gestiondeudas.dominio.repositorio.ClienteRepository;
import com.gestiondeudas.dominio.repositorio.DeudaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ClienteService {

    public record Crear(String nombre, String apellido, String documento,
                        String correo, String telefono, String direccion) {
    }

    private final ClienteRepository clientes;
    private final DeudaRepository deudas;
    private final TotalService totalService;

    public ClienteService(ClienteRepository clientes, DeudaRepository deudas, TotalService totalService) {
        this.clientes = clientes;
        this.deudas = deudas;
        this.totalService = totalService;
    }

    @Transactional
    public Cliente crear(Crear datos) {
        String documento = datos.documento().trim();
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
        if (clientes.existePorDocumentoOCorreo(documento, correo)) {
            throw new ConflictoException("Ya existe una persona con ese documento o correo");
        }
        Cliente guardado = clientes.guardar(Cliente.nuevo(
                datos.nombre().trim(), datos.apellido().trim(), documento, correo,
                datos.telefono(), datos.direccion()));
        totalService.sincronizar(guardado.id(), true);   // fila de total en cero
        return guardado;
    }

    @Transactional(readOnly = true)
    public Cliente obtener(Long id) {
        return clientes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarActivos(int pagina, int tamano) {
        return clientes.listarActivos(pagina, tamano);
    }

    /** Borrado lógico en cascada: cliente, sus deudas y su total. Nunca DELETE. */
    @Transactional
    public Cliente cambiarEstado(Long id, boolean activo) {
        Cliente cliente = obtener(id);
        Cliente actualizado = clientes.guardar(cliente.conActivo(activo));
        deudas.cambiarEstadoPorCliente(id, activo);
        totalService.sincronizar(id, activo);
        return actualizado;
    }
}
