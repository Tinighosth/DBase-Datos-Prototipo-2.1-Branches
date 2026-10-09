package com.gestiondeudas.infraestructura.persistencia.convertidor;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** boolean (dominio) <-> SMALLINT 1/0 (base de datos). */
@Converter
public class ActivoConverter implements AttributeConverter<Boolean, Short> {

    @Override
    public Short convertToDatabaseColumn(Boolean activo) {
        return (short) (Boolean.TRUE.equals(activo) ? 1 : 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Short valor) {
        return valor != null && valor == 1;
    }
}
