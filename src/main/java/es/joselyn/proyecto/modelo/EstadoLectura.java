package es.joselyn.proyecto.modelo;

public enum EstadoLectura {
    NORMAL(0),
    SOSPECHOSA(1),
    ANOMALA(2);

    private final int codigo;

    EstadoLectura(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static EstadoLectura desdeCodigo(int codigo) {
        for (EstadoLectura estado : EstadoLectura.values()) {
            if (estado.getCodigo() == codigo) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Código desconocido: " + codigo);
    }
}


