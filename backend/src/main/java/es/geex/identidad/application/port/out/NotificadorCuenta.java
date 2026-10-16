package es.geex.identidad.application.port.out;

import java.time.Instant;

/** Envía los correos relacionados con la cuenta. El token viaja en claro solo en el enlace del correo. */
public interface NotificadorCuenta {

    void enviarInvitacion(String email, String nombre, String nombreDespacho, String token, Instant expiraEn);

    void enviarAltaEnDespacho(String email, String nombre, String nombreDespacho);

    void enviarRestablecimiento(String email, String nombre, String token, Instant expiraEn);
}
