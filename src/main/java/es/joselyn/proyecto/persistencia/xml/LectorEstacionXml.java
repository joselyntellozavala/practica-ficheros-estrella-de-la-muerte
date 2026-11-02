package es.joselyn.proyecto.persistencia.xml;

import es.joselyn.proyecto.modelo.EstacionImperial;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.Path;

public class LectorEstacionXml {

    private static final Logger log = LogManager.getLogger(LectorEstacionXml.class);
    private final Path ruta;
    private EstacionImperial estacion;
    private int ultimoCiclo;

    public LectorEstacionXml(Path ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("Tiene que existir una ruta");
        }
        this.ruta = ruta;
        log.info("Lector de la estación creado para: {}", ruta);
    }

    public void leer() throws IOException {
        ManejadorEstacionXml manejador = new ManejadorEstacionXml();
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(ruta.toFile(), manejador);
        } catch (SAXParseException e) {
            throw new IOException("Error en " + ruta + ", línea " + e.getLineNumber() + ": " + e.getMessage(), e);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException("No se pudo leer el XML " + ruta + ": " + e.getMessage(), e);
        }
        estacion = manejador.getEstacion();
        ultimoCiclo = manejador.getUltimoCiclo();
        log.info("XML leído: {} sectores, {} oficiales, último ciclo {}", estacion.getListaSectores().size(), estacion.getListaOficiales().size(), ultimoCiclo);
    }

    public EstacionImperial getEstacion() {
        comprobarLeido();
        return estacion;
    }

    public int getUltimoCiclo() {
        comprobarLeido();
        return ultimoCiclo;
    }

    private void comprobarLeido() {
        if (estacion == null) {
            throw new IllegalStateException("El XML no se ha leído todavía: llama antes a leer");
        }
    }

}
