package es.joselyn.proyecto.persistencia.binario;

import es.joselyn.proyecto.modelo.ResultadoAnalisis;
import es.joselyn.proyecto.modelo.Veredicto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EscritorInforme {

    private static final Logger log = LogManager.getLogger(EscritorInforme.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path ruta;

    public EscritorInforme(Path ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("Tiene que existir una ruta");
        }
        this.ruta = ruta;
        log.info("Escritor del informe creado en: {}", ruta);
    }

    public void escribir(ResultadoAnalisis resultado) throws IOException {

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ruta.toFile(), StandardCharsets.UTF_8, false))) {
            escribirLinea(escritor, "INFORME FORENSE - ESTRELLA DE LA MUERTE");
            escribirLinea(escritor, "Destinatario: Lord Vader");
            escribirLinea(escritor, "Fecha del análisis: " + LocalDateTime.now().format(FORMATO_FECHA));
            escribirLinea(escritor, "Ciclos analizados: " + resultado.ultimoCiclo());
            escritor.newLine();

            escribirLinea(escritor, "VEREDICTO: " + resultado.veredicto());
            if (resultado.veredicto() == Veredicto.IDENTIFICADO) {
                escribirLinea(escritor, "Incidente: " + resultado.incidente().getDescripcion());
            }
            escribirLinea(escritor, "Motivo: " + resultado.motivo());
            escritor.newLine();

            escribirLinea(escritor, "EFECTOS DETECTADOS");
            if (resultado.efectos().isEmpty()) {
                escribirLinea(escritor, "- Ninguno");
            }
            for (String efecto : resultado.efectos()) {
                escribirLinea(escritor, "- " + efecto);
            }
            escritor.newLine();

            escribirLinea(escritor, "CONCLUSIÓN");
            escribirLinea(escritor, resultado.conclusion());
        }
        log.info("Informe escrito en {} (veredicto {})", ruta, resultado.veredicto());
    }

    private void escribirLinea(BufferedWriter escritor, String linea) throws IOException {
        escritor.write(linea);
        escritor.newLine();
    }
}