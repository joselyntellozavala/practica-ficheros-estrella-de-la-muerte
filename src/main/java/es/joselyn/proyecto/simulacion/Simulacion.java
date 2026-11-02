package es.joselyn.proyecto.simulacion;

import es.joselyn.proyecto.modelo.*;
import es.joselyn.proyecto.persistencia.binario.FicheroLectura;
import es.joselyn.proyecto.persistencia.texto.FicheroHistorico;
import es.joselyn.proyecto.persistencia.xml.EscritorEstacionXml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.joselyn.proyecto.excepcion.AnomaliaCriticaException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Simulacion {

    private static final String NOMBRE_ESTACION = "Estrella de la muerte";
    private static final String FICHERO_LECTURAS = "lecturas.bin";
    private static final String FICHERO_HISTORICO = "historico_operaciones.txt";
    private static final String FICHERO_ESTRUCTURA = "estructura_estacion.xml";
    private static final int NUMERO_SENSORES = 12;
    private static final String CREDENCIAL_SISTEMA = "SISTEMA";
    private static final String CREDENCIAL_DESCONOCIDA = "SIN-IDENTIFICAR";
    private static final String CREDENCIAL_INTRUSO = "TK-421";
    private static final int MAX_CICLOS = 10;
    private static final int CICLOS_PARA_CONFIRMAR = 3;
    private static final int OPERACIONES_POR_CICLO = 2;
    private static final int MINUTOS_POR_CICLO = 10;

    private static final Logger log = LogManager.getLogger(Simulacion.class);

    private final Path carpetaDatos;
    private final Random random;
    private final TipoIncidente incidenteForzado;
    private final boolean hayIncidente;
    private final FicheroLectura ficheroLectura;
    private final FicheroHistorico ficheroHistorico;
    private final EscritorEstacionXml escritorEstacionXml;
    private final EstacionImperial estacion;

    public static Simulacion normal(Path carpetaDatos, Random random) {
        return new Simulacion(carpetaDatos, random, true, null);
    }

    public static Simulacion sinIncidente(Path carpetaDatos, Random random) {
        return new Simulacion(carpetaDatos, random, false, null);
    }

    public static Simulacion conIncidente(Path carpetaDatos, Random random, TipoIncidente incidente) {
        if (incidente == null) {
            throw new IllegalArgumentException("Tienes que indicar el incidente");
        }
        return new Simulacion(carpetaDatos, random, true, incidente);
    }

    private Simulacion(Path carpetaDatos, Random random, boolean hayIncidente, TipoIncidente incidenteForzado) {
        if (carpetaDatos == null) {
            throw new IllegalArgumentException("Tienes que asignar una ruta");
        }
        if (random == null) {
            throw new IllegalArgumentException("Tienes que asignar un random");
        }

        this.carpetaDatos = carpetaDatos;
        this.random = random;
        this.hayIncidente = hayIncidente;
        this.incidenteForzado = incidenteForzado;

        this.ficheroLectura = new FicheroLectura(carpetaDatos.resolve(FICHERO_LECTURAS), NUMERO_SENSORES);
        this.ficheroHistorico = new FicheroHistorico(carpetaDatos.resolve(FICHERO_HISTORICO));
        this.escritorEstacionXml = new EscritorEstacionXml(carpetaDatos.resolve(FICHERO_ESTRUCTURA));

        this.estacion = montarEstacion();

        String modo;
        if (!hayIncidente) {
            modo = "sin incidente";
        } else if (incidenteForzado == null) {
            modo = "normal";
        } else {
            modo = "prueba, incidente " + incidenteForzado;
        }
        log.info("Simulación creada en {} (modo: {})", carpetaDatos, modo);
    }


    private EstacionImperial montarEstacion() {

        EstacionImperial estacion = new EstacionImperial(NOMBRE_ESTACION);

        Sector mando = new Sector("SEC-01", TipoSector.MANDO, EstadoSector.OPERATIVO);
        Sector detencion = new Sector("SEC-02", TipoSector.DETENCION, EstadoSector.OPERATIVO);
        Sector hangares = new Sector("SEC-03", TipoSector.HANGARES, EstadoSector.OPERATIVO);
        Sector nucleo = new Sector("SEC-04", TipoSector.NUCLEO, EstadoSector.OPERATIVO);
        Sector residuos = new Sector("SEC-05", TipoSector.RESIDUOS, EstadoSector.OPERATIVO);
        Sector superficie = new Sector("SEC-06", TipoSector.SUPERFICIE, EstadoSector.OPERATIVO);

        estacion.agregarSector(mando);
        estacion.agregarSector(detencion);
        estacion.agregarSector(hangares);
        estacion.agregarSector(nucleo);
        estacion.agregarSector(residuos);
        estacion.agregarSector(superficie);

        crearSubsistemaConSensor(nucleo, "RX-01", "Reactor principal", TipoSubsistema.REACTOR, 1, TipoSensor.POTENCIA, 800, 1200);
        crearSubsistemaConSensor(nucleo, "RT-01", "Rayo tractor", TipoSubsistema.RAYO_TRACTOR, 2, TipoSensor.POTENCIA, 40, 60);
        crearSubsistemaConSensor(detencion, "AA-23", "Bloque de detención AA-23", TipoSubsistema.BLOQUE_DETENCION, 3, TipoSensor.CELDAS_ABIERTAS, 0, 0);
        crearSubsistemaConSensor(hangares, "HG-327", "Hangar 327", TipoSubsistema.HANGAR, 4, TipoSensor.SUJETOS_DETECTADOS, 0, 6);
        crearSubsistemaConSensor(residuos, "CP-3263827", "Compactador 3263827", TipoSubsistema.COMPACTADOR, 5, TipoSensor.PRESION, 90, 110);
        crearSubsistemaConSensor(superficie, "CT-01", "Conducto térmico de escape", TipoSubsistema.CONDUCTO_TERMICO, 6, TipoSensor.TEMPERATURA, 300, 350);
        crearSubsistemaConSensor(mando, "SV-01", "Soporte vital mando", TipoSubsistema.SOPORTE_VITAL, 7, TipoSensor.OXIGENO, 19.5, 23.5);
        crearSubsistemaConSensor(detencion, "SV-02", "Soporte vital detención", TipoSubsistema.SOPORTE_VITAL, 8, TipoSensor.OXIGENO, 19.5, 23.5);
        crearSubsistemaConSensor(hangares, "SV-03", "Soporte vital hangares", TipoSubsistema.SOPORTE_VITAL, 9, TipoSensor.OXIGENO, 19.5, 23.5);
        crearSubsistemaConSensor(nucleo, "SV-04", "Soporte vital núcleo", TipoSubsistema.SOPORTE_VITAL, 10, TipoSensor.OXIGENO, 19.5, 23.5);
        crearSubsistemaConSensor(residuos, "SV-05", "Soporte vital residuos", TipoSubsistema.SOPORTE_VITAL, 11, TipoSensor.OXIGENO, 19.5, 23.5);
        crearSubsistemaConSensor(superficie, "SV-06", "Soporte vital superficie", TipoSubsistema.SOPORTE_VITAL, 12, TipoSensor.OXIGENO, 19.5, 23.5);


        estacion.agregarOficial(new Oficial("OF-01", "Wilhuff Tarkin", RangoOficial.GRAN_MOFF, "SEC-01"));
        estacion.agregarOficial(new Oficial("OF-02", "Conan Antonio Motti", RangoOficial.ALMIRANTE, "SEC-01"));
        estacion.agregarOficial(new Oficial("OF-03", "Wullf Yularen", RangoOficial.CORONEL, "SEC-02"));
        estacion.agregarOficial(new Oficial("OF-04", "Pell Darrow", RangoOficial.TENIENTE, "SEC-02"));
        estacion.agregarOficial(new Oficial("OF-05", "Varek Thrane", RangoOficial.COMANDANTE, "SEC-03"));
        estacion.agregarOficial(new Oficial("OF-06", "Lis Kesh", RangoOficial.ALFEREZ, "SEC-03"));
        estacion.agregarOficial(new Oficial("OF-07", "Cassio Tagge", RangoOficial.GENERAL, "SEC-04"));
        estacion.agregarOficial(new Oficial("OF-08", "Orrin Vael", RangoOficial.MAYOR, "SEC-05"));
        estacion.agregarOficial(new Oficial("OF-09", "Dax Morrell", RangoOficial.CAPITAN, "SEC-06"));

        log.info("Estación montada: {}", estacion);
        return estacion;
    }

    private void crearSubsistemaConSensor(Sector sector, String idSubsistema, String nombre, TipoSubsistema tipoSubsistema, int idSensor, TipoSensor tipoSensor, double minimo, double maximo) {

        Subsistema subsistema = new Subsistema(idSubsistema, nombre, sector.getIdSector(), tipoSubsistema, EstadoSubsistema.OPERATIVO);
        subsistema.agregarSensor(new Sensor(idSensor, idSubsistema, tipoSensor, minimo, maximo));
        sector.agregarSubsistema(subsistema);

    }

    public void ejecutar() throws IOException, AnomaliaCriticaException {

        Files.createDirectories(carpetaDatos);
        Files.deleteIfExists(carpetaDatos.resolve(FICHERO_LECTURAS));
        Files.deleteIfExists(carpetaDatos.resolve(FICHERO_HISTORICO));
        Files.deleteIfExists(carpetaDatos.resolve(FICHERO_ESTRUCTURA));
        escritorEstacionXml.crearDocumento(estacion);

        Huella huella = null;
        int cicloInicioIncidente = 0;
        if (hayIncidente) {
            TipoIncidente incidente;
            if (incidenteForzado != null) {
                incidente = incidenteForzado;
            } else {
                TipoIncidente[] todos = TipoIncidente.values();
                incidente = todos[random.nextInt(todos.length)];
            }
            cicloInicioIncidente = 3 + random.nextInt(4);
            huella = crearHuella(incidente);
            log.info("Incidente previsto: {} a partir del ciclo {} (sensor {})", incidente, cicloInicioIncidente, huella.idSensor());
        } else {
            log.info("Simulación sin incidente: {} ciclos de funcionamiento normal", MAX_CICLOS);
        }

        List<Sensor> sensores = sensoresOrdenados();
        LocalDateTime inicio = LocalDateTime.now().withNano(0);

        for (int ciclo = 1; ciclo <= MAX_CICLOS; ciclo++) {
            LocalDateTime fechaCiclo = inicio.plusMinutes((long) (ciclo - 1) * MINUTOS_POR_CICLO);
            boolean incidenteActivo = huella != null && ciclo >= cicloInicioIncidente;

            List<Sensor> sospechosos = new ArrayList<>();
            for (Sensor sensor : sensores) {
                boolean esSensorDelIncidente = incidenteActivo && sensor.getIdSensor() == huella.idSensor();
                double valor;
                if (esSensorDelIncidente) {
                    valor = huella.valorAnomalo();
                } else {
                    valor = valorNormal(sensor);
                }

                EstadoLectura estado;
                if (sensor.estaFueraDeRango(valor)) {
                    estado = EstadoLectura.SOSPECHOSA;
                } else {
                    estado = EstadoLectura.NORMAL;
                }

                Lectura lectura = new Lectura(ciclo, sensor.getIdSensor(), valor, estado);
                ficheroLectura.guardarLectura(lectura);
                if (estado == EstadoLectura.SOSPECHOSA) {
                    sospechosos.add(sensor);
                }
            }

            registrarOperacionesNormales(ciclo, fechaCiclo);
            if (incidenteActivo && ciclo == cicloInicioIncidente) {
                registrarSintoma(huella, ciclo, fechaCiclo);
            }

            escritorEstacionXml.actualizarUltimoCiclo(ciclo);

            for (Sensor sensor : sospechosos) {
                if (confirmarAnomalia(ciclo, sensor.getIdSensor())) {
                    aplicarConsecuencias(huella, ciclo, fechaCiclo);
                    throw new AnomaliaCriticaException("Anomalía crítica: sensor " + sensor.getIdSensor() + " fuera de rango durante " + CICLOS_PARA_CONFIRMAR + " ciclos seguidos (ciclo " + ciclo + ")", ciclo, sensor.getIdSensor());
                }
            }
            log.info("Ciclo {} completado: {} lecturas, {} fuera de rango", ciclo, sensores.size(), sospechosos.size());
        }

        if (huella == null) {
            log.info("Simulación completada sin incidencias tras {} ciclos", MAX_CICLOS);
        } else {
            log.warn("La simulación terminó tras {} ciclos sin anomalía crítica", MAX_CICLOS);
        }
    }


    private boolean confirmarAnomalia(int ciclo, int idSensor) throws IOException {
        if (ciclo < CICLOS_PARA_CONFIRMAR) {
            return false;
        }
        for (int anterior = ciclo - 1; anterior > ciclo - CICLOS_PARA_CONFIRMAR; anterior--) {
            Lectura lectura = ficheroLectura.obtenerLectura(anterior, idSensor);
            if (lectura.estado() == EstadoLectura.NORMAL) {
                return false;
            }
        }
        for (int c = ciclo - CICLOS_PARA_CONFIRMAR + 1; c <= ciclo; c++) {
            ficheroLectura.cambiarEstadoLectura(c, idSensor, EstadoLectura.ANOMALA);
        }
        log.warn("Anomalía confirmada en el sensor {}: ciclos {} a {} marcados como ANOMALA",
                idSensor, ciclo - CICLOS_PARA_CONFIRMAR + 1, ciclo);
        return true;
    }

    private void aplicarConsecuencias(Huella huella, int ciclo, LocalDateTime fechaCiclo) throws IOException {
        Subsistema subsistema = buscarSubsistemaDeSensor(huella.idSensor());
        subsistema.cambiarEstadoSubsistema(huella.estadoSubsistema(), ciclo);
        escritorEstacionXml.actualizarSubsistema(subsistema);

        Sector sector = buscarSector(subsistema.getIdSector());
        if (sector.getEstadoSector() != huella.estadoSector()) {
            sector.setEstadoSector(huella.estadoSector());
            escritorEstacionXml.actualizarSector(sector);
        }

        ficheroHistorico.registrarOperacion(new Operacion(fechaCiclo, ciclo, sector.getIdSector(),
                subsistema.getIdSubsistema(), CREDENCIAL_SISTEMA, TipoOperacion.AVISO,
                "Lecturas fuera de rango durante " + CICLOS_PARA_CONFIRMAR + " ciclos; subsistema "
                        + huella.estadoSubsistema() + ", sector " + sector.getEstadoSector()));
    }


    private double valorNormal(Sensor sensor) {
        double valor = sensor.getMinimoNormal() + random.nextDouble() * (sensor.getMaximoNormal() - sensor.getMinimoNormal());
        if (sensor.getTipo().esRecuento()) {
            return Math.round(valor);
        }
        return Math.round(valor * 100) / 100.0;
    }


    private void registrarOperacionesNormales(int ciclo, LocalDateTime fechaCiclo) throws IOException {
        List<Oficial> oficiales = estacion.getListaOficiales();
        TipoOperacion[] rutinarias = {TipoOperacion.ORDEN, TipoOperacion.RELEVO,
                TipoOperacion.MANTENIMIENTO, TipoOperacion.ACCESO};

        for (int i = 0; i < OPERACIONES_POR_CICLO; i++) {
            Oficial oficial = oficiales.get(random.nextInt(oficiales.size()));
            List<Subsistema> subsistemas = buscarSector(oficial.getIdSector()).getListaSubsistemas();
            Subsistema subsistema = subsistemas.get(random.nextInt(subsistemas.size()));
            TipoOperacion tipo = rutinarias[random.nextInt(rutinarias.length)];

            String mensaje = switch (tipo) {
                case ORDEN -> "Orden de rutina emitida";
                case RELEVO -> "Relevo de turno completado";
                case MANTENIMIENTO -> "Revisión programada sin incidencias";
                case ACCESO -> "Acceso autorizado";
                case AVISO -> "Aviso de rutina";
            };
            ficheroHistorico.registrarOperacion(new Operacion(fechaCiclo, ciclo, oficial.getIdSector(),
                    subsistema.getIdSubsistema(), oficial.getIdOficial(), tipo, mensaje));
        }
    }


    private void registrarSintoma(Huella huella, int ciclo, LocalDateTime fechaCiclo) throws IOException {
        Subsistema subsistema = buscarSubsistemaDeSensor(huella.idSensor());
        ficheroHistorico.registrarOperacion(new Operacion(fechaCiclo, ciclo, subsistema.getIdSector(),
                subsistema.getIdSubsistema(), huella.credencial(), huella.tipoOperacion(), huella.sintoma()));
    }

    private List<Sensor> sensoresOrdenados() {
        List<Sensor> sensores = new ArrayList<>();
        for (Sector sector : estacion.getListaSectores()) {
            for (Subsistema subsistema : sector.getListaSubsistemas()) {
                sensores.addAll(subsistema.getListaSensores());
            }
        }
        sensores.sort(Comparator.comparingInt(Sensor::getIdSensor));
        return sensores;
    }

    private Subsistema buscarSubsistemaDeSensor(int idSensor) {
        for (Sector sector : estacion.getListaSectores()) {
            for (Subsistema subsistema : sector.getListaSubsistemas()) {
                for (Sensor sensor : subsistema.getListaSensores()) {
                    if (sensor.getIdSensor() == idSensor) {
                        return subsistema;
                    }
                }
            }
        }
        throw new IllegalArgumentException("No existe ningún subsistema con el sensor " + idSensor);
    }

    private Sector buscarSector(String idSector) {
        for (Sector sector : estacion.getListaSectores()) {
            if (sector.getIdSector().equals(idSector)) {
                return sector;
            }
        }
        throw new IllegalArgumentException("No existe el sector " + idSector);
    }


    private Huella crearHuella(TipoIncidente incidente) {
        return switch (incidente) {
            case DESACTIVACION_RAYO_TRACTOR ->
                    new Huella(2, 0, TipoOperacion.ACCESO, CREDENCIAL_DESCONOCIDA, "Acceso a terminal del núcleo sin orden registrada",
                            EstadoSubsistema.FUERA_DE_SERVICIO, EstadoSector.OPERATIVO);
            case FUGA_AA23 -> new Huella(3, 1, TipoOperacion.AVISO, CREDENCIAL_SISTEMA,
                    "Traslado de prisionero sin orden de origen; fallo de comunicación con el bloque",
                    EstadoSubsistema.FUERA_DE_SERVICIO, EstadoSector.ALERTA);
            case FALLO_SOPORTE_VITAL -> {
                int idSensor = 7 + random.nextInt(6);
                yield new Huella(idSensor, 15, TipoOperacion.AVISO, CREDENCIAL_SISTEMA,
                        "Tripulación reporta dificultad respiratoria",
                        EstadoSubsistema.FUERA_DE_SERVICIO, EstadoSector.EMERGENCIA);
            }
            case FALLO_COMPACTADOR -> new Huella(5, 250, TipoOperacion.AVISO, CREDENCIAL_SISTEMA,
                    "Activación del compactador fuera de ciclo",
                    EstadoSubsistema.FUERA_DE_SERVICIO, EstadoSector.ALERTA);
            case INTRUSION_HANGAR -> new Huella(4, 10, TipoOperacion.ACCESO, CREDENCIAL_INTRUSO,
                    "Acceso al hangar 327",
                    EstadoSubsistema.DEGRADADO, EstadoSector.ALERTA);
            case BRECHA_CONDUCTO_TERMICO -> new Huella(6, 900, TipoOperacion.AVISO, CREDENCIAL_SISTEMA,
                    "Impactos registrados en la trinchera",
                    EstadoSubsistema.FUERA_DE_SERVICIO, EstadoSector.EMERGENCIA);
        };
    }
}
