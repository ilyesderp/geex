package es.geex.identidad.application.port.out;

import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.domain.TokenUnUso;
import java.time.Instant;
import java.util.Optional;

public interface TokenUnUsoRepository {

    void guardar(TokenUnUso token);

    Optional<TokenUnUso> buscarEnlacePorHash(String tokenHash);

    /** Invalida los enlaces pendientes anteriores del mismo tipo (al reenviar uno nuevo). */
    void invalidarPendientes(UsuarioId usuario, TokenUnUso.Tipo tipo, Instant ahora);
}
