package es.joselyn.proyecto.persistencia.xml;

import es.joselyn.proyecto.modelo.*;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class EscritorEstacionXml {
    private static final Logger log = LogManager.getLogger(EscritorEstacionXml.class);
    private final Path ruta;
    private Document documento;

    public EscritorEstacionXml(Path ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("Tiene que existir una ruta");
        }
        this.ruta = ruta;
        log.info("Escritor de la estación creado en: {}", ruta);
    }

    public void crearDocumento(EstacionImperial estacionImperial) throws IOException {
        try {
            DocumentBuilder constructor = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            documento = constructor.newDocument();
        } catch (ParserConfigurationException e) {
            throw new IOException("No se pudo crear el documento XML", e);
        }
        Element raiz = documento.createElement(EtiquetasXml.ELEMENTO_ESTACION);
        raiz.setAttribute(EtiquetasXml.ATRIBUTO_NOMBRE, estacionImperial.getNombre());
        raiz.setAttribute(EtiquetasXml.ATRIBUTO_ULTIMO_CICLO, "0");
        documento.appendChild(raiz);

        Element sectores = documento.createElement(EtiquetasXml.ELEMENTO_SECTORES);
        raiz.appendChild(sectores);

        for (Sector sectorLista : estacionImperial.getListaSectores()) {
            sectores.appendChild(crearElementoSector(sectorLista));
        }

        Element oficiales = documento.createElement(EtiquetasXml.ELEMENTO_OFICIALES);
        raiz.appendChild(oficiales);

        for (Oficial oficialLista : estacionImperial.getListaOficiales()) {
            oficiales.appendChild(crearElementoOficial(oficialLista));
        }

        log.info("Estructura XML creada: {} sectores, {} oficiales", estacionImperial.getListaSectores().size(), estacionImperial.getListaOficiales().size());
        guardar();
    }

    private Element crearElementoSector(Sector sector) {
        Element elementoSector = documento.createElement(EtiquetasXml.ELEMENTO_SECTOR);
        elementoSector.setAttribute(EtiquetasXml.ATRIBUTO_ID, sector.getIdSector());
        elementoSector.setAttribute(EtiquetasXml.ATRIBUTO_TIPO, sector.getTipoSector().name());
        elementoSector.setAttribute(EtiquetasXml.ATRIBUTO_ESTADO, sector.getEstadoSector().name());

        for (Subsistema subsistemaLista : sector.getListaSubsistemas()) {
            elementoSector.appendChild(crearElementoSubsistema(subsistemaLista));
        }
        return elementoSector;
    }

    private Element crearElementoSubsistema(Subsistema subsistema) {
        Element elementoSubsistema = documento.createElement(EtiquetasXml.ELEMENTO_SUBSISTEMA);
        elementoSubsistema.setAttribute(EtiquetasXml.ATRIBUTO_ID, subsistema.getIdSubsistema());
        elementoSubsistema.setAttribute(EtiquetasXml.ATRIBUTO_NOMBRE, subsistema.getNombre());
        elementoSubsistema.setAttribute(EtiquetasXml.ATRIBUTO_TIPO, subsistema.getTipoSubsistema().name());
        elementoSubsistema.setAttribute(EtiquetasXml.ATRIBUTO_ESTADO, subsistema.getEstadoSubsistema().name());
        if (subsistema.getCicloUltimoCambio() != 0) {
            elementoSubsistema.setAttribute(EtiquetasXml.ATRIBUTO_CICLO_ULTIMO_CAMBIO, String.valueOf(subsistema.getCicloUltimoCambio()));
        }
        for (Sensor sensoresLista : subsistema.getListaSensores()) {
            elementoSubsistema.appendChild(crearElementoSensor(sensoresLista));
        }
        return elementoSubsistema;
    }

    private Element crearElementoSensor(Sensor sensor) {
        Element elementoSensor = documento.createElement(EtiquetasXml.ELEMENTO_SENSOR);
        elementoSensor.setAttribute(EtiquetasXml.ATRIBUTO_ID, String.valueOf(sensor.getIdSensor()));
        elementoSensor.setAttribute(EtiquetasXml.ATRIBUTO_TIPO, sensor.getTipo().name());
        elementoSensor.setAttribute(EtiquetasXml.ATRIBUTO_MIN, String.valueOf(sensor.getMinimoNormal()));
        elementoSensor.setAttribute(EtiquetasXml.ATRIBUTO_MAX, String.valueOf(sensor.getMaximoNormal()));
        return elementoSensor;
    }

    private Element crearElementoOficial(Oficial oficial) {
        Element elementoOficial = documento.createElement(EtiquetasXml.ELEMENTO_OFICIAL);
        elementoOficial.setAttribute(EtiquetasXml.ATRIBUTO_ID, oficial.getIdOficial());
        elementoOficial.setAttribute(EtiquetasXml.ATRIBUTO_NOMBRE, oficial.getNombre());
        elementoOficial.setAttribute(EtiquetasXml.ATRIBUTO_RANGO, oficial.getRango().name());
        elementoOficial.setAttribute(EtiquetasXml.ATRIBUTO_SECTOR, oficial.getIdSector());
        return elementoOficial;
    }

    private void guardar() throws IOException {
        try {
            Transformer transformador = TransformerFactory.newInstance().newTransformer();
            transformador.setOutputProperty(OutputKeys.INDENT, "yes");
            transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformador.transform(new DOMSource(documento), new StreamResult(ruta.toFile()));
        } catch (TransformerException e) {
            throw new IOException("No se pudo guardar el XML en " + ruta, e);
        }
        log.info("XML guardado en {}", ruta);
    }

    private Element buscarElementoPorId(String etiqueta, String id) {
        NodeList nodos = documento.getElementsByTagName(etiqueta);
        for (int i = 0; i < nodos.getLength(); i++) {
            Element elemento = (Element) nodos.item(i);
            if (elemento.getAttribute(EtiquetasXml.ATRIBUTO_ID).equals(id)) {
                return elemento;
            }
        }
        throw new IllegalArgumentException("La etiqueta " + etiqueta + " y el id " + id + " no existen");
    }

    public void actualizarSubsistema(Subsistema subsistema) throws IOException {
        if (documento == null) {
            throw new IllegalStateException("El documento XML no se ha creado todavía: llama antes a crearDocumento");
        }
        Element elemento = buscarElementoPorId(EtiquetasXml.ELEMENTO_SUBSISTEMA, subsistema.getIdSubsistema());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ESTADO, subsistema.getEstadoSubsistema().name());
        if (subsistema.getCicloUltimoCambio() != 0) {
            elemento.setAttribute(EtiquetasXml.ATRIBUTO_CICLO_ULTIMO_CAMBIO, String.valueOf(subsistema.getCicloUltimoCambio()));
        }
        log.info("Subsistema actualizado: id={}, estado={}, ciclo={}", subsistema.getIdSubsistema(), subsistema.getEstadoSubsistema(), subsistema.getCicloUltimoCambio());
        guardar();
    }

    public void actualizarSector(Sector sector) throws IOException {
        if (documento == null) {
            throw new IllegalStateException("El documento XML no se ha creado todavía: llama antes a crearDocumento");
        }
        Element elemento = buscarElementoPorId(EtiquetasXml.ELEMENTO_SECTOR, sector.getIdSector());
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ESTADO, sector.getEstadoSector().name());
        log.info("Sector actualizado: id={}, estado={}", sector.getIdSector(), sector.getEstadoSector());
        guardar();
    }

    public void actualizarUltimoCiclo(int ciclo) throws IOException {
        if (documento == null) {
            throw new IllegalStateException("El documento XML no se ha creado todavía: llama antes a crearDocumento");
        }
        if (ciclo < 1) {
            throw new IllegalArgumentException("Ciclo no válido: " + ciclo + ". Debe ser mayor o igual que 1");
        }
        Element elemento = documento.getDocumentElement();
        elemento.setAttribute(EtiquetasXml.ATRIBUTO_ULTIMO_CICLO, String.valueOf(ciclo));
        log.info("Último ciclo actualizado: {}", ciclo);
        guardar();
    }

}
