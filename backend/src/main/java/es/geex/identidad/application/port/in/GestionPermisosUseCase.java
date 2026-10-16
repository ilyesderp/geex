package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import java.util.List;
import java.util.Set;

/** Matriz de permisos por rol del despacho (Configuración → Roles y permisos). */
public interface GestionPermisosUseCase {

    List<PermisosDeRol> obtenerMatriz();

    PermisosDeRol actualizarPermisos(Rol rol, Set<Permiso> permisos);

    record PermisosDeRol(Rol rol, String etiqueta, String descripcion, boolean editable, Set<Permiso> permisos) {
    }
}
