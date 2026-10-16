package es.geex.despacho.domain.evento;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.EventoAuditable;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;

/**
 * Se ha creado un despacho. Otros módulos reaccionan inicializando sus datos
 * (listas desplegables, festivos locales conocidos del municipio...).
 */
public record DespachoCreado(
        DespachoId despachoId,
        String nombre,
        String municipio,
        String provincia,
        ComunidadAutonoma comunidadAutonoma,
        Instant ocurridoEn) implements EventoAuditable {

    @Override
    public UsuarioId actorId() {
        return null;
    }

    @Override
    public String accion() {
        return "DESPACHO_CREADO";
    }

    @Override
    public String descripcion() {
        return "Despacho creado: " + nombre;
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
