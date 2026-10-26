package es.joselyn.proyecto.modelo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Oficial {

    private static final Logger log = LogManager.getLogger(Oficial.class);

    private final String idOficial;
    private final String nombre;
    private final RangoOficial rango;
    private final String idSector;

    public Oficial(String idOficial, String nombre, RangoOficial rango, String idSector) {
        this.idOficial = idOficial;
        this.nombre = nombre;
        this.rango = rango;
        this.idSector = idSector;

        log.debug("Oficial creado: id={}, nombre={}, rango={}, sector={}", idOficial, nombre, rango, idSector);

    }

    public String getIdOficial() {
        return idOficial;
    }

    public String getNombre() {
        return nombre;
    }

    public RangoOficial getRango() {
        return rango;
    }

    public String getIdSector() {
        return idSector;
    }

    @Override
    public String toString() {
        return "Oficial[" + idOficial + ", " + nombre + ", " + rango + ", " + idSector + "]";
    }
}

