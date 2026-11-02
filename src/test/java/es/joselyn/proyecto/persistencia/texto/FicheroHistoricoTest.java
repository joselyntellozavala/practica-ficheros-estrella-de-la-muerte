package es.joselyn.proyecto.persistencia.texto;

import es.joselyn.proyecto.modelo.Operacion;
import es.joselyn.proyecto.modelo.TipoOperacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FicheroHistoricoTest {

    @TempDir
    Path carpeta;

    @Test
    void escribeYRecuperaLasOperaciones() throws IOException {
        FicheroHistorico historico = new FicheroHistorico(carpeta.resolve("historico.txt"));
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 2, 10, 0, 0);
        Operacion orden = new Operacion(fecha, 1, "SEC-01", "SV-01", "OF-01", TipoOperacion.ORDEN, "Orden de rutina emitida");
        Operacion aviso = new Operacion(fecha.plusMinutes(10), 2, "SEC-02", "AA-23", "SISTEMA", TipoOperacion.AVISO, "Fallo de comunicación");

        historico.registrarOperacion(orden);
        historico.registrarOperacion(aviso);
        List<Operacion> leidas = historico.obtenerTodasOperaciones();

        assertEquals(List.of(orden, aviso), leidas);
    }
}
