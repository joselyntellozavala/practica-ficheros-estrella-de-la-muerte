package es.joselyn.proyecto.simulacion;

import es.joselyn.proyecto.modelo.EstadoSector;
import es.joselyn.proyecto.modelo.EstadoSubsistema;
import es.joselyn.proyecto.modelo.TipoOperacion;

public record Huella(int idSensor, double valorAnomalo, TipoOperacion tipoOperacion, String credencial, String sintoma,
                     EstadoSubsistema estadoSubsistema,
                     EstadoSector estadoSector) {
    public Huella {
        if (idSensor <= 0) {
            throw new IllegalArgumentException("El id del sensor tiene que ser mayor a cero");
        }

        if (tipoOperacion == null) {
            throw new IllegalArgumentException("Necesitas asignar un tipo de operación");
        }
        if (credencial == null) {
            throw new IllegalArgumentException("Necesitas asignar una credencial");
        }
        if (sintoma == null) {
            throw new IllegalArgumentException("Necesitas asignar un síntoma");
        }
        if (estadoSubsistema == null) {
            throw new IllegalArgumentException("Necesitas asignar un estado de subsistema");
        }
        if (estadoSector == null) {
            throw new IllegalArgumentException("Necesitas asignar un estado de sector");
        }
    }
}
