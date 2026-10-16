package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.domain.Usuario;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    void guardar(Usuario usuario);

    Optional<Usuario> buscar(UsuarioId id);

    Optional<Usuario> buscarPorEmail(String emailNormalizado);

    List<Usuario> buscarVarios(Collection<UsuarioId> ids);
}
