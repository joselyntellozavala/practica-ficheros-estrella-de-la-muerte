package es.joselyn.proyecto.modelo;

public enum TipoIncidente {

    DESACTIVACION_RAYO_TRACTOR("Desactivación del rayo tractor”"),
    FUGA_AA23("Fuga del bloque de detención AA-23"),
    FALLO_SOPORTE_VITAL("Fallo del soporte vital"),
    FALLO_COMPACTADOR("Fallo del compactador de basura"),
    INTRUSION_HANGAR("Intrusión en el hangar 327"),
    BRECHA_CONDUCTO_TERMICO("Brecha del conducto térmico");

    private final String descripcion;

    TipoIncidente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
