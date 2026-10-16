package es.geex.calendario.adapter.out.despacho;

import es.geex.calendario.application.port.out.ComunidadDespachoPort;
import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.despacho.application.port.in.ConsultaDespachos;
import org.springframework.stereotype.Component;

@Component
class ComunidadDespachoAdapter implements ComunidadDespachoPort {

    private final ConsultaDespachos despachos;

    ComunidadDespachoAdapter(ConsultaDespachos despachos) {
        this.despachos = despachos;
    }

    @Override
    public ComunidadAutonoma comunidadDe(DespachoId despacho) {
        return despachos.obtenerComunidadAutonoma(despacho);
    }
}
