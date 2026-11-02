package es.joselyn.proyecto.app;

import es.joselyn.proyecto.excepcion.AnomaliaCriticaException;
import es.joselyn.proyecto.modelo.TipoIncidente;
import es.joselyn.proyecto.simulacion.Simulacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class IntegracionTest {

    @TempDir
    Path carpeta;

    @Test
    void simuladorAnalizadorYSalidas() throws IOException {
        Path datos = carpeta.resolve("datos");
        Path salida = carpeta.resolve("salida");

        // 1. Simulador
        Simulacion simulacion = Simulacion.conIncidente(datos, new Random(3), TipoIncidente.INTRUSION_HANGAR);
        assertThrows(AnomaliaCriticaException.class, simulacion::ejecutar);

        // 2. Analizador con sus argumentos [carpetaDatos] [carpetaSalida]
        AnalizadorApp.main(new String[]{datos.toString(), salida.toString()});

        // 3. Las tres salidas
        String informe = Files.readString(salida.resolve("informe_forense.txt"), StandardCharsets.UTF_8);
        assertTrue(informe.contains("VEREDICTO: IDENTIFICADO"));
        assertTrue(informe.contains(TipoIncidente.INTRUSION_HANGAR.getDescripcion()));

        // 3 lecturas ANOMALA × 20 bytes
        assertEquals(60, Files.size(salida.resolve("lecturas_anomalas.bin")));

        String xml = Files.readString(salida.resolve("elementos_afectados.xml"), StandardCharsets.UTF_8);
        assertTrue(xml.contains("INTRUSION_HANGAR"));
        assertTrue(xml.contains("HG-327"));
    }
}
