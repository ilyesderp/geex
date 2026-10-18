package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.DespachoId;
import es.geex.compartido.domain.Rol;
import es.geex.compartido.domain.UsuarioId;
import java.time.Instant;

/**
 * Inicio, renovación y cierre de sesión.
 *
 * <p>Los rechazos se devuelven como resultado (no como excepción) para que los
 * cambios que deben persistir aunque se deniegue el acceso —contador de intentos
 * fallidos, revocación de sesiones robadas— se confirmen en la transacción.
 */
public interface AutenticacionUseCase {

    ResultadoSesion iniciarSesion(Credenciales credenciales);

    ResultadoSesion refrescar(String tokenRefresco, Origen origen);

    void cerrarSesion(String tokenRefresco);

    ResultadoSesion cambiarDespacho(DespachoId destino, String tokenRefrescoActual, Origen origen);

    /**
     * Si un token de acceso sigue siendo válido para el estado actual: usuario activo, misma versión de
     * contraseña, y membresía activa en ese despacho con el mismo rol. Se comprueba en cada petición para
     * que desactivar a alguien, cambiarle el rol o cambiar la contraseña surta efecto de inmediato, sin
     * esperar a que caduque el token (15 minutos).
     */
    boolean consultarAccesoVigente(UsuarioId usuario, DespachoId despacho, Rol rol, int versionToken);

    record Credenciales(String email, String contrasena, Origen origen) {
    }

    record Origen(String ip, String agente) {
    }

    record SesionEmitida(String tokenAcceso, Instant accesoExpiraEn, String tokenRefresco, Instant refrescoExpiraEn) {
    }

    sealed interface ResultadoSesion {
        record Emitida(SesionEmitida sesion) implements ResultadoSesion {
        }

        record Rechazada(String codigo, String mensaje) implements ResultadoSesion {
        }
    }
}
