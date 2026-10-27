package es.joselyn.proyecto.modelo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Sector {

    private static final Logger log = LogManager.getLogger(Sector.class);

    private final String idSector;
    private final TipoSector tipoSector;
    private EstadoSector estadoSector;
    private final List<Subsistema> listaSubsistemas;

    public Sector(String idSector, TipoSector tipoSector, EstadoSector estadoSector) {
        this.idSector = idSector;
        this.tipoSector = tipoSector;
        this.estadoSector = estadoSector;
        this.listaSubsistemas = new ArrayList<>();

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

    public void agregarSubsistema(Subsistema subsistema) {
        if (!subsistema.getIdSector().equals(idSector)) {
            throw new IllegalArgumentException("El subsistema " + subsistema.getIdSubsistema() + " pertenece a " + subsistema.getIdSector() + ", no se puede añadir a " + idSector);
        }
        listaSubsistemas.add(subsistema);
        log.debug("Subsistema {} añadido a {}", subsistema.getIdSubsistema(), idSector);
    }

    public List<Subsistema> getListaSubsistemas() {
        return List.copyOf(listaSubsistemas);
    }

    @Override
    public String toString() {
        return "Sector[" + idSector + ", " + tipoSector + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sector sector = (Sector) o;
        return Objects.equals(idSector, sector.idSector);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idSector);
    }
}