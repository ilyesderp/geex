package es.geex.compartido.domain;

/** El usuario no tiene permiso para realizar la operación (HTTP 403). */
public class AccesoDenegadoException extends DomainException {

    public AccesoDenegadoException(String mensaje) {
        super("ACCESO_DENEGADO", mensaje);
    }

    public static AccesoDenegadoException faltaPermiso(Permiso permiso) {
        return new AccesoDenegadoException("No tienes permiso para: " + permiso.descripcion().toLowerCase());
    }
}
