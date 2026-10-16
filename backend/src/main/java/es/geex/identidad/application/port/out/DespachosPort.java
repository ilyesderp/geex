package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import java.util.Collection;
import java.util.Map;

/** Lo que Identidad necesita del módulo Despacho. */
public interface DespachosPort {

    boolean existeAlguno();

    DespachoId crear(String nombre, String municipio, String provincia, ComunidadAutonoma comunidad);

    Map<DespachoId, String> nombres(Collection<DespachoId> despachos);
}
