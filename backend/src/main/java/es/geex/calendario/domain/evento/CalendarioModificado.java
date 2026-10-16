package es.geex.calendario.domain.evento;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.EventoAuditable;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;

public record CalendarioModificado(DespachoId despachoId, UsuarioId actorId, String descripcion, String entidadId,
                                   Instant ocurridoEn) implements EventoAuditable {

    @Override
    public String accion() {
        return "CALENDARIO_MODIFICADO";
    }

    @Override
    public String entidadTipo() {
        return "DIA_INHABIL";
    }
}
