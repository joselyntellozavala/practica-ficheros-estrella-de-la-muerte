package es.joselyn.proyecto.forense;

import es.joselyn.proyecto.excepcion.AnomaliaCriticaException;
import es.joselyn.proyecto.modelo.ResultadoAnalisis;
import es.joselyn.proyecto.modelo.TipoIncidente;
import es.joselyn.proyecto.modelo.Veredicto;
import es.joselyn.proyecto.simulacion.Simulacion;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnalizadorForenseTest {

    @TempDir
    Path carpeta;

    @ParameterizedTest
    @EnumSource(TipoIncidente.class)
    void identificaCadaIncidente(TipoIncidente incidente) throws IOException {
        Simulacion simulacion = Simulacion.conIncidente(carpeta, new Random(7), incidente);
        assertThrows(AnomaliaCriticaException.class, simulacion::ejecutar);

        ResultadoAnalisis resultado = new AnalizadorForense(carpeta).analizar();

        assertEquals(Veredicto.IDENTIFICADO, resultado.veredicto());
        assertEquals(incidente, resultado.incidente());
    }
}
