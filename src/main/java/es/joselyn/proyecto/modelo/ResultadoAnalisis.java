package es.joselyn.proyecto.modelo;

import java.util.List;

public record ResultadoAnalisis(Veredicto veredicto, TipoIncidente incidente, String motivo, int ultimoCiclo,
                                List<Lectura> lecturasAnomalas, Sector sector, Subsistema subsistema, Sensor sensor,
                                Oficial oficialAlMando, List<Operacion> operacionesRelacionadas,
                                List<String> efectos, String conclusion) {

    public ResultadoAnalisis {
        if (veredicto == null) {
            throw new IllegalArgumentException("Necesitas asignar un veredicto");
        }

        if ((veredicto == Veredicto.IDENTIFICADO) != (incidente != null)) {
            throw new IllegalArgumentException("El incidente solo existe, y es obligatorio, con veredicto IDENTIFICADO");
        }
        if (motivo == null || conclusion == null) {
            throw new IllegalArgumentException("El motivo y la conclusión son obligatorios");
        }
        lecturasAnomalas = List.copyOf(lecturasAnomalas);
        operacionesRelacionadas = List.copyOf(operacionesRelacionadas);
        efectos = List.copyOf(efectos);
    }
}