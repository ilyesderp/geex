package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.domain.SesionRefresco;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SesionRefrescoRepository {

    void guardar(SesionRefresco sesion);

    /**
     * Busca por hash bloqueando la fila hasta el final de la transacción, para que
     * dos renovaciones simultáneas con el mismo token no puedan tener éxito ambas.
     */
    Optional<SesionRefresco> buscarPorHashParaActualizar(String tokenHash);

    Optional<SesionRefresco> buscarPorHash(String tokenHash);

    void revocarFamilia(UUID familiaId, Instant ahora);

    void revocarDeUsuario(UsuarioId usuario, Instant ahora);

    void revocarDeUsuarioEnDespacho(UsuarioId usuario, DespachoId despacho, Instant ahora);
}
