package es.geex.identidad.application.port.in;

import es.geex.compartido.domain.DespachoId;
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
