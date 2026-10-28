package es.joselyn.proyecto.modelo;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Subsistema {
    private static final Logger log = LogManager.getLogger(Subsistema.class);
    private final String idSubsistema;
    private final String nombre;
    private final String idSector;
    private final List<Sensor> listaSensores;
    private final TipoSubsistema tipoSubsistema;
    private EstadoSubsistema estadoSubsistema;

    public Subsistema(String idSubsistema, String nombre, String idSector, TipoSubsistema tipoSubsistema, EstadoSubsistema estadoSubsistema) {
        this.idSubsistema = idSubsistema;
        this.nombre = nombre;
        this.idSector = idSector;
        this.listaSensores = new ArrayList<>();
        this.tipoSubsistema = tipoSubsistema;
        this.estadoSubsistema = estadoSubsistema;

        log.debug("Subsistema creado: id={}, nombre={}, sector={}, tipo={}, estado={}", idSubsistema, nombre, idSector, tipoSubsistema, estadoSubsistema);
    }

    public String getIdSubsistema() {
        return idSubsistema;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIdSector() {
        return idSector;
    }

    public void agregarSensor(Sensor sensor) {
        if (!sensor.getIdSubsistema().equals(idSubsistema)) {
            throw new IllegalArgumentException("El sensor " + sensor.getIdSensor() + " pertenece a " + sensor.getIdSubsistema() + ", no se puede añadir a " + idSubsistema);
        }
        listaSensores.add(sensor);
        log.debug("Sensor {} añadido a {}", sensor.getIdSensor(), idSubsistema);
    }

    public List<Sensor> getListaSensores() {
        return List.copyOf(listaSensores);
    }

    public TipoSubsistema getTipoSubsistema() {
        return tipoSubsistema;
    }

    public EstadoSubsistema getEstadoSubsistema() {
        return estadoSubsistema;
    }

    public void setEstadoSubsistema(EstadoSubsistema nuevoEstado) {
        EstadoSubsistema estadoAnterior = this.estadoSubsistema;
        this.estadoSubsistema = nuevoEstado;
        log.debug("Subsistema {} cambia de estado: {} -> {}", idSubsistema, estadoAnterior, nuevoEstado);
    }

    @Override
    public String toString() {
        return "Subsistema[" + idSubsistema + ", " + nombre + "]";
    }
}
