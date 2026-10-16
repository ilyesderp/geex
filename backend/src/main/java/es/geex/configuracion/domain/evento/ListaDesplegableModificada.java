package es.geex.configuracion.domain.evento;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.EventoAuditable;
import es.geex.compartido.domain.UsuarioId;
import es.geex.configuracion.domain.TipoCatalogo;
import java.time.Instant;

public record ListaDesplegableModificada(DespachoId despachoId, UsuarioId actorId, TipoCatalogo tipo, String cambio,
                                         Instant ocurridoEn) implements EventoAuditable {

    @Override
    public String accion() {
        return "LISTA_MODIFICADA";
    }

    @Override
    public String descripcion() {
        return "Lista \"" + tipo.nombre() + "\": " + cambio;
    }

    @Override
    public String entidadTipo() {
        return "LISTA_DESPLEGABLE";
    }

    @Override
    public String entidadId() {
        return tipo.name();
    }
}
