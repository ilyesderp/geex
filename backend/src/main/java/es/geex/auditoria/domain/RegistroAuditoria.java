package es.geex.auditoria.domain;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.EventoAuditable;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Entrada inmutable del registro de auditoría. */
public record RegistroAuditoria(
        UUID id,
        DespachoId despachoId,
        UsuarioId actorId,
        String accion,
        String descripcion,
        String entidadTipo,
        String entidadId,
        Instant ocurridoEn) {

    public RegistroAuditoria {
        Objects.requireNonNull(id);
        Objects.requireNonNull(accion);
        Objects.requireNonNull(ocurridoEn);
        descripcion = recortar(descripcion, 500);
        entidadId = recortar(entidadId, 60);
    }

    public static RegistroAuditoria de(EventoAuditable evento) {
        return new RegistroAuditoria(UUID.randomUUID(), evento.despachoId(), evento.actorId(), evento.accion(),
                evento.descripcion(), evento.entidadTipo(), evento.entidadId(), evento.ocurridoEn());
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }
}
