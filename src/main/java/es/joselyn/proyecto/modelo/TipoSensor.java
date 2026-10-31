package es.joselyn.proyecto.modelo;

public enum TipoSensor {

    TEMPERATURA("K"),
    OXIGENO("%"),
    PRESION("kPa"),
    POTENCIA("MW"),
    CELDAS_ABIERTAS("personas"),
    SUJETOS_DETECTADOS("sujetos");

    private final String unidad;

    TipoSensor(String unidad) {
        this.unidad = unidad;
    }

    public String getUnidad() {
        return unidad;
    }
}