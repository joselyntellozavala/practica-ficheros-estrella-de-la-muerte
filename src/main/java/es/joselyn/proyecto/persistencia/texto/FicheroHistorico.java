package es.joselyn.proyecto.persistencia.texto;

import es.joselyn.proyecto.modelo.Operacion;
import es.joselyn.proyecto.modelo.TipoOperacion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FicheroHistorico {

    private static final Logger log = LogManager.getLogger(FicheroHistorico.class);
    private final Path ruta;
    private static final String SEPARADOR = "|";
    private static final String CABECERA = "# fecha_hora | ciclo | sector | subsistema | credencial | tipo | mensaje";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FicheroHistorico(Path ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("Tiene que existir una ruta");
        }
        this.ruta = ruta;
        log.info("Fichero histórico de la estación creado en: {}", ruta);
    }

    public void registrarOperacion(Operacion operacion) throws IOException {
        boolean existe = Files.exists(ruta);
        String lineaOperacion = String.join(SEPARADOR,
                operacion.fechaHora().format(FORMATO_FECHA),
                String.valueOf(operacion.ciclo()),
                operacion.sector(),
                operacion.subsistema(),
                operacion.credencial(),
                operacion.tipo().name(),
                operacion.mensaje());
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ruta.toFile(), StandardCharsets.UTF_8, true))) {
            if (!existe) {
                escritor.write(CABECERA);
                escritor.newLine();
            }
            escritor.write(lineaOperacion);
            escritor.newLine();
        }
        log.debug("Operación registrada: ciclo{}, tipo={}", operacion.ciclo(), operacion.tipo().name());
    }

    public List<Operacion> obtenerTodasOperaciones() throws IOException {
        List<Operacion> listaOperaciones = new ArrayList<>();
        int numeroLinea = 0;
        String linea;
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta.toFile(), StandardCharsets.UTF_8))) {
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank() || linea.startsWith("#")) {
                    continue;
                }
                listaOperaciones.add(convertirLinea(linea, numeroLinea));
            }
        }
        log.info("Total de operaciones: " + listaOperaciones.size());
        return listaOperaciones;
    }

    private Operacion convertirLinea(String linea, int numeroLinea) throws IOException {
        String[] arrayDatos = linea.split("\\|");
        if (arrayDatos.length != 7) {
            throw new IOException("El array tiene " + arrayDatos.length + " posiciones");
        }
        try {
            LocalDateTime fechaHora = LocalDateTime.parse(arrayDatos[0].trim(), FORMATO_FECHA);
            int ciclo = Integer.parseInt(arrayDatos[1].trim());
            String sector = arrayDatos[2].trim();
            String subsistema = arrayDatos[3].trim();
            String credencial = arrayDatos[4].trim();
            TipoOperacion tipo = TipoOperacion.valueOf(arrayDatos[5].trim());
            String mensaje = arrayDatos[6].trim();
            Operacion operacion = new Operacion(fechaHora, ciclo, sector, subsistema, credencial, tipo, mensaje);
            return operacion;
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new IOException("Línea " + numeroLinea + " con formato incorrecto: " + linea, e);
        }
    }
}
