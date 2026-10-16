package es.geex.identidad.adapter.out.despacho;

import es.geex.compartido.domain.ComunidadAutonoma;
import es.geex.compartido.domain.DespachoId;
import es.geex.despacho.application.port.in.ConsultaDespachos;
import es.geex.despacho.application.port.in.CrearDespachoUseCase;
import es.geex.identidad.application.port.out.DespachosPort;
import java.util.Collection;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Implementa el puerto de salida de Identidad usando la API pública del módulo Despacho. */
@Component
class DespachosAdapter implements DespachosPort {

    private final CrearDespachoUseCase crear;
    private final ConsultaDespachos consulta;

    DespachosAdapter(CrearDespachoUseCase crear, ConsultaDespachos consulta) {
        this.crear = crear;
        this.consulta = consulta;
    }

    @Override
    public boolean existeAlguno() {
        return consulta.existeAlguno();
    }

    @Override
    public DespachoId crear(String nombre, String municipio, String provincia, ComunidadAutonoma comunidad) {
        return crear.crear(new CrearDespachoUseCase.NuevoDespacho(nombre, municipio, provincia, comunidad));
    }

    @Override
    public Map<DespachoId, String> nombres(Collection<DespachoId> despachos) {
        return consulta.obtenerNombres(despachos);
    }
}
