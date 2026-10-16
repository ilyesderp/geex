package es.geex.identidad.adapter.in.web;

import es.geex.identidad.adapter.config.PropiedadesSeguridad;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cookie del token de refresco: HttpOnly (JavaScript no puede leerla),
 * SameSite=Strict (no se envía desde otros sitios) y limitada a {@code /api/auth}.
 */
@Component
class CookieRefresco {

    static final String NOMBRE = "geex_refresco";
    private static final String RUTA = "/api/auth";

    private final PropiedadesSeguridad propiedades;

    CookieRefresco(PropiedadesSeguridad propiedades) {
        this.propiedades = propiedades;
    }

    String crear(String token, Instant expira, Instant ahora) {
        return base(token).maxAge(Duration.between(ahora, expira)).build().toString();
    }

    String borrar() {
        return base("").maxAge(Duration.ZERO).build().toString();
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOMBRE, valor)
                .httpOnly(true)
                .secure(propiedades.cookieSegura())
                .sameSite("Strict")
                .path(RUTA);
    }
}
