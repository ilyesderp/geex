package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.DespachoId;
import es.geex.identidad.domain.MatrizPermisos;

public interface MatrizPermisosRepository {

    MatrizPermisos obtener(DespachoId despacho);

    void guardar(MatrizPermisos matriz);
}
