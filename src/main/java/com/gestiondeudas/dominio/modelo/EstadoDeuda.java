package com.gestiondeudas.dominio.modelo;

public enum EstadoDeuda {
    PENDIENTE((short) 1),
    PAGADA((short) 2);

    private final short codigo;

    EstadoDeuda(short codigo) {
        this.codigo = codigo;
    }

    public short codigo() {
        return codigo;
    }

    public static EstadoDeuda desde(short codigo) {
        for (EstadoDeuda e : values()) {
            if (e.codigo == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de deuda desconocido: " + codigo);
    }
}
