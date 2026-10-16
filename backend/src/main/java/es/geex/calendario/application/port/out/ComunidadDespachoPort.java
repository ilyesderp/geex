package es.geex.calendario.application.port.out;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;

public interface ComunidadDespachoPort {

    ComunidadAutonoma comunidadDe(DespachoId despacho);
}
