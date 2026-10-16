package es.geex.despacho.application.port.in;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import java.util.Collection;
import java.util.Map;

/** Consultas que otros módulos necesitan sobre los despachos. */
public interface ConsultaDespachos {

    boolean existeAlguno();

    ComunidadAutonoma obtenerComunidadAutonoma(DespachoId despacho);

    Map<DespachoId, String> obtenerNombres(Collection<DespachoId> despachos);
}
