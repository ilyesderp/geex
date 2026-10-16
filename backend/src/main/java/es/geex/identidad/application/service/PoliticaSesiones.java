package es.geex.identidad.application.service;

import java.time.Duration;
import java.util.Objects;

/** Duraciones de las credenciales, configurables desde la aplicación. */
public record PoliticaSesiones(
        Duration duracionRefresco,
        Duration duracionInvitacion,
        Duration duracionRestablecimiento) {

    public PoliticaSesiones {
        Objects.requireNonNull(duracionRefresco);
        Objects.requireNonNull(duracionInvitacion);
        Objects.requireNonNull(duracionRestablecimiento);
    }
}
