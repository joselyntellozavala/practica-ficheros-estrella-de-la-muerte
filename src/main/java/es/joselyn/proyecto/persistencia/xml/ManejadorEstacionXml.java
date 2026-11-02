package es.joselyn.proyecto.persistencia.xml;

import es.joselyn.proyecto.modelo.*;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class ManejadorEstacionXml extends DefaultHandler {

    private Locator locator;
    private EstacionImperial estacion;
    private int ultimoCiclo;

    private Sector sectorActual;
    private Subsistema subsistemaActual;

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atributos) throws SAXException {
        try {
            switch (qName) {
                case EtiquetasXml.ELEMENTO_ESTACION -> leerEstacion(atributos);
                case EtiquetasXml.ELEMENTO_SECTOR -> leerSector(atributos);
                case EtiquetasXml.ELEMENTO_SUBSISTEMA -> leerSubsistema(atributos);
                case EtiquetasXml.ELEMENTO_SENSOR -> leerSensor(atributos);
                case EtiquetasXml.ELEMENTO_OFICIAL -> leerOficial(atributos);
                default -> {
                }
            }
        } catch (IllegalArgumentException e) {
            throw new SAXParseException("Valor no válido en <" + qName + ">: " + e.getMessage(), locator, e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals(EtiquetasXml.ELEMENTO_SECTOR)) {
            sectorActual = null;
        } else if (qName.equals(EtiquetasXml.ELEMENTO_SUBSISTEMA)) {
            subsistemaActual = null;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (estacion == null) {
            throw new SAXException("El documento no contiene el elemento <" + EtiquetasXml.ELEMENTO_ESTACION + ">");
        }
    }

    private void leerEstacion(Attributes atributos) throws SAXException {
        estacion = new EstacionImperial(obligatorio(atributos, EtiquetasXml.ATRIBUTO_NOMBRE));
        ultimoCiclo = Integer.parseInt(obligatorio(atributos, EtiquetasXml.ATRIBUTO_ULTIMO_CICLO));
        if (ultimoCiclo < 0) {
            throw new IllegalArgumentException("ultimoCiclo negativo: " + ultimoCiclo);
        }
    }

    private void leerSector(Attributes atributos) throws SAXException {
        exigirPadre(estacion, EtiquetasXml.ELEMENTO_SECTOR, EtiquetasXml.ELEMENTO_ESTACION);
        sectorActual = new Sector(
                obligatorio(atributos, EtiquetasXml.ATRIBUTO_ID),
                TipoSector.valueOf(obligatorio(atributos, EtiquetasXml.ATRIBUTO_TIPO)),
                EstadoSector.valueOf(obligatorio(atributos, EtiquetasXml.ATRIBUTO_ESTADO)));
        estacion.agregarSector(sectorActual);
    }

    private void leerSubsistema(Attributes atributos) throws SAXException {
        exigirPadre(sectorActual, EtiquetasXml.ELEMENTO_SUBSISTEMA, EtiquetasXml.ELEMENTO_SECTOR);
        String ciclo = atributos.getValue(EtiquetasXml.ATRIBUTO_CICLO_ULTIMO_CAMBIO);
        int cicloUltimoCambio = (ciclo == null) ? 0 : Integer.parseInt(ciclo);

        subsistemaActual = new Subsistema(
                obligatorio(atributos, EtiquetasXml.ATRIBUTO_ID),
                obligatorio(atributos, EtiquetasXml.ATRIBUTO_NOMBRE),
                sectorActual.getIdSector(), // el padre lo sabemos por el anidamiento, no por un atributo
                TipoSubsistema.valueOf(obligatorio(atributos, EtiquetasXml.ATRIBUTO_TIPO)),
                EstadoSubsistema.valueOf(obligatorio(atributos, EtiquetasXml.ATRIBUTO_ESTADO)),
                cicloUltimoCambio);
        sectorActual.agregarSubsistema(subsistemaActual);
    }

    private void leerSensor(Attributes atributos) throws SAXException {
        exigirPadre(subsistemaActual, EtiquetasXml.ELEMENTO_SENSOR, EtiquetasXml.ELEMENTO_SUBSISTEMA);
        Sensor sensor = new Sensor(
                Integer.parseInt(obligatorio(atributos, EtiquetasXml.ATRIBUTO_ID)),
                subsistemaActual.getIdSubsistema(),
                TipoSensor.valueOf(obligatorio(atributos, EtiquetasXml.ATRIBUTO_TIPO)),
                Double.parseDouble(obligatorio(atributos, EtiquetasXml.ATRIBUTO_MIN)),
                Double.parseDouble(obligatorio(atributos, EtiquetasXml.ATRIBUTO_MAX)));
        subsistemaActual.agregarSensor(sensor);
    }

    private void leerOficial(Attributes atributos) throws SAXException {
        exigirPadre(estacion, EtiquetasXml.ELEMENTO_OFICIAL, EtiquetasXml.ELEMENTO_ESTACION);
        estacion.agregarOficial(new Oficial(
                obligatorio(atributos, EtiquetasXml.ATRIBUTO_ID),
                obligatorio(atributos, EtiquetasXml.ATRIBUTO_NOMBRE),
                RangoOficial.valueOf(obligatorio(atributos, EtiquetasXml.ATRIBUTO_RANGO)),
                obligatorio(atributos, EtiquetasXml.ATRIBUTO_SECTOR)));
    }

    private String obligatorio(Attributes atributos, String nombre) throws SAXParseException {
        String valor = atributos.getValue(nombre);
        if (valor == null) {
            throw new SAXParseException("Falta el atributo obligatorio '" + nombre + "'", locator);
        }
        return valor;
    }

    private void exigirPadre(Object padre, String hijo, String nombrePadre) throws SAXParseException {
        if (padre == null) {
            throw new SAXParseException("<" + hijo + "> aparece fuera de <" + nombrePadre + ">", locator);
        }
    }

    EstacionImperial getEstacion() {
        return estacion;
    }

    int getUltimoCiclo() {
        return ultimoCiclo;
    }

}