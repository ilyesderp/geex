package es.geex.compartido.domain;

import java.util.Objects;
import java.util.UUID;

/** Identificador de un despacho (el "workspace" o tenant: cada gestoría es un despacho). */
public record DespachoId(UUID valor) {

    public DespachoId {
        Objects.requireNonNull(valor, "El identificador del despacho es obligatorio");
    }

    public static DespachoId nuevo() {
        return new DespachoId(UUID.randomUUID());
    }

    public static DespachoId de(String valor) {
        return new DespachoId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
