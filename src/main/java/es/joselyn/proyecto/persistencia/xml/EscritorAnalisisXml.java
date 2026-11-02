package es.joselyn.proyecto.persistencia.xml;

import es.joselyn.proyecto.modelo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Path;

public class EscritorAnalisisXml {

    private static final Logger log = LogManager.getLogger(EscritorAnalisisXml.class);

    private final Path ruta;

    public EscritorAnalisisXml(Path ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("Tiene que existir una ruta");
        }
        this.ruta = ruta;
        log.info("Escritor del XML de análisis creado en: {}", ruta);
    }

    public void escribir(ResultadoAnalisis resultado) throws IOException {
        Document documento;
        try {
            documento = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IOException("No se pudo crear el documento XML", e);
        }

        Element raiz = documento.createElement(EtiquetasXml.ELEMENTO_ANALISIS);
        raiz.setAttribute(EtiquetasXml.ATRIBUTO_VEREDICTO, resultado.veredicto().name());
        if (resultado.incidente() != null) {
            raiz.setAttribute(EtiquetasXml.ATRIBUTO_INCIDENTE, resultado.incidente().name());
        }
        raiz.setAttribute(EtiquetasXml.ATRIBUTO_ULTIMO_CICLO, String.valueOf(resultado.ultimoCiclo()));
        documento.appendChild(raiz);

        if (resultado.sector() != null) {
            Element sector = crearElementoSector(documento, resultado.sector());
            raiz.appendChild(sector);

            if (resultado.subsistema() != null) {
                Element subsistema = crearElementoSubsistema(documento, resultado.subsistema());
                sector.appendChild(subsistema);
                if (resultado.sensor() != null) {
                    subsistema.appendChild(crearElementoSensor(documento, resultado.sensor()));
                }
            }
            if (resultado.oficialAlMando() != null) {
                sector.appendChild(crearElementoOficial(documento, resultado.oficialAlMando()));
            }
        }

        guardar(documento);
        log.info("XML de análisis escrito en {} (veredicto {})", ruta, resultado.veredicto());
    }

    private Element crearElementoSector(Document documento, Sector sector) {
        Element elemento = documento.createElement(EtiquetasXml.ELEMENTO_SECTOR);
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ID, sector.getIdSector());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_TIPO, sector.getTipoSector().name());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ESTADO, sector.getEstadoSector().name());
        return elemento;
    }

    private Element crearElementoSubsistema(Document documento, Subsistema subsistema) {
        Element elemento = documento.createElement(EtiquetasXml.ELEMENTO_SUBSISTEMA);
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ID, subsistema.getIdSubsistema());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_NOMBRE, subsistema.getNombre());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_TIPO, subsistema.getTipoSubsistema().name());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ESTADO, subsistema.getEstadoSubsistema().name());
        if (subsistema.getCicloUltimoCambio() != 0) {
            elemento.setAttribute(EtiquetasXml.ATRIBUTO_CICLO_ULTIMO_CAMBIO, String.valueOf(subsistema.getCicloUltimoCambio()));
        }
        return elemento;
    }

    private Element crearElementoSensor(Document documento, Sensor sensor) {
        Element elemento = documento.createElement(EtiquetasXml.ELEMENTO_SENSOR);
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ID, String.valueOf(sensor.getIdSensor()));
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_TIPO, sensor.getTipo().name());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_MIN, String.valueOf(sensor.getMinimoNormal()));
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_MAX, String.valueOf(sensor.getMaximoNormal()));
        return elemento;
    }

    private Element crearElementoOficial(Document documento, Oficial oficial) {
        Element elemento = documento.createElement(EtiquetasXml.ELEMENTO_OFICIAL_AL_MANDO);
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ID, oficial.getIdOficial());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_NOMBRE, oficial.getNombre());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_RANGO, oficial.getRango().name());
        return elemento;
    }

    private void guardar(Document documento) throws IOException {
        try {
            Transformer transformador = TransformerFactory.newInstance().newTransformer();
            transformador.setOutputProperty(OutputKeys.INDENT, "yes");
            transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformador.transform(new DOMSource(documento), new StreamResult(ruta.toFile()));
        } catch (TransformerException e) {
            throw new IOException("No se pudo guardar el XML en " + ruta, e);
        }
    }
}
