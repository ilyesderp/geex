package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;
import java.util.List;

/** Gestión del personal del despacho (apartado Configuración → Usuarios). */
public interface GestionUsuariosUseCase {

    List<UsuarioDespacho> listarUsuarios();

    UsuarioDespacho invitar(Invitacion invitacion);

    void reenviarInvitacion(UsuarioId usuario);

    UsuarioDespacho cambiarRol(UsuarioId usuario, Rol rol);

    void desactivar(UsuarioId usuario);

    void reactivar(UsuarioId usuario);

    record Invitacion(String email, String nombre, String apellidos, Rol rol) {
    }

    enum EstadoUsuario { PENDIENTE_ACTIVACION, ACTIVO, DESACTIVADO }

    record UsuarioDespacho(
            String id,
            String email,
            String nombre,
            String apellidos,
            Rol rol,
            String rolEtiqueta,
            EstadoUsuario estado,
            Instant ultimoAcceso,
            boolean esUstedMismo) {
    }
}
