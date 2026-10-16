package es.geex.soporte;

import es.geex.identidad.application.port.out.NotificadorCuenta;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Sustituye el envío de correos en los tests y guarda los enlaces para poder seguirlos. */
public class CorreosCapturados implements NotificadorCuenta {

    public record Correo(String tipo, String email, String token) {
    }

    private final List<Correo> enviados = new CopyOnWriteArrayList<>();

    @Override
    public void enviarInvitacion(String email, String nombre, String nombreDespacho, String token, Instant expiraEn) {
        enviados.add(new Correo("INVITACION", email, token));
    }

    @Override
    public void enviarAltaEnDespacho(String email, String nombre, String nombreDespacho) {
        enviados.add(new Correo("ALTA", email, null));
    }

    @Override
    public void enviarRestablecimiento(String email, String nombre, String token, Instant expiraEn) {
        enviados.add(new Correo("RESTABLECIMIENTO", email, token));
    }

    public String ultimoToken(String tipo, String email) {
        return enviados.reversed().stream()
                .filter(c -> c.tipo().equals(tipo) && c.email().equals(email))
                .map(Correo::token)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se envió ningún correo " + tipo + " a " + email));
    }
}
