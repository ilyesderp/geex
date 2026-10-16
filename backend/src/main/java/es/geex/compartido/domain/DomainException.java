package es.geex.compartido.domain;

/**
 * Error de negocio. El mensaje está en español porque se muestra al usuario;
 * el código es estable y sirve para que el frontend o los tests lo identifiquen.
 */
public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
