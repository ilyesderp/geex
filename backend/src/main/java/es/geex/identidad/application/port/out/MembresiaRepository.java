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

    /**
     * Cuenta los titulares activos y bloquea esa comprobación en el despacho hasta
     * el final de la transacción: dos cambios simultáneos (p. ej. dos titulares que
     * se retiran el uno al otro) no pueden dejar el despacho sin titular.
     */
    long contarTitularesActivos(DespachoId despacho);
}
