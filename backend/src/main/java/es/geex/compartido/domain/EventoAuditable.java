package es.geex.compartido.domain;

import java.time.Instant;

/**
 * Evento de dominio que debe quedar registrado en la auditoría.
 *
 * <p>El módulo de auditoría escucha esta interfaz genérica, así no depende de
 * ningún otro módulo: cualquier evento que la implemente queda registrado.
 */
public interface EventoAuditable {

    /** Despacho afectado; {@code null} para eventos de plataforma (p. ej. intento de acceso con un correo desconocido). */
    DespachoId despachoId();

    /** Usuario que realiza la acción; {@code null} si es anónima o del sistema. */
    UsuarioId actorId();

    /** Código estable de la acción, p. ej. {@code USUARIO_INVITADO}. */
    String accion();

    /** Descripción legible en español. */
    String descripcion();

    String entidadTipo();

    String entidadId();

    Instant ocurridoEn();
}
