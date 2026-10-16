package es.geex.compartido.domain;

/** Una operación incumple una regla de negocio (se traduce en HTTP 422). */
public class ReglaNegocioException extends DomainException {

    public ReglaNegocioException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
