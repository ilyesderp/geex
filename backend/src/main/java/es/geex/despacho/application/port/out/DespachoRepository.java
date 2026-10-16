package es.geex.despacho.application.port.out;

import es.geex.compartido.domain.DespachoId;
import es.geex.despacho.domain.Despacho;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DespachoRepository {

    void guardar(Despacho despacho);

    Optional<Despacho> buscar(DespachoId id);

    List<Despacho> buscarVarios(Collection<DespachoId> ids);

    boolean existeAlguno();
}
