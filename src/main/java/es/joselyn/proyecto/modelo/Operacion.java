package es.joselyn.proyecto.modelo;

import java.time.LocalDateTime;

public record Operacion(LocalDateTime fechaHora, int ciclo, String sector, String subsistema, String credencial,
                        TipoOperacion tipo, String mensaje) {

    public Operacion {
        if (ciclo <= 0) {
            throw new IllegalArgumentException("Valor inválido para el ciclo: " + ciclo);
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("Necesitas asignarle una fecha y hora");
        }
        if (sector == null) {
            throw new IllegalArgumentException("Necesitas asignarle un sector");
        }
        if (subsistema == null) {
            throw new IllegalArgumentException("Necesitas asignarle un subsistema");
        }
        if (credencial == null) {
            throw new IllegalArgumentException("Necesitas asignarle una credencial");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Necesitas asignarle un tipo de operación");
        }
        if (mensaje == null) {
            throw new IllegalArgumentException("Necesitas asignarle un mensaje");
        }

        comprobarSinSeparador("sector", sector);
        comprobarSinSeparador("subsistema", subsistema);
        comprobarSinSeparador("credencial", credencial);
        comprobarSinSeparador("mensaje", mensaje);
    }

    private static void comprobarSinSeparador(String campo, String texto) {
        if (texto.contains("|")) {
            throw new IllegalArgumentException("El campo " + campo + " no puede contener '|'");
        }
    }
}
