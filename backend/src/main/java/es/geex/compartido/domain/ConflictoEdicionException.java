package es.geex.compartido.domain;

/**
 * Otra persona modificó los datos después de que el usuario los cargara (HTTP 409):
 * en vez de pisar sus cambios sin avisar, se pide recargar.
 */
public class ConflictoEdicionException extends DomainException {

    public ConflictoEdicionException() {
        super("CONFLICTO_EDICION",
                "Otro usuario ha modificado estos datos mientras los editabas. Recarga la página e inténtalo de nuevo.");
    }
}
