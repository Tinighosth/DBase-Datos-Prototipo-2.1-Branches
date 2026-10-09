package com.gestiondeudas.aplicacion.servicio;

import com.gestiondeudas.aplicacion.puerto.CodificadorClave;
import com.gestiondeudas.dominio.excepcion.ConflictoException;
import com.gestiondeudas.dominio.excepcion.RecursoNoEncontradoException;
import com.gestiondeudas.dominio.excepcion.ReglaNegocioException;
import com.gestiondeudas.dominio.modelo.Admin;
import com.gestiondeudas.dominio.modelo.NivelAcceso;
import com.gestiondeudas.dominio.repositorio.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdminService {

    public record Crear(String nombre, String apellido, String documento, String correo,
                        String telefono, String usuario, String claveEnClaro, NivelAcceso nivel) {
        /** La clave nunca debe aparecer en logs. */
        @Override
        public String toString() {
            return "Crear[usuario=" + usuario + ", nivel=" + nivel + "]";
        }
    }

    static final int CLAVE_MIN = 12;
    static final int CLAVE_MAX = 72;   // límite de BCrypt

    private final AdminRepository admins;
    private final CodificadorClave codificador;

    public AdminService(AdminRepository admins, CodificadorClave codificador) {
        this.admins = admins;
        this.codificador = codificador;
    }

    @Transactional
    public Admin crear(Crear datos) {
        String clave = datos.claveEnClaro();
        if (clave == null || clave.length() < CLAVE_MIN || clave.length() > CLAVE_MAX) {
            throw new ReglaNegocioException(
                    "La clave debe tener entre " + CLAVE_MIN + " y " + CLAVE_MAX + " caracteres");
        }
        String documento = datos.documento().trim();
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
        String usuario = datos.usuario().trim().toLowerCase(Locale.ROOT);
        if (admins.existePorDocumentoCorreoOUsuario(documento, correo, usuario)) {
            throw new ConflictoException("Ya existe una persona con ese documento, correo o usuario");
        }
        String hash = codificador.codificar(clave);
        return admins.guardar(Admin.nuevo(datos.nombre().trim(), datos.apellido().trim(),
                documento, correo, datos.telefono(), usuario, hash, datos.nivel()));
    }

    @Transactional(readOnly = true)
    public boolean existeAlguno() {
        return admins.existeAlguno();
    }

    @Transactional
    public Admin cambiarEstado(Long idAdmin, boolean activo, Long idSolicitante) {
        if (!activo && idAdmin.equals(idSolicitante)) {
            throw new ReglaNegocioException("Un administrador no puede desactivarse a sí mismo");
        }
        Admin admin = admins.buscarPorId(idAdmin)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));
        return admins.guardar(admin.conActivo(activo));
    }
}
