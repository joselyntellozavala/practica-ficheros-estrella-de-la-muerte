package es.joselyn.proyecto.persistencia.binario;

import es.joselyn.proyecto.modelo.EstadoLectura;
import es.joselyn.proyecto.modelo.Lectura;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FicheroLecturaTest {

    @TempDir
    Path carpeta;

    @Test
    void anadeLeePorPosicionYModificaElEstado() throws IOException {
        FicheroLectura fichero = new FicheroLectura(carpeta.resolve("lecturas.bin"), 2);

        fichero.guardarLectura(new Lectura(1, 1, 10.0, EstadoLectura.NORMAL));
        fichero.guardarLectura(new Lectura(1, 2, 20.0, EstadoLectura.NORMAL));
        fichero.guardarLectura(new Lectura(2, 1, 30.0, EstadoLectura.NORMAL));
        fichero.guardarLectura(new Lectura(2, 2, 99.0, EstadoLectura.SOSPECHOSA));
        assertEquals(4, fichero.contarRegistros());

        assertEquals(new Lectura(2, 2, 99.0, EstadoLectura.SOSPECHOSA), fichero.obtenerLectura(2, 2));

        fichero.cambiarEstadoLectura(2, 2, EstadoLectura.ANOMALA);
        assertEquals(new Lectura(2, 2, 99.0, EstadoLectura.ANOMALA), fichero.obtenerLectura(2, 2));
        assertEquals(new Lectura(2, 1, 30.0, EstadoLectura.NORMAL), fichero.obtenerLectura(2, 1));
        assertEquals(4, fichero.contarRegistros());
    }
}
