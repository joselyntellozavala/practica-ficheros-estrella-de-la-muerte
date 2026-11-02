package es.joselyn.proyecto.simulacion;

import es.joselyn.proyecto.modelo.*;
import es.joselyn.proyecto.persistencia.binario.FicheroLectura;
import es.joselyn.proyecto.persistencia.texto.FicheroHistorico;
import es.joselyn.proyecto.persistencia.xml.EscritorEstacionXml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Random;

public class Simulacion {

    private static final String FICHERO_LECTURAS = "lecturas.bin";
    private static final String FICHERO_HISTORICO = "historico_operaciones.txt";
    private static final String FICHERO_ESTRUCTURA = "estructura_estacion.xml";
    private static final int NUMERO_SENSORES = 12;
    private static final String NOMBRE_ESTACION = "Estrella de la muerte";

    private static final Logger log = LogManager.getLogger(Simulacion.class);

    private final Path carpetaDatos;
    private final Random random;
    private final TipoIncidente incidenteForzado;
    private final FicheroLectura ficheroLectura;
    private final FicheroHistorico ficheroHistorico;
    private final EscritorEstacionXml escritorEstacionXml;
    private final EstacionImperial estacion;

    public Simulacion(Path carpetaDatos, Random random, TipoIncidente incidenteForzado) {

        if (carpetaDatos == null) {
            throw new IllegalArgumentException("Tienes que asignar una ruta");
        }
        if (random == null) {
            throw new IllegalArgumentException("Tienes que asignar un random");
        }

        // Si no hay incidente forzado, la simulación lo sorteará: modo normal
        // Y si lo hay, es el modo de prueba y se indica cuál

        this.carpetaDatos = carpetaDatos;
        this.random = random;
        this.incidenteForzado = incidenteForzado;

        this.ficheroLectura = new FicheroLectura(carpetaDatos.resolve(FICHERO_LECTURAS), NUMERO_SENSORES);
        this.ficheroHistorico = new FicheroHistorico(carpetaDatos.resolve(FICHERO_HISTORICO));
        this.escritorEstacionXml = new EscritorEstacionXml(carpetaDatos.resolve(FICHERO_ESTRUCTURA));

        this.estacion = montarEstacion();

        log.info("Simulación creada en {} (modo: {})", carpetaDatos, incidenteForzado == null ? "normal" : "prueba, incidente " + incidenteForzado);
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

}
