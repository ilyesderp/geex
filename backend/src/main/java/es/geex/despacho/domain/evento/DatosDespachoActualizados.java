package es.geex.despacho.domain.evento;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.EventoAuditable;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;

public record DatosDespachoActualizados(DespachoId despachoId, UsuarioId actorId, Instant ocurridoEn)
        implements EventoAuditable {

    @Override
    public String accion() {
        return "DESPACHO_ACTUALIZADO";
    }

    @Override
    public String descripcion() {
        return "Datos generales del despacho modificados";
    }

    @Override
    public String entidadTipo() {
        return "DESPACHO";
    }

    @Override
    public String entidadId() {
        return despachoId.toString();
    }
}
