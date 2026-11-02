package es.joselyn.proyecto.app;

import es.joselyn.proyecto.forense.AnalizadorForense;
import es.joselyn.proyecto.modelo.ResultadoAnalisis;
import es.joselyn.proyecto.persistencia.binario.EscritorLecturasAnomalas;
import es.joselyn.proyecto.persistencia.binario.EscritorInforme;
import es.joselyn.proyecto.persistencia.xml.EscritorAnalisisXml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AnalizadorApp {

    static {
        System.setProperty("programa", "analizador");
    }

    private static final Logger log = LogManager.getLogger(AnalizadorApp.class);

    private static final String CARPETA_DATOS = "datos";
    private static final String CARPETA_SALIDA = "salida";
    private static final String SALIDA_INFORME = "informe_forense.txt";
    private static final String SALIDA_LECTURAS = "lecturas_anomalas.bin";
    private static final String SALIDA_XML = "elementos_afectados.xml";

    public static void main(String[] args) {
        log.info("Analizador forense iniciado");

        Path carpetaDatos;
        if (args.length > 0) {
            carpetaDatos = Path.of(args[0]);
        } else {
            carpetaDatos = Path.of(CARPETA_DATOS);
        }

        Path carpetaSalida;
        if (args.length > 1) {
            carpetaSalida = Path.of(args[1]);
        } else {
            carpetaSalida = Path.of(CARPETA_SALIDA);
        }

        List<Path> faltan = ficherosQueFaltan(carpetaDatos);
        if (!faltan.isEmpty()) {
            log.error("No se puede analizar: faltan {} fichero(s) de datos: {}", faltan.size(), faltan);
            log.info("Analizador finalizado sin generar salidas");
            return;
        }

        try {
            ResultadoAnalisis resultado = new AnalizadorForense(carpetaDatos).analizar();

            Files.createDirectories(carpetaSalida);
            new EscritorInforme(carpetaSalida.resolve(SALIDA_INFORME)).escribir(resultado);
            new EscritorLecturasAnomalas(carpetaSalida.resolve(SALIDA_LECTURAS)).escribir(resultado.lecturasAnomalas());
            new EscritorAnalisisXml(carpetaSalida.resolve(SALIDA_XML)).escribir(resultado);

            log.info("Análisis completado: {}. Salidas en {}", resultado.veredicto(), carpetaSalida.toAbsolutePath());
        } catch (IOException e) {
            log.error("Error de entrada/salida durante el análisis", e);
        }
        log.info("Analizador finalizado");
    }


    private static List<Path> ficherosQueFaltan(Path carpetaDatos) {
        List<Path> faltan = new ArrayList<>();
        for (String nombre : List.of(AnalizadorForense.FICHERO_ESTRUCTURA, AnalizadorForense.FICHERO_LECTURAS,
                AnalizadorForense.FICHERO_HISTORICO)) {
            Path fichero = carpetaDatos.resolve(nombre);
            if (!Files.isRegularFile(fichero)) {
                faltan.add(fichero);
            }
        }
        return faltan;
    }
}