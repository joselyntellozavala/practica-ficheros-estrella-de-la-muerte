package es.joselyn.proyecto.modelo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Sector {

    private static final Logger log = LogManager.getLogger(Sector.class);

    private final String idSector;
    private final TipoSector tipoSector;
    private EstadoSector estadoSector;

    public Sector(String idSector, TipoSector tipoSector, EstadoSector estadoSector) {
        this.idSector = idSector;
        this.tipoSector = tipoSector;
        this.estadoSector = estadoSector;

        log.debug("Sector creado: id={}, tipo={}, estado={}", idSector, tipoSector, estadoSector);
    }

    public String getIdSector() {
        return idSector;
    }

    public TipoSector getTipoSector() {
        return tipoSector;
    }

    public EstadoSector getEstadoSector() {
        return estadoSector;
    }

    public void setEstadoSector(EstadoSector nuevoEstado) {
        EstadoSector anterior = this.estadoSector;
        this.estadoSector = nuevoEstado;

        log.debug("Sector {} cambia de estado: {} -> {}", idSector, anterior, nuevoEstado);
    }
}