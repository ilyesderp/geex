package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.identidad.domain.MatrizPermisos;
import java.util.Set;

public interface MatrizPermisosRepository {

    MatrizPermisos obtener(DespachoId despacho);

    /** Guarda la matriz completa (al crear un despacho). */
    void guardar(MatrizPermisos matriz);

    /** Sustituye los permisos de un único rol, sin tocar los demás. */
    void guardarRol(DespachoId despacho, Rol rol, Set<Permiso> permisos);
}
