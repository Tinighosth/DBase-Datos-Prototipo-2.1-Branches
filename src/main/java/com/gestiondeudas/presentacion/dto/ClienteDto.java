package com.gestiondeudas.presentacion.dto;

import com.gestiondeudas.dominio.modelo.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ClienteDto {

    private ClienteDto() {
    }

    static final String NOMBRE = "^[\\p{L} .'-]+$";
    static final String DOCUMENTO = "^[A-Za-z0-9-]{5,20}$";
    static final String TELEFONO = "^\\+?[0-9]{7,15}$";
    static final String SIN_CONTROL = "^[^\\p{Cntrl}]*$";

    public record CrearRequest(
            @NotBlank @Size(max = 80) @Pattern(regexp = NOMBRE) String nombre,
            @NotBlank @Size(max = 80) @Pattern(regexp = NOMBRE) String apellido,
            @NotBlank @Pattern(regexp = DOCUMENTO) String documento,
            @NotBlank @Email @Size(max = 120) String correo,
            @Pattern(regexp = TELEFONO) String telefono,
            @Size(max = 150) @Pattern(regexp = SIN_CONTROL) String direccion) {
    }

    public record Respuesta(Long id, String nombre, String apellido, String documentoEnmascarado,
                            String correo, String telefono, String direccion, int activo) {

        public Respuesta {
            SalidaValidator.id(id, "id");
            SalidaValidator.bit(activo, "activo");
        }

        public static Respuesta desde(Cliente c) {
            return new Respuesta(c.id(), c.nombre(), c.apellido(),
                    SalidaValidator.enmascarar(c.documento()), c.correo(), c.telefono(),
                    c.direccion(), c.activo() ? 1 : 0);
        }
    }
}
