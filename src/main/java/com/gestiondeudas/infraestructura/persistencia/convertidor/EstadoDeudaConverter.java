package com.gestiondeudas.infraestructura.persistencia.convertidor;

import com.gestiondeudas.dominio.modelo.EstadoDeuda;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EstadoDeudaConverter implements AttributeConverter<EstadoDeuda, Short> {

    @Override
    public Short convertToDatabaseColumn(EstadoDeuda estado) {
        return estado == null ? null : estado.codigo();
    }

    @Override
    public EstadoDeuda convertToEntityAttribute(Short codigo) {
        return codigo == null ? null : EstadoDeuda.desde(codigo);
    }
}
