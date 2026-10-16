package es.geex.identidad.adapter.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de seguridad ({@code geex.seguridad.*}).
 *
 * @param jwtSecreto  clave HS256 en base64 (mínimo 32 bytes). Solo por variable de entorno, nunca en el repositorio.
 * @param limitarPeticiones limita la frecuencia de inicio de sesión y recuperación por IP (solo se desactiva en tests).
 * @param cookieSegura marca la cookie de refresco como {@code Secure} (solo HTTPS; los navegadores la aceptan en localhost).
 */
@ConfigurationProperties("geex.seguridad")
public record PropiedadesSeguridad(
        String jwtSecreto,
        @DefaultValue("geex") String emisor,
        @DefaultValue("15m") Duration duracionAcceso,
        @DefaultValue("8h") Duration duracionRefresco,
        @DefaultValue("48h") Duration duracionInvitacion,
        @DefaultValue("1h") Duration duracionRestablecimiento,
        @DefaultValue("true") boolean cookieSegura,
        @DefaultValue("http://localhost:4200") String urlFrontend,
        @DefaultValue("GEEX <no-responder@geex.local>") String remitenteCorreo,
        @DefaultValue("true") boolean limitarPeticiones) {
}
