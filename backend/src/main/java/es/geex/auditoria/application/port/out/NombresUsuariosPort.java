package es.geex.auditoria.application.port.out;

import es.geex.compartido.domain.UsuarioId;
import java.util.Collection;
import java.util.Map;

/** Nombres de los usuarios, resueltos al consultar (así un cambio de nombre se refleja en el histórico). */
public interface NombresUsuariosPort {

    Map<UsuarioId, String> nombres(Collection<UsuarioId> usuarios);
}
