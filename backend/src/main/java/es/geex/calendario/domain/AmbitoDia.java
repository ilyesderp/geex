package es.geex.calendario.domain;

public enum AmbitoDia {
    NACIONAL("Nacional"),
    AUTONOMICO("Autonómico"),
    LOCAL("Local");

    private final String etiqueta;

    AmbitoDia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
