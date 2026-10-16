package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.Permiso;
import es.geex.compartido.domain.Rol;
import java.util.List;
import java.util.Set;

public interface PerfilUseCase {

    Perfil obtenerPerfil();

    record Perfil(
            String usuarioId,
            String email,
            String nombre,
            String apellidos,
            boolean superadmin,
            DespachoAcceso despachoActual,
            Set<Permiso> permisos,
            List<DespachoAcceso> despachos) {
    }

    record DespachoAcceso(String id, String nombre, Rol rol, String rolEtiqueta) {
    }
}
