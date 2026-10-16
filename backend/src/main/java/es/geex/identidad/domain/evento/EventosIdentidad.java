package es.geex.identidad.domain.evento;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.EventoAuditable;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;

/** Eventos de dominio del módulo Identidad. Todos quedan registrados en la auditoría. */
public final class EventosIdentidad {

    private EventosIdentidad() {
    }

    private interface SobreUsuario extends EventoAuditable {
        UsuarioId usuarioId();

        @Override
        default String entidadTipo() {
            return "USUARIO";
        }

        @Override
        default String entidadId() {
            return usuarioId() == null ? null : usuarioId().toString();
        }
    }

    public record InstalacionCompletada(DespachoId despachoId, UsuarioId usuarioId, String email, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public UsuarioId actorId() { return usuarioId; }
        @Override
        public String accion() { return "INSTALACION_COMPLETADA"; }
        @Override
        public String descripcion() { return "Instalación inicial completada. Administrador: " + email; }
    }

    public record SesionIniciada(DespachoId despachoId, UsuarioId usuarioId, String ip, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public UsuarioId actorId() { return usuarioId; }
        @Override
        public String accion() { return "SESION_INICIADA"; }
        @Override
        public String descripcion() { return "Inicio de sesión" + (ip == null ? "" : " desde " + ip); }
    }

    public record InicioSesionFallido(UsuarioId usuarioId, String email, String motivo, String ip, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public DespachoId despachoId() { return null; }
        @Override
        public UsuarioId actorId() { return null; }
        @Override
        public String accion() { return "INICIO_SESION_FALLIDO"; }
        @Override
        public String descripcion() {
            return "Intento de acceso fallido (" + motivo + ") con el correo " + email + (ip == null ? "" : " desde " + ip);
        }
    }

    public record CuentaBloqueada(UsuarioId usuarioId, String email, Instant hasta, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public DespachoId despachoId() { return null; }
        @Override
        public UsuarioId actorId() { return null; }
        @Override
        public String accion() { return "CUENTA_BLOQUEADA"; }
        @Override
        public String descripcion() { return "Cuenta bloqueada temporalmente por intentos fallidos: " + email; }
    }

    public record ReutilizacionSesionDetectada(DespachoId despachoId, UsuarioId usuarioId, String ip, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public UsuarioId actorId() { return null; }
        @Override
        public String accion() { return "SESION_REUTILIZADA"; }
        @Override
        public String descripcion() {
            return "Se reutilizó un token de refresco ya usado; se han cerrado las sesiones de esa familia por seguridad";
        }
    }

    public record UsuarioInvitado(DespachoId despachoId, UsuarioId actorId, UsuarioId usuarioId, String email, Rol rol,
                                  Instant ocurridoEn) implements SobreUsuario {
        @Override
        public String accion() { return "USUARIO_INVITADO"; }
        @Override
        public String descripcion() { return "Invitado " + email + " con el rol " + rol.etiqueta(); }
    }

    public record CuentaActivada(DespachoId despachoId, UsuarioId usuarioId, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public UsuarioId actorId() { return usuarioId; }
        @Override
        public String accion() { return "CUENTA_ACTIVADA"; }
        @Override
        public String descripcion() { return "El usuario activó su cuenta y eligió su contraseña"; }
    }

    public record ContrasenaRestablecida(UsuarioId usuarioId, Instant ocurridoEn) implements SobreUsuario {
        @Override
        public DespachoId despachoId() { return null; }
        @Override
        public UsuarioId actorId() { return usuarioId; }
        @Override
        public String accion() { return "CONTRASENA_RESTABLECIDA"; }
        @Override
        public String descripcion() { return "Contraseña restablecida mediante enlace de recuperación"; }
    }

    public record ContrasenaCambiada(DespachoId despachoId, UsuarioId usuarioId, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public UsuarioId actorId() { return usuarioId; }
        @Override
        public String accion() { return "CONTRASENA_CAMBIADA"; }
        @Override
        public String descripcion() { return "El usuario cambió su contraseña"; }
    }

    public record RolCambiado(DespachoId despachoId, UsuarioId actorId, UsuarioId usuarioId, Rol anterior, Rol nuevo,
                              Instant ocurridoEn) implements SobreUsuario {
        @Override
        public String accion() { return "ROL_CAMBIADO"; }
        @Override
        public String descripcion() { return "Rol cambiado de " + anterior.etiqueta() + " a " + nuevo.etiqueta(); }
    }

    public record AccesoDesactivado(DespachoId despachoId, UsuarioId actorId, UsuarioId usuarioId, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public String accion() { return "ACCESO_DESACTIVADO"; }
        @Override
        public String descripcion() { return "Acceso al despacho desactivado"; }
    }

    public record AccesoReactivado(DespachoId despachoId, UsuarioId actorId, UsuarioId usuarioId, Instant ocurridoEn)
            implements SobreUsuario {
        @Override
        public String accion() { return "ACCESO_REACTIVADO"; }
        @Override
        public String descripcion() { return "Acceso al despacho reactivado"; }
    }

    public record PermisosRolActualizados(DespachoId despachoId, UsuarioId actorId, Rol rol, Instant ocurridoEn)
            implements EventoAuditable {
        @Override
        public String accion() { return "PERMISOS_ACTUALIZADOS"; }
        @Override
        public String descripcion() { return "Permisos del rol " + rol.etiqueta() + " modificados"; }
        @Override
        public String entidadTipo() { return "ROL"; }
        @Override
        public String entidadId() { return rol.name(); }
    }
}
