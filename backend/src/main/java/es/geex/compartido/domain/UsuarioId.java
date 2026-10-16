package es.geex.compartido.domain;

import java.util.Objects;
import java.util.UUID;

/** Identificador de un usuario del despacho (personal interno). */
public record UsuarioId(UUID valor) {

    public UsuarioId {
        Objects.requireNonNull(valor, "El identificador del usuario es obligatorio");
    }

    public static UsuarioId nuevo() {
        return new UsuarioId(UUID.randomUUID());
    }

    public static UsuarioId de(String valor) {
        return new UsuarioId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
