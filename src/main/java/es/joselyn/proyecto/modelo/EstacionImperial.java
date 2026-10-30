package es.joselyn.proyecto.modelo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EstacionImperial {

    private static final Logger log = LogManager.getLogger(EstacionImperial.class);

    private final String nombre;
    private final List<Sector> listaSectores;
    private final List<Oficial> listaOficiales;


    public EstacionImperial(String nombre) {
        this.nombre = nombre;
        this.listaSectores = new ArrayList<>();
        this.listaOficiales = new ArrayList<>();

        log.info("Estación Imperial creada: nombre={}", nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public void agregarSector(Sector sector) {
        if (listaSectores.contains(sector)) {
            throw new IllegalArgumentException("El sector " + sector.getIdSector() + " ya existe en la estación");
        }
        listaSectores.add(sector);
        log.info("Sector {} añadido a la estación", sector.getIdSector());
    }

    public void agregarOficial(Oficial oficial) {
        if (listaOficiales.contains(oficial)) {
            throw new IllegalArgumentException("El oficial " + oficial.getIdOficial() + " ya existe en la estación");
        }
        boolean existe = false;
        for (Sector sectorLista : listaSectores) {
            existe = sectorLista.getIdSector().equals(oficial.getIdSector());
            if(existe){
                break;
            }
        }
        if (!existe) {
            throw new IllegalArgumentException("El sector " + oficial.getIdSector() + " no existe");
        }
        listaOficiales.add(oficial);
        log.info("Oficial {} añadido a la estación en {}", oficial.getIdOficial(), oficial.getIdSector());
    }

    public List<Sector> getListaSectores() {
        return List.copyOf(listaSectores);
    }

    public List<Oficial> getListaOficiales() {
        return List.copyOf(listaOficiales);
    }

    @Override
    public String toString() {
        return "EstacionImperial[" + nombre + ", " + listaSectores.size() + " sectores, " + listaOficiales.size() + " oficiales]";
    }
}
