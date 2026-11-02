package es.joselyn.proyecto.simulacion;

import es.joselyn.proyecto.excepcion.AnomaliaCriticaException;
import es.joselyn.proyecto.modelo.TipoIncidente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SimulacionTest {

    @TempDir
    Path carpeta;

    @Test
    void elIncidenteForzadoLanzaLaExcepcionConLosDatosYaPersistidos() {
       
        Simulacion simulacion = Simulacion.conIncidente(carpeta, new Random(42), TipoIncidente.FUGA_AA23);

        AnomaliaCriticaException e = assertThrows(AnomaliaCriticaException.class, simulacion::ejecutar);

        assertEquals(3, e.getIdSensor());
        assertTrue(e.getCiclo() >= 3);
        // Cuando llega la excepción, los tres ficheros ya están en disco
        assertTrue(Files.exists(carpeta.resolve("lecturas.bin")));
        assertTrue(Files.exists(carpeta.resolve("historico_operaciones.txt")));
        assertTrue(Files.exists(carpeta.resolve("estructura_estacion.xml")));
    }
}
