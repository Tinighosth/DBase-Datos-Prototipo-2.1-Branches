package com.gestiondeudas.presentacion.dto;

import com.gestiondeudas.dominio.modelo.Admin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AdminDto {

    private AdminDto() {
    }

    public record CrearRequest(
            @NotBlank @Size(max = 80) @Pattern(regexp = ClienteDto.NOMBRE) String nombre,
            @NotBlank @Size(max = 80) @Pattern(regexp = ClienteDto.NOMBRE) String apellido,
            @NotBlank @Pattern(regexp = ClienteDto.DOCUMENTO) String documento,
            @NotBlank @Email @Size(max = 120) String correo,
            @Pattern(regexp = ClienteDto.TELEFONO) String telefono,
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9._-]{4,50}$") String usuario,
            @NotBlank @Size(min = 12, max = 72) String clave,
            @NotNull @Min(1) @Max(3) Integer nivel) {

        /** La clave nunca debe llegar a los logs. */
        @Override
        public String toString() {
            return "CrearRequest[usuario=" + usuario + "]";
        }
    }

    /** Nunca incluye clave ni hash. */
    public record Respuesta(Long id, String nombre, String apellido, String usuario, int nivel, int activo) {

        public Respuesta {
            SalidaValidator.id(id, "id");
            SalidaValidator.bit(activo, "activo");
            if (nivel < 1 || nivel > 3) {
                throw new IllegalStateException("Nivel de salida inválido");
            }
        }

        public static Respuesta desde(Admin a) {
            return new Respuesta(a.id(), a.nombre(), a.apellido(), a.usuario(),
                    a.nivel().codigo(), a.activo() ? 1 : 0);
        }
    }
}
