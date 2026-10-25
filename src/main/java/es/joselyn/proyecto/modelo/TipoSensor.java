package es.joselyn.proyecto.modelo;

public enum TipoSensor {

    TEMPERATURA("K"),
    OXIGENO("%"),
    PRESION("kPa"),
    POTENCIA("MW");

    private final String unidad;

    TipoSensor(String unidad) {
        this.unidad = unidad;
    }

    public String getUnidad() {
        return unidad;
    }
}