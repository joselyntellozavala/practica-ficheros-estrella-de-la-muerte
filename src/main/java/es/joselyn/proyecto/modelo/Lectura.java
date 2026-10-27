package es.joselyn.proyecto.modelo;

public record Lectura(int ciclo, int idSensor, double valor, EstadoLectura estado) {
    public Lectura {
        if (ciclo <= 0) {
            throw new IllegalArgumentException("Valor inválido para el ciclo: " + ciclo);
        }
        if (idSensor <= 0) {
            throw new IllegalArgumentException("Valor inválido para el id del sensor: " + idSensor);
        }
        if (estado == null) {
            throw new IllegalArgumentException("Necesitas asignarle un estado a la lectura");
        }
    }
}
