package es.geex.configuracion.application.port.out;

import es.geex.compartido.domain.DespachoId;
import es.geex.configuracion.domain.TipoCatalogo;
import es.geex.configuracion.domain.ValorCatalogo;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ValorCatalogoRepository {

    List<ValorCatalogo> listar(DespachoId despacho, TipoCatalogo tipo);

    Map<TipoCatalogo, long[]> contarPorTipo(DespachoId despacho);

    Optional<ValorCatalogo> buscar(DespachoId despacho, UUID id);

    void guardar(ValorCatalogo valor);

    void guardarTodos(Collection<ValorCatalogo> valores);

    void eliminar(ValorCatalogo valor);
}
