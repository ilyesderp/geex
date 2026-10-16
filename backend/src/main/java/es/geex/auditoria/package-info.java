/**
 * Auditoría: registro de solo inserción de quién hizo qué y cuándo. Escucha de
 * forma genérica cualquier {@code EventoAuditable}, sin depender de los módulos
 * que los publican.
 */
@ApplicationModule(displayName = "Auditoría")
package es.geex.auditoria;

import org.springframework.modulith.ApplicationModule;
