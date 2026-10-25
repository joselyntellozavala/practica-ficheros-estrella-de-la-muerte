package es.joselyn.proyecto.modelo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Sensor {

    private static final Logger log = LogManager.getLogger(Sensor.class);

    private final int idSensor;
    private final String idSubsistema;
    private final TipoSensor tipo;
    private final double minimoNormal;
    private final double maximoNormal;

    public Sensor(int idSensor, String idSubsistema, TipoSensor tipo,
                  double minimoNormal, double maximoNormal) {

        if (idSensor <= 0) {
            throw new IllegalArgumentException("El id del sensor debe ser positivo: " + idSensor);
        }
        if (minimoNormal > maximoNormal) {
            throw new IllegalArgumentException(
                    "Rango inválido en sensor " + idSensor + ": " + minimoNormal + " > " + maximoNormal);
        }

        this.idSensor = idSensor;
        this.idSubsistema = idSubsistema;
        this.tipo = tipo;
        this.minimoNormal = minimoNormal;
        this.maximoNormal = maximoNormal;

        log.debug("Sensor creado: id={}, subsistema={}, tipo={}, rango=[{}, {}] {}",
                idSensor, idSubsistema, tipo, minimoNormal, maximoNormal, tipo.getUnidad());
    }

    public boolean estaFueraDeRango(double valor) {
        return valor < minimoNormal || valor > maximoNormal;
    }

    public int getIdSensor() {
        return idSensor;
    }

    public String getIdSubsistema() {
        return idSubsistema;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public double getMinimoNormal() {
        return minimoNormal;
    }

    public double getMaximoNormal() {
        return maximoNormal;
    }

    @Override
    public String toString() {
        return "Sensor[" + idSensor + ", " + idSubsistema + ", " + tipo + "]";
    }
}