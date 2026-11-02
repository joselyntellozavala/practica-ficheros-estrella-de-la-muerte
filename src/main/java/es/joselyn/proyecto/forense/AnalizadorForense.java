package es.joselyn.proyecto.forense;

import es.joselyn.proyecto.modelo.*;
import es.joselyn.proyecto.persistencia.binario.FicheroLectura;
import es.joselyn.proyecto.persistencia.texto.FicheroHistorico;
import es.joselyn.proyecto.persistencia.xml.LectorEstacionXml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnalizadorForense {

    private static final Logger log = LogManager.getLogger(AnalizadorForense.class);

    public static final String FICHERO_LECTURAS = "lecturas.bin";
    public static final String FICHERO_HISTORICO = "historico_operaciones.txt";
    public static final String FICHERO_ESTRUCTURA = "estructura_estacion.xml";
    private static final String CREDENCIAL_SISTEMA = "SISTEMA";
    private final Path carpetaDatos;

    private EstacionImperial estacion;
    private int ultimoCiclo;
    private List<Lectura> lecturas;
    private List<Operacion> operaciones;

    public AnalizadorForense(Path carpetaDatos) {
        if (carpetaDatos == null) {
            throw new IllegalArgumentException("Tienes que indicar la carpeta de datos");
        }
        this.carpetaDatos = carpetaDatos;
        log.info("Analizador forense creado para la carpeta {}", carpetaDatos);
    }

    private void leerFicheros() throws IOException {
        LectorEstacionXml lector = new LectorEstacionXml(carpetaDatos.resolve(FICHERO_ESTRUCTURA));
        lector.leer();
        estacion = lector.getEstacion();
        ultimoCiclo = lector.getUltimoCiclo();

        int numeroSensores = 0;
        for (Sector sector : estacion.getListaSectores()) {
            for (Subsistema subsistema : sector.getListaSubsistemas()) {
                numeroSensores += subsistema.getListaSensores().size();
            }
        }
        FicheroLectura ficheroLectura = new FicheroLectura(carpetaDatos.resolve(FICHERO_LECTURAS), numeroSensores);
        lecturas = ficheroLectura.obtenerTodasLecturas();

        long esperadas = (long) ultimoCiclo * numeroSensores;
        if (lecturas.size() != esperadas) {
            log.warn("El binario tiene {} lecturas y el XML indica {} ciclos de {} sensores ({} esperadas)",
                    lecturas.size(), ultimoCiclo, numeroSensores, esperadas);
        }

        operaciones = new FicheroHistorico(carpetaDatos.resolve(FICHERO_HISTORICO)).obtenerTodasOperaciones();
        log.info("Datos leídos: {} lecturas, {} operaciones, {} ciclos", lecturas.size(), operaciones.size(), ultimoCiclo);
    }


    public ResultadoAnalisis analizar() throws IOException {
        leerFicheros();

        // 1. BIN: las lecturas que el simulador confirmó como ANOMALA
        List<Lectura> anomalas = new ArrayList<>();
        for (Lectura lectura : lecturas) {
            if (lectura.estado() == EstadoLectura.ANOMALA) {
                anomalas.add(lectura);
            }
        }
        if (anomalas.isEmpty()) {
            return evaluarSinAnomalias();
        }
        log.warn("{} lecturas anómalas encontradas en el binario", anomalas.size());

        int idSensor = anomalas.get(0).idSensor();
        for (Lectura lectura : anomalas) {
            if (lectura.idSensor() != idSensor) {
                return indeterminado("Hay lecturas anómalas de más de un sensor (" + idSensor + " y " + lectura.idSensor() + ")", anomalas, null, null, null);
            }
        }

        // 2. XML: del sensor al subsistema y al sector
        Subsistema subsistema = buscarSubsistemaDeSensor(idSensor);
        if (subsistema == null) {
            return indeterminado("El sensor " + idSensor + " no existe en la estructura de la estación",
                    anomalas, null, null, null);
        }
        Sensor sensor = subsistema.getListaSensores().stream()
                .filter(s -> s.getIdSensor() == idSensor).findFirst().orElseThrow();
        Sector sector = buscarSector(subsistema.getIdSector());

        int primerCiclo = anomalas.get(0).ciclo();
        int ultimoCicloAnomalo = anomalas.get(anomalas.size() - 1).ciclo();
        log.debug("Sensor {} ({}) anómalo entre los ciclos {} y {}", idSensor, subsistema.getIdSubsistema(), primerCiclo, ultimoCicloAnomalo);

        // El XML tiene que confirmar lo que dice el BIN
        if (subsistema.getEstadoSubsistema() == EstadoSubsistema.OPERATIVO) {
            return indeterminado("El binario marca el sensor " + idSensor + " como anómalo, pero el XML muestra " + subsistema.getIdSubsistema() + " OPERATIVO", anomalas, sector, subsistema, sensor);
        }
        if (subsistema.getCicloUltimoCambio() != ultimoCicloAnomalo) {
            return indeterminado("El cambio de estado de " + subsistema.getIdSubsistema() + " (ciclo "
                    + subsistema.getCicloUltimoCambio() + ") no coincide con la última lectura anómala (ciclo "
                    + ultimoCicloAnomalo + ")", anomalas, sector, subsistema, sensor);
        }

        // Dirección del fallo: todas por debajo del mínimo o todas por encima del máximo
        boolean porDebajo = anomalas.get(0).valor() < sensor.getMinimoNormal();
        for (Lectura lectura : anomalas) {
            if ((lectura.valor() < sensor.getMinimoNormal()) != porDebajo) {
                return indeterminado("Las lecturas del sensor " + idSensor + " salen del rango unas por debajo y otras por encima", anomalas, sector, subsistema, sensor);
            }
        }

        // 3. Regla: tipo de subsistema + dirección → incidente candidato
        String direccion;
        if (porDebajo) {
            direccion = "por debajo del mínimo";
        } else {
            direccion = "por encima del máximo";
        }

        TipoIncidente candidato = aplicarRegla(subsistema.getTipoSubsistema(), porDebajo);
        if (candidato == null) {
            return indeterminado("Ningún incidente conocido corresponde a " + subsistema.getTipoSubsistema() + " " + direccion, anomalas, sector, subsistema, sensor);
        }
        log.debug("Regla aplicada: {} {} → candidato {}", subsistema.getTipoSubsistema(), direccion, candidato);


        // 4. TXT: operaciones sobre ese subsistema durante la anomalía
        List<Operacion> relacionadas = new ArrayList<>();
        for (Operacion operacion : operaciones) {
            if (operacion.subsistema().equals(subsistema.getIdSubsistema())
                    && operacion.ciclo() >= primerCiclo && operacion.ciclo() <= ultimoCicloAnomalo) {
                relacionadas.add(operacion);
            }
        }
        if (!corroboraTxt(candidato, relacionadas)) {
            return indeterminado("El histórico no corrobora " + candidato + " en " + subsistema.getIdSubsistema()
                    + " entre los ciclos " + primerCiclo + " y " + ultimoCicloAnomalo, anomalas, sector, subsistema, sensor);
        }

        // Las tres fuentes coinciden
        Oficial oficialAlMando = buscarOficialAlMando(sector.getIdSector());
        String motivo = "BIN, XML y TXT coinciden en " + subsistema.getIdSubsistema() + " entre los ciclos "
                + primerCiclo + " y " + ultimoCicloAnomalo;
        log.info("Veredicto: {} ({})", candidato, motivo);
        return new ResultadoAnalisis(Veredicto.IDENTIFICADO, candidato, motivo, ultimoCiclo, anomalas,
                sector, subsistema, sensor, oficialAlMando, relacionadas,
                describirEfectos(anomalas, sector, subsistema, sensor, relacionadas),
                redactarConclusion(candidato, sector, oficialAlMando));
    }

    private ResultadoAnalisis evaluarSinAnomalias() {

        for (Lectura lectura : lecturas) {
            if (lectura.estado() == EstadoLectura.SOSPECHOSA) {
                return indeterminado("Hay lecturas fuera de rango sin confirmar (sensor " + lectura.idSensor() + ", ciclo " + lectura.ciclo() + ")", List.of(), null, null, null);
            }
        }

        for (Sector sector : estacion.getListaSectores()) {
            if (sector.getEstadoSector() != EstadoSector.OPERATIVO) {
                return indeterminado("No hay lecturas anómalas, pero el sector " + sector.getIdSector() + " está en " + sector.getEstadoSector(), List.of(), sector, null, null);
            }
            for (Subsistema subsistema : sector.getListaSubsistemas()) {
                if (subsistema.getEstadoSubsistema() != EstadoSubsistema.OPERATIVO) {
                    return indeterminado("No hay lecturas anómalas, pero " + subsistema.getIdSubsistema() + " está " + subsistema.getEstadoSubsistema(), List.of(), sector, subsistema, null);
                }
            }
        }
        String motivo = "Ninguna lectura fuera de rango y todos los sectores y subsistemas operativos";
        log.info("Veredicto: SIN_INCIDENTE ({})", motivo);
        return new ResultadoAnalisis(Veredicto.SIN_INCIDENTE, null, motivo, ultimoCiclo, List.of(),
                null, null, null, null, List.of(), List.of(),
                "La estación ha operado con normalidad durante " + ultimoCiclo
                        + " ciclos. No se requiere la intervención de Lord Vader.");
    }

    private ResultadoAnalisis indeterminado(String motivo, List<Lectura> anomalas, Sector sector,
                                            Subsistema subsistema, Sensor sensor) {
        log.warn("Veredicto: INDETERMINADO ({})", motivo);
        return new ResultadoAnalisis(Veredicto.INDETERMINADO, null, motivo, ultimoCiclo, anomalas,
                sector, subsistema, sensor, null, List.of(), List.of(),
                "Los datos no permiten atribuir lo ocurrido a ningún incidente conocido. "
                        + "Se recomienda una inspección presencial antes de informar a Lord Vader.");
    }


    private TipoIncidente aplicarRegla(TipoSubsistema tipo, boolean porDebajo) {
        return switch (tipo) {
            case RAYO_TRACTOR -> porDebajo ? TipoIncidente.DESACTIVACION_RAYO_TRACTOR : null;
            case BLOQUE_DETENCION -> porDebajo ? null : TipoIncidente.FUGA_AA23;
            case SOPORTE_VITAL -> porDebajo ? TipoIncidente.FALLO_SOPORTE_VITAL : null;
            case COMPACTADOR -> porDebajo ? null : TipoIncidente.FALLO_COMPACTADOR;
            case HANGAR -> porDebajo ? null : TipoIncidente.INTRUSION_HANGAR;
            case CONDUCTO_TERMICO -> porDebajo ? null : TipoIncidente.BRECHA_CONDUCTO_TERMICO;
            case REACTOR -> null;
        };
    }

    private boolean corroboraTxt(TipoIncidente incidente, List<Operacion> relacionadas) {
        for (Operacion operacion : relacionadas) {
            boolean corrobora = switch (incidente) {
                case DESACTIVACION_RAYO_TRACTOR, INTRUSION_HANGAR ->
                        operacion.tipo() == TipoOperacion.ACCESO && esCredencialAjena(operacion.credencial());
                case FUGA_AA23, FALLO_SOPORTE_VITAL, FALLO_COMPACTADOR, BRECHA_CONDUCTO_TERMICO ->
                        operacion.tipo() == TipoOperacion.AVISO;
            };
            if (corrobora) {
                log.debug("El histórico corrobora {}: {}", incidente, operacion);
                return true;
            }
        }
        return false;
    }

    private boolean esCredencialAjena(String credencial) {
        if (credencial.equals(CREDENCIAL_SISTEMA)) {
            return false;
        }
        for (Oficial oficial : estacion.getListaOficiales()) {
            if (oficial.getIdOficial().equals(credencial)) {
                return false;
            }
        }
        return true;
    }

    private List<String> describirEfectos(List<Lectura> anomalas, Sector sector, Subsistema subsistema,
                                          Sensor sensor, List<Operacion> relacionadas) {
        List<String> efectos = new ArrayList<>();
        Lectura primera = anomalas.get(0);
        efectos.add("Sensor " + sensor.getIdSensor() + " (" + sensor.getTipo() + ") fuera de rango en los ciclos "
                + primera.ciclo() + " a " + anomalas.get(anomalas.size() - 1).ciclo() + ": " + primera.valor() + " "
                + sensor.getTipo().getUnidad() + " (rango normal " + sensor.getMinimoNormal() + "-"
                + sensor.getMaximoNormal() + ")");
        efectos.add("Subsistema " + subsistema.getIdSubsistema() + " (" + subsistema.getNombre() + ") "
                + subsistema.getEstadoSubsistema() + " desde el ciclo " + subsistema.getCicloUltimoCambio());
        efectos.add("Sector " + sector.getIdSector() + " (" + sector.getTipoSector() + ") en estado "
                + sector.getEstadoSector());
        for (Operacion operacion : relacionadas) {
            if (esCredencialAjena(operacion.credencial())) {
                efectos.add("Ciclo " + operacion.ciclo() + ": " + operacion.tipo() + " con la credencial "
                        + operacion.credencial() + ", que no pertenece a la plantilla");
            }
        }
        return efectos;
    }

    private String redactarConclusion(TipoIncidente incidente, Sector sector, Oficial oficial) {

        String responsable = (oficial == null)
                ? "oficial al mando de " + sector.getIdSector()
                : oficial.getRango() + " " + oficial.getNombre();
        return switch (incidente) {
            case DESACTIVACION_RAYO_TRACTOR -> "Un agente no identificado ha desactivado el rayo tractor: "
                    + "una nave retenida podría abandonar la estación. Se recomienda reasignar al "
                    + responsable + " a Kessel.";
            case FUGA_AA23 -> "Un prisionero ha salido del bloque AA-23 mediante un traslado sin orden. "
                    + "Se recomienda bloquear el sector y que el " + responsable + " responda ante Lord Vader.";
            case FALLO_SOPORTE_VITAL -> "El oxígeno del sector " + sector.getIdSector()
                    + " está por debajo del mínimo seguro. Se recomienda evacuar el sector y que el "
                    + responsable + " explique la falta de mantenimiento.";
            case FALLO_COMPACTADOR -> "El compactador ha funcionado fuera de ciclo con presión excesiva; "
                    + "podría haber intrusos en el sistema de residuos. Se recomienda sellar el sector y que el "
                    + responsable + " lo inspeccione personalmente.";
            case INTRUSION_HANGAR -> "Sujetos ajenos a la plantilla han accedido al hangar 327. "
                    + "Se recomienda registrar todas las naves del hangar y reasignar al " + responsable + " a Kessel.";
            case BRECHA_CONDUCTO_TERMICO -> "El conducto térmico de escape ha recibido impactos y su temperatura "
                    + "supera el máximo: la estación entera está en peligro. Se recomienda la evacuación inmediata "
                    + "del Gran Moff y que el " + responsable + " no la acompañe.";
        };
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
        return null;
    }

    private Sector buscarSector(String idSector) {
        for (Sector sector : estacion.getListaSectores()) {
            if (sector.getIdSector().equals(idSector)) {
                return sector;
            }
        }
        throw new IllegalStateException("El sector " + idSector + " no existe"); // el lector SAX ya garantiza que existe
    }


    private Oficial buscarOficialAlMando(String idSector) {
        return estacion.getListaOficiales().stream()
                .filter(oficial -> oficial.getIdSector().equals(idSector))
                .min(Comparator.comparing(Oficial::getRango))
                .orElse(null);
    }
}
