package es.geex.auditoria.adapter.out.identidad;

import es.geex.auditoria.application.port.out.NombresUsuariosPort;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.in.ConsultaUsuarios;
import java.util.Collection;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
class NombresUsuariosAdapter implements NombresUsuariosPort {

    private final ConsultaUsuarios usuarios;

    NombresUsuariosAdapter(ConsultaUsuarios usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public Map<UsuarioId, String> nombres(Collection<UsuarioId> ids) {
        return ids.isEmpty() ? Map.of() : usuarios.obtenerNombres(ids);
    }
}
