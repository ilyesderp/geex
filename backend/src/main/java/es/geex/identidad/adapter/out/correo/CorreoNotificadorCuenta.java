package es.geex.identidad.adapter.out.correo;

import es.geex.identidad.adapter.config.PropiedadesSeguridad;
import es.geex.identidad.application.port.out.NotificadorCuenta;
import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.HtmlUtils;

/**
 * Correos de cuenta en HTML. Se envían después de confirmar la transacción: si
 * la operación falla y se deshace, no sale ningún correo con un enlace inválido.
 * En local los recoge Mailpit (http://localhost:8025).
 */
@Component
class CorreoNotificadorCuenta implements NotificadorCuenta {

    private static final Logger log = LoggerFactory.getLogger(CorreoNotificadorCuenta.class);
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm").withZone(ZoneId.of("Europe/Madrid"));

    private final JavaMailSender correo;
    private final PropiedadesSeguridad propiedades;
    private final TaskExecutor segundoPlano;

    CorreoNotificadorCuenta(JavaMailSender correo, PropiedadesSeguridad propiedades,
                            @Qualifier("applicationTaskExecutor") TaskExecutor segundoPlano) {
        this.correo = correo;
        this.propiedades = propiedades;
        this.segundoPlano = segundoPlano;
    }

    @Override
    public void enviarInvitacion(String email, String nombre, String nombreDespacho, String token, Instant expiraEn) {
        String enlace = enlace("/activar", token);
        enviarTrasConfirmar(email, "Invitación a GEEX — " + nombreDespacho, plantilla(
                "Hola " + e(nombre) + ",",
                "Te han invitado a trabajar en <strong>" + e(nombreDespacho) + "</strong> con GEEX, "
                        + "la aplicación de gestión de expedientes de extranjería.",
                "Para activar tu cuenta y elegir tu contraseña, pulsa el botón:",
                "Activar mi cuenta", enlace,
                "El enlace caduca el " + FORMATO_FECHA.format(expiraEn) + "."));
    }

    @Override
    public void enviarAltaEnDespacho(String email, String nombre, String nombreDespacho) {
        enviarTrasConfirmar(email, "Acceso a " + nombreDespacho + " en GEEX", plantilla(
                "Hola " + e(nombre) + ",",
                "Ahora también tienes acceso al despacho <strong>" + e(nombreDespacho) + "</strong>.",
                "Inicia sesión con tu cuenta habitual y elige el despacho desde el menú de usuario.",
                "Ir a GEEX", propiedades.urlFrontend(),
                null));
    }

    @Override
    public void enviarRestablecimiento(String email, String nombre, String token, Instant expiraEn) {
        enviarTrasConfirmar(email, "Restablecer tu contraseña de GEEX", plantilla(
                "Hola " + e(nombre) + ",",
                "Hemos recibido una solicitud para restablecer tu contraseña.",
                "Si has sido tú, pulsa el botón para elegir una nueva:",
                "Restablecer contraseña", enlace("/restablecer", token),
                "El enlace caduca el " + FORMATO_FECHA.format(expiraEn)
                        + ". Si no lo has solicitado, ignora este correo: tu contraseña no cambia."));
    }

    private String enlace(String ruta, String token) {
        return propiedades.urlFrontend() + ruta + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    /**
     * El correo sale después del commit (si la operación falla, no se envía un enlace que no existe) y en
     * segundo plano: un servidor de correo lento o caído nunca hace esperar al usuario.
     */
    private void enviarTrasConfirmar(String destinatario, String asunto, String html) {
        Runnable envio = () -> segundoPlano.execute(() -> enviar(destinatario, asunto, html));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    envio.run();
                }
            });
        } else {
            envio.run();
        }
    }

    private void enviar(String destinatario, String asunto, String html) {
        try {
            MimeMessage mensaje = correo.createMimeMessage();
            var ayudante = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            ayudante.setFrom(propiedades.remitenteCorreo());
            ayudante.setTo(destinatario);
            ayudante.setSubject(asunto);
            ayudante.setText(html, true);
            correo.send(mensaje);
        } catch (Exception ex) {
            // No se registra el contenido: contiene un enlace con token
            log.error("No se pudo enviar el correo '{}' a {}", asunto, destinatario, ex);
        }
    }

    private static String plantilla(String saludo, String parrafo1, String parrafo2, String textoBoton,
                                    String urlBoton, String nota) {
        return """
                <!doctype html>
                <html lang="es"><body style="margin:0;background:#f4f6fa;font-family:Segoe UI,Arial,sans-serif;color:#1d2433">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:32px 16px">
                <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;padding:32px">
                <tr><td style="font-size:22px;font-weight:700;color:#173a6b;padding-bottom:24px">GEEX</td></tr>
                <tr><td style="font-size:15px;line-height:1.6">
                <p>%s</p><p>%s</p><p>%s</p>
                <p style="padding:16px 0"><a href="%s" style="background:#1f5fae;color:#ffffff;text-decoration:none;padding:12px 22px;border-radius:8px;font-weight:600;display:inline-block">%s</a></p>
                %s
                <p style="color:#6b7280;font-size:13px">Si el botón no funciona, copia este enlace en tu navegador:<br>%s</p>
                </td></tr></table></td></tr></table></body></html>
                """.formatted(saludo, parrafo1, parrafo2, e(urlBoton), e(textoBoton),
                nota == null ? "" : "<p style=\"color:#6b7280;font-size:13px\">" + nota + "</p>", e(urlBoton));
    }

    private static String e(String texto) {
        return texto == null ? "" : HtmlUtils.htmlEscape(texto);
    }
}
