package es.joselyn.proyecto.persistencia.xml;

import es.joselyn.proyecto.modelo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstacionXmlTest {

    @TempDir
    Path carpeta;

    @Test
    void escribeConDomYLeeConSax() throws IOException {
        Path ruta = carpeta.resolve("estructura.xml");
        EstacionImperial estacion = new EstacionImperial("Estación de prueba");
        Sector sector = new Sector("SEC-02", TipoSector.DETENCION, EstadoSector.OPERATIVO);
        Subsistema bloque = new Subsistema("AA-23", "Bloque AA-23", "SEC-02", TipoSubsistema.BLOQUE_DETENCION, EstadoSubsistema.OPERATIVO);
        bloque.agregarSensor(new Sensor(1, "AA-23", TipoSensor.CELDAS_ABIERTAS, 0, 0));
        sector.agregarSubsistema(bloque);
        estacion.agregarSector(sector);
        estacion.agregarOficial(new Oficial("OF-01", "Wullf Yularen", RangoOficial.CORONEL, "SEC-02"));

        // DOM: crear el documento y modificar nodos como hace el simulador
        EscritorEstacionXml escritor = new EscritorEstacionXml(ruta);
        escritor.crearDocumento(estacion);
        bloque.cambiarEstadoSubsistema(EstadoSubsistema.FUERA_DE_SERVICIO, 4);
        escritor.actualizarSubsistema(bloque);
        sector.setEstadoSector(EstadoSector.ALERTA);
        escritor.actualizarSector(sector);
        escritor.actualizarUltimoCiclo(4);

        // SAX: reconstruir la estación solo a partir del fichero
        LectorEstacionXml lector = new LectorEstacionXml(ruta);
        lector.leer();
        EstacionImperial leida = lector.getEstacion();
        Sector sectorLeido = leida.getListaSectores().get(0);
        Subsistema bloqueLeido = sectorLeido.getListaSubsistemas().get(0);
        Sensor sensorLeido = bloqueLeido.getListaSensores().get(0);

        assertEquals(4, lector.getUltimoCiclo());
        assertEquals(EstadoSector.ALERTA, sectorLeido.getEstadoSector());
        assertEquals(EstadoSubsistema.FUERA_DE_SERVICIO, bloqueLeido.getEstadoSubsistema());
        assertEquals(4, bloqueLeido.getCicloUltimoCambio());
        assertEquals(1, sensorLeido.getIdSensor());
        assertEquals("OF-01", leida.getListaOficiales().get(0).getIdOficial());
    }
}
