package es.geex.compartido.application;

import es.geex.compartido.domain.AccesoDenegadoException;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;

/**
 * Puerto de salida: quién ejecuta el caso de uso y en qué despacho.
 *
 * <p>Los casos de uso no leen nada de Spring Security; reciben este puerto y lo
 * consultan. Así la autorización vive en la capa de aplicación y el dominio
 * sigue libre de frameworks.
 */
public interface ContextoUsuario {

    UsuarioId usuarioId();

    DespachoId despachoId();

    Rol rol();

    boolean esSuperadmin();

    boolean tienePermiso(Permiso permiso);

    default void exigir(Permiso permiso) {
        if (!tienePermiso(permiso)) {
            throw AccesoDenegadoException.faltaPermiso(permiso);
        }
    }

    default void exigirSuperadmin() {
        if (!esSuperadmin()) {
            throw new AccesoDenegadoException("Esta operación solo la puede realizar un administrador de la plataforma");
        }
    }
}
