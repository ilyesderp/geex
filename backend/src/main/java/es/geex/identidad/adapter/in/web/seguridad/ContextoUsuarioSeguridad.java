package es.geex.identidad.adapter.in.web.seguridad;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.geex.compartido.adapter.contexto.ContextoEjecucion;
import es.geex.compartido.application.ContextoUsuario;
import es.geex.compartido.domain.AutenticacionFallidaException;
import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import es.geex.identidad.application.port.in.ConsultaUsuarios;
import es.geex.identidad.domain.evento.EventosIdentidad.PermisosRolActualizados;
import java.time.Duration;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Implementación del puerto {@link ContextoUsuario}: lee el contexto de la
 * ejecución actual y resuelve los permisos del rol (con caché, porque se
 * consultan en cada caso de uso).
 */
@Component
class ContextoUsuarioSeguridad implements ContextoUsuario {

    private record ClaveRol(DespachoId despacho, Rol rol) {
    }

    private final ObjectProvider<ConsultaUsuarios> consultaUsuarios;
    private final Cache<ClaveRol, Set<Permiso>> cachePermisos = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .build();

    ContextoUsuarioSeguridad(ObjectProvider<ConsultaUsuarios> consultaUsuarios) {
        this.consultaUsuarios = consultaUsuarios;
    }

    @Override
    public UsuarioId usuarioId() {
        return datos().usuarioId();
    }

    @Override
    public DespachoId despachoId() {
        return datos().despachoId();
    }

    @Override
    public Rol rol() {
        return datos().rol();
    }

    @Override
    public boolean esSuperadmin() {
        return datos().superadmin();
    }

    @Override
    public boolean tienePermiso(Permiso permiso) {
        var datos = datos();
        return cachePermisos.get(new ClaveRol(datos.despachoId(), datos.rol()),
                        clave -> consultaUsuarios.getObject().obtenerPermisos(clave.despacho(), clave.rol()))
                .contains(permiso);
    }

    /** Tras el commit: si se invalidara antes, otra petición podría volver a cachear los permisos antiguos. */
    @TransactionalEventListener(fallbackExecution = true)
    void alActualizarPermisos(PermisosRolActualizados evento) {
        cachePermisos.invalidate(new ClaveRol(evento.despachoId(), evento.rol()));
    }

    private static ContextoEjecucion.Datos datos() {
        return ContextoEjecucion.actual()
                .filter(d -> d.usuarioId() != null && d.despachoId() != null)
                .orElseThrow(() -> new AutenticacionFallidaException("NO_AUTENTICADO", "Debes iniciar sesión"));
    }
}
