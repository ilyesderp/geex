package es.geex.identidad.adapter.in.web.seguridad;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.in.AutenticacionUseCase;
import es.geex.identidad.domain.evento.EventosIdentidad.AccesoDesactivado;
import es.geex.identidad.domain.evento.EventosIdentidad.AccesoReactivado;
import es.geex.identidad.domain.evento.EventosIdentidad.ContrasenaCambiada;
import es.geex.identidad.domain.evento.EventosIdentidad.ContrasenaRestablecida;
import es.geex.identidad.domain.evento.EventosIdentidad.RolCambiado;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Comprueba en cada petición que el token de acceso sigue correspondiendo al estado actual del usuario
 * (activo, misma versión de contraseña, membresía activa con el mismo rol).
 *
 * <p>El resultado se guarda en caché unos segundos para no consultar la base de datos en cada petición, y
 * la caché se vacía en cuanto se desactiva o reactiva un acceso, cambia un rol o cambia una contraseña:
 * el efecto es inmediato en este servidor. Con varios servidores, el retraso máximo sería la duración de
 * la caché (hasta que se comparta con Redis).
 */
@Component
class ComprobadorAccesoVigente {

    private record Clave(UsuarioId usuario, DespachoId despacho, Rol rol, int versionToken) {
    }

    private final ObjectProvider<AutenticacionUseCase> autenticacion;
    private final Cache<Clave, Boolean> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(10_000)
            .build();

    ComprobadorAccesoVigente(ObjectProvider<AutenticacionUseCase> autenticacion) {
        this.autenticacion = autenticacion;
    }

    boolean vigente(UsuarioId usuario, DespachoId despacho, Rol rol, int versionToken) {
        return cache.get(new Clave(usuario, despacho, rol, versionToken), clave -> ContextoEjecucion.comoSistema(() ->
                autenticacion.getObject().consultarAccesoVigente(usuario, despacho, rol, versionToken)));
    }

    // Tras el commit: si se vaciara antes, otra petición podría volver a guardar el estado antiguo.

    @TransactionalEventListener(fallbackExecution = true)
    void alDesactivar(AccesoDesactivado evento) {
        cache.invalidateAll();
    }

    @TransactionalEventListener(fallbackExecution = true)
    void alReactivar(AccesoReactivado evento) {
        cache.invalidateAll();
    }

    @TransactionalEventListener(fallbackExecution = true)
    void alCambiarRol(RolCambiado evento) {
        cache.invalidateAll();
    }

    @TransactionalEventListener(fallbackExecution = true)
    void alCambiarContrasena(ContrasenaCambiada evento) {
        cache.invalidateAll();
    }

    @TransactionalEventListener(fallbackExecution = true)
    void alRestablecerContrasena(ContrasenaRestablecida evento) {
        cache.invalidateAll();
    }
}
