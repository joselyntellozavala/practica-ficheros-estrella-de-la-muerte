package es.joselyn.proyecto.app;

import es.joselyn.proyecto.modelo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimuladorApp {

    static {
        System.setProperty("programa", "simulador");
    }

    private static final Logger log = LogManager.getLogger(SimuladorApp.class);

    private static final String NOMBRE_ESTACION = "Estrella de la muerte";

    public static void main(String[] args) {

        log.info("Simulación iniciada: {}", NOMBRE_ESTACION);

        Sector mando = new Sector("SEC-01", TipoSector.MANDO, EstadoSector.OPERATIVO);
        Sector detencion = new Sector("SEC-02", TipoSector.DETENCION, EstadoSector.OPERATIVO);
        Sector hangares = new Sector("SEC-03", TipoSector.HANGARES, EstadoSector.OPERATIVO);
        Sector nucleo = new Sector("SEC-04", TipoSector.NUCLEO, EstadoSector.OPERATIVO);
        Sector residuos = new Sector("SEC-05", TipoSector.RESIDUOS, EstadoSector.OPERATIVO);
        Sector superficie = new Sector("SEC-06", TipoSector.SUPERFICIE, EstadoSector.OPERATIVO);

        Subsistema soporteHangares = new Subsistema("SV-03", "Soporte vital hangares", "SEC-03", TipoSubsistema.SOPORTE_VITAL, EstadoSubsistema.OPERATIVO);
        hangares.agregarSubsistema(soporteHangares);
        Sensor oxigeno = new Sensor(1, "SV-03", TipoSensor.OXIGENO, 19.5, 23.5);
        soporteHangares.agregarSensor(oxigeno);

        Oficial tarkin = new Oficial("OF-01", "Wilhuff Tarkin", RangoOficial.GRAN_MOFF, "SEC-01");

        soporteHangares.setEstadoSubsistema(EstadoSubsistema.DEGRADADO);
        log.info("Simulación finalizada");
    }
}
