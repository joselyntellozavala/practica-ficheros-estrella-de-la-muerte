package es.joselyn.proyecto.persistencia.binario;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.joselyn.proyecto.modelo.EstadoLectura;
import es.joselyn.proyecto.modelo.Lectura;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FicheroLectura {

    private static final int TAMANO_REGISTRO = 20;
    private static final int DESPLAZAMIENTO_ESTADO = 16;
    private static final Logger log = LogManager.getLogger(FicheroLectura.class);

    private final Path ruta;
    private final int numeroSensores;

    public FicheroLectura(Path ruta, int numeroSensores) {
        if (ruta == null) {
            throw new IllegalArgumentException("Tiene que asignarle una ruta");
        }
        if (numeroSensores < 1) {
            throw new IllegalArgumentException("El número de sensores debe ser mayor a 0. No es válido: " + numeroSensores);
        }
        this.ruta = ruta;
        this.numeroSensores = numeroSensores;
        log.debug("Fichero de mediciones preparado: {} con {} sensores", ruta, numeroSensores);
    }

    public void guardarLectura(Lectura lectura) throws IOException {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(ruta.toFile(), "rw")) {
            long posicionEsperada = posicion(lectura.ciclo(), lectura.idSensor());
            if (posicionEsperada != randomAccessFile.length()) {
                throw new IllegalStateException("Lectura fuera de orden (ciclo=" + lectura.ciclo() + ", sensor= " + lectura.idSensor() + "): posición esperada " + posicionEsperada + ", tamaño del fichero " + randomAccessFile.length());
            }
            randomAccessFile.seek(posicionEsperada);
            escribirRegistro(randomAccessFile, lectura);
            log.trace("Lectura guardada: ciclo={}, sensor={}, posición={}", lectura.ciclo(), lectura.idSensor(), posicionEsperada);
        }
    }

    public Lectura obtenerLectura(int ciclo, int idSensor) throws IOException {
        Lectura lectura;
        long posicionBuscada;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(ruta.toFile(), "r")) {
            posicionBuscada = posicion(ciclo, idSensor);
            if (posicionBuscada + TAMANO_REGISTRO > randomAccessFile.length()) {
                throw new IllegalArgumentException("No existe la lectura del ciclo " + ciclo + ", sensor " + idSensor);
            }
            randomAccessFile.seek(posicionBuscada);
            lectura = leerRegistro(randomAccessFile);
        }
        log.trace("Lectura obtenida: ciclo={}, sensor={}, posicion={}, estado={}", ciclo, idSensor, posicionBuscada, lectura.estado());
        return lectura;
    }

    public void cambiarEstadoLectura(int ciclo, int idSensor, EstadoLectura estadoLectura) throws IOException {
        long posicionDeseada;
        if (estadoLectura == null) {
            throw new IllegalArgumentException("El estado de lectura no puede ser nulo");
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(ruta.toFile(), "rw")) {
            posicionDeseada = posicion(ciclo, idSensor);
            if (posicionDeseada + TAMANO_REGISTRO > randomAccessFile.length()) {
                throw new IllegalArgumentException("No existe la lectura del ciclo " + ciclo + ", sensor " + idSensor);
            }
            randomAccessFile.seek(posicionDeseada + DESPLAZAMIENTO_ESTADO);
            randomAccessFile.writeInt(estadoLectura.getCodigo());
        }
        log.debug("Estado de lectura cambiado: ciclo={}, sensor={}, estado={}", ciclo, idSensor, estadoLectura);
    }

    public long contarRegistros() throws IOException {
        long total;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(ruta.toFile(), "r")) {
            long resto = randomAccessFile.length() % TAMANO_REGISTRO;
            if (resto != 0) {
                throw new IOException("Fichero de lecturas corrupto: " + randomAccessFile.length() + " bytes no es múltiplo de " + TAMANO_REGISTRO);
            }
            total = randomAccessFile.length() / TAMANO_REGISTRO;
        }
        return total;
    }

    public List<Lectura> obtenerTodas() throws IOException {
        long totalLecturas = contarRegistros();
        List<Lectura> listaLecturas = new ArrayList<>();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(ruta.toFile(), "r")) {
            for (long i = 0; i < totalLecturas; i++) {
                Lectura lectura = leerRegistro(randomAccessFile);
                listaLecturas.add(lectura);
            }
        }
        log.debug("Lista de lecturas creada. Total: {} lecturas", totalLecturas);
        return listaLecturas;
    }

    private long posicion(int ciclo, int idSensor) {
        if (ciclo < 1) {
            throw new IllegalArgumentException("Valor del ciclo inválido: " + ciclo);
        }
        if (idSensor < 1 || idSensor > numeroSensores) {
            throw new IllegalArgumentException("Id de sensor inválido: " + idSensor + ". Debe estar entre 1 y " + numeroSensores);
        }
        long registrosDelante = (long) (ciclo - 1) * numeroSensores + (idSensor - 1);
        return registrosDelante * TAMANO_REGISTRO;
    }

    private void escribirRegistro(RandomAccessFile randomAccessFile, Lectura lectura) throws IOException {
        randomAccessFile.writeInt(lectura.ciclo());
        randomAccessFile.writeInt(lectura.idSensor());
        randomAccessFile.writeDouble(lectura.valor());
        randomAccessFile.writeInt(lectura.estado().getCodigo());
    }

    private Lectura leerRegistro(RandomAccessFile randomAccessFile) throws IOException {
        int ciclo = randomAccessFile.readInt();
        int idSensor = randomAccessFile.readInt();
        double valor = randomAccessFile.readDouble();
        int codigo = randomAccessFile.readInt();

        return new Lectura(ciclo, idSensor, valor, EstadoLectura.desdeCodigo(codigo));
    }
}
