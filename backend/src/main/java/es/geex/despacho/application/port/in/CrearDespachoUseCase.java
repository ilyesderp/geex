package es.geex.despacho.application.port.in;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;

/** Crea un despacho nuevo (desde la instalación inicial o, más adelante, desde el alta en la plataforma). */
public interface CrearDespachoUseCase {

    DespachoId crear(NuevoDespacho datos);

    record NuevoDespacho(String nombre, String municipio, String provincia, ComunidadAutonoma comunidadAutonoma) {
    }
}
