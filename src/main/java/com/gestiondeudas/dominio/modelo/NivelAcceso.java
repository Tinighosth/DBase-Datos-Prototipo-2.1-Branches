package com.gestiondeudas.dominio.modelo;

public enum NivelAcceso {
    BASICO((short) 1),
    INTERMEDIO((short) 2),
    TOTAL((short) 3);

    private final short codigo;

    NivelAcceso(short codigo) {
        this.codigo = codigo;
    }

    public short codigo() {
        return codigo;
    }

    public static NivelAcceso desde(short codigo) {
        for (NivelAcceso n : values()) {
            if (n.codigo == codigo) {
                return n;
            }
        }
        throw new IllegalArgumentException("Nivel de acceso desconocido: " + codigo);
    }
}
