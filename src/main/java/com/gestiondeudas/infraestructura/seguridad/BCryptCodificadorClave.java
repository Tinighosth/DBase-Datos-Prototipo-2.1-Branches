package com.gestiondeudas.infraestructura.seguridad;

import com.gestiondeudas.aplicacion.puerto.CodificadorClave;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptCodificadorClave implements CodificadorClave {

    private final PasswordEncoder encoder;

    BCryptCodificadorClave(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String codificar(CharSequence claveEnClaro) {
        return encoder.encode(claveEnClaro);
    }
}
