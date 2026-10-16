package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Consultas que otros módulos (y los adaptadores de seguridad) necesitan sobre los usuarios. */
public interface ConsultaUsuarios {

    Map<UsuarioId, String> obtenerNombres(Collection<UsuarioId> usuarios);

    Set<Permiso> obtenerPermisos(DespachoId despacho, Rol rol);
}
