package es.geex.compartido.domain;

/** El recurso no existe o no pertenece al despacho actual (HTTP 404). */
public class NoEncontradoException extends DomainException {

    public NoEncontradoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
