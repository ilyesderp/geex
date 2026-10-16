package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.domain.Membresia;
import java.util.List;
import java.util.Optional;

public interface MembresiaRepository {

    void guardar(Membresia membresia);

    Optional<Membresia> buscar(UsuarioId usuario, DespachoId despacho);

    List<Membresia> listarPorDespacho(DespachoId despacho);

    List<Membresia> listarActivasDeUsuario(UsuarioId usuario);

    long contarTitularesActivos(DespachoId despacho);
}
