package es.joselyn.proyecto.app;

import es.joselyn.proyecto.excepcion.AnomaliaCriticaException;
import es.joselyn.proyecto.modelo.TipoIncidente;
import es.joselyn.proyecto.simulacion.Simulacion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class SimuladorApp {

    static {
        System.setProperty("programa", "simulador");
    }

    private static final Logger log = LogManager.getLogger(SimuladorApp.class);

    private static final Path CARPETA_DATOS = Path.of("datos");
    private static final String MODO_SIN_INCIDENTE = "SIN_INCIDENTE";

    public static void main(String[] args) {

        log.info("Simulador iniciado");
        Simulacion simulacion;
        try {
            simulacion = crearSimulacion(args);
        } catch (IllegalArgumentException e) {
            log.error("Argumento no válido: {}. Usa SIN_INCIDENTE o uno de estos incidentes: {}", args[0], Arrays.toString(TipoIncidente.values()));
            return;
        }

        try {
            simulacion.ejecutar();
        } catch (AnomaliaCriticaException e) {
            log.error("Evento crítico capturado en el ciclo {} (sensor {}): {}",
                    e.getCiclo(), e.getIdSensor(), e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error de entrada/salida en los ficheros de datos", e);
        }

        log.info("Simulador finalizado. Datos en: {}", CARPETA_DATOS.toAbsolutePath());
    }

    private static Simulacion crearSimulacion(String[] args) {
        Random random = new Random();
        if (args.length == 0) {
            return Simulacion.normal(CARPETA_DATOS, random);
        }
        if (args[0].equals(MODO_SIN_INCIDENTE)) {
            return Simulacion.sinIncidente(CARPETA_DATOS, random);
        }
        return Simulacion.conIncidente(CARPETA_DATOS, random, TipoIncidente.valueOf(args[0]));
    }
}