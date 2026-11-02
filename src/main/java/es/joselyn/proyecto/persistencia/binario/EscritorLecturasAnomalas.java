package es.joselyn.proyecto.persistencia.binario;

import es.joselyn.proyecto.modelo.Lectura;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class EscritorLecturasAnomalas {

    private static final Logger log = LogManager.getLogger(EscritorLecturasAnomalas.class);

    private final Path ruta;

    public EscritorLecturasAnomalas(Path ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("Tiene que existir una ruta");
        }
        this.ruta = ruta;
        log.info("Escritor de lecturas anómalas creado en: {}", ruta);
    }

    public void escribir(List<Lectura> lecturas) throws IOException {
        
        try (DataOutputStream salida = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(ruta)))) {
            for (Lectura lectura : lecturas) {
                salida.writeInt(lectura.ciclo());
                salida.writeInt(lectura.idSensor());
                salida.writeDouble(lectura.valor());
                salida.writeInt(lectura.estado().getCodigo());
                log.debug("Lectura anómala copiada: ciclo={}, sensor={}, valor={}", lectura.ciclo(), lectura.idSensor(), lectura.valor());
            }
        }
        log.info("{} lecturas anómalas escritas en {}", lecturas.size(), ruta);
    }
}