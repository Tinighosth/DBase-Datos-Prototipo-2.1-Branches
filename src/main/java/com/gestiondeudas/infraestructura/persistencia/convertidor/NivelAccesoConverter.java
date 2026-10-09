package com.gestiondeudas.infraestructura.persistencia.convertidor;

import com.gestiondeudas.dominio.modelo.NivelAcceso;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NivelAccesoConverter implements AttributeConverter<NivelAcceso, Short> {

    @Override
    public Short convertToDatabaseColumn(NivelAcceso nivel) {
        return nivel == null ? null : nivel.codigo();
    }

    @Override
    public NivelAcceso convertToEntityAttribute(Short codigo) {
        return codigo == null ? null : NivelAcceso.desde(codigo);
    }
}
