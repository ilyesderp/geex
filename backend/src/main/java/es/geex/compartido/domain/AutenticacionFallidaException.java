package es.geex.compartido.domain;

/** Credenciales o sesión no válidas (HTTP 401). */
public class AutenticacionFallidaException extends DomainException {

    public AutenticacionFallidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
