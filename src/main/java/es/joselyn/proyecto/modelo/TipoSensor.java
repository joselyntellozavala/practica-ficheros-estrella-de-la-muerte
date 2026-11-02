package es.joselyn.proyecto.modelo;

public enum TipoSensor {

    TEMPERATURA("K", false),
    OXIGENO("%", false),
    PRESION("kPa", false),
    POTENCIA("MW", false),
    CELDAS_ABIERTAS("celdas", true),
    SUJETOS_DETECTADOS("sujetos", true);

    private final String unidad;
    private final boolean recuento;

    TipoSensor(String unidad, boolean recuento) {
        this.unidad = unidad;
        this.recuento = recuento;
    }

    public String getUnidad() {
        return unidad;
    }

    public boolean esRecuento() {
        return recuento;
    }
}