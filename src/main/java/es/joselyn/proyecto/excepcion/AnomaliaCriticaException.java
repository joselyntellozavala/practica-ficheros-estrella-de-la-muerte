package es.joselyn.proyecto.excepcion;

public class AnomaliaCriticaException extends Exception {

    private final int ciclo;
    private final int idSensor;

    public AnomaliaCriticaException(String mensaje, int ciclo, int idSensor) {
        super(mensaje);
        this.ciclo = ciclo;
        this.idSensor = idSensor;
    }

    public int getCiclo() {
        return ciclo;
    }

    public int getIdSensor() {
        return idSensor;
    }
}
